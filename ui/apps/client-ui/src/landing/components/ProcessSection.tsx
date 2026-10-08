import type { CSSProperties } from 'react';
import { useTranslation } from 'react-i18next';
import { cn } from '@package/shared-ui/cn';
import { RevealOnScroll } from './RevealOnScroll.tsx';

type ProcessStep = {
  id: number;
  titleKey: string;
  descriptionKey: string;
  isHighlighted?: boolean;
};

export type ProcessSectionProps = {
  className?: string;
};

const steps: ProcessStep[] = [
  { id: 1, titleKey: 'process.step1Title', descriptionKey: 'process.step1Description' },
  { id: 2, titleKey: 'process.step2Title', descriptionKey: 'process.step2Description' },
  { id: 3, titleKey: 'process.step3Title', descriptionKey: 'process.step3Description', isHighlighted: true },
];

// Az összekötő vonal lineárisan töltődik ki; minden lépés akkor jelenik meg, amikor a vonal
// eléri az oszlopa közepét (1/6, 3/6, 5/6 szélességnél).
const LINE_DELAY_MS = 300;
const LINE_DURATION_MS = 1500;
const stepRevealDelayMs = (index: number) => LINE_DELAY_MS + (LINE_DURATION_MS * (2 * index + 1)) / 6;
const revealDelay = (delayMs: number) => ({ '--reveal-delay': `${Math.round(delayMs)}ms` }) as CSSProperties;

export const ProcessSection = ({ className }: ProcessSectionProps) => {
  const { t } = useTranslation('landing');

  return (
    <section className={cn('py-32 px-8 overflow-hidden bg-surface-container-low', className)}>
      <RevealOnScroll className="max-w-7xl mx-auto">
        <div className="text-center mb-24 reveal-fade-up">
          <h2 className="font-headline text-4xl font-extrabold text-on-surface mb-4 tracking-tight">{t('process.heading')}</h2>
          <p className="text-on-surface-variant font-medium text-lg">
            {t('process.subtitle')}
          </p>
        </div>

        <div className="relative">
          <div className="absolute top-10 left-0 w-full h-1 bg-surface-container-high hidden md:block -translate-y-1/2 rounded-full overflow-hidden">
            <div
              className="h-full kinetic-gradient reveal-line"
              style={{ ...revealDelay(LINE_DELAY_MS), '--reveal-duration': `${LINE_DURATION_MS}ms` } as CSSProperties}
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-16 relative">
            {steps.map((step, index) => (
              <article key={step.id} className="flex flex-col items-center text-center">
                <div
                  className={cn(
                    'w-20 h-20 rounded-full shadow-2xl flex items-center justify-center z-10 mb-8 border-4 border-surface ring-8 ring-surface-container-low reveal-pop',
                    step.isHighlighted ? 'kinetic-gradient reveal-pulse' : 'bg-surface-container-lowest'
                  )}
                  style={revealDelay(stepRevealDelayMs(index))}
                >
                  <span className={cn('text-2xl font-black', step.isHighlighted ? 'text-on-primary' : 'text-on-surface')}>
                    {step.id}
                  </span>
                </div>

                <div className="reveal-fade-up" style={revealDelay(stepRevealDelayMs(index) + 150)}>
                  <h3 className="text-xl font-bold text-on-surface mb-4">{t(step.titleKey)}</h3>
                  <p className="text-sm font-medium text-on-surface-variant leading-relaxed max-w-xs">{t(step.descriptionKey)}</p>
                </div>
              </article>
            ))}
          </div>
        </div>
      </RevealOnScroll>
    </section>
  );
};

