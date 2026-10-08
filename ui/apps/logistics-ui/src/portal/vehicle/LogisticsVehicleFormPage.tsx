import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { FormSection, PrecisionInput } from '@package/shared-ui';
import type { VehicleDTO } from '@package/shared-core/api/LogisticsApiClient';
import { createVehicle, getVehicleById, updateVehicle } from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityFormShell } from '../shared/EntityFormShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

type VehicleFormState = {
  registrationNumber: string;
  maximumPackableVolume: string;
};

const toFormState = (vehicle: VehicleDTO): VehicleFormState => ({
  registrationNumber: vehicle.registrationNumber ?? '',
  maximumPackableVolume: typeof vehicle.maximumPackableVolume === 'number' ? String(vehicle.maximumPackableVolume) : '',
});

const parsePositiveNumber = (value: string): number | null => {
  const parsed = Number(value.trim());
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
};

const validateForm = (formState: VehicleFormState, t: (key: string) => string): string | null => {
  if (!formState.registrationNumber.trim()) {
    return t('form.registrationNumberRequired');
  }

  if (parsePositiveNumber(formState.maximumPackableVolume) === null) {
    return t('form.maximumPackableVolumeInvalid');
  }

  return null;
};

const buildPayload = (formState: VehicleFormState): VehicleDTO => ({
  registrationNumber: formState.registrationNumber.trim(),
  maximumPackableVolume: parsePositiveNumber(formState.maximumPackableVolume) ?? undefined,
});

export const LogisticsVehicleFormPage = () => {
  const { t } = useTranslation('vehicle');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();

  const vehicleId = params.vehicleId ? Number(params.vehicleId) : null;
  const isEditMode = typeof vehicleId === 'number';
  const hasValidVehicleId = !isEditMode || (Number.isInteger(vehicleId) && (vehicleId as number) > 0);

  const [draftFormState, setDraftFormState] = useState<VehicleFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const vehicleQuery = useQuery({
    queryKey: ['logistics', 'vehicle', vehicleId],
    queryFn: () => getVehicleById(vehicleId as number),
    enabled: isEditMode && hasValidVehicleId,
  });

  const initialFormState = useMemo<VehicleFormState>(() => {
    if (isEditMode) {
      return vehicleQuery.data ? toFormState(vehicleQuery.data) : { registrationNumber: '', maximumPackableVolume: '' };
    }

    return { registrationNumber: '', maximumPackableVolume: '' };
  }, [isEditMode, vehicleQuery.data]);

  const formState = draftFormState ?? initialFormState;

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updateVehicle(vehicleId as number, payload);
      }

      return createVehicle(payload);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'vehicles'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'vehicle'] }),
      ]);

      navigate('/portal/vehicles');
    },
  });

  if (!hasValidVehicleId) {
    return <Navigate to="/portal/vehicles" replace />;
  }

  const handleInputChange = (key: keyof VehicleFormState, value: string) => {
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
      activeHref="#/portal/vehicles"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('form.eyebrow')}
      heading={isEditMode ? t('form.headingEdit') : t('form.headingCreate')}
      backLinks={
        <Link
          to="/portal/vehicles"
          className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          {t('form.backToList')}
        </Link>
      }
      isLoading={isEditMode && vehicleQuery.isLoading}
      loadingMessage={t('form.loading')}
      isError={vehicleQuery.isError}
      errorMessage={t('form.errorHeading')}
      errorDetail={(vehicleQuery.error as Error)?.message}
    >
      <FormSection icon="delivery_truck_speed" title={t('form.sectionTitle')} className="mt-6">
        <div className="grid gap-4 md:grid-cols-2">
          <PrecisionInput
            label={t('form.registrationNumberLabel')}
            value={formState.registrationNumber}
            onChange={(event) => handleInputChange('registrationNumber', event.target.value)}
            placeholder={t('form.registrationNumberPlaceholder')}
            required
          />

          <PrecisionInput
            label={t('form.maximumPackableVolumeLabel')}
            type="number"
            value={formState.maximumPackableVolume}
            onChange={(event) => handleInputChange('maximumPackableVolume', event.target.value)}
            placeholder={t('form.maximumPackableVolumePlaceholder')}
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
            onClick={() => navigate('/portal/vehicles')}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {tCommon('buttons.cancel')}
          </button>
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
