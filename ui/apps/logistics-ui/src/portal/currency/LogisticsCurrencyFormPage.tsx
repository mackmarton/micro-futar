import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { FormSection, PrecisionInput } from '@package/shared-ui';
import type { CurrencyDTO } from '@package/shared-core/api/LogisticsApiClient';
import {
  createCurrency,
  getCurrencyById,
  updateCurrency,
} from '../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../navigation';
import { EntityFormShell } from '../shared/EntityFormShell';

type CurrencyFormState = {
  code: string;
  name: string;
  symbol: string;
};

const emptyFormState: CurrencyFormState = { code: '', name: '', symbol: '' };

const toFormState = (currency: CurrencyDTO): CurrencyFormState => ({
  code: currency.code ?? '',
  name: currency.name ?? '',
  symbol: currency.symbol ?? '',
});

const validateForm = (formState: CurrencyFormState): string | null => {
  if (!formState.code.trim()) {
    return 'A pénznem kódja kötelező.';
  }

  if (!formState.name.trim()) {
    return 'A pénznem neve kötelező.';
  }

  return null;
};

const buildPayload = (formState: CurrencyFormState): CurrencyDTO => ({
  code: formState.code.trim().toUpperCase(),
  name: formState.name.trim(),
  symbol: formState.symbol.trim() || undefined,
});

export const LogisticsCurrencyFormPage = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const params = useParams();

  const currencyId = params.currencyId ? Number(params.currencyId) : null;
  const isEditMode = typeof currencyId === 'number';
  const hasValidCurrencyId = !isEditMode || (Number.isInteger(currencyId) && (currencyId as number) > 0);

  const [draftFormState, setDraftFormState] = useState<CurrencyFormState | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const currencyQuery = useQuery({
    queryKey: ['logistics', 'currency', currencyId],
    queryFn: () => getCurrencyById(currencyId as number),
    enabled: isEditMode && hasValidCurrencyId,
  });

  const initialFormState = useMemo<CurrencyFormState>(() => {
    if (isEditMode) {
      return currencyQuery.data ? toFormState(currencyQuery.data) : emptyFormState;
    }

    return emptyFormState;
  }, [isEditMode, currencyQuery.data]);

  const formState = draftFormState ?? initialFormState;

  const saveMutation = useMutation({
    mutationFn: async () => {
      const payload = buildPayload(formState);

      if (isEditMode) {
        return updateCurrency(currencyId as number, payload);
      }

      return createCurrency(payload);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['logistics', 'currencies'] }),
        queryClient.invalidateQueries({ queryKey: ['logistics', 'currency'] }),
      ]);

      navigate('/portal/currencies');
    },
  });

  if (!hasValidCurrencyId) {
    return <Navigate to="/portal/currencies" replace />;
  }

  const handleInputChange = (key: keyof CurrencyFormState, value: string) => {
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
    <EntityFormShell
      title={isEditMode ? 'Pénznem szerkesztés' : 'Pénznem létrehozás'}
      activeHref="#/portal/currencies"
      navigationItems={logisticsNavigationItems}
      eyebrow="Pénznem form"
      heading={isEditMode ? 'Pénznem szerkesztés' : 'Új pénznem létrehozás'}
      backLinks={
        <Link
          to="/portal/currencies"
          className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          Vissza a pénznemekhez
        </Link>
      }
      isLoading={isEditMode && currencyQuery.isLoading}
      loadingMessage="A pénznem adatainak betöltése folyamatban..."
      isError={currencyQuery.isError}
      errorMessage="A pénznem adatainak betöltése sikertelen."
      errorDetail={(currencyQuery.error as Error)?.message}
    >
      <FormSection icon="payments" title="Pénznem adatai" className="mt-6">
        <div className="grid gap-4 md:grid-cols-3">
          <PrecisionInput
            label="Kód"
            value={formState.code}
            onChange={(event) => handleInputChange('code', event.target.value)}
            placeholder="Pl.: HUF"
            required
          />

          <PrecisionInput
            label="Név"
            value={formState.name}
            onChange={(event) => handleInputChange('name', event.target.value)}
            placeholder="Pl.: Magyar forint"
            required
          />

          <PrecisionInput
            label="Jel"
            value={formState.symbol}
            onChange={(event) => handleInputChange('symbol', event.target.value)}
            placeholder="Pl.: Ft"
          />
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
            {saveMutation.isPending ? 'Mentés...' : isEditMode ? 'Módosítás mentése' : 'Pénznem létrehozása'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/portal/currencies')}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-5 py-3 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Mégse
          </button>
        </div>
      </FormSection>
    </EntityFormShell>
  );
};
