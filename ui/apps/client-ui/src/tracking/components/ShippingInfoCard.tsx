import { useTranslation } from 'react-i18next';
import { cn } from '@package/shared-ui';

export type ShippingInfoCardProps = {
  title?: string;
  addressTitle?: string;
  addressPrimary?: string;
  addressSecondary?: string;
  contentTitle?: string;
  contentValue?: string;
  noteTitle?: string;
  note?: string;
  securityNotice?: string;
  className?: string;
};

const iconBoxClassName =
  'bg-teal-50 w-10 h-10 rounded-lg flex items-center justify-center text-teal-600 shrink-0';

export const ShippingInfoCard = ({
  title,
  addressTitle,
  addressPrimary,
  securityNotice,
  className,
}: ShippingInfoCardProps) => {
  const { t } = useTranslation('tracking');
  const resolvedTitle = title ?? t('shipping.title');
  const resolvedAddressTitle = addressTitle ?? t('shipping.addressTitle');
  const resolvedAddressPrimary = addressPrimary ?? t('shipping.defaultAddress');
  const resolvedSecurityNotice = securityNotice ?? t('shipping.securityNotice');

  return (
    <section className={cn('bg-surface-container-lowest p-6 rounded-xl shadow-sm border border-surface-container', className)}>
      <h3 className="text-sm font-bold mb-6 text-on-surface-variant uppercase tracking-widest flex items-center gap-2">
        <span className="material-symbols-outlined text-teal-600 text-lg" aria-hidden="true">
          info
        </span>
        {resolvedTitle}
      </h3>

      <div className="space-y-6">
        <div className="flex gap-4">
          <div className={iconBoxClassName}>
            <span className="material-symbols-outlined text-lg" aria-hidden="true">
              location_on
            </span>
          </div>
          <div>
            <p className="text-[10px] text-on-surface-variant uppercase font-bold tracking-wider mb-0.5">{resolvedAddressTitle}</p>
            <p className="text-sm font-semibold text-on-surface">{resolvedAddressPrimary}</p>
          </div>
        </div>
      </div>

      <div className="mt-6">
        <div className="flex items-center gap-2 text-on-tertiary-fixed-variant bg-tertiary-fixed p-3 rounded-lg">
          <span className="material-symbols-outlined shrink-0 text-sm" aria-hidden="true">
            security
          </span>
          <p className="text-[10px] font-medium leading-tight">{resolvedSecurityNotice}</p>
        </div>
      </div>
    </section>
  );
};

