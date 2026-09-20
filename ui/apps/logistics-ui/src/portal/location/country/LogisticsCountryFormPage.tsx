import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { FormSection, PrecisionInput } from '@package/shared-ui';
import type { LocationCountryDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createCountry,
  getAllRegions,
  getCountryById,
  updateCountry,
} from '../../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../../navigation';
import { EntityFormShell } from '../../shared/EntityFormShell';

type CountryFormState = {
  name: string;
  regionId: string;
};

const parseSelectedId = (value: string | null) => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

const toFormState = (country: LocationCountryDTO): CountryFormState => ({
  name: country.name ?? '',
  regionId: typeof country.regionId === 'number' ? String(country.regionId) : '',
});

const validateForm = (formState: CountryFormState): string | null => {
  if (!formState.name.trim()) {
    return 'Az ország neve kötelező.';
  }

  if (!formState.regionId) {
    return 'A régió kiválasztása kötelező.';
  }

  return null;
};

const buildPayload = (formState: CountryFormState): LocationCountryDTO => ({
  name: formState.name.trim(),
  regionId: Number(formState.regionId),
});

export const LogisticsCountryFormPage = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();
  const [searchParams] = useSearchParams();

  const countryId = params.countryId ? Number(params.countryId) : null;
  const isEditMode = typeof countryId === 'number';
  const hasValidCountryId = !isEditMode || (Number.isInteger(countryId) && (countryId as number) > 0);
  const preselectedRegionId = parseSelectedId(searchParams.get('regionId'));
  const fallbackRegionId = preselectedRegionId !== null ? String(preselectedRegionId) : '';

  const [draftFormState, setDraftFormState] = useState<CountryFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const regionsQuery = useQuery({
    queryKey: ['logistics', 'locations', 'regions'],
    queryFn: getAllRegions,
    enabled: hasValidCountryId,
  });

  const countryQuery = useQuery({
    queryKey: ['logistics', 'locations', 'country', countryId],
    queryFn: () => getCountryById(countryId as number),
    enabled: isEditMode && hasValidCountryId,
  });

  const initialFormState = useMemo<CountryFormState>(() => {
    if (isEditMode) {
      return countryQuery.data ? toFormState(countryQuery.data) : { name: '', regionId: '' };
    }

    return { name: '', regionId: fallbackRegionId };
  }, [countryQuery.data, fallbackRegionId, isEditMode]);

  const formState = draftFormState ?? initialFormState;

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updateCountry(countryId as number, payload);
      }

      return createCountry(payload);
    },
    onSuccess: async (savedCountry) => {
      const savedRegionId = savedCountry.regionId;
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'locations', 'countries'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'locations', 'country'] }),
      ]);
      navigate(
        typeof savedRegionId === 'number'
          ? `/portal/locations/countries?regionId=${savedRegionId}`
          : '/portal/locations/countries',
      );
    },
  });

  if (!hasValidCountryId) {
    return <Navigate to="/portal/locations/countries" replace />;
  }

  const handleInputChange = (key: keyof CountryFormState, value: string) => {
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

  const countriesPageHref = formState.regionId
    ? `/portal/locations/countries?regionId=${formState.regionId}`
    : '/portal/locations/countries';

  return (
    <EntityFormShell
      title={isEditMode ? 'Ország szerkesztés' : 'Ország létrehozás'}
      activeHref="#/portal/locations/regions"
      navigationItems={logisticsNavigationItems}
      eyebrow="Helyszín form"
      heading={isEditMode ? 'Ország szerkesztés' : 'Új ország létrehozás'}
      backLinks={
        <Link
          to={countriesPageHref}
          className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          Vissza az országokhoz
        </Link>
      }
      isLoading={regionsQuery.isLoading || (isEditMode && countryQuery.isLoading)}
      loadingMessage="Az ország form betöltése folyamatban..."
      isError={regionsQuery.isError || countryQuery.isError}
      errorMessage="Az ország form megnyitása sikertelen."
      errorDetail={(regionsQuery.error as Error | null)?.message ?? (countryQuery.error as Error | null)?.message}
    >
      <FormSection icon="location_city" title="Ország adatai" className="mt-6">
        <div className="grid gap-4 md:grid-cols-2">
          <PrecisionInput
            label="Ország név"
            value={formState.name}
            onChange={(event) => handleInputChange('name', event.target.value)}
            placeholder="Pl.: Magyarország"
            required
          />

          <label className="block">
            <span className="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-3 block">
              Régió
              <span className="ml-1 text-red-600" aria-hidden="true">*</span>
            </span>
            <select
              value={formState.regionId}
              onChange={(event) => handleInputChange('regionId', event.target.value)}
              className="w-full bg-surface-container-lowest border-none rounded-lg p-4 focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all"
            >
              <option value="">Válassz régiót</option>
              {(regionsQuery.data ?? []).map((region) => (
                <option key={region.id ?? region.name} value={region.id ?? ''}>
                  {region.name ?? 'N/A'}
                </option>
              ))}
            </select>
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
            {saveMutation.isPending ? 'Mentés...' : isEditMode ? 'Módosítás mentése' : 'Ország létrehozása'}
          </button>
          <button
            type="button"
            onClick={() => navigate(countriesPageHref)}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Mégse
          </button>
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
