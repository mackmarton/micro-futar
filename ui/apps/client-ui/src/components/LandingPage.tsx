import { HeroSection } from './HeroSection';
import { LandingNavBar } from './LandingNavBar';
import { ProcessSection } from './ProcessSection';

export type LandingPageProps = {
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const LandingPage = ({ className }: LandingPageProps) => {
  return (
    <div className={cn('bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed', className)}>
      <LandingNavBar />

      <main className="pt-24">
        <HeroSection />
        <ProcessSection />
      </main>

    </div>
  );
};

