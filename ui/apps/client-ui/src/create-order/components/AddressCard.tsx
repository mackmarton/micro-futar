import { useMemo, useState } from 'react';
import { PrecisionInput } from '@package/shared-ui';
import type { CityOption, CountryOption } from '../api/ordersApi.ts';

export type AddressCardField = 'name' | 'phone' | 'email' | 'zipCode' | 'country' | 'city' | 'address';

export type AddressCardValue = Record<AddressCardField, string>;

export type AddressCardProp = {
  title: string;
  iconName: string;
  value: AddressCardValue;
  countryOptions: CountryOption[];
  isCountryLoading: boolean;
  cityOptions: CityOption[];
  isCityLoading: boolean;
  onChange: (field: AddressCardField, fieldValue: string) => void;
};

export type AddressCardProps = AddressCardProp;

export const AddressCard = ({
  title,
  iconName,
  value,
  countryOptions,
  isCountryLoading,
  cityOptions,
  isCityLoading,
  onChange,
}: AddressCardProps) => {
  const countryInputId = `${iconName}-country`;
  const countryListboxId = `${countryInputId}-listbox`;
  const cityInputId = `${iconName}-city`;
  const cityListboxId = `${cityInputId}-listbox`;
  const [countryQuery, setCountryQuery] = useState('');
  const [isCountryMenuOpen, setIsCountryMenuOpen] = useState(false);
  const [cityQuery, setCityQuery] = useState('');
  const [isCityMenuOpen, setIsCityMenuOpen] = useState(false);

  const selectedCountryLabel = useMemo(
    () => countryOptions.find((option) => option.value === value.country)?.label ?? '',
    [countryOptions, value.country],
  );

  const normalizedCountryQuery = countryQuery.trim().toLocaleLowerCase('hu');
  const filteredCountryOptions = useMemo(() => {
    if (!normalizedCountryQuery) {
      return countryOptions;
    }

    return countryOptions.filter((option) => option.label.toLocaleLowerCase('hu').includes(normalizedCountryQuery));
  }, [countryOptions, normalizedCountryQuery]);

  const inputValue = isCountryMenuOpen ? countryQuery : selectedCountryLabel;

  const selectedCityLabel = useMemo(
    () => cityOptions.find((option) => option.value === value.city)?.label ?? '',
    [cityOptions, value.city],
  );
  const normalizedCityQuery = cityQuery.trim().toLocaleLowerCase('hu');
  const filteredCityOptions = useMemo(() => {
    if (!normalizedCityQuery) {
      return cityOptions;
    }

    return cityOptions.filter((option) => option.label.toLocaleLowerCase('hu').includes(normalizedCityQuery));
  }, [cityOptions, normalizedCityQuery]);
  const cityInputValue = isCityMenuOpen ? cityQuery : selectedCityLabel;

  const handleCountrySelect = (selectedCountryValue: string) => {
    onChange('country', selectedCountryValue);
    setCountryQuery('');
    setIsCountryMenuOpen(false);
  };

  const handleCountryInputBlur = () => {
    setIsCountryMenuOpen(false);
    setCountryQuery('');
  };

  const handleCitySelect = (selectedCityValue: string) => {
    onChange('city', selectedCityValue);
    setCityQuery('');
    setIsCityMenuOpen(false);
  };

  const handleCityInputBlur = () => {
    const normalizedQuery = cityQuery.trim().toLocaleLowerCase('hu');
    if (normalizedQuery) {
      const exactMatch = cityOptions.find(
        (option) => option.label.trim().toLocaleLowerCase('hu') === normalizedQuery,
      );

      if (exactMatch) {
        onChange('city', exactMatch.value);
      }
    }

    setIsCityMenuOpen(false);
    setCityQuery('');
  };

  return (
    <section className="bg-surface-container-low rounded-xl p-8 space-y-6">
      <div className="flex items-center gap-3 text-on-primary-container">
        <span className="material-symbols-outlined text-teal-600">{iconName}</span>
        <h2 className="text-xl font-bold tracking-tight">{title}</h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <PrecisionInput label="Név" value={value.name} onChange={(event) => onChange('name', event.target.value)} />
        <PrecisionInput
          label="Telefonszám"
          type="tel"
          value={value.phone}
          onChange={(event) => onChange('phone', event.target.value)}
        />
        <PrecisionInput
          label="Email cím"
          type="email"
          value={value.email}
          onChange={(event) => onChange('email', event.target.value)}
          wrapperClassName="md:col-span-2"
        />

        <div className="grid grid-cols-3 gap-4 md:col-span-2">
          <PrecisionInput
            label="Irányítószám"
            value={value.zipCode}
            onChange={(event) => onChange('zipCode', event.target.value)}
          />
          <div className="col-span-2 relative">
            <label
              htmlFor={countryInputId}
              className="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-3 block"
            >
              Ország
            </label>
            <input
              id={countryInputId}
              type="text"
              role="combobox"
              aria-controls={countryListboxId}
              aria-expanded={isCountryMenuOpen}
              aria-autocomplete="list"
              autoComplete="off"
              value={inputValue}
              placeholder={isCountryLoading ? 'Országok betöltése...' : 'Kezdjen gépelni az országhoz'}
              onFocus={() => {
                setCountryQuery('');
                setIsCountryMenuOpen(true);
              }}
              onBlur={handleCountryInputBlur}
              onChange={(event) => {
                setCountryQuery(event.target.value);
                setIsCountryMenuOpen(true);
                onChange('country', '');
              }}
              disabled={isCountryLoading || countryOptions.length === 0}
              className="w-full bg-surface-container-lowest border-none rounded-lg p-4 focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all disabled:cursor-not-allowed disabled:opacity-70"
            />
            {isCountryMenuOpen && !isCountryLoading && countryOptions.length > 0 ? (
              <ul
                id={countryListboxId}
                role="listbox"
                className="absolute z-20 mt-2 max-h-56 w-full overflow-auto rounded-lg border border-outline-variant bg-surface-container-lowest py-1 shadow-lg"
              >
                {filteredCountryOptions.length > 0 ? (
                  filteredCountryOptions.map((countryOption) => (
                    <li key={countryOption.value} role="option" aria-selected={countryOption.value === value.country}>
                      <button
                        type="button"
                        className="w-full px-4 py-2 text-left text-sm hover:bg-surface-container"
                        onMouseDown={(event) => {
                          event.preventDefault();
                        }}
                        onClick={() => handleCountrySelect(countryOption.value)}
                      >
                        {countryOption.label}
                      </button>
                    </li>
                  ))
                ) : (
                  <li className="px-4 py-2 text-sm text-on-surface-variant">Nincs találat.</li>
                )}
              </ul>
            ) : null}
          </div>
        </div>

        <div className="relative">
          <label
            htmlFor={cityInputId}
            className="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-3 block"
          >
            Város
          </label>
          <input
            id={cityInputId}
            type="text"
            role="combobox"
            aria-controls={cityListboxId}
            aria-expanded={isCityMenuOpen}
            aria-autocomplete="list"
            autoComplete="off"
            value={cityInputValue}
            placeholder={
              !value.country
                ? 'Válasszon előbb országot'
                : isCityLoading
                  ? 'Városok betöltése...'
                  : 'Kezdjen gépelni a városhoz'
            }
            onFocus={() => {
              setCityQuery('');
              setIsCityMenuOpen(true);
            }}
            onBlur={handleCityInputBlur}
            onChange={(event) => {
              setCityQuery(event.target.value);
              setIsCityMenuOpen(true);
            }}
            disabled={isCityLoading || !value.country || cityOptions.length === 0}
            className="w-full bg-surface-container-lowest border-none rounded-lg p-4 focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all disabled:cursor-not-allowed disabled:opacity-70"
          />
          {isCityMenuOpen && !isCityLoading && value.country && cityOptions.length > 0 ? (
            <ul
              id={cityListboxId}
              role="listbox"
              className="absolute z-20 mt-2 max-h-56 w-full overflow-auto rounded-lg border border-outline-variant bg-surface-container-lowest py-1 shadow-lg"
            >
              {filteredCityOptions.length > 0 ? (
                filteredCityOptions.map((cityOption) => (
                  <li key={cityOption.value} role="option" aria-selected={cityOption.value === value.city}>
                    <button
                      type="button"
                      className="w-full px-4 py-2 text-left text-sm hover:bg-surface-container"
                      onMouseDown={(event) => {
                        event.preventDefault();
                      }}
                      onClick={() => handleCitySelect(cityOption.value)}
                    >
                      {cityOption.label}
                    </button>
                  </li>
                ))
              ) : (
                <li className="px-4 py-2 text-sm text-on-surface-variant">Nincs talalat.</li>
              )}
            </ul>
          ) : null}
        </div>
        <PrecisionInput
          label="Cím"
          value={value.address}
          onChange={(event) => onChange('address', event.target.value)}
        />
      </div>
    </section>
  );
};



