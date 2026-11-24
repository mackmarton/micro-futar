import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { FormSection, PrecisionInput } from '@package/shared-ui';
import type { CountryBaseFeeDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createCountryBaseFee,
  getAllCountries,
  getAllPackageSizes,
  getCountryBaseFeeById,
  updateCountryBaseFee,
} from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityFormShell } from '../shared/EntityFormShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

type CountryBaseFeeFormState = {
  countryId: string;
  packageSizeId: string;
  price: string;
};

const emptyFormState: CountryBaseFeeFormState = { countryId: '', packageSizeId: '', price: '' };

const toFormState = (fee: CountryBaseFeeDTO): CountryBaseFeeFormState => ({
  countryId: typeof fee.countryId === 'number' ? String(fee.countryId) : '',
  packageSizeId: typeof fee.packageSizeId === 'number' ? String(fee.packageSizeId) : '',
  price: typeof fee.price === 'number' ? String(fee.price) : '',
});

const parseNonNegativeNumber = (value: string): number | null => {
  if (!value.trim()) {
    return null;
  }
  const parsed = Number(value.trim());
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : null;
};

const validateForm = (formState: CountryBaseFeeFormState, t: (key: string) => string): string | null => {
  if (!formState.countryId) {
    return t('form.countryRequired');
  }

  if (!formState.packageSizeId) {
    return t('form.packageSizeRequired');
  }

  if (parseNonNegativeNumber(formState.price) === null) {
    return t('form.priceInvalid');
  }

  return null;
};

const buildPayload = (formState: CountryBaseFeeFormState): CountryBaseFeeDTO => ({
  countryId: Number(formState.countryId),
  packageSizeId: Number(formState.packageSizeId),
  price: parseNonNegativeNumber(formState.price) ?? undefined,
});

const selectClassName =
  'mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface disabled:cursor-not-allowed disabled:opacity-70';

export const LogisticsCountryBaseFeeFormPage = () => {
  const { t } = useTranslation('countryBaseFee');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();

  const countryBaseFeeId = params.countryBaseFeeId ? Number(params.countryBaseFeeId) : null;
  const isEditMode = typeof countryBaseFeeId === 'number';
  const hasValidId = !isEditMode || (Number.isInteger(countryBaseFeeId) && (countryBaseFeeId as number) > 0);

  const [draftFormState, setDraftFormState] = useState<CountryBaseFeeFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const feeQuery = useQuery({
    queryKey: ['logistics', 'country-base-fee', countryBaseFeeId],
    queryFn: () => getCountryBaseFeeById(countryBaseFeeId as number),
    enabled: isEditMode && hasValidId,
  });
  const countriesQuery = useQuery({ queryKey: ['logistics', 'countries'], queryFn: getAllCountries });
  const packageSizesQuery = useQuery({ queryKey: ['logistics', 'package-sizes'], queryFn: getAllPackageSizes });

  const initialFormState = useMemo<CountryBaseFeeFormState>(
    () => (isEditMode && feeQuery.data ? toFormState(feeQuery.data) : emptyFormState),
    [isEditMode, feeQuery.data],
  );

  const formState = draftFormState ?? initialFormState;

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updateCountryBaseFee(countryBaseFeeId as number, payload);
      }

      return createCountryBaseFee(payload);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'country-base-fees'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'country-base-fee'] }),
      ]);

      navigate('/portal/country-base-fees');
    },
  });

  if (!hasValidId) {
    return <Navigate to="/portal/country-base-fees" replace />;
  }

  const handleInputChange = (key: keyof CountryBaseFeeFormState, value: string) => {
    setDraftFormState((previous) => ({
      ...(previous ?? initialFormState),
      [key]: value,
    }));
    setValidationError(null);
  };

  const handleSubmit = () => {
    const errorMessage = validateForm(formState, t);
    setValidationError(errorMessage);

    if (errorMessage) {
      return;
    }

    saveMutation.mutate();
  };

  const lookupError = countriesQuery.error ?? packageSizesQuery.error ?? feeQuery.error;

  return (
    <EntityFormShell
      title={isEditMode ? t('form.titleEdit') : t('form.titleCreate')}
      activeHref="#/portal/country-base-fees"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('form.eyebrow')}
      heading={isEditMode ? t('form.headingEdit') : t('form.headingCreate')}
      backLinks={
        <Link
          to="/portal/country-base-fees"
          className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          {t('form.backToList')}
        </Link>
      }
      isLoading={(isEditMode && feeQuery.isLoading) || countriesQuery.isLoading || packageSizesQuery.isLoading}
      loadingMessage={t('form.loading')}
      isError={feeQuery.isError || countriesQuery.isError || packageSizesQuery.isError}
      errorMessage={t('form.errorHeading')}
      errorDetail={(lookupError as Error | null)?.message}
    >
      <FormSection icon="request_quote" title={t('form.sectionTitle')} className="mt-6">
        <div className="grid gap-4 md:grid-cols-2">
          <label className="rounded-2xl bg-surface-container-lowest p-4">
            <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('form.countryLabel')}</p>
            <select
              value={formState.countryId}
              onChange={(event) => handleInputChange('countryId', event.target.value)}
              className={selectClassName}
            >
              <option value="">{t('form.countryPlaceholder')}</option>
              {(countriesQuery.data ?? []).map((country) => (
                <option key={country.id ?? country.name} value={country.id ?? ''}>
                  {/* country.name backend-ről érkező szabad szöveg, nem fordítjuk. */}
                  {country.name ?? tCommon('status.notAvailable')}
                </option>
              ))}
            </select>
          </label>

          <label className="rounded-2xl bg-surface-container-lowest p-4">
            <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('form.packageSizeLabel')}</p>
            <select
              value={formState.packageSizeId}
              onChange={(event) => handleInputChange('packageSizeId', event.target.value)}
              className={selectClassName}
            >
              <option value="">{t('form.packageSizePlaceholder')}</option>
              {(packageSizesQuery.data ?? []).map((packageSize) => (
                <option key={packageSize.id ?? packageSize.name} value={packageSize.id ?? ''}>
                  {packageSize.name ?? tCommon('status.notAvailable')}
                </option>
              ))}
            </select>
          </label>

          <PrecisionInput
            label={t('form.priceLabel')}
            type="number"
            value={formState.price}
            onChange={(event) => handleInputChange('price', event.target.value)}
            placeholder={t('form.pricePlaceholder')}
            required
          />
        </div>

        {validationError ? (
          <div className="mt-4 rounded-xl bg-surface-container-lowest p-4">
            <p className="font-body text-on-surface">{validationError}</p>
          </div>
        ) : null}

        {saveMutation.isError ? (
          <div className="mt-4 rounded-xl bg-surface-container-lowest p-4">
            <p className="font-body text-on-surface">
              {(saveMutation.error as Error)?.message ?? t('form.saveFailed')}
            </p>
          </div>
        ) : null}

        <div className="mt-6 flex flex-wrap gap-3">
          <button
            type="button"
            onClick={handleSubmit}
            disabled={saveMutation.isPending}
            className="inline-flex items-center rounded-lg bg-primary px-5 py-3 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container disabled:cursor-not-allowed disabled:opacity-70"
          >
            {saveMutation.isPending ? tCommon('buttons.saving') : isEditMode ? t('form.submitEdit') : t('form.submitCreate')}
          </button>
          <button
            type="button"
            onClick={() => navigate('/portal/country-base-fees')}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {tCommon('buttons.cancel')}
          </button>
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
