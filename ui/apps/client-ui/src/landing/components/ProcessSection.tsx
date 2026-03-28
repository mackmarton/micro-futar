type ProcessStep = {
  id: number;
  title: string;
  description: string;
  isHighlighted?: boolean;
};

export type ProcessSectionProps = {
  className?: string;
};

const steps: ProcessStep[] = [
  {
    id: 1,
    title: 'Csomag összeállítása',
    description: 'Készítse el csomagját, és gondoskodjon a megfelelő csomagolásról. Mérje le a pontos méreteket.',
  },
  {
    id: 2,
    title: 'Online feladás',
    description:
      'Adja meg az adatokat weboldalunkon, fizesse ki a szállítást, és nyomtassa ki a címkét pár kattintással.',
  },
  {
    id: 3,
    title: 'Gyors kézbesítés',
    description:
      'Futárunk felveszi a küldeményt, és mi a legrövidebb úton eljuttatjuk a címzetthez.',
    isHighlighted: true,
  },
];

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const ProcessSection = ({ className }: ProcessSectionProps) => {
  return (
    <section className={cn('py-32 px-8 overflow-hidden bg-surface-container-low', className)}>
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-24">
          <h2 className="font-headline text-4xl font-extrabold text-on-surface mb-4 tracking-tight">Hogyan működik?</h2>
          <p className="text-on-surface-variant font-medium text-lg">
            Egyszerű, átlátható folyamat a feladástól az érkezésig.
          </p>
        </div>

        <div className="relative">
          <div className="absolute top-1/2 left-0 w-full h-1 bg-surface-container-high hidden md:block -translate-y-1/2 rounded-full overflow-hidden">
            <div className="h-full kinetic-gradient w-1/2" />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-16 relative">
            {steps.map((step) => (
              <article key={step.id} className="flex flex-col items-center text-center">
                <div
                  className={cn(
                    'w-20 h-20 rounded-full shadow-2xl flex items-center justify-center z-10 mb-8 border-4 border-surface ring-8 ring-surface-container-low',
                    step.isHighlighted ? 'kinetic-gradient' : 'bg-surface-container-lowest'
                  )}
                >
                  <span className={cn('text-2xl font-black text-on-surface', step.isHighlighted && 'text-on-primary')}>
                    {step.id}
                  </span>
                </div>

                <h3 className="text-xl font-bold text-on-surface mb-4">{step.title}</h3>
                <p className="text-sm font-medium text-on-surface-variant leading-relaxed max-w-xs">{step.description}</p>
              </article>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

