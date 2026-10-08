import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { FormSection, PrecisionInput } from '@package/shared-ui';
import type { PackageSizeDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createPackageSize,
  getPackageSizeById,
  updatePackageSize,
} from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityFormShell } from '../shared/EntityFormShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

type PackageSizeFormState = {
  name: string;
  maxLength: string;
};

const toFormState = (packageSize: PackageSizeDTO): PackageSizeFormState => ({
  name: packageSize.name ?? '',
  maxLength: typeof packageSize.maxLength === 'number' ? String(packageSize.maxLength) : '',
});

const parsePositiveNumber = (value: string): number | null => {
  const parsed = Number(value.trim());
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
};

const validateForm = (formState: PackageSizeFormState, t: (key: string) => string): string | null => {
  if (!formState.name.trim()) {
    return t('form.nameRequired');
  }

  if (parsePositiveNumber(formState.maxLength) === null) {
    return t('form.maxLengthInvalid');
  }

  return null;
};

const buildPayload = (formState: PackageSizeFormState): PackageSizeDTO => ({
  name: formState.name.trim(),
  maxLength: parsePositiveNumber(formState.maxLength) ?? undefined,
});

export const LogisticsPackageSizeFormPage = () => {
  const { t } = useTranslation('packageSize');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();

  const packageSizeId = params.packageSizeId ? Number(params.packageSizeId) : null;
  const isEditMode = typeof packageSizeId === 'number';
  const hasValidPackageSizeId = !isEditMode || (Number.isInteger(packageSizeId) && (packageSizeId as number) > 0);

  const [draftFormState, setDraftFormState] = useState<PackageSizeFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const packageSizeQuery = useQuery({
    queryKey: ['logistics', 'package-size', packageSizeId],
    queryFn: () => getPackageSizeById(packageSizeId as number),
    enabled: isEditMode && hasValidPackageSizeId,
  });

  const initialFormState = useMemo<PackageSizeFormState>(() => {
    if (isEditMode) {
      return packageSizeQuery.data ? toFormState(packageSizeQuery.data) : { name: '', maxLength: '' };
    }

    return { name: '', maxLength: '' };
  }, [isEditMode, packageSizeQuery.data]);

  const formState = draftFormState ?? initialFormState;

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updatePackageSize(packageSizeId as number, payload);
      }

      return createPackageSize(payload);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'package-sizes'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'package-size'] }),
      ]);

      navigate('/portal/package-sizes');
    },
  });

  if (!hasValidPackageSizeId) {
    return <Navigate to="/portal/package-sizes" replace />;
  }

  const handleInputChange = (key: keyof PackageSizeFormState, value: string) => {
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
      activeHref="#/portal/package-sizes"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('form.eyebrow')}
      heading={isEditMode ? t('form.headingEdit') : t('form.headingCreate')}
      backLinks={
        <Link
          to="/portal/package-sizes"
          className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          {t('form.backToList')}
        </Link>
      }
      isLoading={isEditMode && packageSizeQuery.isLoading}
      loadingMessage={t('form.loading')}
      isError={packageSizeQuery.isError}
      errorMessage={t('form.errorHeading')}
      errorDetail={(packageSizeQuery.error as Error)?.message}
    >
      <FormSection icon="deployed_code" title={t('form.sectionTitle')} className="mt-6">
        <div className="grid gap-4 md:grid-cols-2">
          <PrecisionInput
            label={t('form.nameLabel')}
            value={formState.name}
            onChange={(event) => handleInputChange('name', event.target.value)}
            placeholder={t('form.namePlaceholder')}
            required
          />

          <PrecisionInput
            label={t('form.maxLengthLabel')}
            type="number"
            value={formState.maxLength}
            onChange={(event) => handleInputChange('maxLength', event.target.value)}
            placeholder={t('form.maxLengthPlaceholder')}
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
            onClick={() => navigate('/portal/package-sizes')}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {tCommon('buttons.cancel')}
          </button>
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
