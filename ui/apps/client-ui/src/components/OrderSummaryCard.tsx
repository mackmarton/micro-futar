export type OrderSummaryItem = {
  label: string;
  amount: string;
};

export type OrderSummaryCardProps = {
  items?: OrderSummaryItem[];
  total?: string;
  onSubmit?: () => void;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const defaultItems: OrderSummaryItem[] = [
  { label: 'Alapdíj (M méret)', amount: '1 490 Ft' },
  { label: 'Expressz felár', amount: '890 Ft' },
  { label: 'Súly felár (2.5kg)', amount: '450 Ft' },
];

export const OrderSummaryCard = ({
  items = defaultItems,
  total = '2 830 Ft',
  onSubmit,
  className,
}: OrderSummaryCardProps) => {
  return (
    <aside className={cn('lg:sticky lg:top-16 xl:top-24 space-y-6', className)}>
      <div className="bg-surface-container-lowest p-8 rounded-2xl shadow-[0_16px_32px_rgba(11,28,48,0.06)]">
        <h3 className="text-xs font-black uppercase tracking-[0.2em] text-on-surface-variant mb-6">Rendelés összegzése</h3>

        <div className="space-y-4 mb-8">
          {items.map((item) => (
            <div key={item.label} className="flex justify-between items-center text-sm">
              <span className="text-on-surface-variant">{item.label}</span>
              <span className="font-bold text-on-surface">{item.amount}</span>
            </div>
          ))}

          <div className="pt-4 border-t border-dashed border-outline-variant/40 flex justify-between items-end">
            <span className="text-on-surface-variant text-sm mb-1">Aktuális végösszeg</span>
            <span className="text-3xl font-black text-on-surface">{total}</span>
          </div>
        </div>

        <button
          type="button"
          onClick={onSubmit}
          className="w-full py-5 bg-primary text-on-primary rounded-xl font-bold flex items-center justify-center gap-2 hover:bg-on-primary-container transition-all active:scale-95"
        >
          Fizetés és feladás
          <span className="material-symbols-outlined text-sm" aria-hidden="true">
            arrow_forward
          </span>
        </button>

        <p className="text-[10px] text-center text-on-surface-variant mt-4 leading-relaxed">
          A gombra kattintva elfogadja az Általános Szerződési Feltételeket és az Adatkezelési Tájékoztatót.
        </p>
      </div>
    </aside>
  );
};

