'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

/**
 * A korábbi (Vite + HashRouter) verzió `/#/portal/...` alakú URL-eket használt. A hash
 * nem jut el a szerverig, így ezek a linkek (könyvjelzők, kiküldött e-mailek) mindig
 * a főoldalra érkeznek; innen irányítjuk át őket az új, útvonal alapú címre.
 */
export const LegacyHashRedirect = () => {
  const router = useRouter();

  useEffect(() => {
    const { hash } = window.location;

    if (hash.startsWith('#/') && hash.length > 2) {
      router.replace(hash.slice(1));
    }
  }, [router]);

  return null;
};
