import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { PortalLayout } from '@package/shared-ui';
import type { DepoTransitDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createDepoTransit,
  getAllDepos,
  getAllPackageSizes,
  getDepoTransitById,
  updateDepoTransit,
} from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

type TransitDirection = 'outgoing' | 'incoming' | 'standalone';

type DepoTransitFormState = {
  originDepoId: string;
  destinationDepoId: string;
  packageSizeId: string;
  transportType: 'ROAD' | 'AIR';
  price: string;
};

const getTransitDirection = (value: string | null): TransitDirection => {
  if (value === 'outgoing' || value === 'incoming') {
    return value;
  }

  return 'standalone';
};

const buildInitialFormState = (depoId: number, direction: TransitDirection): DepoTransitFormState => {
  if (direction === 'outgoing') {
    return {
      originDepoId: String(depoId),
      destinationDepoId: '',
      packageSizeId: '',
      transportType: 'ROAD',
      price: '',
    };
  }

  if (direction === 'incoming') {
    return {
      originDepoId: '',
      destinationDepoId: String(depoId),
      packageSizeId: '',
      transportType: 'ROAD',
      price: '',
    };
  }

  return {
    originDepoId: String(depoId),
    destinationDepoId: '',
    packageSizeId: '',
    transportType: 'ROAD',
    price: '',
  };
};

const toFormState = (depoTransit: DepoTransitDTO): DepoTransitFormState => ({
  originDepoId: typeof depoTransit.originDepoId === 'number' ? String(depoTransit.originDepoId) : '',
  destinationDepoId: typeof depoTransit.destinationDepoId === 'number' ? String(depoTransit.destinationDepoId) : '',
  packageSizeId: typeof depoTransit.packageSizeId === 'number' ? String(depoTransit.packageSizeId) : '',
  transportType: depoTransit.transportType ?? 'ROAD',
  price: typeof depoTransit.price === 'number' ? String(depoTransit.price) : '',
});

const buildPayload = (formState: DepoTransitFormState): DepoTransitDTO => ({
  originDepoId: Number(formState.originDepoId),
  destinationDepoId: Number(formState.destinationDepoId),
  packageSizeId: Number(formState.packageSizeId),
  transportType: formState.transportType,
  price: Number(formState.price),
});

const validateForm = (
  formState: DepoTransitFormState,
  depoId: number,
  direction: TransitDirection,
  t: (key: string) => string,
): string | null => {
  if (!formState.originDepoId) {
    return t('transitForm.validation.originRequired');
  }

  if (!formState.destinationDepoId) {
    return t('transitForm.validation.destinationRequired');
  }

  if (formState.originDepoId === formState.destinationDepoId) {
    return t('transitForm.validation.sameOriginDestination');
  }

  if (!formState.packageSizeId) {
    return t('transitForm.validation.packageSizeRequired');
  }

  if (!formState.price.trim()) {
    return t('transitForm.validation.priceRequired');
  }

  const parsedPrice = Number(formState.price);
  if (!Number.isFinite(parsedPrice) || parsedPrice < 0) {
    return t('transitForm.validation.priceNonNegative');
  }

  if (direction === 'outgoing' && Number(formState.originDepoId) !== depoId) {
    return t('transitForm.validation.outgoingOriginMismatch');
  }

  if (direction === 'incoming' && Number(formState.destinationDepoId) !== depoId) {
    return t('transitForm.validation.incomingDestinationMismatch');
  }

  return null;
};

const getDirectionLabel = (direction: TransitDirection, t: (key: string) => string) => {
  if (direction === 'outgoing') {
    return t('transitForm.direction.outgoing');
  }

  if (direction === 'incoming') {
    return t('transitForm.direction.incoming');
  }

  return t('transitForm.direction.standalone');
};

