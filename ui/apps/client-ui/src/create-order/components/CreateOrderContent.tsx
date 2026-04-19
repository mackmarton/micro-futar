import { AddressCard, type AddressCardProps } from './AddressCard.tsx';
import { OrderSummaryCard, type OrderSummaryCardProps } from './OrderSummaryCard.tsx';
import { PackageDetailsSection, type PackageDetailsValue, type PackageSizeId } from './PackageDetailsSection.tsx';
import type { PackageSizeOption } from '../api/ordersApi.ts';

export type CreateOrderContentProps = {
  senderAddressCardProps: AddressCardProps;
  recipientAddressCardProps: AddressCardProps;
  packageDetailsValue: PackageDetailsValue;
  packageSizeOptions: PackageSizeOption[];
  isPackageSizesLoading: boolean;
  isPackageSizeEnabled: (sizeId: PackageSizeId) => boolean;
  sizeAvailabilityHint: string | null;
  onSizeChange: (sizeId: PackageSizeId) => void;
  onWeightChange: (weight: string) => void;
  onDescriptionChange: (description: string) => void;
  orderSummaryCardProps: OrderSummaryCardProps;
  countriesErrorMessage: string | null;
  senderCitiesErrorMessage: string | null;
  recipientCitiesErrorMessage: string | null;
  packageSizesErrorMessage: string | null;
  countryPricesErrorMessage: string | null;
  retryCountries: () => void;
  retrySenderCities: () => void;
  retryRecipientCities: () => void;
  retryPackageSizes: () => void;
  retryCountryPrices: () => void;
};

export const CreateOrderContent = ({
  senderAddressCardProps,
  recipientAddressCardProps,
  packageDetailsValue,
  packageSizeOptions,
  isPackageSizesLoading,
  isPackageSizeEnabled,
  sizeAvailabilityHint,
  onSizeChange,
  onWeightChange,
  onDescriptionChange,
  orderSummaryCardProps,
  countriesErrorMessage,
  senderCitiesErrorMessage,
  recipientCitiesErrorMessage,
  packageSizesErrorMessage,
  countryPricesErrorMessage,
  retryCountries,
  retrySenderCities,
  retryRecipientCities,
  retryPackageSizes,
  retryCountryPrices,
}: CreateOrderContentProps) => {
  return (
    <>
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
                    onClick={retryCountries}
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
            sizeAvailabilityHint={sizeAvailabilityHint}
            onSizeChange={onSizeChange}
            onWeightChange={onWeightChange}
            onDescriptionChange={onDescriptionChange}
          />
        </div>
        <OrderSummaryCard {...orderSummaryCardProps} />
      </div>
    </>
  );
};

