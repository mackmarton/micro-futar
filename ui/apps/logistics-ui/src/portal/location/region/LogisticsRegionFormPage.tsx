import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import type { LocationRegionDTO } from '@package/shared-core/api/LogisticsApiClient';
import { createRegion, getRegionById, updateRegion } from '../../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../../navigation';
import { EntityFormShell } from '../../shared/EntityFormShell';

type RegionFormState = {
  name: string;
};

const toFormState = (region: LocationRegionDTO): RegionFormState => ({
  name: region.name ?? '',
});

const validateForm = (formState: RegionFormState): string | null => {
  if (!formState.name.trim()) {
    return 'A régió neve kötelező.';
  }

  return null;
};

const buildPayload = (formState: RegionFormState): LocationRegionDTO => ({
  name: formState.name.trim(),
});

export const LogisticsRegionFormPage = () => {
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
    const errorMessage = validateForm(formState);
    setValidationError(errorMessage);

    if (errorMessage) {
      return;
    }

    saveMutation.mutate();
  };

  return (
    <EntityFormShell
      title={isEditMode ? 'Régió szerkesztés' : 'Régió létrehozás'}
      activeHref="#/portal/locations/regions"
      navigationItems={logisticsNavigationItems}
      eyebrow="Helyszín form"
      heading={isEditMode ? 'Régió szerkesztés' : 'Új régió létrehozás'}
      backLinks={
        <Link
          to="/portal/locations/regions"
          className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          Vissza a régiókhoz
        </Link>
      }
      isLoading={isEditMode && regionQuery.isLoading}
      loadingMessage="A régió adatainak betöltése folyamatban..."
      isError={regionQuery.isError}
      errorMessage="A régió adatainak betöltése sikertelen."
      errorDetail={(regionQuery.error as Error)?.message}
    >
      <section className="mt-6 rounded-3xl bg-surface-container-low p-6 md:p-8">
        <label className="block rounded-2xl bg-surface-container-lowest p-4">
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Régió név</p>
          <input
            type="text"
            value={formState.name}
            onChange={(event) => handleInputChange(event.target.value)}
            className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
            placeholder="Pl.: Nyugat-Európa"
          />
        </label>

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
            {saveMutation.isPending ? 'Mentés...' : isEditMode ? 'Módosítás mentése' : 'Régió létrehozása'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/portal/locations/regions')}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Mégse
          </button>
        </div>
      </section>
    </EntityFormShell>
  );
};
