import { defineConfig, devices } from '@playwright/test';

// The stack of the repo's docker-compose.yml has to be running; see README.md.
export default defineConfig({
  testDir: './tests',
  // One stack, shared data: the scenarios run one after the other.
  workers: 1,
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  // No retries: a flaky test has to be noticed and fixed.
  retries: 0,
  timeout: 90_000,
  // Replication over Kafka is asynchronous.
  expect: { timeout: 15_000 },
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    ...devices['Desktop Chrome'],
    locale: 'hu-HU',
    timezoneId: 'Europe/Budapest',
    // The courier UI is a PWA; its service worker would bypass page.route.
    serviceWorkers: 'block',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    // The browser has to reach `keycloak` even where /etc/hosts is not set up.
    launchOptions: { args: ['--host-resolver-rules=MAP keycloak 127.0.0.1'] },
  },
  projects: [
    { name: 'auth', testMatch: 'setup/auth.setup.ts' },
    { name: 'data', testMatch: 'setup/data.setup.ts', dependencies: ['auth'] },
    { name: 'e2e', testMatch: '*.spec.ts', dependencies: ['data'] },
  ],
});
