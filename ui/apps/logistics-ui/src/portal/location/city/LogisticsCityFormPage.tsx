import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { FormSection, PrecisionInput } from '@package/shared-ui';
import type { LocationCityDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createCity,
  getAllCountries,
  getCityById,
  getCountryById,
  updateCity,
} from '../../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../../navigation';
import { EntityFormShell } from '../../shared/EntityFormShell';

type CityFormState = {
  name: string;
  countryId: string;
};

const parseSelectedId = (value: string | null) => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

const toFormState = (city: LocationCityDTO): CityFormState => ({
  name: city.name ?? '',
  countryId: typeof city.countryId === 'number' ? String(city.countryId) : '',
});

const validateForm = (formState: CityFormState): string | null => {
  if (!formState.name.trim()) {
    return 'A város neve kötelező.';
  }

  if (!formState.countryId) {
    return 'Az ország kiválasztása kötelező.';
  }

  return null;
};

const buildPayload = (formState: CityFormState): LocationCityDTO => ({
  name: formState.name.trim(),
  countryId: Number(formState.countryId),
});

export const LogisticsCityFormPage = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();
  const [searchParams] = useSearchParams();

  const cityId = params.cityId ? Number(params.cityId) : null;
  const isEditMode = typeof cityId === 'number';
  const hasValidCityId = !isEditMode || (Number.isInteger(cityId) && (cityId as number) > 0);
  const preselectedCountryId = parseSelectedId(searchParams.get('countryId'));
  const fallbackCountryId = preselectedCountryId !== null ? String(preselectedCountryId) : '';

  const [draftFormState, setDraftFormState] = useState<CityFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const countriesQuery = useQuery({
    queryKey: ['logistics', 'locations', 'all-countries'],
    queryFn: getAllCountries,
    enabled: hasValidCityId,
  });

  const cityQuery = useQuery({
    queryKey: ['logistics', 'locations', 'city', cityId],
    queryFn: () => getCityById(cityId as number),
    enabled: isEditMode && hasValidCityId,
  });

  const initialFormState = useMemo<CityFormState>(() => {
    if (isEditMode) {
      return cityQuery.data ? toFormState(cityQuery.data) : { name: '', countryId: '' };
    }

    return { name: '', countryId: fallbackCountryId };
  }, [cityQuery.data, fallbackCountryId, isEditMode]);

  const formState = draftFormState ?? initialFormState;

  const selectedCountryNumber = parseSelectedId(formState.countryId);
  const selectedCountryQuery = useQuery({
    queryKey: ['logistics', 'locations', 'country', selectedCountryNumber],
    queryFn: () => getCountryById(selectedCountryNumber as number),
    enabled: selectedCountryNumber !== null,
  });

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updateCity(cityId as number, payload);
      }

      return createCity(payload);
    },
    onSuccess: async (savedCity) => {
      const savedCountryId = savedCity.countryId;
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'locations', 'cities'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'locations', 'city'] }),
      ]);
      navigate(
        typeof savedCountryId === 'number'
          ? `/portal/locations/cities?countryId=${savedCountryId}`
          : '/portal/locations/cities',
      );
    },
  });

  if (!hasValidCityId) {
    return <Navigate to="/portal/locations/cities" replace />;
  }

  const handleInputChange = (key: keyof CityFormState, value: string) => {
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

  const citiesPageHref = formState.countryId
    ? `/portal/locations/cities?countryId=${formState.countryId}`
    : '/portal/locations/cities';
  const regionId = selectedCountryQuery.data?.regionId;
  const countriesPageHref =
    typeof regionId === 'number' ? `/portal/locations/countries?regionId=${regionId}` : '/portal/locations/countries';

  return (
    <EntityFormShell
      title={isEditMode ? 'Város szerkesztés' : 'Város létrehozás'}
      activeHref="#/portal/locations/regions"
      navigationItems={logisticsNavigationItems}
      eyebrow="Helyszín form"
      heading={isEditMode ? 'Város szerkesztés' : 'Új város létrehozás'}
      backLinks={
        <>
          <Link
            to={citiesPageHref}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Vissza a városokhoz
          </Link>
          <Link
            to={countriesPageHref}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Vissza az országokhoz
          </Link>
        </>
      }
      isLoading={countriesQuery.isLoading || (isEditMode && cityQuery.isLoading)}
      loadingMessage="A város form betöltése folyamatban..."
      isError={countriesQuery.isError || cityQuery.isError}
      errorMessage="A város form megnyitása sikertelen."
      errorDetail={(countriesQuery.error as Error | null)?.message ?? (cityQuery.error as Error | null)?.message}
    >
      <FormSection icon="location_city" title="Város adatai" className="mt-6">
        <div className="grid gap-4 md:grid-cols-2">
          <PrecisionInput
            label="Város név"
            value={formState.name}
            onChange={(event) => handleInputChange('name', event.target.value)}
            placeholder="Pl.: Budapest"
            required
          />

          <label className="block">
            <span className="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-3 block">
              Ország
              <span className="ml-1 text-red-600" aria-hidden="true">*</span>
            </span>
            <select
              value={formState.countryId}
              onChange={(event) => handleInputChange('countryId', event.target.value)}
              className="w-full bg-surface-container-lowest border-none rounded-lg p-4 focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all"
            >
              <option value="">Válassz országot</option>
              {(countriesQuery.data ?? []).map((country) => (
                <option key={country.id ?? country.name} value={country.id ?? ''}>
                  {country.name ?? 'N/A'}
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
            {saveMutation.isPending ? 'Mentés...' : isEditMode ? 'Módosítás mentése' : 'Város létrehozása'}
          </button>
          <button
            type="button"
            onClick={() => navigate(citiesPageHref)}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Mégse
          </button>
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
