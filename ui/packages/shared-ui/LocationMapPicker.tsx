import { useEffect, useRef } from 'react';
import * as L from 'leaflet';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';
import 'leaflet/dist/leaflet.css';

// Vite resolves image imports to a URL string, Next.js to a StaticImageData object.
const assetUrl = (asset: string | { src: string }) => (typeof asset === 'string' ? asset : asset.src);

// Leaflet's default icon path detection breaks once bundlers hash the image file names.
const defaultMarkerIcon = L.icon({
  iconRetinaUrl: assetUrl(markerIcon2x),
  iconUrl: assetUrl(markerIcon),
  shadowUrl: assetUrl(markerShadow),
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41],
});

export type MapCoordinate = {
  latitude: number;
  longitude: number;
};

export type MapBounds = {
  minLatitude: number;
  minLongitude: number;
  maxLatitude: number;
  maxLongitude: number;
};

export type LocationMapPickerProps = {
  center: MapCoordinate;
  markerPosition: MapCoordinate;
  onMarkerChange: (value: MapCoordinate) => void;
  /** Initial zoom level, default 15 (street level). */
  zoom?: number;
  /** Optional rectangle drawn on the map (display only, not editable on the map). */
  bounds?: MapBounds | null;
};

export const LocationMapPicker = ({
  center,
  markerPosition,
  onMarkerChange,
  zoom = 15,
  bounds = null,
}: LocationMapPickerProps) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<L.Map | null>(null);
  const markerRef = useRef<L.Marker | null>(null);
  const rectangleRef = useRef<L.Rectangle | null>(null);
  const onMarkerChangeRef = useRef(onMarkerChange);

  useEffect(() => {
    onMarkerChangeRef.current = onMarkerChange;
  }, [onMarkerChange]);

  useEffect(() => {
    if (!mapContainerRef.current || mapRef.current) {
      return;
    }

    const map = L.map(mapContainerRef.current, {
      zoomControl: true,
      attributionControl: true,
    }).setView([center.latitude, center.longitude], zoom);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(map);

    const marker = L.marker([markerPosition.latitude, markerPosition.longitude], {
      draggable: true,
      icon: defaultMarkerIcon,
    }).addTo(map);

    marker.on('dragend', () => {
      const updatedPosition = marker.getLatLng();
      onMarkerChangeRef.current({
        latitude: updatedPosition.lat,
        longitude: updatedPosition.lng,
      });
    });

    map.on('click', (event: L.LeafletMouseEvent) => {
      marker.setLatLng(event.latlng);
      onMarkerChangeRef.current({
        latitude: event.latlng.lat,
        longitude: event.latlng.lng,
      });
    });

    mapRef.current = map;
    markerRef.current = marker;

    return () => {
      marker.off();
      map.off();
      map.remove();
      mapRef.current = null;
      markerRef.current = null;
      rectangleRef.current = null;
    };
    // The map is created once; later center/marker/bounds changes are applied by the effects below.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const minLatitude = bounds?.minLatitude;
  const minLongitude = bounds?.minLongitude;
  const maxLatitude = bounds?.maxLatitude;
  const maxLongitude = bounds?.maxLongitude;

  useEffect(() => {
    const map = mapRef.current;
    if (!map) {
      return;
    }

    rectangleRef.current?.remove();
    rectangleRef.current = null;

    if (
      minLatitude === undefined ||
      minLongitude === undefined ||
      maxLatitude === undefined ||
      maxLongitude === undefined
    ) {
      return;
    }

    rectangleRef.current = L.rectangle(
      [
        [minLatitude, minLongitude],
        [maxLatitude, maxLongitude],
      ],
      { color: '#2563eb', weight: 2, fillOpacity: 0.08, interactive: false },
    ).addTo(map);
  }, [minLatitude, minLongitude, maxLatitude, maxLongitude]);

  useEffect(() => {
    if (!mapRef.current || !markerRef.current) {
      return;
    }

    mapRef.current.setView([center.latitude, center.longitude], mapRef.current.getZoom(), {
      animate: true,
    });
    markerRef.current.setLatLng([markerPosition.latitude, markerPosition.longitude]);
  }, [center.latitude, center.longitude, markerPosition.latitude, markerPosition.longitude]);

  return <div ref={mapContainerRef} className="relative z-0 h-[360px] w-full rounded-xl" />;
};
