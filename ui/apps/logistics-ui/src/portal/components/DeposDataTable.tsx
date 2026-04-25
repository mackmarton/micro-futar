import { useEffect, useMemo, useRef, useState } from 'react';
import type { DepoWithLookups } from '../api/logisticsDeposApi';
import { Link } from 'react-router-dom';

type DeposDataTableProps = {
  depos: DepoWithLookups[];
};

const valueOrFallback = (value?: string | number) =>
  value === 0 || (typeof value === 'string' && value.length > 0) ? value : 'N/A';

export const DeposDataTable = ({ depos }: DeposDataTableProps) => {
  const headerCellClass = 'px-5 py-4 align-top text-[10px] uppercase tracking-widest font-semibold';

  const [selectedCountry, setSelectedCountry] = useState('');
  const [selectedCity, setSelectedCity] = useState('');
  const [openFilterMenu, setOpenFilterMenu] = useState<'country' | 'city' | null>(null);

  const countryFilterRef = useRef<HTMLTableCellElement | null>(null);
  const cityFilterRef = useRef<HTMLTableCellElement | null>(null);

  const countryOptions = useMemo(() => {
    return Array.from(
      new Set(
        depos
          .map((depo) => depo.countryName?.trim())
          .filter((value): value is string => Boolean(value)),
      ),
    ).sort((a, b) => a.localeCompare(b));
  }, [depos]);

  const cityOptions = useMemo(() => {
    const source = selectedCountry
      ? depos.filter((depo) => (depo.countryName?.trim() ?? '') === selectedCountry)
      : depos;

    return Array.from(
      new Set(
        source
          .map((depo) => depo.cityName?.trim())
          .filter((value): value is string => Boolean(value)),
      ),
    ).sort((a, b) => a.localeCompare(b));
  }, [depos, selectedCountry]);

  const filteredDepos = useMemo(() => {
    return depos.filter((depo) => {
      const matchesCountry = selectedCountry
        ? (depo.countryName?.trim() ?? '') === selectedCountry
        : true;
      const matchesCity = selectedCity ? (depo.cityName?.trim() ?? '') === selectedCity : true;

      return matchesCountry && matchesCity;
    });
  }, [depos, selectedCountry, selectedCity]);

  const hasActiveFilters = Boolean(selectedCountry || selectedCity);

  useEffect(() => {
    const handleOutsideClick = (event: MouseEvent) => {
      const target = event.target as Node;

      if (
        countryFilterRef.current?.contains(target) ||
        cityFilterRef.current?.contains(target)
      ) {
        return;
      }

      setOpenFilterMenu(null);
    };

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpenFilterMenu(null);
      }
    };

    document.addEventListener('mousedown', handleOutsideClick);
    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  const handleCountryChange = (value: string) => {
    setSelectedCountry(value);

    if (!value) {
      setSelectedCity('');
      return;
    }

    const cityStillValid = depos.some(
      (depo) => (depo.countryName?.trim() ?? '') === value && (depo.cityName?.trim() ?? '') === selectedCity,
    );

    if (!cityStillValid) {
      setSelectedCity('');
    }
  };

  return (
    <section className="bg-surface-container-low rounded-3xl p-6 md:p-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Depo adatok</p>
        </div>
        <div className="flex flex-col items-end">
          <p className="font-body text-on-surface-variant">Megjelenített rekordok: {filteredDepos.length} / {depos.length}</p>
          <button
            type="button"
            onClick={() => {
              setSelectedCountry('');
              setSelectedCity('');
            }}
            disabled={!hasActiveFilters}
            className="mt-2 rounded-lg bg-surface px-3 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:opacity-70"
          >
            Szűrők törlése
          </button>
        </div>
      </div>

      <div className="mt-6 rounded-2xl bg-surface-container-lowest overflow-x-auto">
        <table className="w-full min-w-[880px] text-left font-body">
          <thead className="bg-surface-container-low text-on-surface-variant">
            <tr>
              <th ref={countryFilterRef} className={`${headerCellClass} relative pr-14`}>
                <span className="leading-none">Ország</span>
                <button
                  type="button"
                  onClick={() => setOpenFilterMenu((previous) => (previous === 'country' ? null : 'country'))}
                  className={`absolute right-5 top-1/2 inline-flex h-7 w-7 -translate-y-1/2 items-center justify-center rounded-lg transition-colors ${selectedCountry ? 'bg-primary text-on-primary' : 'bg-surface text-on-surface hover:bg-surface-container'}`}
                  aria-label="Ország szűrő menü"
                  aria-haspopup="listbox"
                  aria-expanded={openFilterMenu === 'country'}
                  aria-controls="depos-country-filter-menu"
                >
                  <span className="material-symbols-outlined block text-[18px] leading-none" aria-hidden="true">
                    filter_list
                  </span>
                </button>
                {openFilterMenu === 'country' ? (
                  <div
                    id="depos-country-filter-menu"
                    className="absolute left-5 top-14 z-20 w-56 rounded-xl bg-surface p-3 shadow-sm"
                  >
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Ország szűrő</p>
                    <select
                      value={selectedCountry}
                      onChange={(event) => {
                        handleCountryChange(event.target.value);
                        setOpenFilterMenu(null);
                      }}
                      className="mt-2 w-full rounded-lg bg-surface-container-lowest px-3 py-2 font-body text-on-surface"
                    >
                      <option value="">Minden ország</option>
                      {countryOptions.map((country) => (
                        <option key={country} value={country}>
                          {country}
                        </option>
                      ))}
                    </select>
                  </div>
                ) : null}
              </th>
              <th ref={cityFilterRef} className={`${headerCellClass} relative pr-14`}>
                <span className="leading-none">Város</span>
                <button
                  type="button"
                  onClick={() => setOpenFilterMenu((previous) => (previous === 'city' ? null : 'city'))}
                  className={`absolute right-5 top-1/2 inline-flex h-7 w-7 -translate-y-1/2 items-center justify-center rounded-lg transition-colors ${selectedCity ? 'bg-primary text-on-primary' : 'bg-surface text-on-surface hover:bg-surface-container'}`}
                  aria-label="Város szűrő menü"
                  aria-haspopup="listbox"
                  aria-expanded={openFilterMenu === 'city'}
                  aria-controls="depos-city-filter-menu"
                >
                  <span className="material-symbols-outlined block text-[18px] leading-none" aria-hidden="true">
                    filter_list
                  </span>
                </button>
                {openFilterMenu === 'city' ? (
                  <div
                    id="depos-city-filter-menu"
                    className="absolute left-5 top-14 z-20 w-56 rounded-xl bg-surface p-3 shadow-sm"
                  >
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Város szűrő</p>
                    <select
                      value={selectedCity}
                      onChange={(event) => {
                        setSelectedCity(event.target.value);
                        setOpenFilterMenu(null);
                      }}
                      className="mt-2 w-full rounded-lg bg-surface-container-lowest px-3 py-2 font-body text-on-surface"
                    >
                      <option value="">Minden város</option>
                      {cityOptions.map((city) => (
                        <option key={city} value={city}>
                          {city}
                        </option>
                      ))}
                    </select>
                  </div>
                ) : null}
              </th>
              <th className={headerCellClass}>Irányítószám</th>
              <th className={headerCellClass}>Cím</th>
              <th className={headerCellClass}>Részletek</th>
              <th className={headerCellClass}>Szerkesztés</th>
            </tr>
          </thead>
          <tbody>
            {filteredDepos.map((depo, index) => (
              <tr
                key={depo.id ?? `${depo.address ?? 'depo'}-${index}`}
                className={index % 2 === 0 ? 'bg-surface-container-lowest' : 'bg-surface-container-low'}
              >
                <td className="px-5 py-4 text-on-surface">{valueOrFallback(depo.countryName)}</td>
                <td className="px-5 py-4 text-on-surface">{valueOrFallback(depo.cityName)}</td>
                <td className="px-5 py-4 text-on-surface">{valueOrFallback(depo.zip)}</td>
                <td className="px-5 py-4 text-on-surface">{valueOrFallback(depo.address)}</td>
                <td className="px-5 py-4 text-on-surface">
                  {typeof depo.id === 'number' ? (
                    <Link
                      to={`/portal/depos/${depo.id}`}
                      className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                    >
                      Megnyit
                    </Link>
                  ) : (
                    <span className="text-on-surface-variant">N/A</span>
                  )}
                </td>
                <td className="px-5 py-4 text-on-surface">
                  {typeof depo.id === 'number' ? (
                    <Link
                      to={`/portal/depos/${depo.id}/edit`}
                      className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
                    >
                      Szerkeszt
                    </Link>
                  ) : (
                    <span className="text-on-surface-variant">N/A</span>
                  )}
                </td>
              </tr>
            ))}

            {filteredDepos.length === 0 ? (
              <tr>
                <td className="px-5 py-8 text-center font-body text-on-surface-variant" colSpan={6}>
                  Nincs találat a kiválasztott ország és város szűrőkre.
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </section>
  );
};

