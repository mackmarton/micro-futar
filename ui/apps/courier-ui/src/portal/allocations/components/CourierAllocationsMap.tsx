import { useEffect, useMemo, useRef, useState } from 'react';
import * as L from 'leaflet';
import { useNavigate } from 'react-router-dom';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';
import 'leaflet/dist/leaflet.css';
import type { CourierAllocation } from '../api/courierAllocationsApi.ts';

type CourierAllocationsMapProps = {
  allocations: CourierAllocation[];
};

const DEFAULT_CENTER: L.LatLngExpression = [47.4979, 19.0402];
const DEFAULT_ZOOM = 11;

const defaultMarkerIcon = L.icon({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41],
});

export const CourierAllocationsMap = ({ allocations }: CourierAllocationsMapProps) => {
  const navigate = useNavigate();
  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<L.Map | null>(null);
  const markerLayerRef = useRef<L.LayerGroup | null>(null);
  const [currentPosition, setCurrentPosition] = useState<{ latitude: number; longitude: number } | null>(null);

  const points = useMemo(
    () =>
      allocations.map((allocation) => ({
        id: `${allocation.assignmentId ?? allocation.shipmentRouteId ?? allocation.shipmentId ?? 'allocation'}-${allocation.parcelNumber}`,
        assignmentId: allocation.assignmentId,
        latitude: allocation.latitude,
        longitude: allocation.longitude,
        popupLabel: `${allocation.assignmentType}: ${allocation.parcelNumber}`,
      })),
    [allocations],
  );

  useEffect(() => {
    if (!mapContainerRef.current || mapRef.current) {
      return;
    }

    const map = L.map(mapContainerRef.current, {
      zoomControl: true,
      attributionControl: true,
    }).setView(DEFAULT_CENTER, DEFAULT_ZOOM);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(map);

    markerLayerRef.current = L.layerGroup().addTo(map);
    mapRef.current = map;

    return () => {
      map.remove();
      markerLayerRef.current = null;
      mapRef.current = null;
    };
  }, []);

  useEffect(() => {
    if (!('geolocation' in navigator)) {
      return;
    }

    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        setCurrentPosition({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        });
      },
      () => {
        // No-op: user may deny location permission.
      },
      {
        enableHighAccuracy: true,
        timeout: 10_000,
        maximumAge: 30_000,
      },
    );

    return () => {
      navigator.geolocation.clearWatch(watchId);
    };
  }, []);

  useEffect(() => {
    if (!mapRef.current || !markerLayerRef.current) {
      return;
    }

    const markerLayer = markerLayerRef.current;
    markerLayer.clearLayers();

    if (points.length === 0 && !currentPosition) {
      mapRef.current.setView(DEFAULT_CENTER, DEFAULT_ZOOM, { animate: true });
      return;
    }

    const bounds = L.latLngBounds([]);

    points.forEach((point) => {
      const marker = L.marker([point.latitude, point.longitude], { icon: defaultMarkerIcon });
      marker.bindPopup(point.popupLabel);
      marker.on('click', () => {
        if (typeof point.assignmentId === 'number') {
          navigate(`/portal/allocated-packages/${point.assignmentId}`);
        }
      });
      marker.addTo(markerLayer);
      bounds.extend([point.latitude, point.longitude]);
    });

    if (currentPosition) {
      const currentPositionMarker = L.circleMarker([currentPosition.latitude, currentPosition.longitude], {
        radius: 8,
        color: '#0c9488',
        weight: 2,
        fillColor: '#6bd8cb',
        fillOpacity: 0.95,
      });
      currentPositionMarker.bindPopup('Jelenlegi pozíció');
      currentPositionMarker.addTo(markerLayer);
      bounds.extend([currentPosition.latitude, currentPosition.longitude]);
    }

    if (bounds.isValid()) {
      mapRef.current.fitBounds(bounds.pad(0.2), { animate: true });
    }
  }, [points, currentPosition, navigate]);

  return (
    <section className="rounded-3xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm md:p-5">
      <h2 className="text-xl font-headline font-bold text-on-surface">Térkép nézet</h2>
      <div ref={mapContainerRef} className="relative z-0 mt-4 h-[460px] w-full rounded-2xl" />
    </section>
  );
};
