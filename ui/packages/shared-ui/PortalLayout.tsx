import { createContext, useContext, useLayoutEffect, useMemo, useState, type ReactNode } from 'react';
import { BottomNavBar } from './BottomNavBar';
import type { BottomNavItem } from './BottomNavBar';
import { SideNavBar } from './SideNavBar';
import type { NavigationItem } from './SideNavBar';
import { TopNavBar } from './TopNavBar';

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
  className?: string;
  contentClassName?: string;
  children: ReactNode;
};

type PortalLayoutState = {
  title: string;
  activeHref: string;
  navigationItems: PortalNavigationItem[];
  contentClassName?: string;
};

type PortalLayoutContextValue = {
  setLayoutState: (nextState: PortalLayoutState) => void;
};

const defaultNavigationItems: PortalNavigationItem[] = [
  {
    label: 'Saját csomagjaim',
    href: '#/portal/dashboard',
    sideIcon: 'package_2',
    bottomIcon: 'home',
    onlyLoggedIn: true,
  },
  {
    label: 'Csomag feladása',
    href: '#/portal/create-order',
    sideIcon: 'add_circle',
    bottomIcon: 'add_box',
  },
  {
    label: 'Nyomonkövetés',
    href: '#/portal/tracking',
    sideIcon: 'local_shipping',
    bottomIcon: 'local_shipping',
  },
];

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');
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
      contentClassName,
    }),
    [activeHref, contentClassName, navigationItems, title],
  );

  useLayoutEffect(() => {
    if (parentLayout) {
      parentLayout.setLayoutState(desiredLayoutState);
    }
  }, [desiredLayoutState, parentLayout]);

  if (parentLayout) {
    return <>{children}</>;
  }

  const [layoutState, setLayoutState] = useState<PortalLayoutState>(desiredLayoutState);

  useLayoutEffect(() => {
    setLayoutState(desiredLayoutState);
  }, [desiredLayoutState]);

  const sideNavigationItems = toSideNavigationItems(layoutState.navigationItems, layoutState.activeHref);
  const bottomNavigationItems = toBottomNavigationItems(layoutState.navigationItems, layoutState.activeHref);

  return (
    <PortalLayoutContext.Provider value={{ setLayoutState }}>
      <div className={cn('bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed', className)}>
        <SideNavBar navigationItems={sideNavigationItems} />

        <main className="lg:ml-64 min-h-screen flex flex-col pb-24 lg:pb-0">
          <TopNavBar title={layoutState.title} />

          <div className={cn('max-w-7xl mx-auto p-6 md:p-10 w-full', layoutState.contentClassName)}>{children}</div>
        </main>

        <BottomNavBar items={bottomNavigationItems} />
      </div>
    </PortalLayoutContext.Provider>
  );
};
