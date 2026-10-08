import { useTranslation } from 'react-i18next';
import type { CourierAllocation } from '../api/courierAllocationsApi.ts';

type CourierAllocatedPackageDetailsSectionProps = {
  selectedAllocation: CourierAllocation;
  canPickUp: boolean;
  canFulfill: boolean;
  canFail: boolean;
  isActionPending: boolean;
  actionErrorMessage: string | null;
  onPickUp: (assignmentId: number) => void;
  onFulfill: (assignmentId: number) => void;
  onFail: (assignmentId: number) => void;
};

export const CourierAllocatedPackageDetailsSection = ({
  selectedAllocation,
  canPickUp,
  canFulfill,
  canFail,
  isActionPending,
  actionErrorMessage,
  onPickUp,
  onFulfill,
  onFail,
}: CourierAllocatedPackageDetailsSectionProps) => {
  const { t } = useTranslation(['allocations', 'common']);
  const isDelivery = selectedAllocation.assignmentType === 'Delivery';
  const contactLabel = isDelivery ? t('allocations:details.contactLabel.recipient') : t('allocations:details.contactLabel.sender');
  // selectedAllocation.{senderName,recipientName,senderPhone,recipientPhone,routeAddress,parcelNumber}
  // a shipment DTO-ból jönnek a backendről, szabadszöveges adatok - nem fordítjuk.
  const contactName = isDelivery ? selectedAllocation.recipientName : selectedAllocation.senderName;
  const locationLabel = isDelivery ? t('allocations:details.locationLabel.delivery') : t('allocations:details.locationLabel.pickup');
  const contactPhone = isDelivery ? selectedAllocation.recipientPhone : selectedAllocation.senderPhone;
  const hasCallablePhone = contactPhone.trim().length > 0 && contactPhone !== '-';
  const navigationHref = `https://www.google.com/maps/dir/?api=1&destination=${selectedAllocation.latitude},${selectedAllocation.longitude}&travelmode=driving`;
  const assignmentTypeLabel = t(
    `common:assignmentType.${selectedAllocation.assignmentType === 'Delivery' ? 'delivery' : 'pickup'}`,
  );
  const actionTileClassName =
    'inline-flex h-24 w-full flex-col items-center justify-center gap-2 rounded-xl bg-surface-container px-4 py-3 text-center text-sm font-body font-semibold text-on-surface transition-colors enabled:hover:bg-surface-container-high disabled:cursor-not-allowed disabled:opacity-50';
  const statusPillClassName = 'inline-flex items-center gap-1 rounded-lg bg-surface-container px-3 py-1.5 text-xs font-medium text-on-surface';

  return (
    <section className="rounded-2xl bg-surface-container-low p-6 md:p-8">
      <span className="inline-flex rounded-full bg-teal-100 px-3 py-1 text-xs font-semibold text-teal-900">
        {assignmentTypeLabel}
      </span>

      <div className="mt-5 grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
        <div className="space-y-5">
          <div className="flex items-center gap-3 p-1">
            <span className="inline-flex h-9 w-9 items-center justify-center rounded-lg bg-surface-container-high text-on-surface">
              <span className="material-symbols-outlined text-base" aria-hidden="true">sell</span>
            </span>
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-on-surface-variant">{t('allocations:details.parcelNumberLabel')}</p>
              <p className="text-base font-medium text-on-surface">{selectedAllocation.parcelNumber}</p>
            </div>
          </div>

          <div className="grid gap-3 md:grid-cols-2">
            <div className="flex items-center gap-3">
              <span className="inline-flex h-9 w-9 items-center justify-center rounded-lg bg-surface-container text-on-surface">
                <span className="material-symbols-outlined text-base" aria-hidden="true">person</span>
              </span>
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-on-surface-variant">{contactLabel}</p>
                <p className="text-base font-medium text-on-surface">{contactName}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <span className="inline-flex h-9 w-9 items-center justify-center rounded-lg bg-surface-container text-on-surface">
                <span className="material-symbols-outlined text-base" aria-hidden="true">call</span>
              </span>
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-on-surface-variant">{t('allocations:details.phoneLabel')}</p>
                <p className="text-base font-medium text-on-surface">{contactPhone}</p>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <span className="inline-flex h-9 w-9 items-center justify-center rounded-lg bg-surface-container text-on-surface">
              <span className="material-symbols-outlined text-base" aria-hidden="true">location_on</span>
            </span>
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-on-surface-variant">{locationLabel}</p>
              <p className="text-base font-medium text-on-surface">{selectedAllocation.routeAddress}</p>
            </div>
          </div>

          <div className="flex flex-wrap gap-3">
            <div className={statusPillClassName}>
              <span className="material-symbols-outlined text-sm" aria-hidden="true">lens</span>
              {t('allocations:details.statusPill.pickedUpLabel')}: {selectedAllocation.pickedUpForDelivery ? t('common:status.yes') : t('common:status.no')}
            </div>
            <div className={statusPillClassName}>
              <span className="material-symbols-outlined text-sm" aria-hidden="true">lens</span>
              {t('allocations:details.statusPill.failedLabel')}: {selectedAllocation.failed ? t('common:status.yes') : t('common:status.no')}
            </div>
          </div>
        </div>

        <div className="border-t border-outline-variant pt-6 xl:border-l xl:border-t-0 xl:pl-6 xl:pt-0">
          <div className="grid grid-cols-2 gap-3">
            <a
              href={hasCallablePhone ? `tel:${contactPhone}` : undefined}
              aria-disabled={!hasCallablePhone}
              onClick={(event) => {
                if (!hasCallablePhone) {
                  event.preventDefault();
                }
              }}
              className={`${actionTileClassName} ${!hasCallablePhone ? 'pointer-events-none opacity-50' : ''}`}
            >
              <span className="material-symbols-outlined text-xl" aria-hidden="true">call</span>
              {t('common:actions.call')}
            </a>
            <button
              type="button"
              disabled={!canPickUp || isActionPending}
              onClick={() => {
                if (typeof selectedAllocation.assignmentId !== 'number') {
                  return;
                }
                onPickUp(selectedAllocation.assignmentId);
              }}
              className={actionTileClassName}
            >
              <span className="material-symbols-outlined text-xl" aria-hidden="true">inventory_2</span>
              {t('allocations:details.actions.pickUpParcel')}
            </button>
          </div>

          <div className="mt-3 space-y-3">
            <a
              href={navigationHref}
              target="_blank"
              rel="noreferrer"
              className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-surface-container px-4 py-3 font-body text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container-high"
            >
              <span className="material-symbols-outlined text-lg" aria-hidden="true">navigation</span>
              {t('common:actions.navigate')}
            </a>
            <button
              type="button"
              disabled={!canFulfill || isActionPending}
              onClick={() => {
                if (typeof selectedAllocation.assignmentId !== 'number') {
                  return;
                }
                onFulfill(selectedAllocation.assignmentId);
              }}
              className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-[linear-gradient(95deg,#000000_0%,#0c9488_100%)] px-4 py-4 font-body text-base font-semibold text-on-primary disabled:cursor-not-allowed disabled:opacity-50"
            >
              <span className="material-symbols-outlined text-lg" aria-hidden="true">check_circle</span>
              {t('allocations:details.actions.confirmDelivery')}
            </button>
            <button
              type="button"
              disabled={!canFail || isActionPending}
              onClick={() => {
                if (typeof selectedAllocation.assignmentId !== 'number') {
                  return;
                }
                onFail(selectedAllocation.assignmentId);
              }}
              className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-red-100 px-4 py-4 font-body text-base font-semibold text-red-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <span className="material-symbols-outlined text-lg" aria-hidden="true">warning</span>
              {t('allocations:details.actions.markFailed')}
            </button>
          </div>
        </div>
      </div>

      {actionErrorMessage ? (
        <p className="mt-4 text-sm text-red-600">{actionErrorMessage}</p>
      ) : null}
    </section>
  );
};
