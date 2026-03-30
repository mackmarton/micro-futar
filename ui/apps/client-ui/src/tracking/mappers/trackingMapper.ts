import type { TrackingDTO, TrackingPartDTO } from '@package/shared-core/api/TrackingApiClient';
import type { TrackingProgressStep } from '../components/TrackingProgressCard.tsx';
import type { TrackingTimelineEvent } from '../components/TrackingTimelineCard.tsx';

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

const formatTimestamp = (value?: string) => {
  if (!value) {
    return 'Ismeretlen időpont';
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('hu-HU', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

const toTimelineEvent = (part: TrackingPart, index: number): TrackingTimelineEvent => ({
  title: part.place ? `${part.title} (${part.place})` : part.title,
  timestamp: formatTimestamp(part.time),
  description: part.destination
    ? 'A csomag megérkezett a célállomásra.'
    : 'A csomag feldolgozása folyamatban van ezen az állomáson.',
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
    statusLabel: destinationPart ? 'Kézbesítve' : 'Szállítás alatt',
    deliveryTimeValue: formatTimestamp(destinationPart?.time ?? latestPart?.time),
    progressSteps: orderedByTimeAsc.map((part, index) => toProgressStep(part, index, orderedByTimeAsc.length)),
    timelineEvents: orderedByTimeDesc.map(toTimelineEvent),
    shippingAddressPrimary: destinationPart?.place ?? latestPart?.place,
  };
};

