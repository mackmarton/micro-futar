import {useQuery} from '@tanstack/react-query';
import {PortalLayout} from '@package/shared-ui';
import {Link} from 'react-router-dom';
import {useTranslation} from 'react-i18next';
import {getAllDeposWithLookups} from '../api/logisticsDeposApi';
import {useLogisticsNavigationItems} from '../navigation';
import {LanguageSwitcher} from '../../i18n/LanguageSwitcher';
import {DeposDataTable} from './components/DeposDataTable';

export const LogisticsDeposPage = () => {
    const {t} = useTranslation('depo');
    const {t: tCommon} = useTranslation('common');
    const navigationItems = useLogisticsNavigationItems();

    const {data, isLoading, isError, error, refetch} = useQuery({
        queryKey: ['logistics', 'depos'],
        queryFn: getAllDeposWithLookups,
    });

    const depos = data ?? [];

    return (
        <PortalLayout
            title={t('list.heading')}
            activeHref="#/portal/depos"
            navigationItems={navigationItems}
            topBarRightSlot={<LanguageSwitcher />}
        >
            <section className="rounded-2xl bg-surface-container-low p-6">
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('list.eyebrow')}</p>
                <h1 className="mt-2 text-2xl font-headline text-on-surface">{t('list.heading')}</h1>
                <div className="mt-4">
                    <Link
                        to="/portal/depos/new"
                        className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                    >
                        {t('list.addNew')}
                    </Link>
                </div>
            </section>

            {isLoading ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.loading')}</p>
                    <p className="mt-2 font-body text-on-surface">{t('list.loading')}</p>
                </section>
            ) : null}

            {isError ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.error')}</p>
                    <p className="mt-2 font-body text-on-surface">{t('list.errorHeading')}</p>
                    <p className="mt-1 font-body text-on-surface-variant">{(error as Error)?.message ?? tCommon('status.unknownError')}</p>
                    <button
                        type="button"
                        onClick={() => {
                            void refetch();
                        }}
                        className="mt-4 inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                    >
                        {tCommon('buttons.retry')}
                    </button>
                </section>
            ) : null}

            {!isLoading && !isError && depos.length === 0 ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.emptyState')}</p>
                    <p className="mt-2 font-body text-on-surface">{t('list.empty')}</p>
                </section>
            ) : null}

            {!isLoading && !isError && depos.length > 0 ?
                <div className="mt-6">
                    <DeposDataTable depos={depos}/>
                </div>
                : null}
        </PortalLayout>
    );
};
