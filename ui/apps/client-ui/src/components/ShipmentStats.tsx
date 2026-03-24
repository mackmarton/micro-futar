type StatStatus = 'active' | 'inProgress' | 'delivered';

export type ShipmentStatItem = {
  status: StatStatus;
  label: string;
  value: number | string;
};

export type ShipmentStatsObject = {
  active: number | string;
  inProgress: number | string;
  delivered: number | string;
};

export type ShipmentStatsProps = {
  stats: ShipmentStatItem[] | ShipmentStatsObject;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const STAT_META: Record<StatStatus, { label: string; borderClass: string }> = {
  active: {
    label: 'Aktív szállítások',
    borderClass: 'border-primary-fixed',
  },
  inProgress: {
    label: 'Folyamatban lévő',
    borderClass: 'border-tertiary-fixed',
  },
  delivered: {
    label: 'Kiszállítva (30 nap)',
    borderClass: 'border-on-primary-container',
  },
};

const ORDER: StatStatus[] = ['active', 'inProgress', 'delivered'];

const normalizeStats = (stats: ShipmentStatsProps['stats']): ShipmentStatItem[] => {
  if (Array.isArray(stats)) {
    const byStatus = new Map(stats.map((item) => [item.status, item]));

    return ORDER.map((status) => {
      const fromInput = byStatus.get(status);
      if (fromInput) {
        return {
          status,
          label: fromInput.label,
          value: fromInput.value,
        };
      }

      return {
        status,
        label: STAT_META[status].label,
        value: 0,
      };
    });
  }

  return ORDER.map((status) => ({
    status,
    label: STAT_META[status].label,
    value: stats[status],
  }));
};

export const ShipmentStats = ({ stats, className }: ShipmentStatsProps) => {
  const cards = normalizeStats(stats);

  return (
    <section className={cn('grid grid-cols-1 md:grid-cols-3 gap-6 mb-12', className)}>
      {cards.map((item) => (
        <article
          key={item.status}
          className={cn(
            'bg-surface-container-lowest p-6 rounded-xl shadow-sm flex flex-col gap-1 border-b-2',
            STAT_META[item.status].borderClass
          )}
        >
          <span className="text-xs font-bold uppercase tracking-widest text-on-surface-variant">
            {item.label}
          </span>
          <span className="text-4xl font-headline font-extrabold text-on-surface">{item.value}</span>
        </article>
      ))}
    </section>
  );
};

