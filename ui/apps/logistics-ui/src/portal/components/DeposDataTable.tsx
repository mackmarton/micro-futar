import type { DepoWithLookups } from '../api/logisticsDeposApi';
import { Link } from 'react-router-dom';

type DeposDataTableProps = {
  depos: DepoWithLookups[];
};

const valueOrFallback = (value?: string | number) => (value ?? value === 0 ? value : 'N/A');

export const DeposDataTable = ({ depos }: DeposDataTableProps) => {
  return (
    <section className="bg-surface-container-low rounded-3xl p-6 md:p-8">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Depo adatok</p>
        </div>
        <p className="font-body text-on-surface-variant">Összes rekord: {depos.length}</p>
      </div>

      <div className="mt-6 rounded-2xl bg-surface-container-lowest overflow-x-auto">
        <table className="w-full min-w-[880px] text-left font-body">
          <thead className="bg-surface-container-low text-on-surface-variant">
            <tr>
              <th className="px-5 py-4 text-[10px] uppercase tracking-widest font-semibold">Ország</th>
              <th className="px-5 py-4 text-[10px] uppercase tracking-widest font-semibold">Város</th>
              <th className="px-5 py-4 text-[10px] uppercase tracking-widest font-semibold">Irányítószám</th>
              <th className="px-5 py-4 text-[10px] uppercase tracking-widest font-semibold">Cím</th>
              <th className="px-5 py-4 text-[10px] uppercase tracking-widest font-semibold">Részletek</th>
              <th className="px-5 py-4 text-[10px] uppercase tracking-widest font-semibold">Szerkesztés</th>
            </tr>
          </thead>
          <tbody>
            {depos.map((depo, index) => (
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
    </section>
  );
};

