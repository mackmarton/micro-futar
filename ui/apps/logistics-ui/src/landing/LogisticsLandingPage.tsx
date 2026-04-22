import { useAuth } from '@package/shared-ui';
import { Link } from 'react-router-dom';
import { hasLogisticsPortalAccess } from '../auth/portalAccess';

export const LogisticsLandingPage = () => {
  const { user, login, logout } = useAuth();
  const hasPortalAccess = hasLogisticsPortalAccess(user);

  return (
    <div className="bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed">
      <main className="max-w-5xl mx-auto px-6 py-16 md:py-24">
        <section className="rounded-3xl border border-outline-variant bg-surface-container-lowest p-8 md:p-12 shadow-sm">
          <img
            src="/micro-futar-logo.svg"
            alt="micro-futar logo"
            className="h-14 w-14 md:h-16 md:w-16"
          />
          <h1 className="mt-6 text-4xl md:text-5xl font-headline font-black tracking-tight">
            Logisztkai portál
          </h1>
          <p className="mt-4 max-w-2xl text-on-surface-variant text-lg">
            Üdvözlünk a micro-futár alkalmazás logisztikai portálján.
          </p>

          {user && !hasPortalAccess ? (
            <p className="mt-6 rounded-lg border border-error/40 bg-error-container/50 px-4 py-3 text-sm font-medium text-error">
              Nincs jogosultságod a portál használatához. A belépéshez logistics-admin vagy logistics-user szerepkör szükséges.
            </p>
          ) : null}

          <div className="mt-8 flex flex-wrap gap-3">
            {user ? (
              <>
                {hasPortalAccess ? (
                  <Link
                    to="/portal/dashboard"
                    className="inline-flex items-center gap-2 rounded-lg bg-primary px-5 py-3 font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                  >
                    <span className="material-symbols-outlined" aria-hidden="true">
                      dashboard
                    </span>
                    Portál megnyitása
                  </Link>
                ) : null}
                <button
                  type="button"
                  onClick={logout}
                  className="inline-flex items-center gap-2 rounded-lg border border-outline px-5 py-3 font-semibold text-on-surface hover:bg-surface-container transition-colors"
                >
                  <span className="material-symbols-outlined" aria-hidden="true">
                    logout
                  </span>
                  Kijelentkezes
                </button>
              </>
            ) : (
              <button
                type="button"
                onClick={login}
                className="inline-flex items-center gap-2 rounded-lg bg-primary px-5 py-3 font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
              >
                <span className="material-symbols-outlined" aria-hidden="true">
                  login
                </span>
                Bejelentkezes
              </button>
            )}
          </div>
        </section>
      </main>
    </div>
  );
};


