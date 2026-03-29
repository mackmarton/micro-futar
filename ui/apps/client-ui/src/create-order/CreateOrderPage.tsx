import { useCallback, useEffect, useMemo, useState } from 'react';
import { BottomNavBar, SideNavBar, TopNavBar } from '@package/shared-ui';
import { OrderSummaryCard } from './components/OrderSummaryCard.tsx';
import {
  PackageDetailsSection,
  type PackageDetailsValue,
  type PackageSizeId,
} from './components/PackageDetailsSection.tsx';
import { AddressCard, type AddressCardField, type AddressCardValue } from './components/AddressCard.tsx';
import { useCountries } from './hooks/useCountries.ts';
import { useCities } from './hooks/useCities.ts';
import { usePackageSizes } from './hooks/usePackageSizes.ts';
import { useCountryPrices } from './hooks/useCountryPrices.ts';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const isAddressCardValid = (addressCard: AddressCardValue) => {
  const hasRequiredTextValues = (
    ['name', 'phone', 'zipCode', 'country', 'city', 'address'] satisfies AddressCardField[]
  ).every((field) => addressCard[field].trim().length > 0);

  const hasValidEmail = EMAIL_PATTERN.test(addressCard.email.trim());

  return hasRequiredTextValues && hasValidEmail;
};

const sideNavigationItems = [
  { label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'package_2', onlyLoggedIn: true },
  { label: 'Csomag feladása', href: '#/create-order', icon: 'add_circle', isActive: true },
  { label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping' },
];

const initialPackageDetails: PackageDetailsValue = {
  sizeId: 0,
  weight: '2.5',
  description: '',
};

type AddressCardRole = 'sender' | 'recipient';

const initialAddressCardValue: AddressCardValue = {
  name: '',
  phone: '',
  email: '',
  zipCode: '',
  country: '',
  city: '',
  address: '',
};

const initialAddressCards: Record<AddressCardRole, AddressCardValue> = {
  sender: initialAddressCardValue,
  recipient: initialAddressCardValue,
};

export const CreateOrderPage = () => {
  const [addressCards, setAddressCards] = useState<Record<AddressCardRole, AddressCardValue>>(initialAddressCards);
  const [packageDetailsValue, setPackageDetailsValue] = useState<PackageDetailsValue>(initialPackageDetails);
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
  }, [
    isPackageSizeEnabled,
    isPackageSizesLoading,
    packageDetailsValue.sizeId,
    packageSizeOptions,
  ]);

  const handleAddressCardChange = (role: AddressCardRole, field: AddressCardField, fieldValue: string) => {
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

  const orderSummaryCardProps = {
    minPrice: selectedCountryPrice?.minPrice,
    maxPrice: selectedCountryPrice?.maxPrice,
    isSubmitDisabled
  };

  return (
    <div className="bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed">
      <SideNavBar navigationItems={sideNavigationItems} />

      <main className="lg:ml-64 min-h-screen flex flex-col pb-24 lg:pb-0">
        <TopNavBar title="Csomag feladása" />

        <div className="max-w-7xl mx-auto p-6 md:p-10 w-full">
          <div className="mb-12">
            <h2 className="text-4xl font-extrabold tracking-tight text-on-surface mb-2">Csomag feladása</h2>
            <p className="text-on-surface-variant text-lg">
              Hozza létre új szállítmányát néhány egyszerű lépésben.
            </p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
            <div className="lg:col-span-2 space-y-8">
              {countriesErrorMessage || senderCitiesErrorMessage || recipientCitiesErrorMessage || packageSizesErrorMessage || countryPricesErrorMessage ? (
                <div className="rounded-lg border border-red-300 bg-red-50 p-4 text-sm text-red-700">
                  {countriesErrorMessage ? <p>{countriesErrorMessage}</p> : null}
                  {senderCitiesErrorMessage ? <p>Felado varosok: {senderCitiesErrorMessage}</p> : null}
                  {recipientCitiesErrorMessage ? <p>Cimzett varosok: {recipientCitiesErrorMessage}</p> : null}
                  {packageSizesErrorMessage ? <p>Csomagmeretek: {packageSizesErrorMessage}</p> : null}
                  {countryPricesErrorMessage ? <p>Orszagpar arak: {countryPricesErrorMessage}</p> : null}
                  <div className="mt-3 flex gap-2">
                    {countriesErrorMessage ? (
                      <button
                        type="button"
                        className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                        onClick={retry}
                      >
                        Orszagok ujratoltese
                      </button>
                    ) : null}
                    {senderCitiesErrorMessage ? (
                      <button
                        type="button"
                        className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                        onClick={retrySenderCities}
                      >
                        Felado varosok ujratoltese
                      </button>
                    ) : null}
                    {recipientCitiesErrorMessage ? (
                      <button
                        type="button"
                        className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                        onClick={retryRecipientCities}
                      >
                        Cimzett varosok ujratoltese
                      </button>
                    ) : null}
                    {packageSizesErrorMessage ? (
                      <button
                        type="button"
                        className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                        onClick={retryPackageSizes}
                      >
                        Csomagmeretek ujratoltese
                      </button>
                    ) : null}
                    {countryPricesErrorMessage ? (
                      <button
                        type="button"
                        className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                        onClick={retryCountryPrices}
                      >
                        Orszagpar arak ujratoltese
                      </button>
                    ) : null}
                  </div>
                </div>
              ) : null}

              <AddressCard {...senderAddressCardProps} />
              <AddressCard {...recipientAddressCardProps} />
              <PackageDetailsSection
                value={packageDetailsValue}
                sizeOptions={packageSizeOptions}
                isSizeLoading={isPackageSizesLoading}
                isPackageSizeEnabled={isPackageSizeEnabled}
                sizeAvailabilityHint={
                  !isRouteSelected
                    ? 'A csomagméretek az országpár kiválasztása után válnak elérhetővé.'
                    : isCountryPricesLoading
                      ? 'Az országpárhoz tartozó csomagméretek betöltése folyamatban van.'
                      : null
                }
                onSizeChange={handleSizeChange}
                onWeightChange={handleWeightChange}
                onDescriptionChange={handleDescriptionChange}
              />
            </div>
            <OrderSummaryCard {...orderSummaryCardProps} />
          </div>
        </div>
      </main>

      <BottomNavBar
        items={[
          { label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'home', onlyLoggedIn: true},
          { label: 'Csomag feladása', href: '#/create-order', icon: 'add_box', isActive: true },
          { label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping' },
        ]}
      />
    </div>
  );
};

