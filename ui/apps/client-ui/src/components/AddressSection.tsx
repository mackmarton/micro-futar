import { FormSection, PrecisionInput } from '@package/shared-ui';

type AddressFields = {
  city: string;
  street: string;
  houseNumber: string;
  postalCode: string;
};

export type AddressSectionValue = {
  pickup: AddressFields;
  recipient: AddressFields;
};

type AddressTarget = keyof AddressSectionValue;
type AddressField = keyof AddressFields;

export type AddressSectionProps = {
  value?: AddressSectionValue;
  onChange?: (target: AddressTarget, field: AddressField, fieldValue: string) => void;
  className?: string;
};

const EMPTY_ADDRESS: AddressFields = {
  city: '',
  street: '',
  houseNumber: '',
  postalCode: '',
};

const EMPTY_VALUE: AddressSectionValue = {
  pickup: EMPTY_ADDRESS,
  recipient: EMPTY_ADDRESS,
};

const labelClassName = 'text-xs font-bold uppercase tracking-widest text-on-surface-variant';

export const AddressSection = ({ value = EMPTY_VALUE, onChange, className }: AddressSectionProps) => {
  const handleFieldChange = (target: AddressTarget, field: AddressField) => (nextValue: string | number) => {
    onChange?.(target, field, String(nextValue));
  };

  return (
    <FormSection icon="location_on" title="Szállítási címek" className={className}>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
        <div className="space-y-4">
          <label className={labelClassName}>Felvételi cím (Honnan)</label>
          <div className="space-y-3">
            <PrecisionInput
              label="Város"
              placeholder="Város"
              value={value.pickup.city}
              onChange={(event) => handleFieldChange('pickup', 'city')(event.target.value)}
            />

            <div className="grid grid-cols-3 gap-2">
              <div className="col-span-2">
                <PrecisionInput
                  label="Utca"
                  placeholder="Utca"
                  value={value.pickup.street}
                  onChange={(event) => handleFieldChange('pickup', 'street')(event.target.value)}
                />
              </div>

              <PrecisionInput
                label="Házszám"
                placeholder="Házszám"
                value={value.pickup.houseNumber}
                onChange={(event) => handleFieldChange('pickup', 'houseNumber')(event.target.value)}
              />
            </div>

            <PrecisionInput
              label="Irányítószám"
              placeholder="Irányítószám"
              value={value.pickup.postalCode}
              onChange={(event) => handleFieldChange('pickup', 'postalCode')(event.target.value)}
            />
          </div>
        </div>

        <div className="space-y-4">
          <label className={labelClassName}>Címzett címe (Hova)</label>
          <div className="space-y-3">
            <PrecisionInput
              label="Város"
              placeholder="Város"
              value={value.recipient.city}
              onChange={(event) => handleFieldChange('recipient', 'city')(event.target.value)}
            />

            <div className="grid grid-cols-3 gap-2">
              <div className="col-span-2">
                <PrecisionInput
                  label="Utca"
                  placeholder="Utca"
                  value={value.recipient.street}
                  onChange={(event) => handleFieldChange('recipient', 'street')(event.target.value)}
                />
              </div>

              <PrecisionInput
                label="Házszám"
                placeholder="Házszám"
                value={value.recipient.houseNumber}
                onChange={(event) => handleFieldChange('recipient', 'houseNumber')(event.target.value)}
              />
            </div>

            <PrecisionInput
              label="Irányítószám"
              placeholder="Irányítószám"
              value={value.recipient.postalCode}
              onChange={(event) => handleFieldChange('recipient', 'postalCode')(event.target.value)}
            />
          </div>
        </div>
      </div>
    </FormSection>
  );
};

