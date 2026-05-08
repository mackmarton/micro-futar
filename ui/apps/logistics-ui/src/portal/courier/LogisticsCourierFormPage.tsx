import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { PortalLayout } from '@package/shared-ui';
import type { CourierDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createCourier,
  getAllDepos,
  getAllVehicles,
  getCourierById,
  updateCourier,
} from '../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../navigation';

type CourierFormState = {
  name: string;
  email: string;
  telephone: string;
  vehicleId: string;
  qualifiedFor: 'ROAD' | 'AIR';
  courierType: 'CROSS_DEPO' | 'DELIVERY';
  depoId: string;
};

const getDefaultCourierType = (value: string | null): CourierFormState['courierType'] => {
  if (value === 'CROSS_DEPO' || value === 'DELIVERY') {
    return value;
  }

  return 'DELIVERY';
};

const buildEmptyFormState = (courierType: CourierFormState['courierType']): CourierFormState => ({
  name: '',
  email: '',
  telephone: '',
  vehicleId: '',
  qualifiedFor: 'ROAD',
  courierType,
  depoId: '',
});

const toFormState = (courier: CourierDTO): CourierFormState => ({
  name: courier.name ?? '',
  email: courier.email ?? '',
  telephone: courier.telephone ?? '',
  vehicleId: typeof courier.vehicleId === 'number' ? String(courier.vehicleId) : '',
  qualifiedFor: courier.qualifiedFor ?? 'ROAD',
  courierType: courier.courierType ?? 'DELIVERY',
  depoId: typeof courier.depoId === 'number' ? String(courier.depoId) : '',
});

const validateForm = (formState: CourierFormState): string | null => {
  if (!formState.name.trim()) {
    return 'A név megadása kötelező.';
  }

  if (!formState.email.trim()) {
    return 'Az email megadása kötelező.';
  }

  if (!formState.telephone.trim()) {
    return 'A telefonszám megadása kötelező.';
  }

  if (!formState.vehicleId) {
    return 'A jármű kiválasztása kötelező.';
  }

  if (formState.courierType === 'DELIVERY' && !formState.depoId) {
    return 'Delivery típusnál a depó kiválasztása kötelező.';
  }

  return null;
};

const buildPayload = (formState: CourierFormState): CourierDTO => ({
  name: formState.name.trim(),
  email: formState.email.trim(),
  telephone: formState.telephone.trim(),
  vehicleId: Number(formState.vehicleId),
  qualifiedFor: formState.qualifiedFor,
  courierType: formState.courierType,
  depoId: formState.depoId ? Number(formState.depoId) : undefined,
});

