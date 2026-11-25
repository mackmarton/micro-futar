import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { FormSection, PrecisionInput, useAuth } from '@package/shared-ui';
import { LocationMapPicker } from '@package/shared-ui/LocationMapPicker';
import type { LocationCityDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createCity,
  geocodeCity,
  getAllCountries,
  getCityById,
  getCountryById,
  updateCity,
} from '../../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../../navigation';
import { EntityFormShell } from '../../shared/EntityFormShell';
import { LanguageSwitcher } from '../../../i18n/LanguageSwitcher';

type CityFormState = {
  name: string;
  countryId: string;
  centerLatitude: string;
  centerLongitude: string;
  bboxMinLatitude: string;
  bboxMinLongitude: string;
  bboxMaxLatitude: string;
  bboxMaxLongitude: string;
};

type GeoField = Exclude<keyof CityFormState, 'name' | 'countryId'>;

const GEO_FIELDS: GeoField[] = [
  'centerLatitude',
  'centerLongitude',
  'bboxMinLatitude',
  'bboxMinLongitude',
  'bboxMaxLatitude',
  'bboxMaxLongitude',
];

// Roughly the middle of Hungary, shown while the city has no centre yet.
const DEFAULT_MAP_CENTER = { latitude: 47.1625, longitude: 19.5033 };

const emptyFormState = (countryId = ''): CityFormState => ({
  name: '',
  countryId,
  centerLatitude: '',
  centerLongitude: '',
  bboxMinLatitude: '',
  bboxMinLongitude: '',
  bboxMaxLatitude: '',
  bboxMaxLongitude: '',
});

const numberToField = (value?: number) => (typeof value === 'number' ? String(value) : '');

