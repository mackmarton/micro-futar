import type { Metadata } from 'next';
import Link from 'next/link';

export const metadata: Metadata = {
  title: 'Page not found',
  robots: { index: false, follow: true },
};

export default function NotFound() {
  return (
    <main className="min-h-[70vh] flex flex-col items-center justify-center gap-6 px-8 text-center">
      <h1 className="font-headline text-5xl font-extrabold tracking-tight text-on-surface">404</h1>
      <p className="text-on-surface-variant text-lg">The page you&apos;re looking for could not be found.</p>
      <Link
        href="/en"
        className="kinetic-gradient text-on-primary px-6 py-3 rounded-lg font-bold text-sm"
      >
        Back to homepage
      </Link>
    </main>
  );
}
