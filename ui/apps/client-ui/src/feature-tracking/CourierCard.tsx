export type CourierCardProps = {
  name?: string;
  courierId?: string;
  avatarUrl?: string;
  vehicle?: string;
  rating?: string;
  onCall?: () => void;
  onMessage?: () => void;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const CourierCard = ({
  name = 'Kovács István',
  courierId = '#9921',
  avatarUrl =
    'https://lh3.googleusercontent.com/aida-public/AB6AXuCg0Om-zEunLGfYv0o2_1haaKAS7rId2PLYphj9B4qP8h8vtRY3RDZKEFYCPweYTdBX1og3X_lhJrCKa1MEY9R7WAwFqj95w0QeGuM0gPynHBqOQ09yKI-qvXIMaQDrwvobwhKGLMVGZlCV01pGhyrMmkvfojTOVxWBf45M_HzketvBTRrct1iEoA6YnAlktz3o6ZiZkwDgho0RLjkfeaELldZ-TtcOmVAllWOtDZfQaf2TTPouhAm6LCd5I-LHYjtaMUnvAhYPZew',
  vehicle = 'Renault Kangoo (ABC-123)',
  rating = '4.9',
  onCall,
  onMessage,
  className,
}: CourierCardProps) => {
  return (
    <section className={cn('bg-surface-container-low p-6 rounded-xl', className)}>
      <h4 className="text-sm font-bold text-on-surface-variant uppercase tracking-widest mb-6">FUTÁR ADATAI</h4>

      <div className="flex items-center gap-4 mb-6">
        <img src={avatarUrl} alt="Futár profil" className="w-16 h-16 rounded-full object-cover" />
        <div>
          <p className="text-lg font-bold text-on-surface">{name}</p>
          <p className="text-sm text-on-surface-variant">Azonosító: {courierId}</p>
        </div>
      </div>

      <div className="space-y-4">
        <div className="flex items-center justify-between text-sm gap-4">
          <span className="text-on-surface-variant">Jármű:</span>
          <span className="font-semibold text-on-surface text-right">{vehicle}</span>
        </div>
        <div className="flex items-center justify-between text-sm gap-4">
          <span className="text-on-surface-variant">Minősítés:</span>
          <span className="flex items-center text-tertiary-fixed-dim">
            <span
              className="material-symbols-outlined text-xs"
              aria-hidden="true"
              style={{ fontVariationSettings: "'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}
            >
              star
            </span>
            <span className="font-bold ml-1">{rating}</span>
          </span>
        </div>
      </div>

      <div className="mt-8 grid grid-cols-2 gap-3">
        <button
          type="button"
          onClick={onCall}
          className="flex items-center justify-center gap-2 bg-white text-on-surface py-3 rounded-lg text-sm font-bold shadow-sm hover:bg-surface-container transition-colors"
        >
          <span className="material-symbols-outlined text-lg" aria-hidden="true">
            call
          </span>
          Hívás
        </button>
        <button
          type="button"
          onClick={onMessage}
          className="flex items-center justify-center gap-2 bg-white text-on-surface py-3 rounded-lg text-sm font-bold shadow-sm hover:bg-surface-container transition-colors"
        >
          <span className="material-symbols-outlined text-lg" aria-hidden="true">
            chat
          </span>
          Üzenet
        </button>
      </div>
    </section>
  );
};

