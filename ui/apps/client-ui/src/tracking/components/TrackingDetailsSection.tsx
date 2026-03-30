import { ShippingInfoCard } from './ShippingInfoCard.tsx';
import { TrackingProgressCard } from './TrackingProgressCard.tsx';
import type { TrackingProgressStep } from './TrackingProgressCard.tsx';
import { TrackingTimelineCard } from './TrackingTimelineCard.tsx';
import type { TrackingTimelineEvent } from './TrackingTimelineCard.tsx';

export type TrackingDetailsSectionProps = {
  trackingNumber?: string;
  statusLabel?: string;
  deliveryTimeValue?: string;
  progressSteps?: TrackingProgressStep[];
  timelineEvents?: TrackingTimelineEvent[];
  shippingAddressPrimary?: string;
  shippingSecurityNotice?: string;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const TrackingDetailsSection = ({
  trackingNumber = 'MF-7281-902',
  statusLabel,
  deliveryTimeValue = 'Ma, 14:15',
  progressSteps,
  timelineEvents,
  shippingAddressPrimary,
  shippingSecurityNotice,
  className,
}: TrackingDetailsSectionProps) => {
  return (
    <div className={cn('space-y-3', className)}>
      <TrackingProgressCard
        trackingNumber={trackingNumber}
        statusLabel={statusLabel}
        deliveryTimeValue={deliveryTimeValue}
        steps={progressSteps}
      />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <div className="lg:col-span-8 space-y-8">
          <TrackingTimelineCard events={timelineEvents} />
        </div>

        <aside className="lg:col-span-4 space-y-6">
          <ShippingInfoCard addressPrimary={shippingAddressPrimary} securityNotice={shippingSecurityNotice} />
        </aside>
      </div>
    </div>
  );
};

