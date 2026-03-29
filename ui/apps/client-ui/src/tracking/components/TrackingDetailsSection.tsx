import { ShippingInfoCard } from './ShippingInfoCard.tsx';
import { TrackingProgressCard } from './TrackingProgressCard.tsx';
import { TrackingTimelineCard } from './TrackingTimelineCard.tsx';

export type TrackingDetailsSectionProps = {
  trackingNumber?: string;
  deliveryTimeValue?: string;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const TrackingDetailsSection = ({
  trackingNumber = 'MF-7281-902',
  className,
}: TrackingDetailsSectionProps) => {
  return (
    <div className={cn('space-y-3', className)}>
      <TrackingProgressCard trackingNumber={trackingNumber} />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <div className="lg:col-span-8 space-y-8">
          <TrackingTimelineCard />
        </div>

        <aside className="lg:col-span-4 space-y-6">
          <ShippingInfoCard />
        </aside>
      </div>
    </div>
  );
};

