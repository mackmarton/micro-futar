import { useEffect, useMemo, useRef, useState } from 'react';
import type { DepoWithLookups } from '../api/logisticsDeposApi';
import { Link } from 'react-router-dom';
import { createPortal } from 'react-dom';

type DeposDataTableProps = {
  depos: DepoWithLookups[];
};

const valueOrFallback = (value?: string | number) =>
  value === 0 || (typeof value === 'string' && value.length > 0) ? value : 'N/A';

export const DeposDataTable = ({ depos }: DeposDataTableProps) => {
  const headerCellClass = 'px-5 py-4 align-top text-[10px] uppercase tracking-widest font-semibold';
  const filterMenuWidth = 224;
  const filterMenuEstimatedHeight = 260;

  const [selectedCountry, setSelectedCountry] = useState('');
  const [selectedCity, setSelectedCity] = useState('');
  const [countrySearchQuery, setCountrySearchQuery] = useState('');
  const [citySearchQuery, setCitySearchQuery] = useState('');
  const [openFilterMenu, setOpenFilterMenu] = useState<'country' | 'city' | null>(null);

  const countryFilterRef = useRef<HTMLTableCellElement | null>(null);
  const cityFilterRef = useRef<HTMLTableCellElement | null>(null);
  const mobileCountryFilterRef = useRef<HTMLDivElement | null>(null);
  const mobileCityFilterRef = useRef<HTMLDivElement | null>(null);
  const countryFilterButtonRef = useRef<HTMLButtonElement | null>(null);
  const cityFilterButtonRef = useRef<HTMLButtonElement | null>(null);
  const activeFilterButtonRef = useRef<HTMLButtonElement | null>(null);
  const countryFilterMenuRef = useRef<HTMLDivElement | null>(null);
  const cityFilterMenuRef = useRef<HTMLDivElement | null>(null);
  const [filterMenuPosition, setFilterMenuPosition] = useState<{ top: number; left: number } | null>(null);

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

  const filteredCountryOptions = useMemo(() => {
    const query = countrySearchQuery.trim().toLocaleLowerCase();
    if (!query) {
      return countryOptions;
    }

    return countryOptions.filter((country) => country.toLocaleLowerCase().includes(query));
  }, [countryOptions, countrySearchQuery]);

  const filteredCityOptions = useMemo(() => {
    const query = citySearchQuery.trim().toLocaleLowerCase();
    if (!query) {
      return cityOptions;
    }

    return cityOptions.filter((city) => city.toLocaleLowerCase().includes(query));
  }, [cityOptions, citySearchQuery]);

  const updateFilterMenuPosition = (fallbackMenu?: 'country' | 'city') => {
    const button = activeFilterButtonRef.current
      ?? (fallbackMenu === 'country' ? countryFilterButtonRef.current : cityFilterButtonRef.current);
    if (!button) {
      return;
    }

    const rect = button.getBoundingClientRect();
    const viewportPadding = 8;

    let left = rect.left;
    if (left + filterMenuWidth > window.innerWidth - viewportPadding) {
      left = window.innerWidth - filterMenuWidth - viewportPadding;
    }
    left = Math.max(viewportPadding, left);

    let top = rect.bottom + 8;
    if (top + filterMenuEstimatedHeight > window.innerHeight - viewportPadding) {
      top = Math.max(viewportPadding, rect.top - filterMenuEstimatedHeight - 8);
    }

    setFilterMenuPosition({ top, left });
  };

  useEffect(() => {
    const handleOutsideClick = (event: MouseEvent) => {
      const target = event.target as Node;

      if (
        countryFilterRef.current?.contains(target) ||
        cityFilterRef.current?.contains(target) ||
        mobileCountryFilterRef.current?.contains(target) ||
        mobileCityFilterRef.current?.contains(target) ||
        countryFilterMenuRef.current?.contains(target) ||
        cityFilterMenuRef.current?.contains(target)
      ) {
        return;
      }

      setOpenFilterMenu(null);
      setCountrySearchQuery('');
      setCitySearchQuery('');
      activeFilterButtonRef.current = null;
    };

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpenFilterMenu(null);
        setCountrySearchQuery('');
        setCitySearchQuery('');
        activeFilterButtonRef.current = null;
      }
    };

    document.addEventListener('mousedown', handleOutsideClick);
    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  useEffect(() => {
    if (!openFilterMenu) {
      return;
    }

    const initialRaf = window.requestAnimationFrame(() => {
      updateFilterMenuPosition(openFilterMenu);
    });

    const handlePositionRefresh = () => updateFilterMenuPosition(openFilterMenu);
    window.addEventListener('resize', handlePositionRefresh);
    window.addEventListener('scroll', handlePositionRefresh, true);

    return () => {
      window.cancelAnimationFrame(initialRaf);
      window.removeEventListener('resize', handlePositionRefresh);
      window.removeEventListener('scroll', handlePositionRefresh, true);
    };
  }, [openFilterMenu]);

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

  const toggleFilterMenu = (menu: 'country' | 'city', button: HTMLButtonElement) => {
    activeFilterButtonRef.current = button;
    setOpenFilterMenu((previous) => {
      if (previous === menu) {
        activeFilterButtonRef.current = null;
        return null;
      }

      return menu;
    });
    setCountrySearchQuery('');
    setCitySearchQuery('');
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
              setCountrySearchQuery('');
              setCitySearchQuery('');
            }}
            disabled={!hasActiveFilters}
            className="mt-2 rounded-lg bg-surface px-3 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:opacity-70"
          >
            Szűrők törlése
          </button>
        </div>
      </div>

      <div className="mt-6 rounded-2xl bg-surface-container-lowest overflow-visible">
        <div className="p-4 md:hidden">
          <div className="grid grid-cols-2 gap-2">
            <div ref={mobileCountryFilterRef}>
              <button
                type="button"
                onClick={(event) => toggleFilterMenu('country', event.currentTarget)}
                className={`w-full rounded-lg px-3 py-2 text-left font-body text-sm transition-colors ${selectedCountry ? 'bg-primary text-on-primary' : 'bg-surface text-on-surface'}`}
              >
                Ország szűrő
              </button>
            </div>
            <div ref={mobileCityFilterRef}>
              <button
                type="button"
                onClick={(event) => toggleFilterMenu('city', event.currentTarget)}
                className={`w-full rounded-lg px-3 py-2 text-left font-body text-sm transition-colors ${selectedCity ? 'bg-primary text-on-primary' : 'bg-surface text-on-surface'}`}
              >
                Város szűrő
              </button>
            </div>
          </div>

          {filteredDepos.length === 0 ? (
            <p className="mt-4 rounded-xl bg-surface-container-low px-4 py-5 text-center font-body text-on-surface-variant">
              Nincs találat a kiválasztott ország és város szűrőkre.
            </p>
          ) : (
            <div className="mt-4 space-y-3">
              {filteredDepos.map((depo, index) => (
                <article
                  key={depo.id ?? `${depo.address ?? 'depo-mobile'}-${index}`}
                  className="rounded-2xl bg-surface-container-low p-4"
                >
                  <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Depo</p>
                  <div className="mt-3 space-y-2 font-body text-sm text-on-surface">
                    <p><span className="text-on-surface-variant">Ország:</span> {valueOrFallback(depo.countryName)}</p>
                    <p><span className="text-on-surface-variant">Város:</span> {valueOrFallback(depo.cityName)}</p>
                    <p><span className="text-on-surface-variant">Irányítószám:</span> {valueOrFallback(depo.zip)}</p>
                    <p><span className="text-on-surface-variant">Cím:</span> {valueOrFallback(depo.address)}</p>
                  </div>
                  <div className="mt-4 flex gap-2">
                    {typeof depo.id === 'number' ? (
                      <>
                        <Link
                          to={`/portal/depos/${depo.id}`}
                          className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-on-primary"
                        >
                          Megnyit
                        </Link>
                        <Link
                          to={`/portal/depos/${depo.id}/edit`}
                          className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
                        >
                          Szerkeszt
                        </Link>
                      </>
                    ) : (
                      <span className="text-on-surface-variant">N/A</span>
                    )}
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>

        <div className="hidden overflow-x-auto md:block">
          <table className="w-full min-w-[880px] text-left font-body">
          <thead className="bg-surface-container-low text-on-surface-variant">
            <tr>
              <th ref={countryFilterRef} className={`${headerCellClass} relative pr-14`}>
                <span className="leading-none">Ország</span>
                <button
                  ref={countryFilterButtonRef}
                  type="button"
                  onClick={(event) => toggleFilterMenu('country', event.currentTarget)}
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
              </th>
              <th ref={cityFilterRef} className={`${headerCellClass} relative pr-14`}>
                <span className="leading-none">Város</span>
                <button
                  ref={cityFilterButtonRef}
                  type="button"
                  onClick={(event) => toggleFilterMenu('city', event.currentTarget)}
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

          </tbody>
          </table>
        </div>

        {filteredDepos.length === 0 ? (
          <div className="hidden md:block">
            <p className="px-5 py-8 text-center font-body text-on-surface-variant">
              Nincs találat a kiválasztott ország és város szűrőkre.
            </p>
          </div>
        ) : null}
      </div>

      {openFilterMenu && filterMenuPosition
        ? createPortal(
            <div
              ref={openFilterMenu === 'country' ? countryFilterMenuRef : cityFilterMenuRef}
              id={openFilterMenu === 'country' ? 'depos-country-filter-menu' : 'depos-city-filter-menu'}
              className="fixed z-[1200] w-56 rounded-xl bg-surface p-3 shadow-sm"
              style={{ top: filterMenuPosition.top, left: filterMenuPosition.left }}
            >
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">
                {openFilterMenu === 'country' ? 'Ország szűrő' : 'Város szűrő'}
              </p>
              <input
                type="text"
                value={openFilterMenu === 'country' ? countrySearchQuery : citySearchQuery}
                onChange={(event) => {
                  if (openFilterMenu === 'country') {
                    setCountrySearchQuery(event.target.value);
                    return;
                  }

                  setCitySearchQuery(event.target.value);
                }}
                placeholder={openFilterMenu === 'country' ? 'Keresés országra' : 'Keresés városra'}
                className="mt-2 w-full rounded-lg bg-surface-container-lowest px-3 py-2 font-body text-on-surface placeholder:text-on-surface-variant"
              />

              {openFilterMenu === 'country' ? (
                <div className="mt-2 max-h-44 overflow-y-auto rounded-lg bg-surface-container-lowest p-1">
                  <button
                    type="button"
                    onClick={() => {
                      handleCountryChange('');
                      setOpenFilterMenu(null);
                      setCountrySearchQuery('');
                    }}
                    className={`w-full rounded-md px-2 py-2 text-left font-body transition-colors ${selectedCountry === '' ? 'bg-primary text-on-primary' : 'text-on-surface hover:bg-surface-container-low'}`}
                  >
                    Minden ország
                  </button>
                  {filteredCountryOptions.map((country) => (
                    <button
                      key={country}
                      type="button"
                      onClick={() => {
                        handleCountryChange(country);
                        setOpenFilterMenu(null);
                        setCountrySearchQuery('');
                      }}
                      className={`mt-1 w-full rounded-md px-2 py-2 text-left font-body transition-colors ${selectedCountry === country ? 'bg-primary text-on-primary' : 'text-on-surface hover:bg-surface-container-low'}`}
                    >
                      {country}
                    </button>
                  ))}
                  {filteredCountryOptions.length === 0 ? (
                    <p className="px-2 py-2 font-body text-on-surface-variant">Nincs találat</p>
                  ) : null}
                </div>
              ) : (
                <div className="mt-2 max-h-44 overflow-y-auto rounded-lg bg-surface-container-lowest p-1">
                  <button
                    type="button"
                    onClick={() => {
                      setSelectedCity('');
                      setOpenFilterMenu(null);
                      setCitySearchQuery('');
                    }}
                    className={`w-full rounded-md px-2 py-2 text-left font-body transition-colors ${selectedCity === '' ? 'bg-primary text-on-primary' : 'text-on-surface hover:bg-surface-container-low'}`}
                  >
                    Minden város
                  </button>
                  {filteredCityOptions.map((city) => (
                    <button
                      key={city}
                      type="button"
                      onClick={() => {
                        setSelectedCity(city);
                        setOpenFilterMenu(null);
                        setCitySearchQuery('');
                      }}
                      className={`mt-1 w-full rounded-md px-2 py-2 text-left font-body transition-colors ${selectedCity === city ? 'bg-primary text-on-primary' : 'text-on-surface hover:bg-surface-container-low'}`}
                    >
                      {city}
                    </button>
                  ))}
                  {filteredCityOptions.length === 0 ? (
                    <p className="px-2 py-2 font-body text-on-surface-variant">Nincs találat</p>
                  ) : null}
                </div>
              )}
            </div>,
            document.body,
          )
        : null}
    </section>
  );
};

