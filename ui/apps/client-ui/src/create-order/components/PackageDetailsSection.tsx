import { FormSection } from '@package/shared-ui';
import type { PackageSizeOption } from '../api/ordersApi.ts';

export type PackageSizeId = number;

export type PackageDetailsValue = {
  sizeId: PackageSizeId;
  weight: string;
  description: string;
};

export type PackageDetailsSectionProps = {
  value: PackageDetailsValue;
  sizeOptions: PackageSizeOption[];
  isSizeLoading: boolean;
  isPackageSizeEnabled?: (sizeId: PackageSizeId) => boolean;
  sizeAvailabilityHint?: string | null;
  onSizeChange?: (size: PackageSizeId) => void;
  onWeightChange?: (weight: string) => void;
  onDescriptionChange?: (description: string) => void;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const labelClassName = 'text-xs font-bold uppercase tracking-widest text-on-surface-variant block';

export const PackageDetailsSection = ({
  value,
  sizeOptions,
  isSizeLoading,
  isPackageSizeEnabled,
  sizeAvailabilityHint,
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

          {isSizeLoading ? (
            <p className="text-sm text-on-surface-variant">Csomagméretek betöltése...</p>
          ) : sizeOptions.length === 0 ? (
            <p className="text-sm text-on-surface-variant">Nincsenek elérhető csomagméretek.</p>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
              {sizeOptions.map((option) => {
                const isActive = value.sizeId === option.id;
                const isEnabled = isPackageSizeEnabled?.(option.id) ?? true;

                return (
                  <button
                    key={option.id}
                    type="button"
                    onClick={() => onSizeChange?.(option.id)}
                    disabled={!isEnabled}
                    className={cn(
                      'flex flex-col items-center justify-center p-6 rounded-xl border-2 transition-all group',
                      !isEnabled && 'cursor-not-allowed opacity-50',
                      isActive
                        ? 'bg-white border-surface-tint'
                        : 'bg-surface-container-lowest hover:bg-primary-fixed border-transparent'
                    )}
                    aria-pressed={isActive}
                  >
                    <span className="text-2xl font-bold mb-1 text-on-surface">{option.name}</span>
                    <span className="text-[10px] text-on-surface-variant group-hover:text-primary">Maximum oldalhossz:<br/>{option.maxLength} cm</span>
                  </button>
                );
              })}
            </div>
          )}
          {sizeAvailabilityHint ? (
            <p className="mt-3 text-xs text-on-surface-variant">{sizeAvailabilityHint}</p>
          ) : null}
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


