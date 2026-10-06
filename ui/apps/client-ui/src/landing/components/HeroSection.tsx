'use client';

import { useState, type CSSProperties, type FormEvent } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { cn } from '@package/shared-ui';

export type HeroSectionProps = {
  className?: string;
  imageSrc?: string;
};

const DEFAULT_IMAGE_SRC = '/storage.webp';

const HEADLINE_LINES = [
  { text: 'Gyorsaság.' },
  { text: 'Biztonság.' },
  { text: 'Precizitás.', className: 'text-on-primary-container' },
];
const HEADLINE_FIRST_DELAY_MS = 100;
const HEADLINE_STAGGER_MS = 140;

export const HeroSection = ({ className, imageSrc = DEFAULT_IMAGE_SRC }: HeroSectionProps) => {
  const router = useRouter();
  const [trackingNumber, setTrackingNumber] = useState('');

  const navigateToTracking = (value: string) => {
    const trimmedValue = value.trim();

    if (trimmedValue) {
      const query = new URLSearchParams({ trackingNumber: trimmedValue }).toString();
      router.push(`/portal/tracking?${query}`);
      return;
    }

    router.push('/portal/tracking');
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
            {HEADLINE_LINES.map((line, index) => (
              <span key={line.text} className="hero-line">
                <span
                  className={cn('hero-line-inner', line.className)}
                  style={{ '--reveal-delay': `${HEADLINE_FIRST_DELAY_MS + index * HEADLINE_STAGGER_MS}ms` } as CSSProperties}
                >
                  {line.text}
                </span>{' '}
              </span>
            ))}
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
          <Image
            src={imageSrc}
            alt="Modern logisztikai raktár automatizált polcrendszerrel"
            width={512}
            height={512}
            sizes="(min-width: 1024px) 50vw, 100vw"
            priority
            className="w-full h-full object-cover"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-primary/40 to-transparent" />
        </div>
      </div>
    </section>
  );
};

