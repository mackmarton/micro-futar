import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const markerIconUrl = new URL('leaflet/dist/images/marker-icon.png', import.meta.url).toString();
const markerIcon2xUrl = new URL('leaflet/dist/images/marker-icon-2x.png', import.meta.url).toString();
const markerShadowUrl = new URL('leaflet/dist/images/marker-shadow.png', import.meta.url).toString();

const defaultMarkerIcon = L.icon({
  iconUrl: markerIconUrl,
  iconRetinaUrl: markerIcon2xUrl,
  shadowUrl: markerShadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

export type MapCoordinate = {
  latitude: number;
  longitude: number;
};

type DepoLocationMapPickerProps = {
  center: MapCoordinate;
  markerPosition: MapCoordinate;
  onMarkerChange: (value: MapCoordinate) => void;
};

export const DepoLocationMapPicker = ({
  center,
  markerPosition,
  onMarkerChange,
}: DepoLocationMapPickerProps) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<L.Map | null>(null);
  const markerRef = useRef<L.Marker | null>(null);

  useEffect(() => {
    if (!mapContainerRef.current || mapRef.current) {
      return;
    }

    const map = L.map(mapContainerRef.current, {
      zoomControl: true,
      attributionControl: true,
    }).setView([center.latitude, center.longitude], 15);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(map);

    const marker = L.marker([markerPosition.latitude, markerPosition.longitude], {
      icon: defaultMarkerIcon,
      draggable: true,
    }).addTo(map);

    marker.on('dragend', () => {
      const updatedPosition = marker.getLatLng();
      onMarkerChange({
        latitude: updatedPosition.lat,
        longitude: updatedPosition.lng,
      });
    });

    map.on('click', (event: L.LeafletMouseEvent) => {
      marker.setLatLng(event.latlng);
      onMarkerChange({
        latitude: event.latlng.lat,
        longitude: event.latlng.lng,
      });
    });

    mapRef.current = map;
    markerRef.current = marker;

    return () => {
      map.remove();
      mapRef.current = null;
      markerRef.current = null;
    };
  }, [center.latitude, center.longitude, markerPosition.latitude, markerPosition.longitude, onMarkerChange]);

  useEffect(() => {
    if (!mapRef.current || !markerRef.current) {
      return;
    }

    mapRef.current.setView([center.latitude, center.longitude], mapRef.current.getZoom(), {
      animate: true,
    });
    markerRef.current.setLatLng([markerPosition.latitude, markerPosition.longitude]);
  }, [center.latitude, center.longitude, markerPosition.latitude, markerPosition.longitude]);

  return <div ref={mapContainerRef} className="h-[360px] w-full rounded-xl" />;
};

