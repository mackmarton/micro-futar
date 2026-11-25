import { useTranslation } from 'react-i18next';
import { useAuth } from '@package/shared-ui';

type LoadErrorButtonProps = {
  isEmailNotVerified: boolean;
  onRetry: () => Promise<void>;
};

// Ellenőrizetlen e-mailnél csak új token segít, ezért újratöltés helyett új bejelentkezést kínálunk.
export const LoadErrorButton = ({ isEmailNotVerified, onRetry }: LoadErrorButtonProps) => {
  const { t } = useTranslation('common');
  const { login } = useAuth();

  return (
    <button
      type="button"
      onClick={() => {
        if (isEmailNotVerified) {
          login();
        } else {
          void onRetry();
        }
      }}
      className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-medium text-on-primary"
    >
      {isEmailNotVerified ? t('actions.loginAgain') : t('actions.retry')}
    </button>
  );
};
