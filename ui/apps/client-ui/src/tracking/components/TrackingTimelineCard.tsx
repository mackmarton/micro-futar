import type { ReactNode } from 'react';

export type TrackingTimelineStatus = 'completed' | 'previous';

export type TrackingTimelineEvent = {
  title: string;
  timestamp: string;
  description: string;
  icon?: ReactNode | string;
  status?: TrackingTimelineStatus;
};

export type TrackingTimelineCardProps = {
  title?: string;
  events?: TrackingTimelineEvent[];
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const renderIcon = (icon: ReactNode | string) => {
  if (typeof icon === 'string') {
    return (
      <span className="material-symbols-outlined text-[10px]" aria-hidden="true">
        {icon}
      </span>
    );
  }

  return icon;
};

const defaultEvents: TrackingTimelineEvent[] = [
  {
    title: 'Kiszállítva (Budapest, XI.)',
    timestamp: '2024. okt. 24. 14:15',
    description: 'A csomag sikeresen átadva a címzettnek vagy a megjelölt helyen hagyva.',
    icon: 'done_all',
    status: 'completed',
  },
  {
    title: 'Futárnál: Kiszállítás alatt',
    timestamp: '2024. okt. 24. 11:30',
    description: 'A futár felrakodta a csomagot és megkezdte a kézbesítési kört.',
    icon: 'local_shipping',
    status: 'completed',
  },
  {
    title: 'Beérkezett a depóba (Budapest)',
    timestamp: '2024. okt. 24. 08:45',
    description: 'A küldemény feldolgozása megtörtént a helyi elosztó központban.',
    icon: 'warehouse',
    status: 'previous',
  },
  {
    title: 'Felvéve a feladótól',
    timestamp: '2024. okt. 23. 17:45',
    description: 'A futár átvette a csomagot a partnerünktől.',
    icon: 'hail',
    status: 'previous',
  },
];

type TimelineItemProps = {
  event: TrackingTimelineEvent;
};

const TimelineItem = ({ event }: TimelineItemProps) => {
  const isCompleted = event.status !== 'previous';

  return (
    <div className="relative">
      <div
        className={cn(
          'absolute -left-[41px] top-0 w-5 h-5 rounded-full ring-4 ring-white shadow-sm flex items-center justify-center',
          isCompleted ? 'bg-teal-600 text-white' : 'bg-surface-container text-outline'
        )}
      >
        {renderIcon(event.icon ?? (isCompleted ? 'done_all' : 'radio_button_unchecked'))}
      </div>

      <div>
        <p className="text-sm font-bold text-on-surface">{event.title}</p>
        <p className="text-xs text-on-surface-variant font-medium mt-1">{event.timestamp}</p>
        <p className="text-sm text-on-surface-variant mt-2">{event.description}</p>
      </div>
    </div>
  );
};

export const TrackingTimelineCard = ({
  title = 'Részletes eseménytörténet',
  events = defaultEvents,
  className,
}: TrackingTimelineCardProps) => {
  return (
    <section className={cn('bg-surface-container-lowest p-8 rounded-xl shadow-sm', className)}>
      <h3 className="text-xl font-bold mb-8 text-on-surface flex items-center gap-2">
        <span className="material-symbols-outlined text-teal-600" aria-hidden="true">
          history
        </span>
        {title}
      </h3>

      <div className="relative ml-4 pl-8 border-l-2 border-surface-container space-y-12">
        {events.map((event, index) => (
          <TimelineItem key={`${event.timestamp}-${index}`} event={event} />
        ))}
      </div>
    </section>
  );
};