export const LogisticsDepoTransitFormPage = () => {
  const { t } = useTranslation('depo');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();
  const [searchParams] = useSearchParams();

  const depoId = Number(params.depoId);
  const hasValidDepoId = Number.isInteger(depoId) && depoId > 0;
  const depoTransitId = params.depoTransitId ? Number(params.depoTransitId) : null;
  const isEditMode = typeof depoTransitId === 'number';
  const hasValidDepoTransitId = !isEditMode || (Number.isInteger(depoTransitId) && (depoTransitId as number) > 0);
  const direction = getTransitDirection(searchParams.get('direction'));
  const hasFixedOrigin = direction === 'outgoing';
  const hasFixedDestination = direction === 'incoming';

  const formContextKey = `${isEditMode ? `edit-${depoTransitId}` : 'new'}-${depoId}-${direction}`;
  const [draftFormStates, setDraftFormStates] = useState<Record<string, DepoTransitFormState>>({});
  const [validationError, setValidationError] = useState<string | null>(null);

  const deposQuery = useQuery({
    queryKey: ['logistics', 'depos'],
    queryFn: getAllDepos,
    enabled: hasValidDepoId,
  });

  const packageSizesQuery = useQuery({
    queryKey: ['logistics', 'package-sizes'],
    queryFn: getAllPackageSizes,
    enabled: hasValidDepoId,
  });

  const depoTransitQuery = useQuery({
    queryKey: ['logistics', 'depo-transit', depoTransitId],
    queryFn: () => getDepoTransitById(depoTransitId as number),
    enabled: hasValidDepoId && isEditMode && hasValidDepoTransitId,
  });

  const initialFormState = useMemo<DepoTransitFormState>(() => {
    if (!isEditMode || !depoTransitQuery.data) {
      return buildInitialFormState(depoId, direction);
    }

    const next = toFormState(depoTransitQuery.data);

    if (hasFixedOrigin) {
      next.originDepoId = String(depoId);
    }

    if (hasFixedDestination) {
      next.destinationDepoId = String(depoId);
    }

    return next;
  }, [depoId, depoTransitQuery.data, direction, hasFixedDestination, hasFixedOrigin, isEditMode]);

  const formState = draftFormStates[formContextKey] ?? initialFormState;

  const currentDepoName = useMemo(() => {
    return (
      (deposQuery.data ?? []).find((depo) => depo.id === depoId)?.name
      ?? `#${depoId}`
    );
  }, [depoId, deposQuery.data]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      const normalizedFormState: DepoTransitFormState = {
        ...formState,
        ...(hasFixedOrigin ? { originDepoId: String(depoId) } : {}),
        ...(hasFixedDestination ? { destinationDepoId: String(depoId) } : {}),
      };
      const payload = buildPayload(normalizedFormState);

      if (isEditMode) {
        return updateDepoTransit(depoTransitId as number, payload);
      }

      return createDepoTransit(payload);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'depo'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'depos'] }),
      ]);

      navigate(`/portal/depos/${depoId}`);
    },
  });

  if (!hasValidDepoId || !hasValidDepoTransitId) {
    return <Navigate to="/portal/depos" replace />;
  }

  const isPageLoading =
    deposQuery.isLoading
    || packageSizesQuery.isLoading
    || (isEditMode && depoTransitQuery.isLoading);

  const isPageError =
    deposQuery.isError
    || packageSizesQuery.isError
    || depoTransitQuery.isError;

  const pageError =
    (deposQuery.error as Error | null)
    ?? (packageSizesQuery.error as Error | null)
    ?? (depoTransitQuery.error as Error | null);

  const handleInputChange = <K extends keyof DepoTransitFormState>(key: K, value: DepoTransitFormState[K]) => {
    setDraftFormStates((previous) => {
      const current = previous[formContextKey] ?? initialFormState;

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
    const normalizedFormState: DepoTransitFormState = {
      ...formState,
      ...(hasFixedOrigin ? { originDepoId: String(depoId) } : {}),
      ...(hasFixedDestination ? { destinationDepoId: String(depoId) } : {}),
    };

    const errorMessage = validateForm(normalizedFormState, depoId, direction, t);
    setValidationError(errorMessage);

    if (errorMessage) {
      return;
    }

    setDraftFormStates((previous) => ({
      ...previous,
      [formContextKey]: normalizedFormState,
    }));
    saveMutation.mutate();
  };

  return (
    <PortalLayout
      title={isEditMode ? t('transitForm.titleEdit') : t('transitForm.titleCreate')}
      activeHref="#/portal/depos"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
    >
      <section className="rounded-3xl bg-surface-container-low p-6 md:p-8">
        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('transitForm.eyebrow')}</p>
        <h1 className="mt-2 text-2xl md:text-3xl font-headline text-on-surface">
          {isEditMode ? t('transitForm.headingEdit') : t('transitForm.headingCreate')}
        </h1>
        <p className="mt-3 font-body text-on-surface-variant">
          {t('transitForm.contextLine', { direction: getDirectionLabel(direction, t), depoName: currentDepoName })}
        </p>

        <div className="mt-5 flex flex-wrap gap-3">
          <Link
            to={`/portal/depos/${depoId}`}
            className="inline-flex items-center gap-2 rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {t('transitForm.backToDetails')}
          </Link>
        </div>
      </section>

      {isPageLoading ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.loading')}</p>
          <p className="mt-2 font-body text-on-surface">{t('transitForm.loading')}</p>
        </section>
      ) : null}

      {isPageError ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.error')}</p>
          <p className="mt-2 font-body text-on-surface">{t('transitForm.errorHeading')}</p>
          <p className="mt-1 font-body text-on-surface-variant">{pageError?.message ?? tCommon('status.unknownError')}</p>
        </section>
      ) : null}

      {!isPageLoading && !isPageError ? (
        <section className="mt-6 rounded-3xl bg-surface-container-low p-6 md:p-8">
          <div className="grid gap-4 md:grid-cols-2">
            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('transitForm.fields.originDepo')}</p>
              <select
                value={formState.originDepoId}
                onChange={(event) => handleInputChange('originDepoId', event.target.value)}
                disabled={hasFixedOrigin}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface disabled:cursor-not-allowed disabled:opacity-70"
              >
                <option value="">{t('transitForm.fields.originDepoPlaceholder')}</option>
                {(deposQuery.data ?? []).map((depo) => (
                  <option key={depo.id ?? depo.name} value={depo.id ?? ''}>
                    {/* depo.name backend-ről érkező szabad szöveg, nem fordítjuk. */}
                    {depo.name ?? tCommon('status.notAvailable')}
                  </option>
                ))}
              </select>
              {hasFixedOrigin ? (
                <p className="mt-2 font-body text-on-surface-variant">{t('transitForm.fields.fixedOriginHint')}</p>
              ) : null}
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('transitForm.fields.destinationDepo')}</p>
              <select
                value={formState.destinationDepoId}
                onChange={(event) => handleInputChange('destinationDepoId', event.target.value)}
                disabled={hasFixedDestination}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface disabled:cursor-not-allowed disabled:opacity-70"
              >
                <option value="">{t('transitForm.fields.destinationDepoPlaceholder')}</option>
                {(deposQuery.data ?? []).map((depo) => (
                  <option key={depo.id ?? depo.name} value={depo.id ?? ''}>
                    {/* depo.name backend-ről érkező szabad szöveg, nem fordítjuk. */}
                    {depo.name ?? tCommon('status.notAvailable')}
                  </option>
                ))}
              </select>
              {hasFixedDestination ? (
                <p className="mt-2 font-body text-on-surface-variant">{t('transitForm.fields.fixedDestinationHint')}</p>
              ) : null}
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('transitForm.fields.packageSize')}</p>
              <select
                value={formState.packageSizeId}
                onChange={(event) => handleInputChange('packageSizeId', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
              >
                <option value="">{t('transitForm.fields.packageSizePlaceholder')}</option>
                {(packageSizesQuery.data ?? []).map((packageSize) => (
                  <option key={packageSize.id ?? packageSize.name} value={packageSize.id ?? ''}>
                    {/* packageSize.name backend-ről érkező szabad szöveg, nem fordítjuk. */}
                    {packageSize.name ?? tCommon('status.notAvailable')}
                  </option>
                ))}
              </select>
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('transitForm.fields.transportType')}</p>
              <select
                value={formState.transportType}
                onChange={(event) =>
                  handleInputChange('transportType', event.target.value as DepoTransitFormState['transportType'])
                }
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
              >
                <option value="ROAD">{t('transportType.road')}</option>
                <option value="AIR">{t('transportType.air')}</option>
              </select>
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4 md:col-span-2">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('transitForm.fields.price')}</p>
              <input
                type="number"
                min="0"
                step="1"
                value={formState.price}
                onChange={(event) => handleInputChange('price', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder={t('transitForm.fields.pricePlaceholder')}
              />
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
                {(saveMutation.error as Error)?.message ?? t('transitForm.saveFailed')}
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
              {saveMutation.isPending ? tCommon('buttons.saving') : isEditMode ? t('transitForm.submitEdit') : t('transitForm.submitCreate')}
            </button>
            <button
              type="button"
              onClick={() => navigate(`/portal/depos/${depoId}`)}
              className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
            >
              {tCommon('buttons.cancel')}
            </button>
          </div>
        </section>
      ) : null}
    </PortalLayout>
  );
};
