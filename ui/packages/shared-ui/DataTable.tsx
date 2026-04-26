import { createPortal } from 'react-dom';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { ReactNode } from 'react';

export type DataTableColumn<T> = {
  id: string;
  header: string;
  cell: (row: T, index: number) => ReactNode;
  mobileLabel?: string;
  filterId?: string;
  headerClassName?: string;
  cellClassName?: string;
};

export type DataTableFilter<T> = {
  id: string;
  label: string;
  allOptionLabel: string;
  getOptionValue: (row: T) => string | undefined;
  dependsOn?: string;
  dependsOnText?: string;
};

type DataTableProps<T> = {
  data: T[];
  rowKey: (row: T, index: number) => string;
  title: string;
  columns: DataTableColumn<T>[];
  filters?: DataTableFilter<T>[];
  emptyMessage: string;
  mobileCardEyebrow?: string;
  recordCountLabel?: (visible: number, total: number) => string;
  renderMobileActions?: (row: T) => ReactNode;
};

const buildEmptyFilterState = (filters: DataTableFilter<unknown>[]) => {
  return filters.reduce<Record<string, string>>((acc, filter) => {
    acc[filter.id] = '';
    return acc;
  }, {});
};

export const DataTable = <T,>({
  data,
  rowKey,
  title,
  columns,
  filters = [],
  emptyMessage,
  mobileCardEyebrow = 'Rekord',
  recordCountLabel,
  renderMobileActions,
}: DataTableProps<T>) => {
  const headerCellClass = 'px-5 py-4 align-top text-[10px] uppercase tracking-widest font-semibold';
  const filterMenuWidth = 224;
  const filterMenuEstimatedHeight = 260;

  const [selectedFilters, setSelectedFilters] = useState<Record<string, string>>(() =>
    buildEmptyFilterState(filters as DataTableFilter<unknown>[]),
  );
  const [searchQueries, setSearchQueries] = useState<Record<string, string>>(() =>
    buildEmptyFilterState(filters as DataTableFilter<unknown>[]),
  );
  const [openFilterMenu, setOpenFilterMenu] = useState<string | null>(null);
  const [filterMenuPosition, setFilterMenuPosition] = useState<{ top: number; left: number } | null>(null);

  const desktopFilterCellRefs = useRef<Record<string, HTMLTableCellElement | null>>({});
  const mobileFilterContainerRefs = useRef<Record<string, HTMLDivElement | null>>({});
  const filterButtonRefs = useRef<Record<string, HTMLButtonElement | null>>({});
  const activeFilterButtonRef = useRef<HTMLButtonElement | null>(null);
  const filterMenuRef = useRef<HTMLDivElement | null>(null);

  const filtersById = useMemo(() => {
    return filters.reduce<Record<string, DataTableFilter<T>>>((acc, filter) => {
      acc[filter.id] = filter;
      return acc;
    }, {});
  }, [filters]);

  const getOptionsForFilter = useCallback((filter: DataTableFilter<T>, state: Record<string, string>) => {
    const dependsOn = filter.dependsOn;
    const source = dependsOn
      ? data.filter((row) => {
          const parentValue = state[dependsOn] ?? '';
          if (!parentValue) {
            return false;
          }

          const parentFilter = filtersById[dependsOn];
          const rowParentValue = parentFilter?.getOptionValue(row)?.trim() ?? '';
          return rowParentValue === parentValue;
        })
      : data;

    return Array.from(
      new Set(
        source
          .map((row) => filter.getOptionValue(row)?.trim())
          .filter((value): value is string => Boolean(value)),
      ),
    ).sort((a, b) => a.localeCompare(b));
  }, [data, filtersById]);

  const optionsByFilter = filters.reduce<Record<string, string[]>>((acc, filter) => {
    acc[filter.id] = getOptionsForFilter(filter, selectedFilters);
    return acc;
  }, {});

  const filteredOptionsByFilter = filters.reduce<Record<string, string[]>>((acc, filter) => {
    const query = (searchQueries[filter.id] ?? '').trim().toLocaleLowerCase();
    const options = optionsByFilter[filter.id] ?? [];
    acc[filter.id] = query
      ? options.filter((option) => option.toLocaleLowerCase().includes(query))
      : options;
    return acc;
  }, {});

  const filteredData = useMemo(() => {
    if (!filters.length) {
      return data;
    }

    return data.filter((row) => {
      return filters.every((filter) => {
        const selectedValue = selectedFilters[filter.id] ?? '';
        if (!selectedValue) {
          return true;
        }

        return (filter.getOptionValue(row)?.trim() ?? '') === selectedValue;
      });
    });
  }, [data, filters, selectedFilters]);

  const hasActiveFilters = useMemo(
    () => Object.values(selectedFilters).some((value) => Boolean(value)),
    [selectedFilters],
  );

  const updateFilterMenuPosition = (fallbackFilterId?: string) => {
    const button = activeFilterButtonRef.current ?? (fallbackFilterId ? filterButtonRefs.current[fallbackFilterId] : null);
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

      const clickedOnFilterTrigger = filters.some((filter) => {
        return (
          desktopFilterCellRefs.current[filter.id]?.contains(target) ||
          mobileFilterContainerRefs.current[filter.id]?.contains(target)
        );
      });

      if (clickedOnFilterTrigger || filterMenuRef.current?.contains(target)) {
        return;
      }

      setOpenFilterMenu(null);
      setSearchQueries(buildEmptyFilterState(filters as DataTableFilter<unknown>[]));
      activeFilterButtonRef.current = null;
    };

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpenFilterMenu(null);
        setSearchQueries(buildEmptyFilterState(filters as DataTableFilter<unknown>[]));
        activeFilterButtonRef.current = null;
      }
    };

    document.addEventListener('mousedown', handleOutsideClick);
    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
      document.removeEventListener('keydown', handleEscape);
    };
  }, [filters]);

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

  const applyFilterChange = (filterId: string, value: string) => {
    setSelectedFilters((previous) => {
      const next = { ...previous, [filterId]: value };

      // Keep dependent filters valid when a parent filter changes.
      let changed = true;
      while (changed) {
        changed = false;

        for (const filter of filters) {
          if (!filter.dependsOn) {
            continue;
          }

          const parentValue = next[filter.dependsOn] ?? '';
          if (!parentValue && next[filter.id]) {
            next[filter.id] = '';
            changed = true;
            continue;
          }

          if (!next[filter.id]) {
            continue;
          }

          const options = getOptionsForFilter(filter, next);
          if (!options.includes(next[filter.id])) {
            next[filter.id] = '';
            changed = true;
          }
        }
      }

      return next;
    });

    setSearchQueries((previous) => ({ ...previous, [filterId]: '' }));
    setOpenFilterMenu(null);
    activeFilterButtonRef.current = null;
  };

  const resetFilters = () => {
    const empty = buildEmptyFilterState(filters as DataTableFilter<unknown>[]);
    setSelectedFilters(empty);
    setSearchQueries(empty);
    setOpenFilterMenu(null);
    setFilterMenuPosition(null);
    activeFilterButtonRef.current = null;
  };

  const toggleFilterMenu = (filterId: string, button: HTMLButtonElement) => {
    activeFilterButtonRef.current = button;

    setOpenFilterMenu((previous) => {
      if (previous === filterId) {
        activeFilterButtonRef.current = null;
        return null;
      }

      return filterId;
    });

    setSearchQueries((previous) => ({ ...previous, [filterId]: '' }));
  };

  const openFilter = openFilterMenu ? filtersById[openFilterMenu] : undefined;
  const isOpenFilterBlockedByDependency = Boolean(
    openFilter?.dependsOn && !(selectedFilters[openFilter.dependsOn] ?? ''),
  );

  return (
    <section className="bg-surface-container-low rounded-3xl p-6 md:p-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{title}</p>
        </div>
        <div className="flex flex-col items-end">
          <p className="font-body text-on-surface-variant">
            {recordCountLabel
              ? recordCountLabel(filteredData.length, data.length)
              : `Megjelenített rekordok: ${filteredData.length} / ${data.length}`}
          </p>
          <button
            type="button"
            onClick={resetFilters}
            hidden={!hasActiveFilters}
            className="mt-2 rounded-lg bg-surface px-3 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:opacity-70"
          >
            Szűrők törlése
          </button>
        </div>
      </div>

      <div className="mt-6 rounded-2xl bg-surface-container-lowest overflow-visible">
        <div className="p-4 md:hidden">
          {filters.length > 0 ? (
            <div className="grid grid-cols-2 gap-2">
              {filters.map((filter) => (
                <div
                  key={filter.id}
                  ref={(element) => {
                    mobileFilterContainerRefs.current[filter.id] = element;
                  }}
                >
                  <button
                    ref={(element) => {
                      filterButtonRefs.current[filter.id] = element;
                    }}
                    type="button"
                    onClick={(event) => toggleFilterMenu(filter.id, event.currentTarget)}
                    className={`w-full rounded-lg px-3 py-2 text-left font-body text-sm transition-colors ${selectedFilters[filter.id] ? 'bg-primary text-on-primary' : 'bg-surface text-on-surface'}`}
                  >
                    {filter.label} szuro
                  </button>
                </div>
              ))}
            </div>
          ) : null}

          {filteredData.length === 0 ? (
            <p className="mt-4 rounded-xl bg-surface-container-low px-4 py-5 text-center font-body text-on-surface-variant">
              {emptyMessage}
            </p>
          ) : (
            <div className="mt-4 space-y-3">
              {filteredData.map((row, index) => (
                <article key={rowKey(row, index)} className="rounded-2xl bg-surface-container-low p-4">
                  <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{mobileCardEyebrow}</p>
                  <div className="mt-3 space-y-2 font-body text-sm text-on-surface">
                    {columns
                      .filter((column) => Boolean(column.mobileLabel))
                      .map((column) => (
                        <p key={column.id}>
                          <span className="text-on-surface-variant">{column.mobileLabel}:</span>{' '}
                          {column.cell(row, index)}
                        </p>
                      ))}
                  </div>
                  {renderMobileActions ? <div className="mt-4 flex gap-2">{renderMobileActions(row)}</div> : null}
                </article>
              ))}
            </div>
          )}
        </div>

        <div className="hidden overflow-x-auto md:block">
          <table className="w-full min-w-[880px] text-left font-body">
            <thead className="bg-surface-container-low text-on-surface-variant">
              <tr>
                {columns.map((column) => {
                  const filter = column.filterId ? filtersById[column.filterId] : undefined;

                  return (
                    <th
                      key={column.id}
                      className={`${headerCellClass} ${column.headerClassName ?? ''} ${filter ? 'relative pr-14' : ''}`}
                      ref={
                        filter
                          ? (element) => {
                              desktopFilterCellRefs.current[filter.id] = element;
                            }
                          : undefined
                      }
                    >
                      <span className="leading-none">{column.header}</span>
                      {filter ? (
                        <button
                          ref={(element) => {
                            filterButtonRefs.current[filter.id] = element;
                          }}
                          type="button"
                          onClick={(event) => toggleFilterMenu(filter.id, event.currentTarget)}
                          className={`absolute right-5 top-1/2 inline-flex h-7 w-7 -translate-y-1/2 items-center justify-center rounded-lg transition-colors ${selectedFilters[filter.id] ? 'bg-primary text-on-primary' : 'bg-surface text-on-surface hover:bg-surface-container'}`}
                          aria-label={`${filter.label} szuro menu`}
                        >
                          <span className="material-symbols-outlined block text-[18px] leading-none" aria-hidden="true">
                            filter_list
                          </span>
                        </button>
                      ) : null}
                    </th>
                  );
                })}
              </tr>
            </thead>
            <tbody>
              {filteredData.map((row, index) => (
                <tr
                  key={rowKey(row, index)}
                  className={index % 2 === 0 ? 'bg-surface-container-lowest' : 'bg-surface-container-low'}
                >
                  {columns.map((column) => (
                    <td key={column.id} className={`px-5 py-4 text-on-surface ${column.cellClassName ?? ''}`}>
                      {column.cell(row, index)}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredData.length === 0 ? (
          <div className="hidden md:block">
            <p className="px-5 py-8 text-center font-body text-on-surface-variant">{emptyMessage}</p>
          </div>
        ) : null}
      </div>

      {openFilter && filterMenuPosition
        ? createPortal(
            <div
              ref={filterMenuRef}
              className="fixed z-[1200] w-56 rounded-xl bg-surface p-3 shadow-sm"
              style={{ top: filterMenuPosition.top, left: filterMenuPosition.left }}
            >
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{openFilter.label} szuro</p>
              <input
                type="text"
                value={searchQueries[openFilter.id] ?? ''}
                onChange={(event) =>
                  setSearchQueries((previous) => ({ ...previous, [openFilter.id]: event.target.value }))
                }
                placeholder={`Kereses ${openFilter.label.toLocaleLowerCase()} alapjan`}
                disabled={isOpenFilterBlockedByDependency}
                className="mt-2 w-full rounded-lg bg-surface-container-lowest px-3 py-2 font-body text-on-surface placeholder:text-on-surface-variant"
              />
              <div className="mt-2 max-h-44 overflow-y-auto rounded-lg bg-surface-container-lowest p-1">
                {isOpenFilterBlockedByDependency ? (
                  <p className="px-2 py-2 font-body text-on-surface-variant">
                    {openFilter.dependsOnText ?? 'Elobb valaszd ki a kapcsolodo szurot.'}
                  </p>
                ) : (
                  <>
                    <button
                      type="button"
                      onClick={() => applyFilterChange(openFilter.id, '')}
                      className={`w-full rounded-md px-2 py-2 text-left font-body transition-colors ${selectedFilters[openFilter.id] === '' ? 'bg-primary text-on-primary' : 'text-on-surface hover:bg-surface-container-low'}`}
                    >
                      {openFilter.allOptionLabel}
                    </button>
                    {(filteredOptionsByFilter[openFilter.id] ?? []).map((option) => (
                      <button
                        key={option}
                        type="button"
                        onClick={() => applyFilterChange(openFilter.id, option)}
                        className={`mt-1 w-full rounded-md px-2 py-2 text-left font-body transition-colors ${selectedFilters[openFilter.id] === option ? 'bg-primary text-on-primary' : 'text-on-surface hover:bg-surface-container-low'}`}
                      >
                        {option}
                      </button>
                    ))}
                    {(filteredOptionsByFilter[openFilter.id] ?? []).length === 0 ? (
                      <p className="px-2 py-2 font-body text-on-surface-variant">Nincs talalat</p>
                    ) : null}
                  </>
                )}
              </div>
            </div>,
            document.body,
          )
        : null}
    </section>
  );
};

