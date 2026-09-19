import type { ReactNode } from 'react';
import { PortalLayout } from '@package/shared-ui';
import type { PortalLayoutProps } from '@package/shared-ui';

export type EntityListShellProps = {
  title: string;
  activeHref: string;
  navigationItems?: PortalLayoutProps['navigationItems'];
  eyebrow: string;
  heading: string;
  contextInfo?: ReactNode;
  headerActions: ReactNode;
  readyGuard?: boolean;
  emptyGuardMessage?: string;
  isLoading: boolean;
  loadingMessage: string;
  isError: boolean;
  errorMessage: string;
  errorDetail?: string;
  onRetry: () => void;
  children: ReactNode;
};

export const EntityListShell = ({
  title,
  activeHref,
  navigationItems,
  eyebrow,
  heading,
  contextInfo,
  headerActions,
  readyGuard = true,
  emptyGuardMessage,
  isLoading,
  loadingMessage,
  isError,
  errorMessage,
  errorDetail,
  onRetry,
  children,
}: EntityListShellProps) => (
  <PortalLayout title={title} activeHref={activeHref} navigationItems={navigationItems}>
    <section className="rounded-2xl bg-surface-container-low p-6">
      <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{eyebrow}</p>
      <h1 className="mt-2 text-2xl font-headline text-on-surface">{heading}</h1>
      {contextInfo}
      <div className="mt-4 flex flex-wrap gap-3">{headerActions}</div>
    </section>

    {!readyGuard ? (
      <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
        <p className="font-body text-on-surface">{emptyGuardMessage}</p>
      </section>
    ) : null}

    {readyGuard && isLoading ? (
      <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
        <p className="font-body text-on-surface">{loadingMessage}</p>
      </section>
    ) : null}

    {readyGuard && isError ? (
      <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
        <p className="font-body text-on-surface">{errorMessage}</p>
        <p className="mt-1 font-body text-on-surface-variant">{errorDetail ?? 'Ismeretlen hiba'}</p>
        <button
          type="button"
          onClick={onRetry}
          className="mt-3 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          Újrapróbálás
        </button>
      </section>
    ) : null}

    {readyGuard && !isLoading && !isError ? <div className="mt-6">{children}</div> : null}
  </PortalLayout>
);
