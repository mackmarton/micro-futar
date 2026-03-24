import { useState, type ChangeEvent, type FormEvent } from 'react';

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

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const TrackingHero = ({
  title = 'Nyomonkövetés',
  subtitle = 'Kísérje figyelemmel küldeménye útját valós időben a felvételtől a sikeres kézbesítésig.',
  placeholder = 'Adja meg a követési számot (pl. MF-7281-902)',
  buttonLabel = 'Keresés',
  value,
  onValueChange,
  onSearch,
  className,
}: TrackingHeroProps) => {
  const [internalValue, setInternalValue] = useState('');
  const inputValue = value ?? internalValue;

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
      <h2 className="font-headline text-4xl font-extrabold tracking-tight text-on-surface mb-4">{title}</h2>
      <p className="text-on-surface-variant mb-8 font-body">{subtitle}</p>

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
          placeholder={placeholder}
          className="block w-full pl-12 pr-32 py-5 bg-surface-container-lowest border-none rounded-xl shadow-sm focus:ring-2 focus:ring-on-primary-container text-on-surface transition-all placeholder:text-outline-variant font-medium"
        />

        <button
          type="submit"
          className="absolute right-2 top-2 bottom-2 px-6 bg-primary text-on-primary rounded-lg font-bold hover:bg-on-primary-container transition-all flex items-center gap-2"
        >
          {buttonLabel}
        </button>
      </form>
    </section>
  );
};

