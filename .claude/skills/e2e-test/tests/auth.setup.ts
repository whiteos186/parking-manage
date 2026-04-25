import { test as setup, expect } from '@playwright/test';
import path from 'path';

const STORAGE_STATE = path.join(__dirname, '..', 'auth', 'admin.json');

setup('authenticate as admin', async ({ page, request }) => {
  // Step 1: Check captcha state
  const captchaRes = await request.get('/dev-api/captchaImage');
  const captchaData = await captchaRes.json();

  if (captchaData.captchaEnabled) {
    throw new Error(
      'Captcha is enabled. Disable it before running E2E tests:\n' +
      "  UPDATE sys_config SET config_value = 'false' " +
      "WHERE config_key = 'sys.account.captchaEnabled';"
    );
  }

  // Step 2: Login via API (captcha disabled, code/uuid empty)
  const loginRes = await request.post('/dev-api/login', {
    data: {
      username: 'admin',
      password: 'admin123',
      code: '',
      uuid: '',
    },
  });
  expect(loginRes.ok()).toBeTruthy();

  const loginData = await loginRes.json();
  const token = loginData.token;
  expect(token).toBeTruthy();

  // Step 3: Navigate to app root to establish page context
  await page.goto('/');

  // Step 4: Set Admin-Token cookie (matches js-cookie behavior)
  const hostname = new URL(page.url()).hostname;
  await page.context().addCookies([
    {
      name: 'Admin-Token',
      value: token,
      domain: hostname,
      path: '/',
    },
  ]);

  // Step 5: Reload to trigger permission guard → GetInfo → GenerateRoutes
  await page.goto('/');

  // Step 6: Dismiss any dialog (e.g. initial password change prompt)
  const dialog = page.locator('.el-message-box');
  if (await dialog.isVisible({ timeout: 3000 }).catch(() => false)) {
    await page.locator('.el-message-box__btns .el-button--default').click();
  }

  // Step 7: Wait for app to be fully loaded
  await page.waitForSelector('.app-container', { timeout: 15_000 });

  // Step 8: Save authenticated state
  await page.context().storageState({ path: STORAGE_STATE });
});
