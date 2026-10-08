import { createContext, useContext, useLayoutEffect, useMemo, useState, type ReactNode } from 'react';
import { BottomNavBar } from './BottomNavBar';
import type { BottomNavItem } from './BottomNavBar';
import { SideNavBar } from './SideNavBar';
import type { NavigationItem } from './SideNavBar';
import { TopNavBar } from './TopNavBar';
import { cn } from './cn';

type PortalNavigationItem = {
  label: string;
  href: string;
  sideIcon: string;
  bottomIcon: string;
  onlyLoggedIn?: boolean;
};

export type PortalLayoutProps = {
  title: string;
  activeHref: string;
  navigationItems?: PortalNavigationItem[];
  topBarRightSlot?: ReactNode;
  logoHref?: string;
  brandSubtitle?: string;
  className?: string;
  contentClassName?: string;
  children: ReactNode;
};

type PortalLayoutState = {
  title: string;
  activeHref: string;
  navigationItems: PortalNavigationItem[];
  topBarRightSlot?: ReactNode;
  logoHref?: string;
  brandSubtitle?: string;
  contentClassName?: string;
};

type PortalLayoutContextValue = {
  setLayoutState: (nextState: PortalLayoutState) => void;
};

const defaultNavigationItems: PortalNavigationItem[] = [
  {
    label: 'Saját csomagjaim',
    href: '/portal/dashboard',
    sideIcon: 'package_2',
    bottomIcon: 'home',
    onlyLoggedIn: true,
  },
  {
    label: 'Csomag feladása',
    href: '/portal/create-order',
    sideIcon: 'add_circle',
    bottomIcon: 'add_box',
  },
  {
    label: 'Nyomonkövetés',
    href: '/portal/tracking',
    sideIcon: 'local_shipping',
    bottomIcon: 'local_shipping',
  },
];

const PortalLayoutContext = createContext<PortalLayoutContextValue | null>(null);

const toSideNavigationItems = (
  navigationItems: PortalNavigationItem[],
  activeHref: string,
): NavigationItem[] => {
  return navigationItems.map((item) => ({
    label: item.label,
    href: item.href,
    icon: item.sideIcon,
    isActive: item.href === activeHref,
    onlyLoggedIn: item.onlyLoggedIn,
  }));
};

const toBottomNavigationItems = (
  navigationItems: PortalNavigationItem[],
  activeHref: string,
): BottomNavItem[] => {
  return navigationItems.map((item) => ({
    label: item.label,
    href: item.href,
    icon: item.bottomIcon,
    isActive: item.href === activeHref,
    onlyLoggedIn: item.onlyLoggedIn,
  }));
};

export const PortalLayout = ({
  title,
  activeHref,
  navigationItems = defaultNavigationItems,
  topBarRightSlot,
  logoHref,
  brandSubtitle,
  className,
  contentClassName,
  children,
}: PortalLayoutProps) => {
  const parentLayout = useContext(PortalLayoutContext);

  const desiredLayoutState = useMemo<PortalLayoutState>(
    () => ({
      title,
      activeHref,
      navigationItems,
      topBarRightSlot,
      logoHref,
      brandSubtitle,
      contentClassName,
    }),
    [activeHref, brandSubtitle, contentClassName, logoHref, navigationItems, title, topBarRightSlot],
  );

  const [layoutState, setLayoutState] = useState<PortalLayoutState>(desiredLayoutState);

  useLayoutEffect(() => {
    if (parentLayout) {
      parentLayout.setLayoutState(desiredLayoutState);
    } else {
      setLayoutState(desiredLayoutState);
    }
  }, [desiredLayoutState, parentLayout]);

  if (parentLayout) {
    return <>{children}</>;
  }

  const sideNavigationItems = toSideNavigationItems(layoutState.navigationItems, layoutState.activeHref);
  const bottomNavigationItems = toBottomNavigationItems(layoutState.navigationItems, layoutState.activeHref);

  return (
    <PortalLayoutContext.Provider value={{ setLayoutState }}>
      <div className={cn('bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed', className)}>
        <SideNavBar navigationItems={sideNavigationItems} logoHref={layoutState.logoHref} brandSubtitle={layoutState.brandSubtitle} />

        <main className="lg:ml-64 min-h-screen flex flex-col pb-24 lg:pb-0">
          <TopNavBar title={layoutState.title} rightSlot={layoutState.topBarRightSlot} />

          <div className={cn('max-w-7xl mx-auto p-6 md:p-10 w-full', layoutState.contentClassName)}>{children}</div>
        </main>

        <BottomNavBar items={bottomNavigationItems} />
      </div>
    </PortalLayoutContext.Provider>
  );
};
