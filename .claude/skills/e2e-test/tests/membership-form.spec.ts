import { test, expect } from '../fixtures/test-with-console';
import { PAGES } from '../helpers/page-map';

const PAGE = PAGES.membershipForm;

test.describe(PAGE.name, () => {
  test('loads create-mode without console errors', async ({ page, navigateTo }) => {
    await navigateTo(PAGE.route);
    await page.waitForSelector(PAGE.loadedSelector, { timeout: 15_000 });

    await expect(page.locator('.el-page-header')).toBeVisible();
    await expect(page.locator('.el-form')).toBeVisible();
  });
});
