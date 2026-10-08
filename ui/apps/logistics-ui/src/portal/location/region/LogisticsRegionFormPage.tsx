import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { FormSection, PrecisionInput } from '@package/shared-ui';
import type { LocationRegionDTO } from '@package/shared-core/api/LogisticsApiClient';
import { createRegion, getRegionById, updateRegion } from '../../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../../navigation';
import { EntityFormShell } from '../../shared/EntityFormShell';
import { LanguageSwitcher } from '../../../i18n/LanguageSwitcher';

type RegionFormState = {
  name: string;
};

const toFormState = (region: LocationRegionDTO): RegionFormState => ({
  name: region.name ?? '',
});

const validateForm = (formState: RegionFormState, t: (key: string) => string): string | null => {
  if (!formState.name.trim()) {
    return t('region.form.nameRequired');
  }

  return null;
};

const buildPayload = (formState: RegionFormState): LocationRegionDTO => ({
  name: formState.name.trim(),
});

export const LogisticsRegionFormPage = () => {
  const { t } = useTranslation('location');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();

  const regionId = params.regionId ? Number(params.regionId) : null;
  const isEditMode = typeof regionId === 'number';
  const hasValidRegionId = !isEditMode || (Number.isInteger(regionId) && (regionId as number) > 0);

  const [draftFormState, setDraftFormState] = useState<RegionFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const regionQuery = useQuery({
    queryKey: ['logistics', 'locations', 'region', regionId],
    queryFn: () => getRegionById(regionId as number),
    enabled: isEditMode && hasValidRegionId,
  });

  const initialFormState = useMemo<RegionFormState>(() => {
    if (isEditMode) {
      return regionQuery.data ? toFormState(regionQuery.data) : { name: '' };
    }

    return { name: '' };
  }, [isEditMode, regionQuery.data]);

  const formState = draftFormState ?? initialFormState;

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updateRegion(regionId as number, payload);
      }

      return createRegion(payload);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'locations', 'regions'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'locations', 'region'] }),
      ]);
      navigate('/portal/locations/regions');
    },
  });

  if (!hasValidRegionId) {
    return <Navigate to="/portal/locations/regions" replace />;
  }

  const handleInputChange = (value: string) => {
    setDraftFormState((previous) => ({
      ...(previous ?? initialFormState),
      name: value,
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
      title={isEditMode ? t('region.form.titleEdit') : t('region.form.titleCreate')}
      activeHref="#/portal/locations/regions"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('region.form.eyebrow')}
      heading={isEditMode ? t('region.form.headingEdit') : t('region.form.headingCreate')}
      backLinks={
        <Link
          to="/portal/locations/regions"
          className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          {t('region.form.backToList')}
        </Link>
      }
      isLoading={isEditMode && regionQuery.isLoading}
      loadingMessage={t('region.form.loading')}
      isError={regionQuery.isError}
      errorMessage={t('region.form.errorHeading')}
      errorDetail={(regionQuery.error as Error)?.message}
    >
      <FormSection icon="location_city" title={t('region.form.sectionTitle')} className="mt-6">
        <PrecisionInput
          label={t('region.form.nameLabel')}
          value={formState.name}
          onChange={(event) => handleInputChange(event.target.value)}
          placeholder={t('region.form.namePlaceholder')}
          required
        />

        {validationError ? (
          <div className="mt-4 rounded-xl bg-surface-container-lowest p-4">
            <p className="font-body text-on-surface">{validationError}</p>
          </div>
        ) : null}

        {saveMutation.isError ? (
          <div className="mt-4 rounded-xl bg-surface-container-lowest p-4">
            <p className="font-body text-on-surface">
              {(saveMutation.error as Error)?.message ?? t('region.form.saveFailed')}
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
            {saveMutation.isPending ? tCommon('buttons.saving') : isEditMode ? t('region.form.submitEdit') : t('region.form.submitCreate')}
          </button>
          <button
            type="button"
            onClick={() => navigate('/portal/locations/regions')}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {tCommon('buttons.cancel')}
          </button>
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
