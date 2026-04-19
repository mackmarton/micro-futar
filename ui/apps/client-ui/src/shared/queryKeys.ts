export const queryKeys = {
  countries: ['countries'] as const,
  cities: (countryId: string) => ['cities', countryId] as const,
  packageSizes: ['packageSizes'] as const,
  countryPrices: (originCountryId: string, destinationCountryId: string) =>
    ['countryPrices', originCountryId, destinationCountryId] as const,
  userShipments: ['userShipments'] as const,
  tracking: (trackingNumber: string) => ['tracking', trackingNumber] as const,
};

