import {TrackingHero} from './components';
import {TrackingDetailsSection} from './components';
import {BottomNavBar} from "@package/shared-ui/BottomNavBar.tsx";
import {TopNavBar} from "@package/shared-ui/TopNavBar.tsx";
import {SideNavBar} from "@package/shared-ui/SideNavBar.tsx";

export type TrackPackagePageProps = {
    className?: string;
};
const sideNavigationItems = [
    {label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'package_2', onlyLoggedIn: true},
    {label: 'Csomag feladása', href: '#/create-order', icon: 'add_circle'},
    {label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping', isActive: true},
];

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const TrackPackagePage = ({className}: TrackPackagePageProps) => {
    return (
        <div
            className="bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed">
            <SideNavBar navigationItems={sideNavigationItems}/>

            <main className="lg:ml-64 min-h-screen flex flex-col pb-24 lg:pb-0">
                <TopNavBar title="Nyomonkövetés"/>
                <div className={cn('max-w-7xl mx-auto px-6 py-8 md:p-12', className)}>
                    <TrackingHero/>
                    <TrackingDetailsSection/>
                </div>
            </main>

            <BottomNavBar
                items={[
                    {label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'home', onlyLoggedIn: true},
                    {label: 'Csomag feladása', href: '#/create-order', icon: 'add_box'},
                    {label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping', isActive: true},
                ]}
            />
        </div>
    );
};