const parseNumber = (value: string): number | null => {
  if (!value.trim()) {
    return null;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : Number.NaN;
};

const withGeo = (state: CityFormState, city: LocationCityDTO): CityFormState => ({
  ...state,
  centerLatitude: numberToField(city.centerLatitude),
  centerLongitude: numberToField(city.centerLongitude),
  bboxMinLatitude: numberToField(city.bboxMinLatitude),
  bboxMinLongitude: numberToField(city.bboxMinLongitude),
  bboxMaxLatitude: numberToField(city.bboxMaxLatitude),
  bboxMaxLongitude: numberToField(city.bboxMaxLongitude),
});

const parseSelectedId = (value: string | null) => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

const toFormState = (city: LocationCityDTO): CityFormState => ({
  ...withGeo(emptyFormState(typeof city.countryId === 'number' ? String(city.countryId) : ''), city),
  name: city.name ?? '',
});

const validateForm = (formState: CityFormState, t: (key: string) => string): string | null => {
  if (!formState.name.trim()) {
    return t('city.form.nameRequired');
  }

  if (!formState.countryId) {
    return t('city.form.countryRequired');
  }

  const values = Object.fromEntries(GEO_FIELDS.map((field) => [field, parseNumber(formState[field])])) as Record<
    GeoField,
    number | null
  >;
  if (GEO_FIELDS.some((field) => Number.isNaN(values[field]))) {
    return t('city.form.invalidCoordinates');
  }

  const latitudes = [values.centerLatitude, values.bboxMinLatitude, values.bboxMaxLatitude];
  const longitudes = [values.centerLongitude, values.bboxMinLongitude, values.bboxMaxLongitude];
  if (
    latitudes.some((value) => value !== null && (value < -90 || value > 90)) ||
    longitudes.some((value) => value !== null && (value < -180 || value > 180))
  ) {
    return t('city.form.invalidCoordinates');
  }

  if ((values.centerLatitude === null) !== (values.centerLongitude === null)) {
    return t('city.form.invalidCenter');
  }

  const bboxValues = [values.bboxMinLatitude, values.bboxMinLongitude, values.bboxMaxLatitude, values.bboxMaxLongitude];
  const filledBboxValues = bboxValues.filter((value) => value !== null).length;
  if (filledBboxValues !== 0 && filledBboxValues !== 4) {
    return t('city.form.invalidBbox');
  }
  if (
    filledBboxValues === 4 &&
    ((values.bboxMinLatitude as number) > (values.bboxMaxLatitude as number) ||
      (values.bboxMinLongitude as number) > (values.bboxMaxLongitude as number))
  ) {
    return t('city.form.invalidBbox');
  }

  return null;
};

// Geo values that are left empty are omitted: the server then keeps whatever it has stored.
const buildPayload = (formState: CityFormState): LocationCityDTO => {
  const payload: LocationCityDTO = {
    name: formState.name.trim(),
    countryId: Number(formState.countryId),
  };
  for (const field of GEO_FIELDS) {
    const value = parseNumber(formState[field]);
    if (value !== null) {
      payload[field] = value;
    }
  }
  return payload;
};

const GeoNumberInput = ({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
}) => (
  <label className="block">
    <span className="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-3 block">{label}</span>
    <input
      type="number"
      step="any"
      inputMode="decimal"
      value={value}
      onChange={(event) => onChange(event.target.value)}
      className="w-full bg-surface-container-lowest border-none rounded-lg p-4 focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all"
    />
  </label>
);

export const LogisticsCityFormPage = () => {
  const { t } = useTranslation('location');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();
  const [searchParams] = useSearchParams();

  const cityId = params.cityId ? Number(params.cityId) : null;
  const isEditMode = typeof cityId === 'number';
  const hasValidCityId = !isEditMode || (Number.isInteger(cityId) && (cityId as number) > 0);
  const preselectedCountryId = parseSelectedId(searchParams.get('countryId'));
  const fallbackCountryId = preselectedCountryId !== null ? String(preselectedCountryId) : '';

  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  const [draftFormState, setDraftFormState] = useState<CityFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [regeocodeMessage, setRegeocodeMessage] = useState<string | null>(null);

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
      return cityQuery.data ? toFormState(cityQuery.data) : emptyFormState();
    }

    return emptyFormState(fallbackCountryId);
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

  const regeocodeMutation = useMutation({
    mutationFn: () => geocodeCity(cityId as number),
    onSuccess: (city) => {
      queryClient.setQueryData(['logistics', 'locations', 'city', cityId], city);
      setDraftFormState((previous) => withGeo(previous ?? initialFormState, city));
      setValidationError(null);
      setRegeocodeMessage(t('city.form.regeocodeDone'));
      void queryClient.invalidateQueries({ queryKey: ['logistics', 'locations', 'cities'] });
    },
    onError: () => setRegeocodeMessage(null),
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
    setRegeocodeMessage(null);
  };

  const centerLatitude = parseNumber(formState.centerLatitude);
  const centerLongitude = parseNumber(formState.centerLongitude);
  const hasCenter =
    centerLatitude !== null && centerLongitude !== null && !Number.isNaN(centerLatitude) && !Number.isNaN(centerLongitude);
  const markerPosition = hasCenter
    ? { latitude: centerLatitude as number, longitude: centerLongitude as number }
    : DEFAULT_MAP_CENTER;

  const bboxNumbers = [
    parseNumber(formState.bboxMinLatitude),
    parseNumber(formState.bboxMinLongitude),
    parseNumber(formState.bboxMaxLatitude),
    parseNumber(formState.bboxMaxLongitude),
  ];
  const mapBounds = bboxNumbers.every((value) => value !== null && !Number.isNaN(value))
    ? {
        minLatitude: bboxNumbers[0] as number,
        minLongitude: bboxNumbers[1] as number,
        maxLatitude: bboxNumbers[2] as number,
        maxLongitude: bboxNumbers[3] as number,
      }
    : null;

  const savedCity = cityQuery.data;
  const geocodedAtLabel = savedCity?.geocodedAt
    ? new Date(savedCity.geocodedAt).toLocaleString()
    : t('city.form.neverGeocoded');

  const handleSubmit = () => {
    const errorMessage = validateForm(formState, t);
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
      title={isEditMode ? t('city.form.titleEdit') : t('city.form.titleCreate')}
      activeHref="#/portal/locations/regions"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('city.form.eyebrow')}
      heading={isEditMode ? t('city.form.headingEdit') : t('city.form.headingCreate')}
      backLinks={
        <>
          <Link
            to={citiesPageHref}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {t('city.form.backToList')}
          </Link>
          <Link
            to={countriesPageHref}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {t('city.form.backToCountries')}
          </Link>
        </>
      }
      isLoading={countriesQuery.isLoading || (isEditMode && cityQuery.isLoading)}
      loadingMessage={t('city.form.loading')}
      isError={countriesQuery.isError || cityQuery.isError}
      errorMessage={t('city.form.errorHeading')}
      errorDetail={(countriesQuery.error as Error | null)?.message ?? (cityQuery.error as Error | null)?.message}
    >
      <FormSection icon="location_city" title={t('city.form.sectionTitle')} className="mt-6">
        <div className="grid gap-4 md:grid-cols-2">
          <PrecisionInput
            label={t('city.form.nameLabel')}
            value={formState.name}
            onChange={(event) => handleInputChange('name', event.target.value)}
            placeholder={t('city.form.namePlaceholder')}
            required
          />

          <label className="block">
            <span className="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-3 block">
              {t('city.form.countryLabel')}
              <span className="ml-1 text-red-600" aria-hidden="true">*</span>
            </span>
            <select
              value={formState.countryId}
              onChange={(event) => handleInputChange('countryId', event.target.value)}
              className="w-full bg-surface-container-lowest border-none rounded-lg p-4 focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all"
            >
              <option value="">{t('city.form.countryPlaceholder')}</option>
              {(countriesQuery.data ?? []).map((country) => (
                <option key={country.id ?? country.name} value={country.id ?? ''}>
                  {/* country.name backend-ről érkező szabad szöveg, nem fordítjuk. */}
                  {country.name ?? tCommon('status.notAvailable')}
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
              {(saveMutation.error as Error)?.message ?? t('city.form.saveFailed')}
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
            {saveMutation.isPending ? tCommon('buttons.saving') : isEditMode ? t('city.form.submitEdit') : t('city.form.submitCreate')}
          </button>
          <button
            type="button"
            onClick={() => navigate(citiesPageHref)}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {tCommon('buttons.cancel')}
          </button>
        </div>
      </FormSection>

      <FormSection icon="map" title={t('city.form.geoTitle')} className="mt-6">
        <p className="font-body text-on-surface-variant">{t('city.form.geoHint')}</p>

        {isEditMode ? (
          <div className="mt-4 flex flex-wrap items-center gap-3">
            <span className="font-body text-on-surface">
              {t('city.form.geocodedAt')}: {geocodedAtLabel}
            </span>
            <span
              className={
                savedCity?.geoManual
                  ? 'rounded-full bg-blue-100 px-2 py-0.5 text-xs font-semibold text-blue-800'
                  : 'rounded-full bg-surface-container-lowest px-2 py-0.5 text-xs font-semibold text-on-surface-variant'
              }
            >
              {savedCity?.geoManual ? t('city.form.manualYes') : t('city.form.manualNo')}
            </span>
            {isAdmin ? (
              <button
                type="button"
                onClick={() => regeocodeMutation.mutate()}
                disabled={regeocodeMutation.isPending}
                className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:opacity-70"
              >
                {regeocodeMutation.isPending ? t('city.form.regeocoding') : t('city.form.regeocode')}
              </button>
            ) : null}
          </div>
        ) : null}

        {regeocodeMutation.isError ? (
          <p className="mt-3 font-body text-red-700" role="alert">
            {(regeocodeMutation.error as Error)?.message || t('city.form.regeocodeFailed')}
          </p>
        ) : null}
        {regeocodeMessage ? (
          <p className="mt-3 font-body text-on-surface" role="status">
            {regeocodeMessage}
          </p>
        ) : null}

        <p className="mt-6 text-xs font-bold uppercase tracking-widest text-on-surface-variant">
          {t('city.form.centerLabel')}
        </p>
        <p className="mt-1 font-body text-sm text-on-surface-variant">{t('city.form.centerHint')}</p>
        <div className="mt-3">
          <LocationMapPicker
            center={markerPosition}
            markerPosition={markerPosition}
            zoom={hasCenter ? 11 : 6}
            bounds={mapBounds}
            onMarkerChange={(position) => {
              setDraftFormState((previous) => ({
                ...(previous ?? initialFormState),
                centerLatitude: position.latitude.toFixed(6),
                centerLongitude: position.longitude.toFixed(6),
              }));
              setValidationError(null);
              setRegeocodeMessage(null);
            }}
          />
        </div>
        <div className="mt-4 grid gap-4 md:grid-cols-2">
          <GeoNumberInput
            label={t('city.form.centerLatitude')}
            value={formState.centerLatitude}
            onChange={(value) => handleInputChange('centerLatitude', value)}
          />
          <GeoNumberInput
            label={t('city.form.centerLongitude')}
            value={formState.centerLongitude}
            onChange={(value) => handleInputChange('centerLongitude', value)}
          />
        </div>

        <p className="mt-6 text-xs font-bold uppercase tracking-widest text-on-surface-variant">
          {t('city.form.bboxTitle')}
        </p>
        <div className="mt-3 grid gap-4 md:grid-cols-2">
          <GeoNumberInput
            label={t('city.form.bboxMinLatitude')}
            value={formState.bboxMinLatitude}
            onChange={(value) => handleInputChange('bboxMinLatitude', value)}
          />
          <GeoNumberInput
            label={t('city.form.bboxMinLongitude')}
            value={formState.bboxMinLongitude}
            onChange={(value) => handleInputChange('bboxMinLongitude', value)}
          />
          <GeoNumberInput
            label={t('city.form.bboxMaxLatitude')}
            value={formState.bboxMaxLatitude}
            onChange={(value) => handleInputChange('bboxMaxLatitude', value)}
          />
          <GeoNumberInput
            label={t('city.form.bboxMaxLongitude')}
            value={formState.bboxMaxLongitude}
            onChange={(value) => handleInputChange('bboxMaxLongitude', value)}
          />
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
