import type { TrackingDTO, TrackingPartDTO } from '@package/shared-core/api/TrackingApiClient';
import type { TrackingProgressStep } from '../components/TrackingProgressCard.tsx';
import type { TrackingTimelineEvent } from '../components/TrackingTimelineCard.tsx';
import type { AppLocale } from '../../i18n/createI18nInstance.ts';

/**
 * A `part.title`/`part.place` a trackingApi válaszából jön (a logisztikai szolgáltatás szabad
 * szövegként adja vissza, pl. "Felvéve a feladótól") – ez backend-oldali, szabadszöveges adat,
 * amit a frontend i18n nem tud lefordítani. Ehhez a backendnek kellene stabil státuszkódot
 * (enum) küldenie a jelenlegi szöveg helyett, amit a frontend aztán maga fordítana.
 */
export type TrackingDetailsFormatters = {
  t: (key: string) => string;
  locale: AppLocale;
};

const DATE_LOCALE_BY_APP_LOCALE: Record<AppLocale, string> = {
  hu: 'hu-HU',
  en: 'en-GB',
};

export type TrackingDetailsViewModel = {
  trackingNumber: string;
  statusLabel: string;
  deliveryTimeValue: string;
  progressSteps: TrackingProgressStep[];
  timelineEvents: TrackingTimelineEvent[];
  shippingAddressPrimary?: string;
};

type TrackingPart = {
  title: string;
  place?: string;
  time?: string;
  destination: boolean;
};

const parseTrackingParts = (trackingDto: TrackingDTO): TrackingPart[] => {
  return Object.entries(trackingDto.trackingParts ?? {})
    .map(([title, part]: [string, TrackingPartDTO]) => ({
      title,
      place: part.place,
      time: part.time,
      destination: Boolean(part.destination),
    }))
    .filter((part) => part.title.trim().length > 0 || Boolean(part.time) || Boolean(part.place));
};

const getUnixTime = (value?: string) => {
  if (!value) {
    return Number.NEGATIVE_INFINITY;
  }

  const timestamp = new Date(value).getTime();
  return Number.isNaN(timestamp) ? Number.NEGATIVE_INFINITY : timestamp;
};

const formatTimestamp = (value: string | undefined, formatters: TrackingDetailsFormatters) => {
  if (!value) {
    return formatters.t('progress.unknownTime');
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat(DATE_LOCALE_BY_APP_LOCALE[formatters.locale], {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

const toTimelineEvent = (
  part: TrackingPart,
  index: number,
  formatters: TrackingDetailsFormatters,
): TrackingTimelineEvent => ({
  title: part.place ? `${part.title} (${part.place})` : part.title,
  timestamp: formatTimestamp(part.time, formatters),
  description: part.destination
    ? formatters.t('timeline.arrivedDescription')
    : formatters.t('timeline.inProgressDescription'),
  icon: part.destination ? 'inventory_2' : 'local_shipping',
  status: index === 0 ? 'completed' : 'previous',
});

const toProgressStep = (part: TrackingPart, index: number, total: number): TrackingProgressStep => ({
  label: part.title,
  completed: index < total - 1,
  isCurrent: index === total - 1,
  icon: index === total - 1 ? 'inventory_2' : 'check',
});

export const mapTrackingDtoToDetails = (
  trackingDto: TrackingDTO | null,
  trackingNumber: string,
  formatters: TrackingDetailsFormatters,
): TrackingDetailsViewModel | null => {
  if (!trackingDto) {
    return null;
  }

  const parts = parseTrackingParts(trackingDto);

  if (parts.length === 0) {
    return null;
  }

  const orderedByTimeAsc = [...parts].sort((left, right) => getUnixTime(left.time) - getUnixTime(right.time));
  const orderedByTimeDesc = [...orderedByTimeAsc].reverse();

  const latestPart = orderedByTimeDesc[0];
  const destinationPart = orderedByTimeDesc.find((part) => part.destination);

  return {
    trackingNumber,
    statusLabel: destinationPart ? formatters.t('progress.deliveredStatus') : formatters.t('progress.inTransitStatus'),
    deliveryTimeValue: formatTimestamp(destinationPart?.time ?? latestPart?.time, formatters),
    progressSteps: orderedByTimeAsc.map((part, index) => toProgressStep(part, index, orderedByTimeAsc.length)),
    timelineEvents: orderedByTimeDesc.map((part, index) => toTimelineEvent(part, index, formatters)),
    shippingAddressPrimary: destinationPart?.place ?? latestPart?.place,
  };
};

