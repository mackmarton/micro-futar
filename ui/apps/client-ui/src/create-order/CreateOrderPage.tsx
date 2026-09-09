import { LocationMapPicker, PortalLayout } from '@package/shared-ui';
import { AddressCard } from './components/AddressCard.tsx';
import { OrderSummaryCard } from './components/OrderSummaryCard.tsx';
import { PackageDetailsSection } from './components/PackageDetailsSection.tsx';
import { useCreateOrderPage } from './hooks/useCreateOrderPage.ts';

const formatCoordinate = (value: number) => value.toFixed(6);

export const CreateOrderPage = () => {
  const {
    senderAddressCardProps,
    recipientAddressCardProps,
    senderLocationPickerProps,
    recipientLocationPickerProps,
    packageDetailsValue,
    packageSizeOptions,
    isPackageSizesLoading,
    isPackageSizeEnabled,
    sizeAvailabilityHint,
    handleSizeChange,
    handleWeightChange,
    handleDescriptionChange,
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
  } = useCreateOrderPage();

  return (
    <PortalLayout title="Csomag feladása" activeHref="#/portal/create-order">
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
                {senderCitiesErrorMessage ? <p>Feladó városok: {senderCitiesErrorMessage}</p> : null}
                {recipientCitiesErrorMessage ? <p>Címzett városok: {recipientCitiesErrorMessage}</p> : null}
                {packageSizesErrorMessage ? <p>Csomagméretek: {packageSizesErrorMessage}</p> : null}
                {countryPricesErrorMessage ? <p>Országpár árak: {countryPricesErrorMessage}</p> : null}
                <div className="mt-3 flex gap-2">
                  {countriesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retry}
                    >
                      Országok újratöltése
                    </button>
                  ) : null}
                  {senderCitiesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retrySenderCities}
                    >
                      Feladó városok újratöltése
                    </button>
                  ) : null}
                  {recipientCitiesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retryRecipientCities}
                    >
                      Címzett városok újratöltése
                    </button>
                  ) : null}
                  {packageSizesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retryPackageSizes}
                    >
                      Csomagméretek újratöltése
                    </button>
                  ) : null}
                  {countryPricesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retryCountryPrices}
                    >
                      Országpár árak újratöltése
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
              onSizeChange={handleSizeChange}
              onWeightChange={handleWeightChange}
              onDescriptionChange={handleDescriptionChange}
            />
          </div>
          <OrderSummaryCard {...orderSummaryCardProps} />
        </div>
      </>
    </PortalLayout>
  );
};
