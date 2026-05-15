import { LocationMapPicker, type MapCoordinate } from '@package/shared-ui';
import { AddressCard, type AddressCardProps } from './AddressCard.tsx';
import { OrderSummaryCard, type OrderSummaryCardProps } from './OrderSummaryCard.tsx';
import { PackageDetailsSection, type PackageDetailsValue, type PackageSizeId } from './PackageDetailsSection.tsx';
import type { PackageSizeOption } from '../api/ordersApi.ts';

export type LocationPickerProps = {
  center: MapCoordinate;
  markerPosition: MapCoordinate;
  onMarkerChange: (value: MapCoordinate) => void;
  onConfirm: () => void;
  isConfirmed: boolean;
  onRepositionFromAddress: () => void;
  isGeocoding: boolean;
  geocodeError: string | null;
};

export type CreateOrderContentProps = {
  senderAddressCardProps: AddressCardProps;
  recipientAddressCardProps: AddressCardProps;
  senderLocationPickerProps: LocationPickerProps;
  recipientLocationPickerProps: LocationPickerProps;
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

const formatCoordinate = (value: number) => value.toFixed(6);

export const CreateOrderContent = ({
  senderAddressCardProps,
  recipientAddressCardProps,
  senderLocationPickerProps,
  recipientLocationPickerProps,
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
          <section className="rounded-2xl bg-surface-container-lowest p-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Feladó koordináták</p>
                <p className="mt-1 font-body text-on-surface-variant">
                  A térkép a beírt címhez igazodik. Húzza a jelölőt vagy kattintson a térképre, majd erősítse meg a pozíciót.
                </p>
              </div>
              <button
                type="button"
                onClick={senderLocationPickerProps.onRepositionFromAddress}
                className="inline-flex items-center rounded-lg bg-surface px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
              >
                Cím alapján újrapozicionálás
              </button>
            </div>

            <div className="mt-4 rounded-xl overflow-hidden">
              <LocationMapPicker
                center={senderLocationPickerProps.center}
                markerPosition={senderLocationPickerProps.markerPosition}
                onMarkerChange={senderLocationPickerProps.onMarkerChange}
              />
            </div>

            <div className="mt-4 flex flex-wrap items-center gap-3">
              <button
                type="button"
                onClick={senderLocationPickerProps.onConfirm}
                className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
              >
                Pin megerősítése
              </button>
              <p className="font-body text-on-surface-variant">
                Jelölt pont: {formatCoordinate(senderLocationPickerProps.markerPosition.latitude)}, {formatCoordinate(senderLocationPickerProps.markerPosition.longitude)}
              </p>
            </div>

            {!senderLocationPickerProps.isConfirmed ? (
              <p className="mt-3 font-body text-on-surface-variant">A feladó koordinátái még nincsenek megerősítve.</p>
            ) : null}
            {senderLocationPickerProps.isGeocoding ? (
              <p className="mt-3 font-body text-on-surface-variant">Automatikus címkeresés folyamatban...</p>
            ) : null}
            {senderLocationPickerProps.geocodeError ? (
              <p className="mt-3 font-body text-on-surface-variant">{senderLocationPickerProps.geocodeError}</p>
            ) : null}
          </section>

          <AddressCard {...recipientAddressCardProps} />
          <section className="rounded-2xl bg-surface-container-lowest p-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Címzett koordináták</p>
                <p className="mt-1 font-body text-on-surface-variant">
                  A térkép a beírt címhez igazodik. Húzza a jelölőt vagy kattintson a térképre, majd erősítse meg a pozíciót.
                </p>
              </div>
              <button
                type="button"
                onClick={recipientLocationPickerProps.onRepositionFromAddress}
                className="inline-flex items-center rounded-lg bg-surface px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
              >
                Cím alapján újrapozicionálás
              </button>
            </div>

            <div className="mt-4 rounded-xl overflow-hidden">
              <LocationMapPicker
                center={recipientLocationPickerProps.center}
                markerPosition={recipientLocationPickerProps.markerPosition}
                onMarkerChange={recipientLocationPickerProps.onMarkerChange}
              />
            </div>

            <div className="mt-4 flex flex-wrap items-center gap-3">
              <button
                type="button"
                onClick={recipientLocationPickerProps.onConfirm}
                className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
              >
                Pin megerősítése
              </button>
              <p className="font-body text-on-surface-variant">
                Jelölt pont: {formatCoordinate(recipientLocationPickerProps.markerPosition.latitude)}, {formatCoordinate(recipientLocationPickerProps.markerPosition.longitude)}
              </p>
            </div>

            {!recipientLocationPickerProps.isConfirmed ? (
              <p className="mt-3 font-body text-on-surface-variant">A címzett koordinátái még nincsenek megerősítve.</p>
            ) : null}
            {recipientLocationPickerProps.isGeocoding ? (
              <p className="mt-3 font-body text-on-surface-variant">Automatikus címkeresés folyamatban...</p>
            ) : null}
            {recipientLocationPickerProps.geocodeError ? (
              <p className="mt-3 font-body text-on-surface-variant">{recipientLocationPickerProps.geocodeError}</p>
            ) : null}
          </section>

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
