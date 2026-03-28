import {BottomNavBar, SideNavBar, TopNavBar} from '@package/shared-ui';
import {type Shipment} from './components/ShipmentTable.tsx';
import {ShipmentStats} from './components/ShipmentStats.tsx';
import {ShipmentTable} from "./components/ShipmentTable.tsx";

const sideNavigationItems = [
    {label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'package_2', isActive: true, onlyLoggedIn: true},
    {label: 'Csomag feladása', href: '#/create-order', icon: 'add_circle'},
    {label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping'},
];

const stats = {
    inProgress: 4,
    delivered: 158,
};

const shipments: Shipment[] = [
    {
        id: '#HU-992834-QX',
        createdAt: '2024. Március 12.',
        destination: 'Budapest, Hungary',
        status: 'inProgress',
        eta: 'Ma, 14:30 - 16:00',
        progressPercent: 65,
    },
    {
        id: '#HU-110293-BA',
        createdAt: '2024. Március 10.',
        destination: 'Debrecen, Hungary',
        status: 'delivered',
        deliveredAt: '2024. Március 11.',
        signerName: 'T. Kovács',
    },
    {
        id: '#HU-882716-ZZ',
        createdAt: '2024. Március 08.',
        destination: 'Szeged, Hungary',
        status: 'failed',
        errorReason: 'Hibás címzés',
    },
];

export const DashboardPage = () => {
    return (
        <div
            className="bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed">
            <SideNavBar navigationItems={sideNavigationItems}/>

            <main className="lg:ml-64 min-h-screen flex flex-col pb-24 lg:pb-0">
                <TopNavBar title="Saját csomagjaim"/>

                <div className="max-w-7xl mx-auto p-6 md:p-10 w-full flex-grow">
                    <ShipmentStats stats={stats}/>

                    <div className="flex justify-between items-end mb-8 gap-6">
                        <div>
                            <h3 className="font-headline text-2xl font-bold text-on-surface">Küldemények listája</h3>
                            <p className="text-on-surface-variant">
                                Kezelje és kövesse nyomon feladott csomagjait egy helyen.
                            </p>
                        </div>

                        <a
                            href="#/create-order"
                            className="hidden md:flex bg-primary text-on-primary px-6 py-3 rounded-lg font-bold items-center gap-2 hover:bg-on-primary-container transition-all"
                        >
              <span className="material-symbols-outlined" aria-hidden="true">
                local_shipping
              </span>
                            Új csomag feladása
                        </a>
                    </div>
                    <ShipmentTable shipments={shipments} />
                </div>
            </main>

            <BottomNavBar
                items={[
                    {
                        label: 'Saját csomagjaim',
                        href: '#/my-shipments',
                        icon: 'home',
                        isActive: true,
                        onlyLoggedIn: true
                    },
                    {label: 'Csomag feladása', href: '#/create-order', icon: 'add_box'},
                    {label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping'},
                ]}
            />
        </div>
    );
};

