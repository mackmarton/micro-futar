import type { ReactNode } from 'react';

export type BottomNavItem = {
  label: string;
  href: string;
  icon: ReactNode | string;
  isActive?: boolean;
  onlyLoggedIn?: boolean;
};

export type BottomNavBarProps = {
  items?: BottomNavItem[];
  className?: string;
};

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

const defaultItems: BottomNavItem[] = [
  { label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'home', isActive: true, onlyLoggedIn: true },
  { label: 'Csomag feladása', href: '#/create-order', icon: 'add_box' },
  { label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping' },
  { label: 'Menü', href: '#', icon: 'menu' },
];

export const BottomNavBar = ({ items = defaultItems, className }: BottomNavBarProps) => {
  return (
    <nav
      className={cn(
        'fixed bottom-0 left-0 z-50 w-full lg:hidden px-4 pb-6 pt-3',
        'flex items-center justify-around',
        'bg-white/90 dark:bg-slate-900/90 backdrop-blur-2xl',
        'shadow-[0_-4px_20px_rgba(0,0,0,0.05)] rounded-t-3xl',
        className
      )}
      aria-label="Alsó mobil navigáció"
    >
      {items.map((item) => (
        <a
          key={`${item.href}-${item.label}`}
          href={item.href}
          className={cn(
            'flex flex-col items-center justify-center gap-0.5 px-3 py-1.5',
            'transition-transform active:scale-90',
            item.isActive
              ? 'rounded-2xl bg-teal-50 dark:bg-teal-900/30 text-teal-600 dark:text-teal-400'
              : 'text-on-surface-variant opacity-80 hover:opacity-100'
          )}
          aria-current={item.isActive ? 'page' : undefined}
        >
          {renderIcon(item.icon)}
          <span className="font-label text-[10px] font-medium">{item.label}</span>
        </a>
      ))}
    </nav>
  );
};

