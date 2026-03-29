export type OrderSummaryCardProps = {
    minPrice?: number;
    maxPrice?: number;
    isSubmitDisabled?: boolean;
    isSubmitting?: boolean;
    submitErrorMessage?: string | null;
    submitSuccessMessage?: string | null;
    className?: string;
    onSubmit?: () => void;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const OrderSummaryCard = ({
                                     minPrice,
                                     maxPrice,
                                     isSubmitDisabled,
                                     isSubmitting,
                                     submitErrorMessage,
                                     submitSuccessMessage,
                                     className,
                                     onSubmit
                                 }: OrderSummaryCardProps) => {
    return (
        <aside className={cn('lg:sticky lg:top-16 xl:top-24 space-y-6', className)}>
            <div className="bg-surface-container-lowest p-8 rounded-2xl shadow-[0_16px_32px_rgba(11,28,48,0.06)]">
                <h3 className="text-xs font-black uppercase tracking-[0.2em] text-on-surface-variant mb-6">Rendelés
                    összegzése</h3>

                <div className="space-y-4 mb-8">
                    <div key="min-price" className="flex justify-between items-center text-sm">
                        <span className="text-on-surface-variant">Minimum ár</span>
                        <span className="font-bold text-on-surface">{minPrice ?? "-"} Ft</span>
                    </div>
                    <div key="max-price" className="flex justify-between items-center text-sm">
                        <span className="text-on-surface-variant">Maximum ár</span>
                        <span className="font-bold text-on-surface">{maxPrice ?? "-"} Ft</span>
                    </div>

                    <div
                        className="pt-4 border-t border-dashed border-outline-variant/40 flex justify-between items-end">
                        <span className="text-on-surface-variant text-sm mb-1">Aktuális végösszeg</span>
                    </div>
                    <span
                        className="text-3xl font-black text-on-surface">{minPrice === undefined ? "- Ft" : minPrice === maxPrice ? minPrice + " Ft" : minPrice + " Ft - " + maxPrice + " Ft"}</span>
                </div>

                <button
                    type="button"
                    onClick={onSubmit}
                    disabled={isSubmitDisabled || isSubmitting}
                    className="w-full py-5 bg-primary text-on-primary rounded-xl font-bold flex items-center justify-center gap-2 hover:bg-on-primary-container transition-all active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-primary"
                >
                    {isSubmitting ? 'Feldolgozás...' : 'Rendelés leadása'}
                    <span className="material-symbols-outlined text-sm" aria-hidden="true">
            arrow_forward
          </span>
                </button>

                {submitErrorMessage ? (
                    <p className="mt-3 text-xs text-red-600">{submitErrorMessage}</p>
                ) : null}
                {!submitErrorMessage && submitSuccessMessage ? (
                    <p className="mt-3 text-xs text-green-700">{submitSuccessMessage}</p>
                ) : null}

                <p className="text-[10px] text-center text-on-surface-variant mt-4 leading-relaxed">
                    A gombra kattintva elfogadja az Általános Szerződési Feltételeket és az Adatkezelési Tájékoztatót.
                </p>
            </div>
        </aside>
    );
};

