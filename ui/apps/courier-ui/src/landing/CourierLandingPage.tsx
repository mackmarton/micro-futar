import { PortalLandingPage } from '@package/shared-ui';
import { hasCourierPortalAccess } from '../auth/portalAccess';

export const CourierLandingPage = () => (
  <PortalLandingPage
    title="Futár portál"
    description="Üdvözlünk a micro-futár alkalmazás futár portálján."
    portalHref="/portal/shipment-pickup"
    hasAccess={hasCourierPortalAccess}
    accessDeniedMessage="Nincs jogosultságod a portál használatához. A belépéshez courier-admin vagy courier-user szerepkör szükséges."
  />
);
