import { FormSection } from '@package/shared-ui';

export type ShippingSpeed = 'standard' | 'express' | 'priority';
export type PaymentMethod = 'card' | 'cod';

export type ShippingOptionsValue = {
  speed: ShippingSpeed;
  paymentMethod: PaymentMethod;
};

export type ShippingOptionsSectionProps = {
  value: ShippingOptionsValue;
  onSpeedChange?: (speed: ShippingSpeed) => void;
  onPaymentMethodChange?: (method: PaymentMethod) => void;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const labelClassName = 'text-xs font-bold uppercase tracking-widest text-on-surface-variant block';

const SPEED_OPTIONS: Array<{ value: ShippingSpeed; label: string }> = [
  { value: 'standard', label: 'Standard (2-3 munkanap)' },
  { value: 'express', label: 'Expressz (24 órán belül)' },
  { value: 'priority', label: 'Priority (Aznapi kiszállítás)' },
];

const PAYMENT_OPTIONS: Array<{ value: PaymentMethod; label: string }> = [
  { value: 'card', label: 'Bankkártya' },
  { value: 'cod', label: 'Utánvét' },
];

export const ShippingOptionsSection = ({
  value,
  onSpeedChange,
  onPaymentMethodChange,
  className,
}: ShippingOptionsSectionProps) => {
  return (
    <FormSection icon="settings_suggest" title="Szállítási opciók" className={className}>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="space-y-4">
          <label className={labelClassName}>Szállítási sebesség</label>
          <select
            value={value.speed}
            onChange={(event) => onSpeedChange?.(event.target.value as ShippingSpeed)}
            className="w-full bg-surface-container-lowest border-none rounded-lg p-4 focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all appearance-none cursor-pointer"
          >
            {SPEED_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-4">
          <label className={labelClassName}>Fizetési mód</label>
          <div className="flex gap-2 p-1 bg-surface-container-lowest rounded-xl">
            {PAYMENT_OPTIONS.map((option) => {
              const isActive = value.paymentMethod === option.value;

              return (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => onPaymentMethodChange?.(option.value)}
                  className={cn(
                    'flex-1 py-3 px-4 rounded-lg font-bold text-sm transition-colors',
                    isActive ? 'bg-primary text-white' : 'text-on-surface-variant hover:bg-surface-container-high'
                  )}
                  aria-pressed={isActive}
                >
                  {option.label}
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </FormSection>
  );
};

