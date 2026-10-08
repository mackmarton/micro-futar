'use client';

import dynamic from 'next/dynamic';
import { useTranslation } from 'react-i18next';
import { PortalLayout } from '@package/shared-ui';
import { AddressCard } from './components/AddressCard.tsx';
import { OrderSummaryCard } from './components/OrderSummaryCard.tsx';
import { PackageDetailsSection } from './components/PackageDetailsSection.tsx';
import { useCreateOrderPage } from './hooks/useCreateOrderPage.ts';
import type { AppLocale } from '../i18n/createI18nInstance.ts';
import { LanguageSwitcher } from '../i18n/LanguageSwitcher.tsx';
import { usePortalNavigation } from '../shared/usePortalNavigation.ts';

// A leaflet betöltéskor hozzányúl a `window`-hoz, ezért a térkép csak a böngészőben töltődik be.
const LocationMapPicker = dynamic(
  () => import('@package/shared-ui/LocationMapPicker').then((module) => module.LocationMapPicker),
  { ssr: false },
);

const formatCoordinate = (value: number) => value.toFixed(6);

export type CreateOrderPageProps = {
  locale: AppLocale;
};

export const CreateOrderPage = ({ locale }: CreateOrderPageProps) => {
  const { t } = useTranslation(['common', 'createOrder']);
  const { activeHref, navigationItems, homeHref, brandSubtitle } = usePortalNavigation(locale, 'createOrder');
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
  } = useCreateOrderPage(locale);

  return (
    <PortalLayout
      title={t('common:nav.createOrder')}
      activeHref={activeHref}
      navigationItems={navigationItems}
      logoHref={homeHref}
      brandSubtitle={brandSubtitle}
      topBarRightSlot={<LanguageSwitcher />}
    >
      <>
        <div className="mb-12">
          <h2 className="text-4xl font-extrabold tracking-tight text-on-surface mb-2">{t('createOrder:page.title')}</h2>
          <p className="text-on-surface-variant text-lg">
            {t('createOrder:page.subtitle')}
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
          <div className="lg:col-span-2 space-y-8">
            {countriesErrorMessage || senderCitiesErrorMessage || recipientCitiesErrorMessage || packageSizesErrorMessage || countryPricesErrorMessage ? (
              <div className="rounded-lg border border-red-300 bg-red-50 p-4 text-sm text-red-700">
                {countriesErrorMessage ? <p>{countriesErrorMessage}</p> : null}
                {senderCitiesErrorMessage ? <p>{t('createOrder:errors.senderCities', { message: senderCitiesErrorMessage })}</p> : null}
                {recipientCitiesErrorMessage ? <p>{t('createOrder:errors.recipientCities', { message: recipientCitiesErrorMessage })}</p> : null}
                {packageSizesErrorMessage ? <p>{t('createOrder:errors.packageSizes', { message: packageSizesErrorMessage })}</p> : null}
                {countryPricesErrorMessage ? <p>{t('createOrder:errors.countryPrices', { message: countryPricesErrorMessage })}</p> : null}
                <div className="mt-3 flex gap-2">
                  {countriesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retry}
                    >
                      {t('createOrder:errors.retryCountries')}
                    </button>
                  ) : null}
                  {senderCitiesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retrySenderCities}
                    >
                      {t('createOrder:errors.retrySenderCities')}
                    </button>
                  ) : null}
                  {recipientCitiesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retryRecipientCities}
                    >
                      {t('createOrder:errors.retryRecipientCities')}
                    </button>
                  ) : null}
                  {packageSizesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retryPackageSizes}
                    >
                      {t('createOrder:errors.retryPackageSizes')}
                    </button>
                  ) : null}
                  {countryPricesErrorMessage ? (
                    <button
                      type="button"
                      className="inline-flex items-center gap-2 rounded-md bg-red-600 px-3 py-2 text-xs font-semibold text-white hover:bg-red-700"
                      onClick={retryCountryPrices}
                    >
                      {t('createOrder:errors.retryCountryPrices')}
                    </button>
                  ) : null}
                </div>
              </div>
            ) : null}

            <AddressCard {...senderAddressCardProps} />
            <section className="rounded-2xl bg-surface-container-lowest p-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('createOrder:map.senderCoordinatesLabel')}</p>
                  <p className="mt-1 font-body text-on-surface-variant">
                    {t('createOrder:map.hint')}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={senderLocationPickerProps.onRepositionFromAddress}
                  className="inline-flex items-center rounded-lg bg-surface px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
                >
                  {t('createOrder:map.repositionButton')}
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
                  {t('createOrder:map.confirmButton')}
                </button>
                <p className="font-body text-on-surface-variant">
                  {t('createOrder:map.markedPoint', {
                    lat: formatCoordinate(senderLocationPickerProps.markerPosition.latitude),
                    lng: formatCoordinate(senderLocationPickerProps.markerPosition.longitude),
                  })}
                </p>
              </div>

              {!senderLocationPickerProps.isConfirmed ? (
                <p className="mt-3 font-body text-on-surface-variant">{t('createOrder:map.senderUnconfirmed')}</p>
              ) : null}
              {senderLocationPickerProps.isGeocoding ? (
                <p className="mt-3 font-body text-on-surface-variant">{t('createOrder:map.geocoding')}</p>
              ) : null}
              {senderLocationPickerProps.geocodeError ? (
                <p className="mt-3 font-body text-on-surface-variant">{senderLocationPickerProps.geocodeError}</p>
              ) : null}
            </section>

            <AddressCard {...recipientAddressCardProps} />
            <section className="rounded-2xl bg-surface-container-lowest p-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('createOrder:map.recipientCoordinatesLabel')}</p>
                  <p className="mt-1 font-body text-on-surface-variant">
                    {t('createOrder:map.hint')}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={recipientLocationPickerProps.onRepositionFromAddress}
                  className="inline-flex items-center rounded-lg bg-surface px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
                >
                  {t('createOrder:map.repositionButton')}
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
                  {t('createOrder:map.confirmButton')}
                </button>
                <p className="font-body text-on-surface-variant">
                  {t('createOrder:map.markedPoint', {
                    lat: formatCoordinate(recipientLocationPickerProps.markerPosition.latitude),
                    lng: formatCoordinate(recipientLocationPickerProps.markerPosition.longitude),
                  })}
                </p>
              </div>

              {!recipientLocationPickerProps.isConfirmed ? (
                <p className="mt-3 font-body text-on-surface-variant">{t('createOrder:map.recipientUnconfirmed')}</p>
              ) : null}
              {recipientLocationPickerProps.isGeocoding ? (
                <p className="mt-3 font-body text-on-surface-variant">{t('createOrder:map.geocoding')}</p>
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
