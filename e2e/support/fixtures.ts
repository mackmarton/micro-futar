import { test as base, type BrowserContext, type Page } from '@playwright/test';
import { storageStatePath, type Role } from './env.ts';
import { mockExternalServices } from './external-mocks.ts';

type Fixtures = {
  /** A page in its own browser context, already logged in as the role (or not logged in, for a guest). */
  pageAs: (role: Role | 'guest') => Promise<Page>;
};

export const test = base.extend<Fixtures>({
  pageAs: async ({ browser, contextOptions }, use, testInfo) => {
    const opened: { role: string; context: BrowserContext; page: Page }[] = [];

    await use(async (role) => {
      // Trace and screenshots cover these contexts automatically, videos have to be requested.
      const context = await browser.newContext({
        ...contextOptions,
        storageState: role === 'guest' ? undefined : storageStatePath(role),
        recordVideo: { dir: testInfo.outputPath('videos') },
      });
      await mockExternalServices(context);
      const page = await context.newPage();
      opened.push({ role, context, page });
      return page;
    });

    const failed = testInfo.status !== testInfo.expectedStatus;
    for (const { role, context, page } of opened) {
      await context.close();
      const video = page.video();
      if (!video) continue;
      if (failed) await testInfo.attach(`video-${role}`, { path: await video.path(), contentType: 'video/webm' });
      else await video.delete();
    }
  },
});

export { expect } from '@playwright/test';
