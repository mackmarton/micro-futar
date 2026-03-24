import { CourierCard } from './CourierCard';
import { ShippingInfoCard } from './ShippingInfoCard';
import { TrackingHero } from './TrackingHero';
import { TrackingProgressCard } from './TrackingProgressCard';
import { TrackingTimelineCard } from './TrackingTimelineCard';

export type TrackPackagePageProps = {
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const TrackPackagePage = ({ className }: TrackPackagePageProps) => {
  return (
    <div className={cn('max-w-7xl mx-auto px-6 py-8 md:p-12', className)}>
      <TrackingHero />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <div className="lg:col-span-8 space-y-8">
          <TrackingProgressCard trackingNumber="MF-7281-902" deliveryTimeValue="Ma, 14:15" />
          <TrackingTimelineCard />
        </div>

        <aside className="lg:col-span-4 space-y-6">
          <CourierCard />
          <ShippingInfoCard />
        </aside>
      </div>
    </div>
  );
};

