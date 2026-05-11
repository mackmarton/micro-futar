import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';

type ManifestShipment = {
  parcelNumber: string;
  recipient: string;
  packageSize: string;
};

const shipments: ManifestShipment[] = [
  {
    parcelNumber: 'PK-2841',
    recipient: 'Northline Medical Labs',
    packageSize: 'S',
  },
  {
    parcelNumber: 'PK-2842',
    recipient: 'Central Pharmacy Hub',
    packageSize: 'XS',
  },
  {
    parcelNumber: 'PK-2843',
    recipient: 'Riverside Diagnostics',
    packageSize: 'M',
  }
];

const columns: DataTableColumn<ManifestShipment>[] = [
  {
    id: 'parcelNumber',
    header: 'Csomagszám',
    mobileLabel: 'Csomagszám',
    cell: (shipment) => shipment.parcelNumber,
  },
  {
    id: 'recipient',
    header: 'Címzett',
    mobileLabel: 'Címzett',
    cell: (shipment) => shipment.recipient,
  },
  {
    id: 'packageSize',
    header: 'Csomagméret',
    mobileLabel: 'Csomagméret',
    cell: (shipment) => shipment.packageSize,
  },
  {
    id: 'action',
    header: 'Művelet',
    cell: () => (
      <button
        type="button"
        className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-on-primary transition-all duration-200 hover:bg-[linear-gradient(95deg,#000000_0%,#0c9488_100%)]"
      >
        Megnyitás
      </button>
    ),
  },
];

export const ManifestDataTable = () => {
  return (
    <DataTable
      data={shipments}
      rowKey={(shipment) => shipment.parcelNumber}
      title="Szállítási jegyzék"
      columns={columns}
      emptyMessage="Nincs küldemény a megadott feltételek alapján."
      mobileCardEyebrow="Küldemény"
      recordCountLabel={(visible, total) => `Látható küldemények: ${visible} / ${total}`}
      renderMobileActions={() => (
        <button
          type="button"
          className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-on-primary"
        >
          Megnyitás
        </button>
      )}
    />
  );
};
