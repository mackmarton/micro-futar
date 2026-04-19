import { useState, type FormEvent } from 'react';

export type HeroSectionProps = {
  className?: string;
  imageSrc?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const DEFAULT_IMAGE_SRC =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuB2YsZ2Xlw_8ME8eviYMFyG77zf6V8kYNaHqoJcduUD6-kb0p3trS2-KHvo5I7U6Un2aGlsOrNETv3hyeX1IXDRI57QLbYubJ0DbaqEz5LcQ_csKKAHa3V47cuahEfkTjH70iMoad989MBjwn_9mJbPnTiFra3rxISevfF0TRYr1rMx1ubZoIbQZgIK9Z3nKrgmEjpUpLutF-p3Otr8zyY4FMj4sM2vHIkDJm6vM1mxM2AG49q_wOBpFTn_aZfbot-Scv3tk3SWEBM';

export const HeroSection = ({ className, imageSrc = DEFAULT_IMAGE_SRC }: HeroSectionProps) => {
  const [trackingNumber, setTrackingNumber] = useState('');

  const navigateToTracking = (value: string) => {
    const trimmedValue = value.trim();

    if (trimmedValue) {
      const query = new URLSearchParams({ trackingNumber: trimmedValue }).toString();
      window.location.hash = `/portal/tracking?${query}`;
      return;
    }

    window.location.hash = '/portal/tracking';
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    navigateToTracking(trackingNumber);
  };

  return (
    <section className={cn('relative px-8 pt-16 pb-32 overflow-hidden', className)}>
      <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
        <div className="z-10">
          <span className="inline-block px-4 py-1.5 rounded-full bg-primary-fixed text-on-primary-fixed-variant text-xs font-bold uppercase tracking-wider mb-6">
            Prémium Logisztika
          </span>

          <h1 className="font-headline text-6xl md:text-7xl font-extrabold text-on-surface leading-[1.1] mb-8 tracking-tight">
            Gyorsaság. <br />
            Biztonság. <br />
            <span className="text-on-primary-container">Precizitás.</span>
          </h1>

          <form
            className="bg-surface-container-lowest p-2 rounded-xl shadow-2xl shadow-on-surface/10 flex flex-col md:flex-row gap-2 max-w-xl"
            onSubmit={handleSubmit}
          >
            <div className="flex-1 flex items-center px-4 gap-3">
              <span className="material-symbols-outlined text-outline" aria-hidden="true">
                location_searching
              </span>
              <input
                type="text"
                value={trackingNumber}
                onChange={(event) => setTrackingNumber(event.target.value)}
                placeholder="Csomagkövetés (pl. MF-12345678)"
                className="w-full border-none focus:ring-0 text-on-surface font-medium bg-transparent py-3"
              />
            </div>

            <button
              type="submit"
              className="kinetic-gradient text-on-primary px-8 py-4 rounded-lg font-bold text-sm flex items-center justify-center gap-2"
            >
              <span>Keresés</span>
              <span className="material-symbols-outlined text-sm" aria-hidden="true">
                arrow_forward
              </span>
            </button>
          </form>
        </div>

        <div className="relative lg:h-[600px] rounded-3xl overflow-hidden shadow-2xl">
          <img
            src={imageSrc}
            alt="Modern logisztikai raktár automatizált polcrendszerrel"
            className="w-full h-full object-cover"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-primary/40 to-transparent" />
        </div>
      </div>
    </section>
  );
};

