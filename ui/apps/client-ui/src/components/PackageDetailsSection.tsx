import { FormSection } from '@package/shared-ui';

export type PackageSize = 'S' | 'M' | 'L' | 'XL';

export type PackageDetailsValue = {
  size: PackageSize;
  weight: string;
  description: string;
};

export type PackageDetailsSectionProps = {
  value: PackageDetailsValue;
  onSizeChange?: (size: PackageSize) => void;
  onWeightChange?: (weight: string) => void;
  onDescriptionChange?: (description: string) => void;
  className?: string;
};

const SIZE_OPTIONS: Array<{ size: PackageSize; label: string }> = [
  { size: 'S', label: 'Levél méret' },
  { size: 'M', label: 'Cipős doboz' },
  { size: 'L', label: 'Közepes doboz' },
  { size: 'XL', label: 'Nagyobb tárgy' },
];

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const labelClassName = 'text-xs font-bold uppercase tracking-widest text-on-surface-variant block';

export const PackageDetailsSection = ({
  value,
  onSizeChange,
  onWeightChange,
  onDescriptionChange,
  className,
}: PackageDetailsSectionProps) => {
  return (
    <FormSection icon="inventory_2" title="Csomag adatai" className={className}>
      <div className="space-y-8">
        <div>
          <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant block mb-4">
            Csomag mérete
          </label>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {SIZE_OPTIONS.map((option) => {
              const isActive = value.size === option.size;

              return (
                <button
                  key={option.size}
                  type="button"
                  onClick={() => onSizeChange?.(option.size)}
                  className={cn(
                    'flex flex-col items-center justify-center p-6 rounded-xl border-2 transition-all group',
                    isActive
                      ? 'bg-white border-surface-tint'
                      : 'bg-surface-container-lowest hover:bg-primary-fixed border-transparent'
                  )}
                  aria-pressed={isActive}
                >
                  <span className="text-2xl font-bold mb-1 text-on-surface">{option.size}</span>
                  <span className="text-[10px] text-on-surface-variant group-hover:text-primary">{option.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="space-y-4">
            <label className={labelClassName}>Súly (kg)</label>
            <div className="relative">
              <input
                type="number"
                value={value.weight}
                onChange={(event) => onWeightChange?.(event.target.value)}
                className="w-full bg-surface-container-lowest border-none rounded-lg p-4 pr-12 focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all"
              />
              <span className="absolute right-4 top-4 text-outline font-bold">kg</span>
            </div>
          </div>

          <div className="space-y-4">
            <label className={labelClassName}>Csomag leírása</label>
            <input
              type="text"
              placeholder="Például: Törékeny elektronika"
              value={value.description}
              onChange={(event) => onDescriptionChange?.(event.target.value)}
              className="w-full bg-surface-container-lowest border-none rounded-lg p-4 focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all"
            />
          </div>
        </div>
      </div>
    </FormSection>
  );
};


