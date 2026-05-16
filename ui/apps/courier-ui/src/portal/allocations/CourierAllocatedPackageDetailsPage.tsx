import { useMemo } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { PortalLayout } from '@package/shared-ui';
import { courierNavigationItems } from '../navigation.ts';
import {
  failShipmentRouteAssignment,
  fulfillShipmentRouteAssignment,
  pickUpParcel,
} from './api/courierAllocationsApi.ts';
import { CourierAllocationsMap } from './components/CourierAllocationsMap.tsx';
import { CourierAllocatedPackageDetailsSection } from './components/CourierAllocatedPackageDetailsSection.tsx';
import { useCourierAllocations } from './hooks/useCourierAllocations.ts';

const toErrorMessage = (error: unknown): string => {
  if (typeof error === 'object' && error !== null && 'error' in error) {
    const responseError = (error as { error?: { message?: string } }).error?.message;
    if (responseError) {
      return responseError;
    }
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }

  return 'A művelet végrehajtása sikertelen volt.';
};

export const CourierAllocatedPackageDetailsPage = () => {
  const { assignmentId } = useParams<{ assignmentId: string }>();
  const { allocations, isLoading, errorMessage, retry } = useCourierAllocations();
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
      void retry();
    },
  });
  const fulfillMutation = useMutation({
    mutationFn: fulfillShipmentRouteAssignment,
    onSuccess: () => {
      void retry();
    },
  });
  const failMutation = useMutation({
    mutationFn: failShipmentRouteAssignment,
    onSuccess: () => {
      void retry();
    },
  });

  const actionErrorMessage =
    pickUpMutation.isError
      ? toErrorMessage(pickUpMutation.error)
      : fulfillMutation.isError
        ? toErrorMessage(fulfillMutation.error)
        : failMutation.isError
          ? toErrorMessage(failMutation.error)
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
    <PortalLayout title="Kiosztott csomag részletei" activeHref="#/portal/allocated-packages" navigationItems={courierNavigationItems}>
      <div className="space-y-6 md:space-y-8">
        <section className="rounded-xl bg-surface-container-low p-6 md:p-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h1 className="text-4xl font-headline font-bold leading-tight text-on-surface md:text-5xl">Kiosztott csomag részletei</h1>
              <p className="mt-3 text-on-surface-variant">Itt kezelheted a felvétel, nyugtázás és hibajelzés műveleteket.</p>
            </div>
            <Link
              to="/portal/allocated-packages"
              className="inline-flex items-center rounded-lg border border-outline px-4 py-2 text-sm font-medium text-on-surface"
            >
              Vissza a listához
            </Link>
          </div>
        </section>

        {!isAssignmentIdValid ? (
          <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
            <p className="text-sm text-red-600">Érvénytelen hozzárendelés azonosító.</p>
          </section>
        ) : null}

        {errorMessage ? (
          <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
            <p className="text-sm text-red-600">{errorMessage}</p>
            <button
              type="button"
              onClick={() => {
                void retry();
              }}
              className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-medium text-on-primary"
            >
              Ujratoltes
            </button>
          </section>
        ) : null}

        {isAssignmentIdValid && !isLoading && !errorMessage && !selectedAllocation ? (
          <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
            <p className="text-sm text-on-surface-variant">A kiválasztott kiosztás nem található a mai listában.</p>
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
