import { useTranslation } from 'react-i18next';
import { PortalLandingPage } from '@package/shared-ui';
import { hasLogisticsPortalAccess } from '../auth/portalAccess';
import { LanguageSwitcher } from '../i18n/LanguageSwitcher';

export const LogisticsLandingPage = () => {
  const { t } = useTranslation('landing');

  return (
    <div className="relative">
      <div className="absolute right-4 top-4 z-10">
        <LanguageSwitcher />
      </div>
      <PortalLandingPage
        title={t('title')}
        description={t('description')}
        portalHref="/portal/depos"
        hasAccess={hasLogisticsPortalAccess}
        accessDeniedMessage={t('accessDeniedMessage')}
        openPortalLabel={t('openPortalLabel')}
        logoutLabel={t('logoutLabel')}
        loginLabel={t('loginLabel')}
      />
    </div>
  );
};
