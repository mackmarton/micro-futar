'use client';

import { useEffect, useRef, type ReactNode } from 'react';

export type RevealOnScrollProps = {
  className?: string;
  children: ReactNode;
};

/**
 * Görgetésre induló belépő animáció a `.reveal-*` osztályú gyerekeknek (lásd index.css).
 *
 * Az előre renderelt HTML-ben nincs `data-reveal`, így a tartalom JS nélkül (és a keresőrobotok
 * számára) is látható. Csak akkor rejtjük el, ha mountoláskor még a látótéren kívül van, és
 * akkor jelenik meg animálva, amikor a felhasználó odagörget.
 */
export const RevealOnScroll = ({ className, children }: RevealOnScrollProps) => {
  const containerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const container = containerRef.current;

    if (!container || window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      return;
    }

    if (container.getBoundingClientRect().top < window.innerHeight) {
      return;
    }

    container.dataset.reveal = 'pending';

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry?.isIntersecting) {
          container.dataset.reveal = 'visible';
          observer.disconnect();
        }
      },
      { threshold: 0.25 },
    );

    observer.observe(container);

    return () => observer.disconnect();
  }, []);

  return (
    <div ref={containerRef} className={className}>
      {children}
    </div>
  );
};
