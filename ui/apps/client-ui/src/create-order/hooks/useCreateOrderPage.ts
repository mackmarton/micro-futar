import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useAuth, type MapCoordinate } from '@package/shared-ui';
import { type AddressCardField, type AddressCardValue } from '../components/AddressCard.tsx';
import { type PackageDetailsValue, type PackageSizeId } from '../components/PackageDetailsSection.tsx';
import { createShipment } from '../api/ordersApi.ts';
import { useCities } from './useCities.ts';
import { useCountries } from './useCountries.ts';
import { useCountryPrices } from './useCountryPrices.ts';
import { usePackageSizes } from './usePackageSizes.ts';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

type AddressCardRole = 'sender' | 'recipient';

type AddressLocationState = {
  center: MapCoordinate;
  markerPosition: MapCoordinate;
  confirmedPosition: MapCoordinate | null;
  isConfirmed: boolean;
  isGeocoding: boolean;
  geocodeError: string | null;
};

const defaultMapCoordinate: MapCoordinate = {
  latitude: 47.497913,
  longitude: 19.040236,
};

const createInitialAddressLocationState = (): AddressLocationState => ({
  center: defaultMapCoordinate,
  markerPosition: defaultMapCoordinate,
  confirmedPosition: null,
  isConfirmed: false,
  isGeocoding: false,
  geocodeError: null,
});

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

const createEmptyAddressCardValue = (): AddressCardValue => ({
  name: '',
  phone: '',
  email: '',
  zipCode: '',
  country: '',
  city: '',
  address: '',
});

const initialPackageDetails: PackageDetailsValue = {
  sizeId: 0,
  weight: '2.5',
  description: '',
};

const isAddressCardValid = (addressCard: AddressCardValue) => {
  const hasRequiredTextValues = (
    ['name', 'phone', 'zipCode', 'country', 'city', 'address'] satisfies AddressCardField[]
  ).every((field) => addressCard[field].trim().length > 0);

  const hasValidEmail = EMAIL_PATTERN.test(addressCard.email.trim());

  return hasRequiredTextValues && hasValidEmail;
};

