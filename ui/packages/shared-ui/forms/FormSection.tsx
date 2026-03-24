import type { ReactNode } from 'react';

export type FormSectionProps = {
  icon: string;
  title: string;
  children: ReactNode;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const FormSection = ({ icon, title, children, className }: FormSectionProps) => {
  return (
    <section className={cn('bg-surface-container-low rounded-xl p-8', className)}>
      <div className="flex items-center gap-3 mb-8">
        <span className="material-symbols-outlined text-primary" aria-hidden="true">
          {icon}
        </span>
        <h3 className="text-xl font-bold text-on-surface">{title}</h3>
      </div>
      {children}
    </section>
  );
};

