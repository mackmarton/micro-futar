export type ShippingInfoCardProps = {
  title?: string;
  addressTitle?: string;
  addressPrimary?: string;
  addressSecondary?: string;
  contentTitle?: string;
  contentValue?: string;
  noteTitle?: string;
  note?: string;
  securityNotice?: string;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const iconBoxClassName =
  'bg-teal-50 w-10 h-10 rounded-lg flex items-center justify-center text-teal-600 shrink-0';

export const ShippingInfoCard = ({
  title = 'Szállítási információk',
  addressTitle = 'Cím',
  addressPrimary = '1117 Budapest, Infopark sétány 1.',
  addressSecondary = 'Épület: B, 4. emelet',
  contentTitle = 'Tartalom',
  contentValue = 'Elektronikai eszköz (1.2 kg)',
  noteTitle = 'Megjegyzés',
  note = '"A kapucsengőn a név: Kovács-Művek. Kérem, a recepción hagyja a csomagot."',
  securityNotice = 'Fényképes igazolvány szükséges.',
  className,
}: ShippingInfoCardProps) => {
  return (
    <section className={cn('bg-surface-container-lowest p-6 rounded-xl shadow-sm border border-surface-container', className)}>
      <h3 className="text-sm font-bold mb-6 text-on-surface-variant uppercase tracking-widest flex items-center gap-2">
        <span className="material-symbols-outlined text-teal-600 text-lg" aria-hidden="true">
          info
        </span>
        {title}
      </h3>

      <div className="space-y-6">
        <div className="flex gap-4">
          <div className={iconBoxClassName}>
            <span className="material-symbols-outlined text-lg" aria-hidden="true">
              location_on
            </span>
          </div>
          <div>
            <p className="text-[10px] text-on-surface-variant uppercase font-bold tracking-wider mb-0.5">{addressTitle}</p>
            <p className="text-sm font-semibold text-on-surface">{addressPrimary}</p>
            <p className="text-[10px] text-on-surface-variant">{addressSecondary}</p>
          </div>
        </div>

        <div className="flex gap-4">
          <div className={iconBoxClassName}>
            <span className="material-symbols-outlined text-lg" aria-hidden="true">
              inventory_2
            </span>
          </div>
          <div>
            <p className="text-[10px] text-on-surface-variant uppercase font-bold tracking-wider mb-0.5">{contentTitle}</p>
            <p className="text-sm font-semibold text-on-surface">{contentValue}</p>
          </div>
        </div>

        <div className="bg-surface-container p-4 rounded-xl border-l-4 border-teal-600">
          <p className="text-[10px] text-on-surface-variant uppercase font-bold tracking-wider mb-2 flex items-center gap-1">
            <span className="material-symbols-outlined text-[12px]" aria-hidden="true">
              sticky_note_2
            </span>
            {noteTitle}
          </p>
          <p className="text-xs italic text-on-surface leading-relaxed">{note}</p>
        </div>
      </div>

      <div className="mt-6">
        <div className="flex items-center gap-2 text-on-tertiary-fixed-variant bg-tertiary-fixed p-3 rounded-lg">
          <span className="material-symbols-outlined shrink-0 text-sm" aria-hidden="true">
            security
          </span>
          <p className="text-[10px] font-medium leading-tight">{securityNotice}</p>
        </div>
      </div>
    </section>
  );
};

