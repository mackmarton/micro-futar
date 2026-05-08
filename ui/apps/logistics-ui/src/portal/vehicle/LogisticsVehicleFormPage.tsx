import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { PortalLayout } from '@package/shared-ui';
import type { VehicleDTO } from '@package/shared-core/api/LogisticsApiClient';
import { createVehicle, getVehicleById, updateVehicle } from '../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../navigation';

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
    <PortalLayout
      title={isEditMode ? 'Jármű szerkesztés' : 'Jármű létrehozás'}
      activeHref="#/portal/vehicles"
      navigationItems={logisticsNavigationItems}
    >
      <section className="rounded-3xl bg-surface-container-low p-6 md:p-8">
        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Jármű form</p>
        <h1 className="mt-2 text-2xl md:text-3xl font-headline text-on-surface">
          {isEditMode ? 'Jármű szerkesztés' : 'Új jármű létrehozás'}
        </h1>
        <div className="mt-5">
          <Link
            to="/portal/vehicles"
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Vissza a járművekhez
          </Link>
        </div>
      </section>

      {isEditMode && vehicleQuery.isLoading ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="font-body text-on-surface">A jármű adatainak betöltése folyamatban...</p>
        </section>
      ) : null}

      {vehicleQuery.isError ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="font-body text-on-surface">A jármű adatainak betöltése sikertelen.</p>
          <p className="mt-1 font-body text-on-surface-variant">
            {(vehicleQuery.error as Error)?.message ?? 'Ismeretlen hiba'}
          </p>
        </section>
      ) : null}

      {(!isEditMode || (!vehicleQuery.isLoading && !vehicleQuery.isError)) ? (
        <section className="mt-6 rounded-3xl bg-surface-container-low p-6 md:p-8">
          <div className="grid gap-4 md:grid-cols-2">
            <label className="rounded-2xl bg-surface-container-lowest p-4 md:col-span-2">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Rendszám</p>
              <input
                type="text"
                value={formState.registrationNumber}
                onChange={(event) => handleInputChange('registrationNumber', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Pl.: ABC-123"
              />
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4 md:col-span-2">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Max térfogat (cm³)</p>
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={formState.maximumPackableVolume}
                onChange={(event) => handleInputChange('maximumPackableVolume', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Pl.: 120000"
              />
            </label>
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
        </section>
      ) : null}
    </PortalLayout>
  );
};
