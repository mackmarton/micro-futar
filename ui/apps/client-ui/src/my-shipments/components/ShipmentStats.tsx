import { useTranslation } from 'react-i18next';
import { cn } from '@package/shared-ui';

type StatStatus = 'inProgress' | 'delivered';

export type ShipmentStatItem = {
  status: StatStatus;
  label: string;
  value: number | string;
};

export type ShipmentStatsObject = {
  inProgress: number | string;
  delivered: number | string;
};

export type ShipmentStatsProps = {
  stats: ShipmentStatItem[] | ShipmentStatsObject;
  className?: string;
};

const STAT_BORDER_CLASS: Record<StatStatus, string> = {
  inProgress: 'border-tertiary-fixed',
  delivered: 'border-on-primary-container',
};

const ORDER: StatStatus[] = ['inProgress', 'delivered'];

const normalizeStats = (
  stats: ShipmentStatsProps['stats'],
  defaultLabel: (status: StatStatus) => string,
): ShipmentStatItem[] => {
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
        label: defaultLabel(status),
        value: 0,
      };
    });
  }

  return ORDER.map((status) => ({
    status,
    label: defaultLabel(status),
    value: stats[status],
  }));
};

export const ShipmentStats = ({ stats, className }: ShipmentStatsProps) => {
  const { t } = useTranslation('dashboard');
  const defaultLabel = (status: StatStatus) => t(`stats.${status}`);
  const cards = normalizeStats(stats, defaultLabel);

  return (
    <section className={cn('grid grid-cols-1 md:grid-cols-2 gap-6 mb-12', className)}>
      {cards.map((item) => (
        <article
          key={item.status}
          className={cn(
            'bg-surface-container-lowest p-6 rounded-xl shadow-sm flex flex-col gap-1 border-b-2',
            STAT_BORDER_CLASS[item.status]
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

