import { PrecisionInput } from '@package/shared-ui';

export type AddressCardField = 'name' | 'phone' | 'email' | 'zipCode' | 'country' | 'city' | 'address';

export type AddressCardValue = Record<AddressCardField, string>;

export type AddressCardProps = {
  title: string;
  iconName: string;
  value: AddressCardValue;
  onChange: (field: AddressCardField, fieldValue: string) => void;
};

export const AddressCard = ({ title, iconName, value, onChange }: AddressCardProps) => {
  return (
    <section className="bg-surface-container-low rounded-xl p-8 space-y-6">
      <div className="flex items-center gap-3 text-on-primary-container">
        <span className="material-symbols-outlined text-teal-600">{iconName}</span>
        <h2 className="text-xl font-bold tracking-tight">{title}</h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <PrecisionInput label="Név" value={value.name} onChange={(event) => onChange('name', event.target.value)} />
        <PrecisionInput
          label="Telefonszám"
          type="tel"
          value={value.phone}
          onChange={(event) => onChange('phone', event.target.value)}
        />
        <PrecisionInput
          label="Email cím"
          type="email"
          value={value.email}
          onChange={(event) => onChange('email', event.target.value)}
          wrapperClassName="md:col-span-2"
        />

        <div className="grid grid-cols-3 gap-4 md:col-span-2">
          <PrecisionInput
            label="Irányítószám"
            value={value.zipCode}
            onChange={(event) => onChange('zipCode', event.target.value)}
          />
          <PrecisionInput
            label="Ország"
            value={value.country}
            onChange={(event) => onChange('country', event.target.value)}
            wrapperClassName="col-span-2"
          />
        </div>

        <PrecisionInput label="Város" value={value.city} onChange={(event) => onChange('city', event.target.value)} />
        <PrecisionInput
          label="Cím"
          value={value.address}
          onChange={(event) => onChange('address', event.target.value)}
        />
      </div>
    </section>
  );
};



