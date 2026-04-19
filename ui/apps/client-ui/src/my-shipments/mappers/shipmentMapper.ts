import type { ShipmentDTO } from '@package/shared-core/api/OrdersApiClient';
import type { Shipment, ShipmentStatus } from '../components/ShipmentTable.tsx';
import type { ShipmentStatsObject } from '../components/ShipmentStats.tsx';

const UNKNOWN_DESTINATION = 'Ismeretlen célállomás';
const UNKNOWN_DATE = '-';

const formatDestination = (shipment: ShipmentDTO) => {
  const segments = [shipment.recipientZip, shipment.recipientAddress]
    .map((value) => (typeof value === 'string' ? value.trim() : ''))
    .filter((value) => value.length > 0);

  return segments.length > 0 ? segments.join(', ') : UNKNOWN_DESTINATION;
};

const getStatus = (shipment: ShipmentDTO): ShipmentStatus => {
  if (shipment.delivered === true) {
    return 'delivered';
  }

  return 'inProgress';
};

export const mapShipmentDtoToShipment = (shipment: ShipmentDTO, index: number): Shipment => {
  const stableFallbackId = shipment.id ?? index + 1;
  const id = shipment.parcelNumber?.trim() || `#HU-${stableFallbackId}`;
  const status = getStatus(shipment);

  return {
    id,
    createdAt: UNKNOWN_DATE,
    destination: formatDestination(shipment),
    status,
  };
};

export const mapShipmentDtosToShipments = (shipments: ShipmentDTO[]): Shipment[] => {
  return shipments.map(mapShipmentDtoToShipment);
};

export const buildShipmentStats = (shipments: Shipment[]): ShipmentStatsObject => {
  return shipments.reduce(
    (acc, shipment) => {
      if (shipment.status === 'inProgress') {
        acc.inProgress += 1;
      }

      if (shipment.status === 'delivered') {
        acc.delivered += 1;
      }

      return acc;
    },
    {
      inProgress: 0,
      delivered: 0,
    },
  );
};

