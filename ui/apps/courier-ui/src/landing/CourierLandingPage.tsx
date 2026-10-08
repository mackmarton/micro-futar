import { useTranslation } from 'react-i18next';
import { PortalLandingPage } from '@package/shared-ui';
import { hasCourierPortalAccess } from '../auth/portalAccess';
import { LanguageSwitcher } from '../i18n/LanguageSwitcher';

export const CourierLandingPage = () => {
  const { t } = useTranslation('landing');

  return (
    <div className="relative">
      {/* A shared-ui `PortalLandingPage`-nek nincs saját fejlécje/rightSlot propja (nem módosítható
          itt), ezért a nyelvváltó egy lebegő overlay-ként kerül a tartalom fölé, hogy kijelentkezve
          is elérhető legyen. */}
      <div className="absolute right-4 top-4 z-10">
        <LanguageSwitcher />
      </div>
      <PortalLandingPage
        title={t('title')}
        description={t('description')}
        portalHref="/portal/shipment-pickup"
        hasAccess={hasCourierPortalAccess}
        accessDeniedMessage={t('accessDeniedMessage')}
        openPortalLabel={t('openPortalLabel')}
        logoutLabel={t('logoutLabel')}
        loginLabel={t('loginLabel')}
      />
    </div>
  );
};
