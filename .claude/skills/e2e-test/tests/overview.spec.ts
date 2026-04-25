import { test, expect } from '../fixtures/test-with-console';
import { PAGES } from '../helpers/page-map';

const PAGE = PAGES.overview;

test.describe(PAGE.name, () => {
  test('loads without console errors', async ({ page, navigateTo }) => {
    await navigateTo(PAGE.route);
    await page.waitForSelector(PAGE.loadedSelector, { timeout: 15_000 });

    await expect(page.locator('.stat-card').first()).toBeVisible();
    await expect(page.locator('.quick-grid')).toBeVisible();
  });
});
