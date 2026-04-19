import { useCallback, useEffect, useMemo, useState } from 'react';
import { useAuth } from '@package/shared-ui';
import { type AddressCardField, type AddressCardValue } from '../components/AddressCard.tsx';
import { type PackageDetailsValue, type PackageSizeId } from '../components/PackageDetailsSection.tsx';
import { createShipment } from '../api/ordersApi.ts';
import { useCities } from './useCities.ts';
import { useCountries } from './useCountries.ts';
import { useCountryPrices } from './useCountryPrices.ts';
import { usePackageSizes } from './usePackageSizes.ts';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

type AddressCardRole = 'sender' | 'recipient';

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
      await createShipment({
        senderName: addressCards.sender.name.trim(),
        senderEmail: addressCards.sender.email.trim(),
        senderPhone: addressCards.sender.phone.trim(),
        senderLocationCountryId: senderCountryId,
        senderZip: addressCards.sender.zipCode.trim(),
        senderLocationCityId: senderCityId,
        senderAddress: addressCards.sender.address.trim(),
        recipientName: addressCards.recipient.name.trim(),
        recipientEmail: addressCards.recipient.email.trim(),
        recipientPhone: addressCards.recipient.phone.trim(),
        recipientLocationCountryId: recipientCountryId,
        recipientZip: addressCards.recipient.zipCode.trim(),
        recipientLocationCityId: recipientCityId,
        recipientAddress: addressCards.recipient.address.trim(),
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

