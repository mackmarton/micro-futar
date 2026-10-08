import { useTranslation } from 'react-i18next';
import { cn } from '@package/shared-ui';

export type ShipmentStatus = 'inProgress' | 'delivered' | 'failed';

export type Shipment = {
  id: string;
  createdAt: string;
  destination: string;
  status: ShipmentStatus;
  eta?: string;
  deliveredAt?: string;
  signerName?: string;
  errorReason?: string;
  progressPercent?: number;
  onDownloadProof?: () => void;
  onRetry?: () => void;
};

export type ShipmentTableProps = {
  shipments: Shipment[];
  totalCountText?: string;
  onPrevPage?: () => void;
  onNextPage?: () => void;
  isPrevDisabled?: boolean;
  isNextDisabled?: boolean;
  className?: string;
};

const STATUS_META: Record<
  ShipmentStatus,
  {
    badgeClass: string;
    icon: string;
    iconContainerClass: string;
  }
> = {
  inProgress: {
    badgeClass: 'bg-tertiary-fixed text-on-tertiary-fixed',
    icon: 'package_2',
    iconContainerClass: 'bg-secondary-container text-on-secondary-container',
  },
  delivered: {
    badgeClass: 'bg-primary-fixed text-on-primary-fixed-variant',
    icon: 'inventory_2',
    iconContainerClass: 'bg-surface-container-low text-on-surface-variant',
  },
  failed: {
    badgeClass: 'bg-error text-on-error',
    icon: 'report',
    iconContainerClass: 'bg-error-container/30 text-error',
  },
};

const STATUS_LABEL_KEY: Record<ShipmentStatus, string> = {
  inProgress: 'table.statusInProgress',
  delivered: 'table.statusDelivered',
  failed: 'table.statusFailed',
};

export const ShipmentTable = ({
  shipments,
  totalCountText,
  onPrevPage,
  onNextPage,
  isPrevDisabled = true,
  isNextDisabled = false,
  className,
}: ShipmentTableProps) => {
  const { t } = useTranslation('dashboard');
  const totalLabel = totalCountText ?? t('table.totalCount', { count: shipments.length });

  return (
    <section className={cn('bg-surface-container-lowest rounded-2xl shadow-sm border border-outline-variant overflow-hidden', className)}>
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-surface-container-low border-b border-outline-variant">
              <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">{t('table.id')}</th>
              <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">{t('table.destination')}</th>
              <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">{t('table.date')}</th>
              <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-on-surface-variant">{t('table.status')}</th>
            </tr>
          </thead>

          <tbody className="divide-y divide-outline-variant/30">
            {shipments.map((shipment) => {
              const meta = STATUS_META[shipment.status];
              const badgeLabel = t(STATUS_LABEL_KEY[shipment.status]);

              return (
                <tr key={shipment.id} className="hover:bg-surface-container-low/50 transition-colors group">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className={cn('w-8 h-8 rounded-lg flex items-center justify-center', meta.iconContainerClass)}>
                        <span className="material-symbols-outlined text-sm" aria-hidden="true">
                          {meta.icon}
                        </span>
                      </div>
                      <span className="font-bold text-on-surface">{shipment.id}</span>
                    </div>
                  </td>

                  <td className="px-6 py-4">
                    <span className="text-on-surface font-medium">{shipment.destination}</span>
                  </td>

                  <td className="px-6 py-4">
                    <span className="text-on-surface-variant text-sm">{shipment.createdAt}</span>
                  </td>

                  <td className="px-6 py-4">
                    <span className={cn('px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-tighter', meta.badgeClass)}>
                      {badgeLabel}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <div className="bg-surface-container-low px-6 py-4 flex justify-between items-center border-t border-outline-variant">
        <span className="text-xs text-on-surface-variant font-medium">{totalLabel}</span>

        <div className="flex gap-2">
          <button
            type="button"
            onClick={onPrevPage}
            disabled={isPrevDisabled}
            className="p-2 rounded hover:bg-surface-container transition-colors disabled:opacity-30"
            aria-label={t('table.prevPage')}
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              chevron_left
            </span>
          </button>

          <button
            type="button"
            onClick={onNextPage}
            disabled={isNextDisabled}
            className="p-2 rounded hover:bg-surface-container transition-colors disabled:opacity-30"
            aria-label={t('table.nextPage')}
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              chevron_right
            </span>
          </button>
        </div>
      </div>
    </section>
  );
};

