import { useState, type ChangeEvent, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { cn } from '@package/shared-ui';

export type TrackingHeroProps = {
  title?: string;
  subtitle?: string;
  placeholder?: string;
  buttonLabel?: string;
  value?: string;
  onValueChange?: (value: string) => void;
  onSearch?: (trackingCode: string) => void;
  className?: string;
};

export const TrackingHero = ({
  title,
  subtitle,
  placeholder,
  buttonLabel,
  value,
  onValueChange,
  onSearch,
  className,
}: TrackingHeroProps) => {
  const { t } = useTranslation('tracking');
  const [internalValue, setInternalValue] = useState('');
  const inputValue = value ?? internalValue;

  const resolvedTitle = title ?? t('hero.title');
  const resolvedSubtitle = subtitle ?? t('hero.subtitle');
  const resolvedPlaceholder = placeholder ?? t('hero.placeholder');
  const resolvedButtonLabel = buttonLabel ?? t('hero.button');

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const nextValue = event.target.value;
    if (value === undefined) {
      setInternalValue(nextValue);
    }
    onValueChange?.(nextValue);
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSearch?.(inputValue.trim());
  };

  return (
    <section className={cn('text-center max-w-2xl mx-auto mb-12', className)}>
      <h2 className="font-headline text-4xl font-extrabold tracking-tight text-on-surface mb-4">{resolvedTitle}</h2>
      <p className="text-on-surface-variant mb-8 font-body">{resolvedSubtitle}</p>

      <form className="relative" onSubmit={handleSubmit}>
        <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
          <span className="material-symbols-outlined text-on-surface-variant" aria-hidden="true">
            search
          </span>
        </div>

        <input
          type="text"
          value={inputValue}
          onChange={handleChange}
          placeholder={resolvedPlaceholder}
          className="block w-full pl-12 pr-32 py-5 bg-surface-container-lowest border-none rounded-xl shadow-sm focus:ring-2 focus:ring-on-primary-container text-on-surface transition-all placeholder:text-outline-variant font-medium"
        />

        <button
          type="submit"
          className="absolute right-2 top-2 bottom-2 px-6 bg-primary text-on-primary rounded-lg font-bold hover:bg-on-primary-container transition-all flex items-center gap-2"
        >
          {resolvedButtonLabel}
        </button>
      </form>
    </section>
  );
};