export const LogisticsCourierFormPage = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();
  const [searchParams] = useSearchParams();

  const courierId = params.courierId ? Number(params.courierId) : null;
  const isEditMode = typeof courierId === 'number';
  const hasValidCourierId = !isEditMode || (Number.isInteger(courierId) && (courierId as number) > 0);
  const defaultCourierType = getDefaultCourierType(searchParams.get('type'));
  const formContextKey = isEditMode ? `edit-${courierId}` : `new-${defaultCourierType}`;

  const [draftFormStates, setDraftFormStates] = useState<Record<string, CourierFormState>>({});
  const [validationError, setValidationError] = useState<string | null>(null);

  const deposQuery = useQuery({
    queryKey: ['logistics', 'depos'],
    queryFn: getAllDepos,
    enabled: hasValidCourierId,
  });

  const vehiclesQuery = useQuery({
    queryKey: ['logistics', 'vehicles'],
    queryFn: getAllVehicles,
    enabled: hasValidCourierId,
  });

  const courierQuery = useQuery({
    queryKey: ['logistics', 'courier', courierId],
    queryFn: () => getCourierById(courierId as number),
    enabled: hasValidCourierId && isEditMode,
  });

  const initialFormState = useMemo(() => {
    if (isEditMode) {
      return courierQuery.data ? toFormState(courierQuery.data) : buildEmptyFormState(defaultCourierType);
    }

    return buildEmptyFormState(defaultCourierType);
  }, [courierQuery.data, defaultCourierType, isEditMode]);

  const formState = draftFormStates[formContextKey] ?? initialFormState;

  const isDeliveryCourier = formState.courierType === 'DELIVERY';

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updateCourier(courierId as number, payload);
      }

      return createCourier(payload);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'couriers'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'vehicles', 'registrations'] }),
      ]);

      navigate('/portal/couriers');
    },
  });

  if (!hasValidCourierId) {
    return <Navigate to="/portal/couriers" replace />;
  }

  const isPageLoading =
    deposQuery.isLoading
    || vehiclesQuery.isLoading
    || (isEditMode && courierQuery.isLoading);

  const isPageError =
    deposQuery.isError
    || vehiclesQuery.isError
    || courierQuery.isError;

  const pageError =
    (deposQuery.error as Error | null)
    ?? (vehiclesQuery.error as Error | null)
    ?? (courierQuery.error as Error | null);

  const handleInputChange = (key: keyof CourierFormState, value: string) => {
    setDraftFormStates((previous) => {
      const current = previous[formContextKey] ?? initialFormState;

      if (key === 'courierType' && value === 'CROSS_DEPO') {
        return {
          ...previous,
          [formContextKey]: {
            ...current,
            courierType: value,
            depoId: '',
          },
        };
      }

      return {
        ...previous,
        [formContextKey]: {
          ...current,
          [key]: value,
        },
      };
    });
    setValidationError(null);
  };

  const handleSubmit = () => {
    const errorMessage = validateForm(formState);
    setValidationError(errorMessage);

    if (errorMessage) {
      return;
    }

    saveMutation.mutate();
  };

  return (
    <PortalLayout
      title={isEditMode ? 'Futár szerkesztés' : 'Futár létrehozás'}
      activeHref="#/portal/couriers"
      navigationItems={logisticsNavigationItems}
    >
      <section className="rounded-3xl bg-surface-container-low p-6 md:p-8">
        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Futár form</p>
        <h1 className="mt-2 text-2xl md:text-3xl font-headline text-on-surface">
          {isEditMode ? 'Futár szerkesztés' : 'Új futár létrehozás'}
        </h1>

        <div className="mt-5 flex flex-wrap gap-3">
          <Link
            to="/portal/couriers"
            className="inline-flex items-center gap-2 rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Vissza a futárok oldalára
          </Link>
        </div>
      </section>

      {isPageLoading ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Betöltés</p>
          <p className="mt-2 font-body text-on-surface">A futár form betöltése folyamatban...</p>
        </section>
      ) : null}

      {isPageError ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Hiba</p>
          <p className="mt-2 font-body text-on-surface">A futár form megnyitása sikertelen.</p>
          <p className="mt-1 font-body text-on-surface-variant">{pageError?.message ?? 'Ismeretlen hiba.'}</p>
        </section>
      ) : null}

      {!isPageLoading && !isPageError ? (
        <section className="mt-6 rounded-3xl bg-surface-container-low p-6 md:p-8">
          <div className="grid gap-4 md:grid-cols-2">
            <label className="rounded-2xl bg-surface-container-lowest p-4 md:col-span-2">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Név</p>
              <input
                type="text"
                value={formState.name}
                onChange={(event) => handleInputChange('name', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Pl.: Kovács Béla"
              />
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Email</p>
              <input
                type="email"
                value={formState.email}
                onChange={(event) => handleInputChange('email', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Pl.: bela.kovacs@example.com"
              />
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Telefonszám</p>
              <input
                type="text"
                value={formState.telephone}
                onChange={(event) => handleInputChange('telephone', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Pl.: +36 30 123 4567"
              />
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Jármű</p>
              <select
                value={formState.vehicleId}
                onChange={(event) => handleInputChange('vehicleId', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
              >
                <option value="">Válassz járművet</option>
                {(vehiclesQuery.data ?? []).map((vehicle) => (
                  <option key={vehicle.id ?? vehicle.registrationNumber} value={vehicle.id ?? ''}>
                    {vehicle.registrationNumber ?? (typeof vehicle.id === 'number' ? `#${vehicle.id}` : 'N/A')}
                  </option>
                ))}
              </select>
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Képzettség</p>
              <select
                value={formState.qualifiedFor}
                onChange={(event) =>
                  handleInputChange('qualifiedFor', event.target.value as CourierFormState['qualifiedFor'])
                }
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
              >
                <option value="ROAD">Földi</option>
                <option value="AIR">Légi</option>
              </select>
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Futár típus</p>
              <select
                value={formState.courierType}
                onChange={(event) =>
                  handleInputChange('courierType', event.target.value as CourierFormState['courierType'])
                }
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
              >
                <option value="DELIVERY">Depó (delivery)</option>
                <option value="CROSS_DEPO">Cross-depó</option>
              </select>
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Depó</p>
              <select
                value={formState.depoId}
                onChange={(event) => handleInputChange('depoId', event.target.value)}
                disabled={!isDeliveryCourier}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface disabled:cursor-not-allowed disabled:opacity-70"
              >
                <option value="">Válassz depót</option>
                {(deposQuery.data ?? []).map((depo) => (
                  <option key={depo.id ?? depo.name} value={depo.id ?? ''}>
                    {depo.name ?? 'N/A'}
                  </option>
                ))}
              </select>
              {!isDeliveryCourier ? (
                <p className="mt-2 font-body text-on-surface-variant">
                  Cross-depó futárnál a depó mező nem használható.
                </p>
              ) : null}
            </label>
          </div>

          {validationError ? (
            <div className="mt-4 rounded-xl bg-surface-container-lowest p-4">
              <p className="font-body text-on-surface">{validationError}</p>
            </div>
          ) : null}

          {saveMutation.isError ? (
            <div className="mt-4 rounded-xl bg-surface-container-lowest p-4">
              <p className="font-body text-on-surface">
                {(saveMutation.error as Error)?.message ?? 'A mentés nem sikerült.'}
              </p>
            </div>
          ) : null}

          <div className="mt-6 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={handleSubmit}
              disabled={saveMutation.isPending}
              className="inline-flex items-center rounded-lg bg-primary px-5 py-3 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container disabled:cursor-not-allowed disabled:opacity-70"
            >
              {saveMutation.isPending ? 'Mentés...' : isEditMode ? 'Módosítás mentése' : 'Futár létrehozása'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/portal/couriers')}
              className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
            >
              Mégse
            </button>
          </div>
        </section>
      ) : null}
    </PortalLayout>
  );
};