export const useCreateOrderPage = () => {
  const [addressCards, setAddressCards] = useState<Record<AddressCardRole, AddressCardValue>>({
    sender: createEmptyAddressCardValue(),
    recipient: createEmptyAddressCardValue(),
  });
  const [packageDetailsValue, setPackageDetailsValue] = useState<PackageDetailsValue>(initialPackageDetails);
  const [isSenderNameTouched, setIsSenderNameTouched] = useState(false);
  const [isSenderEmailTouched, setIsSenderEmailTouched] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitErrorMessage, setSubmitErrorMessage] = useState<string | null>(null);
  const [submitSuccessMessage, setSubmitSuccessMessage] = useState<string | null>(null);
  const [addressLocations, setAddressLocations] = useState<Record<AddressCardRole, AddressLocationState>>({
    sender: createInitialAddressLocationState(),
    recipient: createInitialAddressLocationState(),
  });
  const lastGeocodeQueryRef = useRef<Record<AddressCardRole, string>>({
    sender: '',
    recipient: '',
  });

  const { user, isLoading: isAuthLoading } = useAuth();
  const { countryOptions, isLoading: isCountryLoading, errorMessage: countriesErrorMessage, retry } = useCountries();
  const {
    cityOptions: senderCityOptions,
    isLoading: isSenderCityLoading,
    errorMessage: senderCitiesErrorMessage,
    retry: retrySenderCities,
  } = useCities(addressCards.sender.country);
  const {
    cityOptions: recipientCityOptions,
    isLoading: isRecipientCityLoading,
    errorMessage: recipientCitiesErrorMessage,
    retry: retryRecipientCities,
  } = useCities(addressCards.recipient.country);
  const {
    packageSizeOptions,
    isLoading: isPackageSizesLoading,
    errorMessage: packageSizesErrorMessage,
    retry: retryPackageSizes,
  } = usePackageSizes();
  const {
    countryPrices,
    isLoading: isCountryPricesLoading,
    errorMessage: countryPricesErrorMessage,
    retry: retryCountryPrices,
  } = useCountryPrices(addressCards.sender.country, addressCards.recipient.country);

  const isRouteSelected = Boolean(addressCards.sender.country && addressCards.recipient.country);

  const selectedCountryPrice = useMemo(() => {
    if (!isRouteSelected || !packageDetailsValue.sizeId) {
      return undefined;
    }

    const originCountryId = Number(addressCards.sender.country);
    const destinationCountryId = Number(addressCards.recipient.country);

    if (!Number.isInteger(originCountryId) || !Number.isInteger(destinationCountryId)) {
      return undefined;
    }

    return countryPrices.find(
      (countryPrice) =>
        countryPrice.originCountryId === originCountryId &&
        countryPrice.destinationCountryId === destinationCountryId &&
        countryPrice.packageSizeId === packageDetailsValue.sizeId,
    );
  }, [
    addressCards.recipient.country,
    addressCards.sender.country,
    countryPrices,
    isRouteSelected,
    packageDetailsValue.sizeId,
  ]);

  const availablePackageSizeIds = useMemo(
    () => new Set(countryPrices.map((countryPrice) => countryPrice.packageSizeId)),
    [countryPrices],
  );

  const isPackageSizeEnabled = useCallback(
    (sizeId: PackageSizeId) => {
      if (!isRouteSelected || isCountryPricesLoading || countryPricesErrorMessage) {
        return false;
      }

      return availablePackageSizeIds.has(sizeId);
    },
    [availablePackageSizeIds, countryPricesErrorMessage, isCountryPricesLoading, isRouteSelected],
  );

  useEffect(() => {
    if (!packageDetailsValue.sizeId || isPackageSizesLoading) {
      return;
    }

    const selectedPackageSizeExists = packageSizeOptions.some((option) => option.id === packageDetailsValue.sizeId);
    const selectedPackageSizeAllowed = isPackageSizeEnabled(packageDetailsValue.sizeId);

    if (!selectedPackageSizeExists || !selectedPackageSizeAllowed) {
      setPackageDetailsValue((previous) => ({ ...previous, sizeId: 0 }));
    }
  }, [isPackageSizeEnabled, isPackageSizesLoading, packageDetailsValue.sizeId, packageSizeOptions]);

  useEffect(() => {
    if (isAuthLoading || !user) {
      return;
    }

    const senderNameFromUser = user.name?.trim() || user.preferred_username?.trim() || '';
    const senderEmailFromUser = user.email?.trim() || '';

    setAddressCards((previous) => {
      const senderName = previous.sender.name.trim();
      const senderEmail = previous.sender.email.trim();

      const nextSenderName = !isSenderNameTouched && !senderName && senderNameFromUser
        ? senderNameFromUser
        : previous.sender.name;
      const nextSenderEmail = !isSenderEmailTouched && !senderEmail && senderEmailFromUser
        ? senderEmailFromUser
        : previous.sender.email;

      if (nextSenderName === previous.sender.name && nextSenderEmail === previous.sender.email) {
        return previous;
      }

      return {
        ...previous,
        sender: {
          ...previous.sender,
          name: nextSenderName,
          email: nextSenderEmail,
        },
      };
    });
  }, [isAuthLoading, isSenderEmailTouched, isSenderNameTouched, user]);

  const handleAddressCardChange = (role: AddressCardRole, field: AddressCardField, fieldValue: string) => {
    if (role === 'sender' && field === 'name') {
      setIsSenderNameTouched(true);
    }

    if (role === 'sender' && field === 'email') {
      setIsSenderEmailTouched(true);
    }

    setSubmitErrorMessage(null);
    setSubmitSuccessMessage(null);

    setAddressCards((previous) => ({
      ...previous,
      [role]: {
        ...previous[role],
        ...(field === 'country' ? { city: '' } : {}),
        [field]: fieldValue,
      },
    }));

    if (field === 'country' || field === 'city' || field === 'zipCode' || field === 'address') {
      setAddressLocations((previous) => ({
        ...previous,
        [role]: {
          ...previous[role],
          isConfirmed: false,
        },
      }));
    }
  };

  const handleLocationMarkerChange = (role: AddressCardRole, coordinate: MapCoordinate) => {
    setSubmitErrorMessage(null);
    setSubmitSuccessMessage(null);
    setAddressLocations((previous) => ({
      ...previous,
      [role]: {
        ...previous[role],
        markerPosition: coordinate,
        isConfirmed: false,
      },
    }));
  };

  const handleConfirmLocation = (role: AddressCardRole) => {
    setSubmitErrorMessage(null);
    setSubmitSuccessMessage(null);
    setAddressLocations((previous) => ({
      ...previous,
      [role]: {
        ...previous[role],
        confirmedPosition: previous[role].markerPosition,
        isConfirmed: true,
      },
    }));
  };

  const handleSizeChange = (sizeId: PackageSizeId) => {
    setSubmitErrorMessage(null);
    setSubmitSuccessMessage(null);
    setPackageDetailsValue((previous) => ({ ...previous, sizeId }));
  };

  const handleWeightChange = (weight: string) => {
    setPackageDetailsValue((previous) => ({ ...previous, weight }));
  };

  const handleDescriptionChange = (description: string) => {
    setPackageDetailsValue((previous) => ({ ...previous, description }));
  };

  const senderAddressCardProps = {
    title: 'Feladó adatai',
    iconName: 'person_pin_circle',
    value: addressCards.sender,
    countryOptions,
    isCountryLoading,
    cityOptions: senderCityOptions,
    isCityLoading: isSenderCityLoading,
    onChange: (field: AddressCardField, fieldValue: string) => handleAddressCardChange('sender', field, fieldValue),
  };

  const recipientAddressCardProps = {
    title: 'Címzett adatai',
    iconName: 'local_shipping',
    value: addressCards.recipient,
    countryOptions,
    isCountryLoading,
    cityOptions: recipientCityOptions,
    isCityLoading: isRecipientCityLoading,
    onChange: (field: AddressCardField, fieldValue: string) => handleAddressCardChange('recipient', field, fieldValue),
  };

  const selectedSenderCountryName = useMemo(
    () => countryOptions.find((option) => option.value === addressCards.sender.country)?.label ?? '',
    [addressCards.sender.country, countryOptions],
  );
  const selectedRecipientCountryName = useMemo(
    () => countryOptions.find((option) => option.value === addressCards.recipient.country)?.label ?? '',
    [addressCards.recipient.country, countryOptions],
  );
  const selectedSenderCityName = useMemo(
    () => senderCityOptions.find((option) => option.value === addressCards.sender.city)?.label ?? '',
    [addressCards.sender.city, senderCityOptions],
  );
  const selectedRecipientCityName = useMemo(
    () => recipientCityOptions.find((option) => option.value === addressCards.recipient.city)?.label ?? '',
    [addressCards.recipient.city, recipientCityOptions],
  );

  const senderGeocodeQuery = useMemo(() => {
    const addressParts = [
      addressCards.sender.address.trim(),
      addressCards.sender.zipCode.trim(),
      selectedSenderCityName.trim(),
      selectedSenderCountryName.trim(),
    ].filter(Boolean);

    if (addressParts.length < 3) {
      return '';
    }

    return addressParts.join(', ');
  }, [
    addressCards.sender.address,
    addressCards.sender.zipCode,
    selectedSenderCityName,
    selectedSenderCountryName,
  ]);

  const recipientGeocodeQuery = useMemo(() => {
    const addressParts = [
      addressCards.recipient.address.trim(),
      addressCards.recipient.zipCode.trim(),
      selectedRecipientCityName.trim(),
      selectedRecipientCountryName.trim(),
    ].filter(Boolean);

    if (addressParts.length < 3) {
      return '';
    }

    return addressParts.join(', ');
  }, [
    addressCards.recipient.address,
    addressCards.recipient.zipCode,
    selectedRecipientCityName,
    selectedRecipientCountryName,
  ]);

  const runAddressGeocode = useCallback(
    async (role: AddressCardRole, query: string, signal?: AbortSignal) => {
      if (!query) {
        return;
      }

      setAddressLocations((previous) => ({
        ...previous,
        [role]: {
          ...previous[role],
          isGeocoding: true,
          geocodeError: null,
        },
      }));

      try {
        const result = await geocodeAddress(query, signal);

        if (!result) {
          setAddressLocations((previous) => ({
            ...previous,
            [role]: {
              ...previous[role],
              geocodeError: 'Nem találtunk pontos találatot a megadott címhez.',
            },
          }));
          return;
        }

        setAddressLocations((previous) => ({
          ...previous,
          [role]: {
            ...previous[role],
            center: result,
            markerPosition: result,
            isConfirmed: false,
            geocodeError: null,
          },
        }));
      } catch (error) {
        if ((error as Error).name === 'AbortError') {
          return;
        }

        setAddressLocations((previous) => ({
          ...previous,
          [role]: {
            ...previous[role],
            geocodeError: (error as Error).message ?? 'A cím geokódolása sikertelen.',
          },
        }));
      } finally {
        setAddressLocations((previous) => ({
          ...previous,
          [role]: {
            ...previous[role],
            isGeocoding: false,
          },
        }));
      }
    },
    [],
  );

  useEffect(() => {
    if (!senderGeocodeQuery || senderGeocodeQuery === lastGeocodeQueryRef.current.sender) {
      return;
    }

    const abortController = new AbortController();
    const timeoutId = window.setTimeout(() => {
      lastGeocodeQueryRef.current.sender = senderGeocodeQuery;
      void runAddressGeocode('sender', senderGeocodeQuery, abortController.signal);
    }, 700);

    return () => {
      abortController.abort();
      window.clearTimeout(timeoutId);
    };
  }, [runAddressGeocode, senderGeocodeQuery]);

  useEffect(() => {
    if (!recipientGeocodeQuery || recipientGeocodeQuery === lastGeocodeQueryRef.current.recipient) {
      return;
    }

    const abortController = new AbortController();
    const timeoutId = window.setTimeout(() => {
      lastGeocodeQueryRef.current.recipient = recipientGeocodeQuery;
      void runAddressGeocode('recipient', recipientGeocodeQuery, abortController.signal);
    }, 700);

    return () => {
      abortController.abort();
      window.clearTimeout(timeoutId);
    };
  }, [recipientGeocodeQuery, runAddressGeocode]);

  const handleRepositionFromAddress = (role: AddressCardRole) => {
    const geocodeQuery = role === 'sender' ? senderGeocodeQuery : recipientGeocodeQuery;
    if (!geocodeQuery) {
      setAddressLocations((previous) => ({
        ...previous,
        [role]: {
          ...previous[role],
          geocodeError: 'Adja meg a címet, irányítószámot, várost és országot az automatikus pozicionáláshoz.',
        },
      }));
      return;
    }

    lastGeocodeQueryRef.current[role] = geocodeQuery;
    void runAddressGeocode(role, geocodeQuery);
  };

  const senderLocationPickerProps = {
    center: addressLocations.sender.center,
    markerPosition: addressLocations.sender.markerPosition,
    onMarkerChange: (coordinate: MapCoordinate) => handleLocationMarkerChange('sender', coordinate),
    onConfirm: () => handleConfirmLocation('sender'),
    isConfirmed: addressLocations.sender.isConfirmed,
    onRepositionFromAddress: () => handleRepositionFromAddress('sender'),
    isGeocoding: addressLocations.sender.isGeocoding,
    geocodeError: addressLocations.sender.geocodeError,
  };

  const recipientLocationPickerProps = {
    center: addressLocations.recipient.center,
    markerPosition: addressLocations.recipient.markerPosition,
    onMarkerChange: (coordinate: MapCoordinate) => handleLocationMarkerChange('recipient', coordinate),
    onConfirm: () => handleConfirmLocation('recipient'),
    isConfirmed: addressLocations.recipient.isConfirmed,
    onRepositionFromAddress: () => handleRepositionFromAddress('recipient'),
    isGeocoding: addressLocations.recipient.isGeocoding,
    geocodeError: addressLocations.recipient.geocodeError,
  };

  const isSubmitDisabled = !(
    isAddressCardValid(addressCards.sender) &&
    isAddressCardValid(addressCards.recipient) &&
    packageDetailsValue.sizeId > 0
  );

  const parseSelectedId = (value: string) => {
    const parsedValue = Number(value);
    return Number.isInteger(parsedValue) ? parsedValue : null;
  };

  const handleSubmitOrder = async () => {
    if (isSubmitDisabled || isSubmitting) {
      return;
    }

    const senderCountryId = parseSelectedId(addressCards.sender.country);
    const senderCityId = parseSelectedId(addressCards.sender.city);
    const recipientCountryId = parseSelectedId(addressCards.recipient.country);
    const recipientCityId = parseSelectedId(addressCards.recipient.city);

    if (!senderCountryId || !senderCityId || !recipientCountryId || !recipientCityId) {
      setSubmitErrorMessage('A rendeléshez érvényes ország és város kiválasztása szükséges.');
      return;
    }

    setIsSubmitting(true);
    setSubmitErrorMessage(null);
    setSubmitSuccessMessage(null);

    try {
      const senderConfirmedPosition = addressLocations.sender.confirmedPosition;
      const recipientConfirmedPosition = addressLocations.recipient.confirmedPosition;

      await createShipment({
        senderName: addressCards.sender.name.trim(),
        senderEmail: addressCards.sender.email.trim(),
        senderPhone: addressCards.sender.phone.trim(),
        senderLocationCountryId: senderCountryId,
        senderZip: addressCards.sender.zipCode.trim(),
        senderLocationCityId: senderCityId,
        senderAddress: addressCards.sender.address.trim(),
        senderLatitude: senderConfirmedPosition?.latitude,
        senderLongitude: senderConfirmedPosition?.longitude,
        recipientName: addressCards.recipient.name.trim(),
        recipientEmail: addressCards.recipient.email.trim(),
        recipientPhone: addressCards.recipient.phone.trim(),
        recipientLocationCountryId: recipientCountryId,
        recipientZip: addressCards.recipient.zipCode.trim(),
        recipientLocationCityId: recipientCityId,
        recipientAddress: addressCards.recipient.address.trim(),
        recipientLatitude: recipientConfirmedPosition?.latitude,
        recipientLongitude: recipientConfirmedPosition?.longitude,
        packageSizeId: packageDetailsValue.sizeId,
      });

      setSubmitSuccessMessage('A rendelést sikeresen rögzítettük.');
    } catch (error) {
      console.error('Failed to create shipment.', error);
      setSubmitErrorMessage('A rendelés mentése nem sikerült. Kérjük próbálja újra.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const orderSummaryCardProps = {
    minPrice: selectedCountryPrice?.minPrice,
    maxPrice: selectedCountryPrice?.maxPrice,
    isSubmitDisabled: isSubmitDisabled || isSubmitting,
    isSubmitting,
    submitErrorMessage,
    submitSuccessMessage,
    onSubmit: handleSubmitOrder,
  };

  return {
    packageDetailsValue,
    packageSizeOptions,
    isPackageSizesLoading,
    isPackageSizeEnabled,
    sizeAvailabilityHint: !isRouteSelected
      ? 'A csomagméretek az országpár kiválasztása után válnak elérhetővé.'
      : isCountryPricesLoading
        ? 'Az országpárhoz tartozó csomagméretek betöltése folyamatban van.'
        : null,
    handleSizeChange,
    handleWeightChange,
    handleDescriptionChange,
    senderAddressCardProps,
    recipientAddressCardProps,
    senderLocationPickerProps,
    recipientLocationPickerProps,
    orderSummaryCardProps,
    countriesErrorMessage,
    senderCitiesErrorMessage,
    recipientCitiesErrorMessage,
    packageSizesErrorMessage,
    countryPricesErrorMessage,
    retry,
    retrySenderCities,
    retryRecipientCities,
    retryPackageSizes,
    retryCountryPrices,
  };
};
