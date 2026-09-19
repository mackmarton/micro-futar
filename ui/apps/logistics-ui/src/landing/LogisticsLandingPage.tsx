import { PortalLandingPage } from '@package/shared-ui';
import { hasLogisticsPortalAccess } from '../auth/portalAccess';

export const LogisticsLandingPage = () => (
  <PortalLandingPage
    title="Logisztikai portál"
    description="Üdvözlünk a micro-futár alkalmazás logisztikai portálján."
    portalHref="/portal/depos"
    hasAccess={hasLogisticsPortalAccess}
    accessDeniedMessage="Nincs jogosultságod a portál használatához. A belépéshez logistics-admin vagy logistics-user szerepkör szükséges."
  />
);
