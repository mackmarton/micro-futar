import { useMemo } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { PortalLayout } from '@package/shared-ui';
import { toErrorMessage } from '@package/shared-core';
import { useCourierNavigationItems } from '../navigation.ts';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher.tsx';
import {
  failShipmentRouteAssignment,
  fulfillShipmentRouteAssignment,
  pickUpParcel,
} from './api/courierAllocationsApi.ts';
import { CourierAllocationsMap } from './components/CourierAllocationsMap.tsx';
import { CourierAllocatedPackageDetailsSection } from './components/CourierAllocatedPackageDetailsSection.tsx';
import { useCourierAllocations } from './hooks/useCourierAllocations.ts';
import { LoadErrorButton } from '../LoadErrorButton.tsx';
import { useRefreshCourierData } from '../useRefreshCourierData.ts';

export const CourierAllocatedPackageDetailsPage = () => {
  const { t } = useTranslation(['allocations', 'common']);
  const courierNavigationItems = useCourierNavigationItems();
  const { assignmentId } = useParams<{ assignmentId: string }>();
  const { allocations, isLoading, errorMessage, isEmailNotVerified, retry } = useCourierAllocations();
  const refreshCourierData = useRefreshCourierData();
  const parsedAssignmentId = Number(assignmentId);
  const isAssignmentIdValid = Number.isFinite(parsedAssignmentId);

  const selectedAllocation = useMemo(
    () =>
      isAssignmentIdValid
        ? allocations.find((allocation) => allocation.assignmentId === parsedAssignmentId) ?? null
        : null,
    [allocations, isAssignmentIdValid, parsedAssignmentId],
  );

  const pickUpMutation = useMutation({
    mutationFn: pickUpParcel,
    onSuccess: () => {
      void refreshCourierData();
    },
  });
  const fulfillMutation = useMutation({
    mutationFn: fulfillShipmentRouteAssignment,
    onSuccess: () => {
      void refreshCourierData();
    },
  });
  const failMutation = useMutation({
    mutationFn: failShipmentRouteAssignment,
    onSuccess: () => {
      void refreshCourierData();
    },
  });

  const actionErrorFallback = t('allocations:details.actionErrorFallback');
  const actionErrorMessage =
    pickUpMutation.isError
      ? toErrorMessage(pickUpMutation.error, actionErrorFallback)
      : fulfillMutation.isError
        ? toErrorMessage(fulfillMutation.error, actionErrorFallback)
        : failMutation.isError
          ? toErrorMessage(failMutation.error, actionErrorFallback)
          : null;

  const isActionPending = pickUpMutation.isPending || fulfillMutation.isPending || failMutation.isPending;
  const canPickUp = Boolean(
    selectedAllocation
      && !selectedAllocation.pickedUpForDelivery
      && !selectedAllocation.failed,
  );
  const canFulfill = Boolean(
    selectedAllocation
      && !selectedAllocation.failed
      && selectedAllocation.pickedUpForDelivery,
  );
  const canFail = Boolean(selectedAllocation && !selectedAllocation.failed);

  return (
    <PortalLayout
      title={t('allocations:details.title')}
      activeHref="#/portal/allocated-packages"
      navigationItems={courierNavigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      brandSubtitle={t('common:brand.subtitle')}
    >
      <div className="space-y-6 md:space-y-8">
        <section className="rounded-xl bg-surface-container-low p-6 md:p-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h1 className="text-4xl font-headline font-bold leading-tight text-on-surface md:text-5xl">{t('allocations:details.title')}</h1>
              <p className="mt-3 text-on-surface-variant">{t('allocations:details.subtitle')}</p>
            </div>
            <Link
              to="/portal/allocated-packages"
              className="inline-flex items-center rounded-lg border border-outline px-4 py-2 text-sm font-medium text-on-surface"
            >
              {t('allocations:details.backToList')}
            </Link>
          </div>
        </section>

        {!isAssignmentIdValid ? (
          <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
            <p className="text-sm text-red-600">{t('allocations:details.invalidAssignmentId')}</p>
          </section>
        ) : null}

        {errorMessage ? (
          <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
            <p className="text-sm text-red-600">{errorMessage}</p>
            <LoadErrorButton isEmailNotVerified={isEmailNotVerified} onRetry={retry} />
          </section>
        ) : null}

        {isAssignmentIdValid && !isLoading && !errorMessage && !selectedAllocation ? (
          <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
            <p className="text-sm text-on-surface-variant">{t('allocations:details.notFound')}</p>
          </section>
        ) : null}

        {selectedAllocation ? (
          <>
            <CourierAllocationsMap allocations={[selectedAllocation]} />
            <CourierAllocatedPackageDetailsSection
              selectedAllocation={selectedAllocation}
              canPickUp={canPickUp}
              canFulfill={canFulfill}
              canFail={canFail}
              isActionPending={isActionPending}
              actionErrorMessage={actionErrorMessage}
              onPickUp={(id) => {
                pickUpMutation.mutate(id);
              }}
              onFulfill={(id) => {
                fulfillMutation.mutate(id);
              }}
              onFail={(id) => {
                failMutation.mutate(id);
              }}
            />
          </>
        ) : null}
      </div>
    </PortalLayout>
  );
};
