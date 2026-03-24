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

export type ShipmentCardProps = {
  shipment: Shipment;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const STATUS_META: Record<
  ShipmentStatus,
  {
    badgeLabel: string;
    badgeClass: string;
    icon: string;
    iconContainerClass: string;
    hoverBorderClass: string;
  }
> = {
  inProgress: {
    badgeLabel: 'Folyamatban',
    badgeClass: 'bg-tertiary-fixed text-on-tertiary-fixed',
    icon: 'package_2',
    iconContainerClass: 'bg-secondary-container text-on-secondary-container',
    hoverBorderClass: 'hover:border-primary-fixed/30',
  },
  delivered: {
    badgeLabel: 'Kiszállítva',
    badgeClass: 'bg-primary-fixed text-on-primary-fixed-variant',
    icon: 'inventory_2',
    iconContainerClass: 'bg-surface-container-low text-on-surface-variant',
    hoverBorderClass: 'hover:border-primary-fixed/30',
  },
  failed: {
    badgeLabel: 'Sikertelen',
    badgeClass: 'bg-error text-on-error',
    icon: 'report',
    iconContainerClass: 'bg-error-container/30 text-error',
    hoverBorderClass: 'hover:border-error/30',
  },
};

const trackerWidth = (progressPercent?: number) => {
  if (typeof progressPercent !== 'number') return 65;
  return Math.min(100, Math.max(0, progressPercent));
};

export const ShipmentCard = ({ shipment, className }: ShipmentCardProps) => {
  const meta = STATUS_META[shipment.status];

  return (
    <article
      className={cn(
        'bg-surface-container-lowest p-6 rounded-xl transition-all group border border-transparent hover:shadow-lg',
        meta.hoverBorderClass,
        className
      )}
    >
      <div className="flex justify-between items-start mb-6 gap-6">
        <div className="flex items-center gap-4">
          <div className={cn('w-12 h-12 rounded-lg flex items-center justify-center', meta.iconContainerClass)}>
            <span className="material-symbols-outlined" aria-hidden="true">
              {meta.icon}
            </span>
          </div>
          <div>
            <h4 className="font-bold text-on-surface">{shipment.id}</h4>
            <p className="text-xs text-on-surface-variant font-medium">Feladva: {shipment.createdAt}</p>
          </div>
        </div>

        <span
          className={cn(
            'px-3 py-1 rounded-full text-xs font-bold uppercase tracking-tighter whitespace-nowrap',
            meta.badgeClass
          )}
        >
          {meta.badgeLabel}
        </span>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="flex flex-col gap-1">
          <span className="text-[10px] text-on-surface-variant uppercase tracking-widest font-bold">Célállomás</span>
          <span className="text-on-surface font-semibold">{shipment.destination}</span>
        </div>

        {shipment.status === 'inProgress' && (
          <div className="flex flex-col gap-1 text-right">
            <span className="text-[10px] text-on-surface-variant uppercase tracking-widest font-bold">Várható érkezés</span>
            <span className="text-on-surface font-semibold">{shipment.eta ?? '-'}</span>
          </div>
        )}

        {shipment.status === 'delivered' && (
          <div className="flex flex-col gap-1 text-right">
            <span className="text-[10px] text-on-surface-variant uppercase tracking-widest font-bold">Kézbesítve</span>
            <span className="text-on-surface font-semibold">{shipment.deliveredAt ?? '-'}</span>
          </div>
        )}

        {shipment.status === 'failed' && (
          <div className="flex flex-col gap-1 text-right">
            <span className="text-[10px] text-on-surface-variant uppercase tracking-widest font-bold">Hiba oka</span>
            <span className="text-error font-semibold">{shipment.errorReason ?? '-'}</span>
          </div>
        )}
      </div>

      {shipment.status === 'inProgress' && (
        <div className="flex flex-col gap-2">
          <div className="relative h-2 w-full bg-surface-container-high rounded-full overflow-hidden">
            <div
              className="absolute left-0 top-0 h-full bg-gradient-to-r from-primary-fixed-dim to-on-primary-container rounded-full"
              style={{ width: `${trackerWidth(shipment.progressPercent)}%` }}
            />
          </div>
          <div className="flex justify-between text-[10px] font-bold text-on-surface-variant uppercase tracking-widest">
            <span>Raktár</span>
            <span>Úton</span>
            <span className="opacity-50">Átvéve</span>
          </div>
        </div>
      )}

      {shipment.status === 'delivered' && (
        <div className="p-3 bg-surface-container-low rounded-lg flex items-center justify-between gap-3">
          <span className="text-xs text-on-surface-variant">Aláíró: {shipment.signerName ?? '-'}</span>
          <button
            type="button"
            onClick={shipment.onDownloadProof}
            className="text-on-primary-container text-xs font-bold hover:underline"
          >
            Igazolás letöltése
          </button>
        </div>
      )}

      {shipment.status === 'failed' && (
        <button
          type="button"
          onClick={shipment.onRetry}
          className="w-full bg-secondary-container text-on-secondary-container py-2 rounded-lg font-bold text-sm hover:brightness-95 transition-all"
        >
          Cím módosítása és újraküldés
        </button>
      )}
    </article>
  );
};

