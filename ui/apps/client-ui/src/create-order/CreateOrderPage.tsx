import { PortalLayout } from '@package/shared-ui';
import { CreateOrderContent } from './components/CreateOrderContent.tsx';
import { useCreateOrderPage } from './hooks/useCreateOrderPage.ts';

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
      <CreateOrderContent
        senderAddressCardProps={senderAddressCardProps}
        recipientAddressCardProps={recipientAddressCardProps}
        senderLocationPickerProps={senderLocationPickerProps}
        recipientLocationPickerProps={recipientLocationPickerProps}
        packageDetailsValue={packageDetailsValue}
        packageSizeOptions={packageSizeOptions}
        isPackageSizesLoading={isPackageSizesLoading}
        isPackageSizeEnabled={isPackageSizeEnabled}
        sizeAvailabilityHint={sizeAvailabilityHint}
        onSizeChange={handleSizeChange}
        onWeightChange={handleWeightChange}
        onDescriptionChange={handleDescriptionChange}
        orderSummaryCardProps={orderSummaryCardProps}
        countriesErrorMessage={countriesErrorMessage}
        senderCitiesErrorMessage={senderCitiesErrorMessage}
        recipientCitiesErrorMessage={recipientCitiesErrorMessage}
        packageSizesErrorMessage={packageSizesErrorMessage}
        countryPricesErrorMessage={countryPricesErrorMessage}
        retryCountries={retry}
        retrySenderCities={retrySenderCities}
        retryRecipientCities={retryRecipientCities}
        retryPackageSizes={retryPackageSizes}
        retryCountryPrices={retryCountryPrices}
      />
    </PortalLayout>
  );
};
