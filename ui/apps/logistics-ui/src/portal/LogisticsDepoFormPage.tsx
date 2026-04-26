import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { PortalLayout } from '@package/shared-ui';
import type { DepoDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createDepo,
  getAllCities,
  getAllCountries,
  getDepoByIdWithLookups,
  updateDepo,
} from './api/logisticsDeposApi';
import { logisticsNavigationItems } from './navigation';
import { DepoLocationMapPicker, type MapCoordinate } from './components/DepoLocationMapPicker';

type DepoFormState = {
  name: string;
  locationCountryId: string;
  locationCityId: string;
  zip: string;
  address: string;
  latitude: string;
  longitude: string;
};

const emptyFormState: DepoFormState = {
  name: '',
  locationCountryId: '',
  locationCityId: '',
  zip: '',
  address: '',
  latitude: '',
  longitude: '',
};

const defaultMapCoordinate: MapCoordinate = {
  latitude: 47.497913,
  longitude: 19.040236,
};

const toFormState = (depo: DepoDTO): DepoFormState => ({
  name: depo.name ?? '',
  locationCountryId: typeof depo.locationCountryId === 'number' ? String(depo.locationCountryId) : '',
  locationCityId: typeof depo.locationCityId === 'number' ? String(depo.locationCityId) : '',
  zip: depo.zip ?? '',
  address: depo.address ?? '',
  latitude: typeof depo.latitude === 'number' ? String(depo.latitude) : '',
  longitude: typeof depo.longitude === 'number' ? String(depo.longitude) : '',
});

const parseOptionalNumber = (value: string): number | undefined => {
  const trimmed = value.trim();
  if (!trimmed) {
    return undefined;
  }

  const parsed = Number(trimmed);
  return Number.isFinite(parsed) ? parsed : undefined;
};

const buildPayload = (formState: DepoFormState): DepoDTO => ({
  name: formState.name,
  locationCountryId: Number(formState.locationCountryId),
  locationCityId: Number(formState.locationCityId),
  zip: formState.zip.trim(),
  address: formState.address.trim(),
  latitude: parseOptionalNumber(formState.latitude),
  longitude: parseOptionalNumber(formState.longitude),
});

const validateForm = (formState: DepoFormState): string | null => {
  if (!formState.name) {
    return 'A név megadása kötelező.';
  }

  if (!formState.locationCountryId) {
    return 'Az ország kiválasztása kötelező.';
  }

  if (!formState.locationCityId) {
    return 'A város kiválasztása kötelező.';
  }

  if (!formState.zip.trim()) {
    return 'Az irányítószám megadása kötelező.';
  }

  if (!formState.address.trim()) {
    return 'A cím megadása kötelező.';
  }

  const latitude = parseOptionalNumber(formState.latitude);
  const longitude = parseOptionalNumber(formState.longitude);

  if (latitude === undefined || longitude === undefined) {
    return 'A térképen jelöld ki a depó koordinátáit és erősítsd meg a pint.';
  }

  return null;
};

type GeocodeResult = {
  latitude: number;
  longitude: number;
};

