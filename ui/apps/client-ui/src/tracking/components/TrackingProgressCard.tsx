import type { ReactNode } from 'react';

export type TrackingProgressStep = {
  label: string;
  completed?: boolean;
  isCurrent?: boolean;
  icon?: ReactNode | string;
};

export type TrackingProgressCardProps = {
  trackingNumber: string;
  statusLabel?: string;
  deliveryTimeLabel?: string;
  deliveryTimeValue?: string;
  steps?: TrackingProgressStep[];
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const renderIcon = (icon: ReactNode | string) => {
  if (typeof icon === 'string') {
    return (
      <span className="material-symbols-outlined text-sm" aria-hidden="true">
        {icon}
      </span>
    );
  }

  return icon;
};

const defaultSteps: TrackingProgressStep[] = [
  { label: 'Felvéve', completed: true, icon: 'check' },
  { label: 'Átszállítva', completed: true, icon: 'check' },
  { label: 'Futárnál', completed: true, icon: 'check' },
  { label: 'Kiszállítva', completed: true, isCurrent: true, icon: 'inventory_2' },
];

const getProgressWidth = (steps: TrackingProgressStep[]) => {
  if (steps.length <= 1) return 0;

  const lastCompletedIndex = Math.max(
    ...steps.map((step, index) => (step.completed || step.isCurrent ? index : -1))
  );

  if (lastCompletedIndex < 0) return 0;
  return (lastCompletedIndex / (steps.length - 1)) * 100;
};

export const TrackingProgressCard = ({
  trackingNumber,
  statusLabel = 'Kézbesítve',
  deliveryTimeLabel = 'Kézbesítés időpontja',
  deliveryTimeValue,
  steps = defaultSteps,
  className,
}: TrackingProgressCardProps) => {
  const progressWidth = getProgressWidth(steps);

  return (
    <section className={cn('bg-surface-container-lowest p-8 rounded-xl border-none shadow-sm', className)}>
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-10 gap-4">
        <div>
          <span className="text-xs font-bold uppercase tracking-widest text-on-primary-container bg-primary-fixed px-3 py-1 rounded-full">
            {statusLabel}
          </span>
          <h3 className="text-2xl font-bold mt-2 text-on-surface">{trackingNumber}</h3>
        </div>

        {deliveryTimeValue && (
          <div className="text-right">
            <p className="text-xs uppercase tracking-widest text-on-surface-variant font-medium">{deliveryTimeLabel}</p>
            <p className="text-xl font-bold text-teal-600">{deliveryTimeValue}</p>
          </div>
        )}
      </div>

      <div className="relative pt-8 pb-12">
        <div className="absolute top-12 left-0 w-full h-1 bg-surface-container rounded-full overflow-hidden">
          <div
            className="absolute left-0 top-0 h-full bg-gradient-to-r from-teal-600 to-teal-400"
            style={{ width: `${progressWidth}%` }}
          />
        </div>

        <div className="relative flex justify-between w-full">
          {steps.map((step, index) => {
            const isActive = Boolean(step.completed || step.isCurrent);

            return (
              <div key={`${step.label}-${index}`} className="flex flex-col items-center text-center">
                <div
                  className={cn(
                    'w-8 h-8 rounded-full flex items-center justify-center z-10 mb-4 ring-8 ring-surface-container-lowest',
                    isActive ? 'bg-teal-600 text-white shadow-lg' : 'bg-surface-container text-outline'
                  )}
                >
                  {renderIcon(step.icon ?? (isActive ? 'check' : 'radio_button_unchecked'))}
                </div>
                <p className={cn('text-sm font-bold', isActive ? 'text-on-surface' : 'text-on-surface-variant')}>
                  {step.label}
                </p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};

