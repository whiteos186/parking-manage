import { test as base, Page } from '@playwright/test';

export interface ConsoleEntry {
  type: string;
  text: string;
  url: string;
  timestamp: number;
}

export interface PageErrorEntry {
  message: string;
  stack?: string;
  url: string;
  timestamp: number;
}

export interface ConsoleReport {
  testTitle: string;
  consoleErrors: ConsoleEntry[];
  consoleWarnings: ConsoleEntry[];
  pageErrors: PageErrorEntry[];
}

/** Known benign patterns that should not fail tests */
const IGNORE_PATTERNS = [
  'favicon.ico',
  'ResizeObserver loop',
  'Download the Vue Devtools',
  'You are running Vue in development mode',
];

function isBenign(text: string): boolean {
  return IGNORE_PATTERNS.some((p) => text.includes(p));
}

export const test = base.extend<{
  consoleReport: ConsoleReport;
  navigateTo: (route: string) => Promise<void>;
}>({
  /**
   * Load the SPA once via `/`, wait for dynamic routes to be generated,
   * then provide a client-side navigation helper that uses Vue Router push
   * (avoids full page reload which loses dynamic routes and hits 404).
   */
  navigateTo: async ({ page }, use) => {
    // Initial full load — triggers permission guard → GetInfo → GenerateRoutes
    await page.goto('/');
    await page.waitForSelector('.sidebar-container', { timeout: 15_000 });

    const nav = async (route: string) => {
      await page.evaluate((path) => {
        const app = (document.querySelector('#app') as any).__vue__;
        app.$router.push(path).catch(() => {});
      }, route);
      // Wait for Vue Router navigation + component render
      await page.waitForTimeout(1000);
    };

    await use(nav);
  },

  consoleReport: [async ({ page }, use, testInfo) => {
    const report: ConsoleReport = {
      testTitle: testInfo.title,
      consoleErrors: [],
      consoleWarnings: [],
      pageErrors: [],
    };

    page.on('console', (msg) => {
      const entry: ConsoleEntry = {
        type: msg.type(),
        text: msg.text(),
        url: page.url(),
        timestamp: Date.now(),
      };
      if (msg.type() === 'error') {
        report.consoleErrors.push(entry);
      } else if (msg.type() === 'warning') {
        report.consoleWarnings.push(entry);
      }
    });

    page.on('pageerror', (error) => {
      report.pageErrors.push({
        message: error.message,
        stack: error.stack,
        url: page.url(),
        timestamp: Date.now(),
      });
    });

    await use(report);

    // Attach full report as JSON artifact
    if (report.consoleErrors.length > 0 || report.pageErrors.length > 0) {
      await testInfo.attach('console-errors', {
        body: JSON.stringify({
          consoleErrors: report.consoleErrors,
          pageErrors: report.pageErrors,
        }, null, 2),
        contentType: 'application/json',
      });
    }

    // Filter benign, then fail on real errors
    const realErrors = report.consoleErrors.filter((e) => !isBenign(e.text));
    const realPageErrors = report.pageErrors.filter((e) => !isBenign(e.message));

    if (realErrors.length > 0 || realPageErrors.length > 0) {
      const summary = [
        ...realErrors.map((e) => `[console.error] ${e.text}`),
        ...realPageErrors.map((e) => `[pageerror] ${e.message}`),
      ].join('\n');

      testInfo.annotations.push({
        type: 'console-errors',
        description: summary,
      });

      throw new Error(
        `Page had ${realErrors.length} console error(s) and ` +
        `${realPageErrors.length} uncaught exception(s):\n${summary}`
      );
    }
  }, { auto: true }],
});

export { expect } from '@playwright/test';
