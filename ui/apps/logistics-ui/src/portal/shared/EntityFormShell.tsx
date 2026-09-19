import type { ReactNode } from 'react';
import { PortalLayout } from '@package/shared-ui';
import type { PortalLayoutProps } from '@package/shared-ui';

export type EntityFormShellProps = {
  title: string;
  activeHref: string;
  navigationItems?: PortalLayoutProps['navigationItems'];
  eyebrow: string;
  heading: string;
  backLinks: ReactNode;
  isLoading: boolean;
  loadingMessage: string;
  isError: boolean;
  errorMessage: string;
  errorDetail?: string;
  children: ReactNode;
};

export const EntityFormShell = ({
  title,
  activeHref,
  navigationItems,
  eyebrow,
  heading,
  backLinks,
  isLoading,
  loadingMessage,
  isError,
  errorMessage,
  errorDetail,
  children,
}: EntityFormShellProps) => (
  <PortalLayout title={title} activeHref={activeHref} navigationItems={navigationItems}>
    <section className="rounded-3xl bg-surface-container-low p-6 md:p-8">
      <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{eyebrow}</p>
      <h1 className="mt-2 text-2xl md:text-3xl font-headline text-on-surface">{heading}</h1>
      <div className="mt-5 flex flex-wrap gap-3">{backLinks}</div>
    </section>

    {isLoading ? (
      <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
        <p className="font-body text-on-surface">{loadingMessage}</p>
      </section>
    ) : null}

    {isError ? (
      <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
        <p className="font-body text-on-surface">{errorMessage}</p>
        <p className="mt-1 font-body text-on-surface-variant">{errorDetail ?? 'Ismeretlen hiba'}</p>
      </section>
    ) : null}

    {!isLoading && !isError ? children : null}
  </PortalLayout>
);
