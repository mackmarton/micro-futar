import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { PortalLayout } from '@package/shared-ui';
import type { PackageSizeDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createPackageSize,
  getPackageSizeById,
  updatePackageSize,
} from './api/logisticsDeposApi';
import { logisticsNavigationItems } from './navigation';

type PackageSizeFormState = {
  name: string;
  maxLength: string;
};

const toFormState = (packageSize: PackageSizeDTO): PackageSizeFormState => ({
  name: packageSize.name ?? '',
  maxLength: typeof packageSize.maxLength === 'number' ? String(packageSize.maxLength) : '',
});

const parsePositiveNumber = (value: string): number | null => {
  const parsed = Number(value.trim());
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
};

const validateForm = (formState: PackageSizeFormState): string | null => {
  if (!formState.name.trim()) {
    return 'A csomagméret neve kötelező.';
  }

  if (parsePositiveNumber(formState.maxLength) === null) {
    return 'A max hossz legyen pozitív szám.';
  }

  return null;
};

const buildPayload = (formState: PackageSizeFormState): PackageSizeDTO => ({
  name: formState.name.trim(),
  maxLength: parsePositiveNumber(formState.maxLength) ?? undefined,
});

export const LogisticsPackageSizeFormPage = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();

  const packageSizeId = params.packageSizeId ? Number(params.packageSizeId) : null;
  const isEditMode = typeof packageSizeId === 'number';
  const hasValidPackageSizeId = !isEditMode || (Number.isInteger(packageSizeId) && (packageSizeId as number) > 0);

  const [draftFormState, setDraftFormState] = useState<PackageSizeFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const packageSizeQuery = useQuery({
    queryKey: ['logistics', 'package-size', packageSizeId],
    queryFn: () => getPackageSizeById(packageSizeId as number),
    enabled: isEditMode && hasValidPackageSizeId,
  });

  const initialFormState = useMemo<PackageSizeFormState>(() => {
    if (isEditMode) {
      return packageSizeQuery.data ? toFormState(packageSizeQuery.data) : { name: '', maxLength: '' };
    }

    return { name: '', maxLength: '' };
  }, [isEditMode, packageSizeQuery.data]);

  const formState = draftFormState ?? initialFormState;

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updatePackageSize(packageSizeId as number, payload);
      }

      return createPackageSize(payload);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'package-sizes'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'package-size'] }),
      ]);

      navigate('/portal/package-sizes');
    },
  });

  if (!hasValidPackageSizeId) {
    return <Navigate to="/portal/package-sizes" replace />;
  }

  const handleInputChange = (key: keyof PackageSizeFormState, value: string) => {
    setDraftFormState((previous) => ({
      ...(previous ?? initialFormState),
      [key]: value,
    }));
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
      title={isEditMode ? 'Csomagméret szerkesztés' : 'Csomagméret létrehozás'}
      activeHref="#/portal/package-sizes"
      navigationItems={logisticsNavigationItems}
    >
      <section className="rounded-3xl bg-surface-container-low p-6 md:p-8">
        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Csomagméret form</p>
        <h1 className="mt-2 text-2xl md:text-3xl font-headline text-on-surface">
          {isEditMode ? 'Csomagméret szerkesztés' : 'Új csomagméret létrehozás'}
        </h1>
        <div className="mt-5">
          <Link
            to="/portal/package-sizes"
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Vissza a csomagméretekhez
          </Link>
        </div>
      </section>

      {isEditMode && packageSizeQuery.isLoading ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="font-body text-on-surface">A csomagméret adatainak betöltése folyamatban...</p>
        </section>
      ) : null}

      {packageSizeQuery.isError ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
          <p className="font-body text-on-surface">A csomagméret adatainak betöltése sikertelen.</p>
          <p className="mt-1 font-body text-on-surface-variant">
            {(packageSizeQuery.error as Error)?.message ?? 'Ismeretlen hiba'}
          </p>
        </section>
      ) : null}

      {(!isEditMode || (!packageSizeQuery.isLoading && !packageSizeQuery.isError)) ? (
        <section className="mt-6 rounded-3xl bg-surface-container-low p-6 md:p-8">
          <div className="grid gap-4 md:grid-cols-2">
            <label className="rounded-2xl bg-surface-container-lowest p-4 md:col-span-2">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Név</p>
              <input
                type="text"
                value={formState.name}
                onChange={(event) => handleInputChange('name', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Pl.: Közepes doboz"
              />
            </label>

            <label className="rounded-2xl bg-surface-container-lowest p-4 md:col-span-2">
              <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Max hossz (cm)</p>
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={formState.maxLength}
                onChange={(event) => handleInputChange('maxLength', event.target.value)}
                className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                placeholder="Pl.: 50"
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
              {saveMutation.isPending ? 'Mentés...' : isEditMode ? 'Módosítás mentése' : 'Csomagméret létrehozása'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/portal/package-sizes')}
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
