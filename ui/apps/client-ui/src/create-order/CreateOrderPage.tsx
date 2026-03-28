import { useState } from 'react';
import { BottomNavBar, SideNavBar, TopNavBar } from '@package/shared-ui';
import { AddressSection, type AddressSectionValue } from './components/AddressSection.tsx';
import { OrderSummaryCard } from './components/OrderSummaryCard.tsx';
import {
  PackageDetailsSection,
  type PackageDetailsValue,
  type PackageSize,
} from './components/PackageDetailsSection.tsx';
import {
  ShippingOptionsSection,
  type PaymentMethod,
  type ShippingSpeed,
  type ShippingOptionsValue,
} from './components/ShippingOptionsSection.tsx';

const sideNavigationItems = [
  { label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'package_2', onlyLoggedIn: true },
  { label: 'Csomag feladása', href: '#/create-order', icon: 'add_circle', isActive: true },
  { label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping' },
];

const initialAddress: AddressSectionValue = {
  pickup: {
    city: '',
    street: '',
    houseNumber: '',
    postalCode: '',
  },
  recipient: {
    city: '',
    street: '',
    houseNumber: '',
    postalCode: '',
  },
};

const initialPackageDetails: PackageDetailsValue = {
  size: 'M',
  weight: '2.5',
  description: '',
};

const initialShippingOptions: ShippingOptionsValue = {
  speed: 'express',
  paymentMethod: 'card',
};

export const CreateOrderPage = () => {
  const [addressValue, setAddressValue] = useState<AddressSectionValue>(initialAddress);
  const [packageDetailsValue, setPackageDetailsValue] = useState<PackageDetailsValue>(initialPackageDetails);
  const [shippingOptionsValue, setShippingOptionsValue] = useState<ShippingOptionsValue>(initialShippingOptions);

  const handleAddressChange = (
    target: keyof AddressSectionValue,
    field: keyof AddressSectionValue['pickup'],
    fieldValue: string
  ) => {
    setAddressValue((previous) => ({
      ...previous,
      [target]: {
        ...previous[target],
        [field]: fieldValue,
      },
    }));
  };

  const handleSizeChange = (size: PackageSize) => {
    setPackageDetailsValue((previous) => ({ ...previous, size }));
  };

  const handleWeightChange = (weight: string) => {
    setPackageDetailsValue((previous) => ({ ...previous, weight }));
  };

  const handleDescriptionChange = (description: string) => {
    setPackageDetailsValue((previous) => ({ ...previous, description }));
  };

  const handleSpeedChange = (speed: ShippingSpeed) => {
    setShippingOptionsValue((previous) => ({ ...previous, speed }));
  };

  const handlePaymentChange = (paymentMethod: PaymentMethod) => {
    setShippingOptionsValue((previous) => ({ ...previous, paymentMethod }));
  };

  return (
    <div className="bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed">
      <SideNavBar navigationItems={sideNavigationItems} />

      <main className="lg:ml-64 min-h-screen flex flex-col pb-24 lg:pb-0">
        <TopNavBar title="Csomag feladása" />

        <div className="max-w-7xl mx-auto p-6 md:p-10 w-full">
          <div className="mb-12">
            <h2 className="text-4xl font-extrabold tracking-tight text-on-surface mb-2">Csomag feladása</h2>
            <p className="text-on-surface-variant text-lg">
              Hozza létre új szállítmányát néhány egyszerű lépésben.
            </p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
            <div className="lg:col-span-2 space-y-8">
              <AddressSection value={addressValue} onChange={handleAddressChange} />
              <PackageDetailsSection
                value={packageDetailsValue}
                onSizeChange={handleSizeChange}
                onWeightChange={handleWeightChange}
                onDescriptionChange={handleDescriptionChange}
              />
              <ShippingOptionsSection
                value={shippingOptionsValue}
                onSpeedChange={handleSpeedChange}
                onPaymentMethodChange={handlePaymentChange}
              />
            </div>

            <OrderSummaryCard />
          </div>
        </div>
      </main>

      <BottomNavBar
        items={[
          { label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'home', onlyLoggedIn: true},
          { label: 'Csomag feladása', href: '#/create-order', icon: 'add_box', isActive: true },
          { label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping' },
        ]}
      />
    </div>
  );
};

