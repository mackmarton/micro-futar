import { useEffect, useRef } from 'react';
import * as L from 'leaflet';
import 'leaflet/dist/leaflet.css';

export type MapCoordinate = {
  latitude: number;
  longitude: number;
};

export type LocationMapPickerProps = {
  center: MapCoordinate;
  markerPosition: MapCoordinate;
  onMarkerChange: (value: MapCoordinate) => void;
};

export const LocationMapPicker = ({
  center,
  markerPosition,
  onMarkerChange,
}: LocationMapPickerProps) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<L.Map | null>(null);
  const markerRef = useRef<L.Marker | null>(null);
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
    }).setView([center.latitude, center.longitude], 15);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(map);

    const marker = L.marker([markerPosition.latitude, markerPosition.longitude], {
      draggable: true,
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
    };
  }, []);

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
