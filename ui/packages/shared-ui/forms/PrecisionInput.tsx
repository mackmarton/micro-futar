import type { InputHTMLAttributes } from 'react';

export type PrecisionInputProps = {
  label: string;
  value: string | number;
  className?: string;
  wrapperClassName?: string;
} & Pick<InputHTMLAttributes<HTMLInputElement>, 'placeholder' | 'type' | 'name' | 'id' | 'onChange' | 'onBlur' | 'disabled' | 'required' | 'autoComplete'>;

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const PrecisionInput = ({
  label,
  placeholder,
  type = 'text',
  value,
  className,
  wrapperClassName,
  id,
  onChange,
  onBlur,
  name,
  disabled,
  required,
  autoComplete,
}: PrecisionInputProps) => {
  const inputId = id ?? name ?? label.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className={wrapperClassName}>
      <label
        htmlFor={inputId}
        className="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-3 block"
      >
        {label}
      </label>
      <input
        id={inputId}
        name={name}
        type={type}
        value={value}
        placeholder={placeholder}
        onChange={onChange}
        onBlur={onBlur}
        disabled={disabled}
        required={required}
        autoComplete={autoComplete}
        className={cn(
          'w-full bg-surface-container-lowest border-none rounded-lg p-4',
          'focus:ring-0 border-b-2 border-transparent focus:border-surface-tint transition-all',
          className
        )}
      />
    </div>
  );
};

