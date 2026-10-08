import type { ReactNode } from 'react';
import { Suspense } from 'react';
import { Outlet } from 'react-router-dom';
import { PortalLayout } from './PortalLayout';
import type { PortalLayoutProps } from './PortalLayout';

export type PortalShellProps = {
  title: string;
  activeHref?: string;
  navigationItems?: PortalLayoutProps['navigationItems'];
  loadingLabel?: string;
  topBarRightSlot?: ReactNode;
  logoHref?: string;
  brandSubtitle?: string;
};

export const PortalShell = ({
  title,
  activeHref = '#/portal/dashboard',
  navigationItems,
  loadingLabel = 'Oldal betöltése folyamatban...',
  topBarRightSlot,
  logoHref,
  brandSubtitle,
}: PortalShellProps) => {
  return (
    <PortalLayout
      title={title}
      activeHref={activeHref}
      navigationItems={navigationItems}
      topBarRightSlot={topBarRightSlot}
      logoHref={logoHref}
      brandSubtitle={brandSubtitle}
    >
      <Suspense
        fallback={
          <section className="rounded-2xl bg-surface-container-low p-6">
            <p className="font-body text-on-surface">{loadingLabel}</p>
          </section>
        }
      >
        <Outlet />
      </Suspense>
    </PortalLayout>
  );
};
