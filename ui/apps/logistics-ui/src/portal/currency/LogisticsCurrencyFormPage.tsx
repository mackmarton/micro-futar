import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { FormSection, PrecisionInput } from '@package/shared-ui';
import type { CurrencyDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createCurrency,
  getCurrencyById,
  updateCurrency,
} from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityFormShell } from '../shared/EntityFormShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

type CurrencyFormState = {
  code: string;
  name: string;
  symbol: string;
};

const emptyFormState: CurrencyFormState = { code: '', name: '', symbol: '' };

const toFormState = (currency: CurrencyDTO): CurrencyFormState => ({
  code: currency.code ?? '',
  name: currency.name ?? '',
  symbol: currency.symbol ?? '',
});

const validateForm = (formState: CurrencyFormState, t: (key: string) => string): string | null => {
  if (!formState.code.trim()) {
    return t('form.codeRequired');
  }

  if (!formState.name.trim()) {
    return t('form.nameRequired');
  }

  return null;
};

const buildPayload = (formState: CurrencyFormState): CurrencyDTO => ({
  code: formState.code.trim().toUpperCase(),
  name: formState.name.trim(),
  symbol: formState.symbol.trim() || undefined,
});

export const LogisticsCurrencyFormPage = () => {
  const { t } = useTranslation('currency');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();

  const currencyId = params.currencyId ? Number(params.currencyId) : null;
  const isEditMode = typeof currencyId === 'number';
  const hasValidCurrencyId = !isEditMode || (Number.isInteger(currencyId) && (currencyId as number) > 0);

  const [draftFormState, setDraftFormState] = useState<CurrencyFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const currencyQuery = useQuery({
    queryKey: ['logistics', 'currency', currencyId],
    queryFn: () => getCurrencyById(currencyId as number),
    enabled: isEditMode && hasValidCurrencyId,
  });

  const initialFormState = useMemo<CurrencyFormState>(() => {
    if (isEditMode) {
      return currencyQuery.data ? toFormState(currencyQuery.data) : emptyFormState;
    }

    return emptyFormState;
  }, [isEditMode, currencyQuery.data]);

  const formState = draftFormState ?? initialFormState;

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updateCurrency(currencyId as number, payload);
      }

      return createCurrency(payload);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'currencies'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'currency'] }),
      ]);

      navigate('/portal/currencies');
    },
  });

  if (!hasValidCurrencyId) {
    return <Navigate to="/portal/currencies" replace />;
  }

  const handleInputChange = (key: keyof CurrencyFormState, value: string) => {
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

  return (
    <EntityFormShell
      title={isEditMode ? t('form.titleEdit') : t('form.titleCreate')}
      activeHref="#/portal/currencies"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('form.eyebrow')}
      heading={isEditMode ? t('form.headingEdit') : t('form.headingCreate')}
      backLinks={
        <Link
          to="/portal/currencies"
          className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          {t('form.backToList')}
        </Link>
      }
      isLoading={isEditMode && currencyQuery.isLoading}
      loadingMessage={t('form.loading')}
      isError={currencyQuery.isError}
      errorMessage={t('form.errorHeading')}
      errorDetail={(currencyQuery.error as Error)?.message}
    >
      <FormSection icon="payments" title={t('form.sectionTitle')} className="mt-6">
        <div className="grid gap-4 md:grid-cols-3">
          <PrecisionInput
            label={t('form.codeLabel')}
            value={formState.code}
            onChange={(event) => handleInputChange('code', event.target.value)}
            placeholder={t('form.codePlaceholder')}
            required
          />

          <PrecisionInput
            label={t('form.nameLabel')}
            value={formState.name}
            onChange={(event) => handleInputChange('name', event.target.value)}
            placeholder={t('form.namePlaceholder')}
            required
          />

          <PrecisionInput
            label={t('form.symbolLabel')}
            value={formState.symbol}
            onChange={(event) => handleInputChange('symbol', event.target.value)}
            placeholder={t('form.symbolPlaceholder')}
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
            onClick={() => navigate('/portal/currencies')}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {tCommon('buttons.cancel')}
          </button>
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