const geocodeAddress = async (query: string, signal?: AbortSignal): Promise<GeocodeResult | null> => {
  const url = `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(query)}`;
  const response = await fetch(url, {
    signal,
    headers: {
      Accept: 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error('A cím geokódolása sikertelen.');
  }

  const data = (await response.json()) as Array<{ lat?: string; lon?: string }>;
  const topResult = data[0];

  if (!topResult?.lat || !topResult?.lon) {
    return null;
  }

  const latitude = Number(topResult.lat);
  const longitude = Number(topResult.lon);

  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    return null;
  }

  return { latitude, longitude };
};

const formatCoordinate = (value: number) => value.toFixed(6);

export const LogisticsDepoFormPage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const params = useParams();

  const depoId = params.depoId ? Number(params.depoId) : null;
  const isEditMode = typeof depoId === 'number';
  const hasValidDepoId = !isEditMode || (Number.isInteger(depoId) && depoId > 0);

  const [formState, setFormState] = useState<DepoFormState>(emptyFormState);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [mapCenter, setMapCenter] = useState<MapCoordinate>(defaultMapCoordinate);
  const [mapMarkerPosition, setMapMarkerPosition] = useState<MapCoordinate>(defaultMapCoordinate);
  const [isGeocoding, setIsGeocoding] = useState(false);
  const [geocodeError, setGeocodeError] = useState<string | null>(null);
  const [isPinConfirmed, setIsPinConfirmed] = useState(false);
  const lastGeocodeQueryRef = useRef<string>('');

  const countriesQuery = useQuery({
    queryKey: ['logistics', 'countries'],
    queryFn: getAllCountries,
  });

  const citiesQuery = useQuery({
    queryKey: ['logistics', 'cities'],
    queryFn: getAllCities,
  });

  const depoQuery = useQuery({
    queryKey: ['logistics', 'depo', depoId],
    queryFn: () => getDepoByIdWithLookups(depoId as number),
    enabled: isEditMode && hasValidDepoId,
  });

  useEffect(() => {
    if (depoQuery.data) {
      const nextFormState = toFormState(depoQuery.data);
      const latitude = parseOptionalNumber(nextFormState.latitude);
      const longitude = parseOptionalNumber(nextFormState.longitude);

      setFormState(nextFormState);

      if (latitude !== undefined && longitude !== undefined) {
        const coordinate = { latitude, longitude };
        setMapCenter(coordinate);
        setMapMarkerPosition(coordinate);
        setIsPinConfirmed(true);
      }
    }
  }, [depoQuery.data]);

  const filteredCities = useMemo(() => {
    if (!formState.locationCountryId) {
      return citiesQuery.data ?? [];
    }

    const selectedCountryId = Number(formState.locationCountryId);
    return (citiesQuery.data ?? []).filter((city) => city.countryId === selectedCountryId);
  }, [citiesQuery.data, formState.locationCountryId]);

  const selectedCountryName = useMemo(() => {
    const selectedCountryId = Number(formState.locationCountryId);
    if (!Number.isFinite(selectedCountryId)) {
      return '';
    }

    return (countriesQuery.data ?? []).find((country) => country.id === selectedCountryId)?.name ?? '';
  }, [countriesQuery.data, formState.locationCountryId]);

  const selectedCityName = useMemo(() => {
    const selectedCityId = Number(formState.locationCityId);
    if (!Number.isFinite(selectedCityId)) {
      return '';
    }

    return (citiesQuery.data ?? []).find((city) => city.id === selectedCityId)?.name ?? '';
  }, [citiesQuery.data, formState.locationCityId]);

  const geocodeQuery = useMemo(() => {
    const addressParts = [
      formState.address.trim(),
      formState.zip.trim(),
      selectedCityName.trim(),
      selectedCountryName.trim(),
    ].filter(Boolean);

    if (addressParts.length < 3) {
      return '';
    }

    return addressParts.join(', ');
  }, [formState.address, formState.zip, selectedCityName, selectedCountryName]);

  const updateCoordinatesFromMap = useCallback((coordinate: MapCoordinate) => {
    setFormState((previous) => ({
      ...previous,
      latitude: formatCoordinate(coordinate.latitude),
      longitude: formatCoordinate(coordinate.longitude),
    }));
    setValidationError(null);
    setIsPinConfirmed(true);
  }, []);

  const handleMapMarkerChange = useCallback((coordinate: MapCoordinate) => {
    setMapMarkerPosition(coordinate);
    setIsPinConfirmed(false);
  }, []);

  const runAddressGeocode = useCallback(
    async (query: string, signal?: AbortSignal) => {
      if (!query) {
        return;
      }

      setIsGeocoding(true);
      setGeocodeError(null);

      try {
        const result = await geocodeAddress(query, signal);

        if (!result) {
          setGeocodeError('Nem találtunk pontos találatot a megadott címhez.');
          return;
        }

        setMapCenter(result);
        setMapMarkerPosition(result);
        setIsPinConfirmed(false);
      } catch (error) {
        if ((error as Error).name === 'AbortError') {
          return;
        }

        setGeocodeError((error as Error).message ?? 'A cím geokódolása sikertelen.');
      } finally {
        setIsGeocoding(false);
      }
    },
    [],
  );

  useEffect(() => {
    if (!geocodeQuery || geocodeQuery === lastGeocodeQueryRef.current) {
      return;
    }

    const abortController = new AbortController();
    const timeoutId = window.setTimeout(() => {
      lastGeocodeQueryRef.current = geocodeQuery;
      void runAddressGeocode(geocodeQuery, abortController.signal);
    }, 700);

    return () => {
      abortController.abort();
      window.clearTimeout(timeoutId);
    };
  }, [geocodeQuery, runAddressGeocode]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updateDepo(depoId as number, payload);
      }

      return createDepo(payload);
    },
    onSuccess: async (savedDepo) => {
      await queryClient.invalidateQueries({ queryKey: ['logistics', 'depos'] });

      if (typeof savedDepo.id === 'number') {
        await queryClient.invalidateQueries({ queryKey: ['logistics', 'depo', savedDepo.id] });
        navigate(`/portal/depos/${savedDepo.id}`);
        return;
      }

      navigate('/portal/depos');
    },
  });

  if (!hasValidDepoId) {
    return <Navigate to="/portal/depos" replace />;
  }

  const isPageLoading =
    countriesQuery.isLoading ||
    citiesQuery.isLoading ||
    (isEditMode && depoQuery.isLoading);

  const pageError =
    (countriesQuery.error as Error | null) ??
    (citiesQuery.error as Error | null) ??
    (depoQuery.error as Error | null);

  const isPageError = countriesQuery.isError || citiesQuery.isError || depoQuery.isError;

  const handleInputChange = (key: keyof DepoFormState, value: string | boolean) => {
    setFormState((previous) => ({
      ...previous,
      [key]: value,
    }));

    if (key === 'address' || key === 'zip' || key === 'locationCityId' || key === 'locationCountryId') {
      setIsPinConfirmed(false);
    }
  };

  const handleSubmit = () => {
    if (!isPinConfirmed) {
      setValidationError('A térképen pozicionált pint előbb erősítsd meg.');
      return;
    }

    const errorMessage = validateForm(formState);
    setValidationError(errorMessage);

    if (errorMessage) {
      return;
    }

    saveMutation.mutate();
  };

  return (
    <PortalLayout
      title={isEditMode ? 'Depó szerkesztés' : 'Depó létrehozás'}
      activeHref="#/portal/depos"
      navigationItems={logisticsNavigationItems}
    >
      <section className="rounded-3xl bg-surface-container-low p-6 md:p-8">
        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Depo form</p>
        <h1 className="mt-2 text-2xl md:text-3xl font-headline text-on-surface">
          {isEditMode ? 'Depó szerkesztés' : 'Új depó létrehozás'}
        </h1>
        <p className="mt-3 font-body text-on-surface-variant">
          A kötelező mezők: név, ország, város, irányítószám és cím.
        </p>

        <div className="mt-5 flex flex-wrap gap-3">
          <Link
            to="/portal/depos"
            className="inline-flex items-center gap-2 rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Vissza a depók listájához
          </Link>
        </div>
      </section>

      {isPageLoading ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Betöltés</p>
          <p className="mt-2 font-body text-on-surface">A form adatok betöltése folyamatban...</p>
        </section>
      ) : null}

      {isPageError ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Hiba</p>
          <p className="mt-2 font-body text-on-surface">A form megnyitása sikertelen.</p>
          <p className="mt-1 font-body text-on-surface-variant">{pageError?.message ?? 'Ismeretlen hiba.'}</p>
        </section>
      ) : null}

      {!isPageLoading && !isPageError ? (
        <section className="mt-6 rounded-3xl bg-surface-container-low p-6 md:p-8">
          <div className="grid gap-4 md:grid-cols-2">
            <label className="rounded-2xl bg-surface-container-lowest p-4 col-span-2">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Név</p>
              <input
                  type="text"
                  value={formState.name}
                  onChange={(event) => handleInputChange('name', event.target.value)}
                  className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                  placeholder="Pl.: Budapest"
              />
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Ország</p>
              <select
                value={formState.locationCountryId}
                onChange={(event) => {
                  const newCountryId = event.target.value;
                  handleInputChange('locationCountryId', newCountryId);

                  if (newCountryId && filteredCities.every((city) => String(city.id) !== formState.locationCityId)) {
                    handleInputChange('locationCityId', '');
                  }
                }}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
              >
                <option value="">Válassz országot</option>
                {(countriesQuery.data ?? []).map((country) => (
                  <option key={country.id ?? country.name} value={country.id ?? ''}>
                    {country.name ?? 'N/A'}
                  </option>
                ))}
              </select>
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Város</p>
              <select
                value={formState.locationCityId}
                onChange={(event) => handleInputChange('locationCityId', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
              >
                <option value="">Válassz várost</option>
                {filteredCities.map((city) => (
                  <option key={city.id ?? city.name} value={city.id ?? ''}>
                    {city.name ?? 'N/A'}
                  </option>
                ))}
              </select>
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Irányítószám</p>
              <input
                type="text"
                value={formState.zip}
                onChange={(event) => handleInputChange('zip', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Pl.: 2045"
              />
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Cím</p>
              <input
                type="text"
                value={formState.address}
                onChange={(event) => handleInputChange('address', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Pl.: Fő utca 1."
              />
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Latitude</p>
              <input
                type="text"
                value={formState.latitude}
                readOnly
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Térképről kerül kitöltésre"
              />
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Longitude</p>
              <input
                type="text"
                value={formState.longitude}
                readOnly
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Térképről kerül kitöltésre"
              />
            </label>

            <section className="rounded-2xl bg-surface-container-lowest p-4 md:col-span-2">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Térképes pozíció</p>
                  <p className="mt-1 font-body text-on-surface-variant">
                    A térkép a beírt címre pozicionál. Húzd a pint vagy kattints a térképen, majd erősítsd meg.
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    if (!geocodeQuery) {
                      setGeocodeError('Adj meg címet, várost és országot az automatikus pozicionáláshoz.');
                      return;
                    }

                    lastGeocodeQueryRef.current = geocodeQuery;
                    void runAddressGeocode(geocodeQuery);
                  }}
                  className="inline-flex items-center rounded-lg bg-surface px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
                >
                  Cím alapján újrapozicionálás
                </button>
              </div>

              <div className="mt-4 rounded-xl overflow-hidden">
                <DepoLocationMapPicker
                  center={mapCenter}
                  markerPosition={mapMarkerPosition}
                  onMarkerChange={handleMapMarkerChange}
                />
              </div>

              <div className="mt-4 flex flex-wrap items-center gap-3">
                <button
                  type="button"
                  onClick={() => updateCoordinatesFromMap(mapMarkerPosition)}
                  className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                >
                  Pin megerősítése
                </button>
                <p className="font-body text-on-surface-variant">
                  Jelölt pont: {formatCoordinate(mapMarkerPosition.latitude)}, {formatCoordinate(mapMarkerPosition.longitude)}
                </p>
              </div>

              {isGeocoding ? (
                <p className="mt-3 font-body text-on-surface-variant">Automatikus címkeresés folyamatban...</p>
              ) : null}

              {geocodeError ? <p className="mt-3 font-body text-on-surface-variant">{geocodeError}</p> : null}

              {!isPinConfirmed ? (
                <p className="mt-3 font-body text-on-surface-variant">A mentéshez erősítsd meg a térképen beállított pint.</p>
              ) : null}
            </section>
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
              {saveMutation.isPending ? 'Mentés...' : isEditMode ? 'Módosítás mentése' : 'Depó létrehozása'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/portal/depos')}
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

