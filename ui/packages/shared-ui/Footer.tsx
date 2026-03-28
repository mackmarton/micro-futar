import type { ReactNode } from 'react';

export type FooterActionItem = {
  href: string;
  icon: ReactNode | string;
  label: string;
};

export type FooterProps = {
  logoSrc?: string;
  logoAlt?: string;
  brandName?: string;
  copyrightText?: string;
  actionItems?: FooterActionItem[];
  className?: string;
};

const defaultActionItems: FooterActionItem[] = [
  { href: '#', icon: 'share', label: 'Megosztas' },
  { href: '#', icon: 'language', label: 'Nyelv' },
];

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const renderIcon = (icon: ReactNode | string) => {
  if (typeof icon === 'string') {
    return (
      <span className="material-symbols-outlined" aria-hidden="true">
        {icon}
      </span>
    );
  }

  return icon;
};

export const Footer = ({
  logoSrc,
  logoAlt = 'micro-futar logo',
  brandName = 'micro-futár',
  copyrightText = '© 2024 micro-futár Precision Logistics. All rights reserved.',
  actionItems = defaultActionItems,
  className,
}: FooterProps) => {
  return (
    <footer className={cn('relative z-50 bg-[#eff4ff] w-full py-12 px-8', className)}>
      <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-8">
        <div className="flex items-center gap-3">
          {logoSrc ? (
            <img src={logoSrc} alt={logoAlt} className="w-8 h-8" />
          ) : (
            <div
              className="w-8 h-8 rounded-lg bg-surface-container-lowest text-on-surface flex items-center justify-center font-bold text-xs"
              aria-hidden="true"
            >
              mf
            </div>
          )}
          <span className="text-xl font-bold text-[#0b1c30]">{brandName}</span>
        </div>

        <p className="text-[#0b1c30]/60 text-sm">{copyrightText}</p>

        <div className="flex gap-6">
          {actionItems.map((item) => (
            <a
              key={`${item.label}-${item.href}`}
              href={item.href}
              aria-label={item.label}
              className="text-[#0b1c30]/60 hover:text-primary transition-colors"
            >
              {renderIcon(item.icon)}
            </a>
          ))}
        </div>
      </div>
    </footer>
  );
};

