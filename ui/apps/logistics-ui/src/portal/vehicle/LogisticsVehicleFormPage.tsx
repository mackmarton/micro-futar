import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { FormSection, PrecisionInput } from '@package/shared-ui';
import type { VehicleDTO } from '@package/shared-core/api/LogisticsApiClient';
import { createVehicle, getVehicleById, updateVehicle } from '../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../navigation';
import { EntityFormShell } from '../shared/EntityFormShell';

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

const validateForm = (formState: VehicleFormState): string | null => {
  if (!formState.registrationNumber.trim()) {
    return 'A rendszám megadása kötelező.';
  }

  if (parsePositiveNumber(formState.maximumPackableVolume) === null) {
    return 'A maximális térfogat legyen pozitív szám.';
  }

  return null;
};

const buildPayload = (formState: VehicleFormState): VehicleDTO => ({
  registrationNumber: formState.registrationNumber.trim(),
  maximumPackableVolume: parsePositiveNumber(formState.maximumPackableVolume) ?? undefined,
});

export const LogisticsVehicleFormPage = () => {
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
    const errorMessage = validateForm(formState);
    setValidationError(errorMessage);

    if (errorMessage) {
      return;
    }

    saveMutation.mutate();
  };

  return (
    <EntityFormShell
      title={isEditMode ? 'Jármű szerkesztés' : 'Jármű létrehozás'}
      activeHref="#/portal/vehicles"
      navigationItems={logisticsNavigationItems}
      eyebrow="Jármű form"
      heading={isEditMode ? 'Jármű szerkesztés' : 'Új jármű létrehozás'}
      backLinks={
        <Link
          to="/portal/vehicles"
          className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          Vissza a járművekhez
        </Link>
      }
      isLoading={isEditMode && vehicleQuery.isLoading}
      loadingMessage="A jármű adatainak betöltése folyamatban..."
      isError={vehicleQuery.isError}
      errorMessage="A jármű adatainak betöltése sikertelen."
      errorDetail={(vehicleQuery.error as Error)?.message}
    >
      <FormSection icon="delivery_truck_speed" title="Jármű adatai" className="mt-6">
        <div className="grid gap-4 md:grid-cols-2">
          <PrecisionInput
            label="Rendszám"
            value={formState.registrationNumber}
            onChange={(event) => handleInputChange('registrationNumber', event.target.value)}
            placeholder="Pl.: ABC-123"
            required
          />

          <PrecisionInput
            label="Max térfogat (cm³)"
            type="number"
            value={formState.maximumPackableVolume}
            onChange={(event) => handleInputChange('maximumPackableVolume', event.target.value)}
            placeholder="Pl.: 120000"
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
              {(saveMutation.error as Error)?.message ?? 'A mentés nem sikerült.'}
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
            {saveMutation.isPending ? 'Mentés...' : isEditMode ? 'Módosítás mentése' : 'Jármű létrehozása'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/portal/vehicles')}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Mégse
          </button>
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
