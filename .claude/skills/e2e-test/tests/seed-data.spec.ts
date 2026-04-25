/**
 * 通过页面 UI 自动新增业务测试数据
 * 运行: cd .claude/skills/e2e-test && NODE_PATH="$(npm root -g)" playwright test seed-data.spec.ts --headed
 */
import { test, expect, Page } from '@playwright/test';
import path from 'path';

const STORAGE_STATE = path.join(__dirname, '..', 'auth', 'admin.json');

test.use({ storageState: STORAGE_STATE });

// ── helpers ──────────────────────────────────────────────

/** Navigate to route via Vue Router (no full reload) */
async function go(page: Page, route: string) {
  await page.evaluate((p) => {
    const app = (document.querySelector('#app') as any).__vue__;
    app.$router.push(p).catch(() => {});
  }, route);
  await page.waitForTimeout(1000);
}

/** Click an el-select and pick an option by visible text */
async function pickSelect(page: Page, placeholder: string, optionText: string) {
  // Scope to dialog if open, otherwise page-level (for form pages)
  const dialog = page.locator('.el-dialog__body');
  const scope = await dialog.count() > 0 ? dialog.first() : page;
  const select = scope.locator(`.el-select`).filter({ has: page.locator(`input[placeholder="${placeholder}"]`) });
  await select.click();
  await page.waitForTimeout(300);
  // Dropdown renders at body level; use last() to target the dialog's dropdown (not search form's)
  const option = page.locator('.el-select-dropdown__item').filter({ hasText: optionText }).last();
  await option.click({ timeout: 5000 });
  await page.waitForTimeout(300);
}

/** Fill an el-input by placeholder, scoped to dialog if open */
async function fillInput(page: Page, placeholder: string, value: string) {
  const dialog = page.locator('.el-dialog__body');
  const scope = await dialog.count() > 0 ? dialog.first() : page;
  const input = scope.locator(`input[placeholder="${placeholder}"]`);
  await input.click();
  await input.fill(value);
}

/** Fill an el-textarea by placeholder, scoped to dialog if open */
async function fillTextarea(page: Page, placeholder: string, value: string) {
  const dialog = page.locator('.el-dialog__body');
  const scope = await dialog.count() > 0 ? dialog.first() : page;
  const textarea = scope.locator(`textarea[placeholder="${placeholder}"]`);
  await textarea.click();
  await textarea.fill(value);
}

/** Click the "新增" button in toolbar */
async function clickAdd(page: Page) {
  await page.locator('button').filter({ hasText: '新增' }).first().click();
  await page.waitForTimeout(500);
}

/** Click the primary (blue) button in dialog footer */
async function clickConfirm(page: Page) {
  await page.locator('.el-dialog__footer .el-button--primary').click();
  await page.waitForTimeout(1000);
}

/** Wait for success message */
async function waitSuccess(page: Page) {
  await page.locator('.el-message--success').first().waitFor({ timeout: 5000 });
  await page.waitForTimeout(800);
}

// ── init: load app ───────────────────────────────────────

test.beforeEach(async ({ page }) => {
  await page.goto('/');
  await page.waitForSelector('.sidebar-container', { timeout: 15000 });
});

// ── 1. 停车场 ────────────────────────────────────────────

test.describe.serial('新增业务数据', () => {

  test('1. 新增停车场', async ({ page }) => {
    await go(page, '/archives/lot');
    await page.waitForSelector('.el-table', { timeout: 10000 });

    const lots = [
      { name: '阳光花园停车场', address: '广州市天河区阳光花园小区地下一层' },
      { name: '万达广场停车场', address: '广州市白云区万达广场B1-B3层' },
      { name: '科技园停车场', address: '广州市南沙区科技园综合楼负一层' },
    ];

    for (const lot of lots) {
      await clickAdd(page);
      await fillInput(page, '请输入停车场名称', lot.name);
      await fillTextarea(page, '请输入停车场地址', lot.address);
      await clickConfirm(page);
      await waitSuccess(page);
    }
  });

  // ── 2. 车位 ──────────────────────────────────────────

  test('2. 新增车位', async ({ page }) => {
    await go(page, '/archives/space');
    await page.waitForSelector('.el-table', { timeout: 10000 });

    const spaces = [
      { lot: '阳光花园停车场', code: 'A-001', area: 'A区', floor: 'B1' },
      { lot: '阳光花园停车场', code: 'A-002', area: 'A区', floor: 'B1' },
      { lot: '阳光花园停车场', code: 'B-001', area: 'B区', floor: 'B2' },
      { lot: '万达广场停车场', code: 'M-001', area: '东区', floor: 'B1' },
      { lot: '万达广场停车场', code: 'M-002', area: '东区', floor: 'B1' },
      { lot: '科技园停车场', code: 'K-001', area: '主楼', floor: 'B1' },
    ];

    for (const sp of spaces) {
      await clickAdd(page);
      await pickSelect(page, '请选择停车场', sp.lot);
      await fillInput(page, '请输入车位编码', sp.code);
      await fillInput(page, '请输入区域', sp.area);
      await fillInput(page, '请输入楼层', sp.floor);
      await clickConfirm(page);
      await waitSuccess(page);
    }
  });

  // ── 3. 客户 ──────────────────────────────────────────

  test('3. 新增客户', async ({ page }) => {
    await go(page, '/customers/customer');
    await page.waitForSelector('.el-table', { timeout: 10000 });

    const customers = [
      { code: 'CUS-001', name: '张三', mobile: '13800138001' },
      { code: 'CUS-002', name: '李四', mobile: '13800138002' },
      { code: 'CUS-003', name: '王五', mobile: '13800138003' },
      { code: 'CUS-004', name: '赵六', mobile: '13800138004' },
    ];

    for (const c of customers) {
      await clickAdd(page);
      await fillInput(page, '请输入客户编号', c.code);
      await fillInput(page, '请输入客户名称', c.name);
      await fillInput(page, '请输入手机号', c.mobile);
      await clickConfirm(page);
      await waitSuccess(page);
    }
  });

  // ── 4. 车辆 ──────────────────────────────────────────

  test('4. 新增车辆', async ({ page }) => {
    await go(page, '/customers/vehicle');
    await page.waitForSelector('.el-table', { timeout: 10000 });

    const vehicles = [
      { customer: '张三', plate: '粤A12345', brand: '丰田卡罗拉', color: '白色' },
      { customer: '张三', plate: '粤A67890', brand: '本田雅阁', color: '黑色' },
      { customer: '李四', plate: '粤B11111', brand: '大众帕萨特', color: '银色' },
      { customer: '王五', plate: '粤A22222', brand: '比亚迪汉', color: '红色' },
      { customer: '赵六', plate: '粤B33333', brand: '特斯拉Model3', color: '白色' },
    ];

    for (const v of vehicles) {
      await clickAdd(page);
      await pickSelect(page, '请选择客户', v.customer);
      await fillInput(page, '请输入车牌号', v.plate);
      await fillInput(page, '请输入品牌名称', v.brand);
      await fillInput(page, '请输入车辆颜色', v.color);
      await clickConfirm(page);
      await waitSuccess(page);
    }
  });

  // ── 5. 月卡订单 ────────────────────────────────────────

  test('5. 新增月卡订单', async ({ page }) => {
    await go(page, '/parking/monthly/form');
    await page.waitForSelector('.el-page-header', { timeout: 10000 });

    await pickSelect(page, '请选择停车场', '阳光花园停车场');
    await pickSelect(page, '请选择客户', '张三');
    await page.waitForTimeout(500);
    await pickSelect(page, '请选择车辆', '粤A12345');

    // monthCount — el-input-number, set to 3
    const monthInput = page.locator('.el-input-number input').first();
    await monthInput.click({ clickCount: 3 });
    await monthInput.fill('3');

    // Submit
    await page.locator('.el-form + .el-form-item .el-button--primary, .el-card .el-button--primary').first().click();
    await waitSuccess(page);
  });

  // ── 6. 临停订单 ────────────────────────────────────────

  test('6. 新增临停订单', async ({ page }) => {
    await go(page, '/parking/temp/form');
    await page.waitForSelector('.el-page-header', { timeout: 10000 });

    await pickSelect(page, '请选择停车场', '万达广场停车场');
    await fillInput(page, '请输入车牌号', '粤B55555');

    await page.locator('.el-form + .el-form-item .el-button--primary, .el-card .el-button--primary').first().click();
    await waitSuccess(page);
  });

  // ── 7. 会员订单 ────────────────────────────────────────

  test('7. 新增会员订单', async ({ page }) => {
    await go(page, '/parking/membership/form');
    await page.waitForSelector('.el-page-header', { timeout: 10000 });

    await pickSelect(page, '请选择停车场', '阳光花园停车场');
    await pickSelect(page, '请选择客户', '李四');
    await page.waitForTimeout(500);
    await pickSelect(page, '请选择车辆', '粤B11111');
    // Pick membership type — use the dict label directly
    await pickSelect(page, '请选择会员类型', '金卡');

    await page.locator('.el-form + .el-form-item .el-button--primary, .el-card .el-button--primary').first().click();
    await waitSuccess(page);
  });

  // ── 8. 更多客户 ──────────────────────────────────────

  test('8. 补充客户数据', async ({ page }) => {
    await go(page, '/customers/customer');
    await page.waitForSelector('.el-table', { timeout: 10000 });

    const customers = [
      { code: 'CUS-005', name: '周杰', mobile: '13900139005' },
      { code: 'CUS-006', name: '吴芳', mobile: '13900139006' },
      { code: 'CUS-007', name: '郑强', mobile: '13900139007' },
    ];

    for (const c of customers) {
      await clickAdd(page);
      await fillInput(page, '请输入客户编号', c.code);
      await fillInput(page, '请输入客户名称', c.name);
      await fillInput(page, '请输入手机号', c.mobile);
      await clickConfirm(page);
      await waitSuccess(page);
    }
  });

  // ── 9. 更多车辆 ──────────────────────────────────────

  test('9. 补充车辆数据', async ({ page }) => {
    await go(page, '/customers/vehicle');
    await page.waitForSelector('.el-table', { timeout: 10000 });

    const vehicles = [
      { customer: '周杰', plate: '粤A77701', brand: '宝马3系', color: '蓝色' },
      { customer: '吴芳', plate: '粤B88802', brand: '奔驰C级', color: '黑色' },
      { customer: '郑强', plate: '粤A99903', brand: '奥迪A4L', color: '灰色' },
    ];

    for (const v of vehicles) {
      await clickAdd(page);
      await pickSelect(page, '请选择客户', v.customer);
      await fillInput(page, '请输入车牌号', v.plate);
      await fillInput(page, '请输入品牌名称', v.brand);
      await fillInput(page, '请输入车辆颜色', v.color);
      await clickConfirm(page);
      await waitSuccess(page);
    }
  });

  // ── 10. 更多月卡订单 ──────────────────────────────────

  test('10. 补充月卡订单', async ({ page }) => {
    await go(page, '/parking/monthly/form');
    await page.waitForSelector('.el-page-header', { timeout: 10000 });

    await pickSelect(page, '请选择停车场', '万达广场停车场');
    await pickSelect(page, '请选择客户', '王五');
    await page.waitForTimeout(500);
    await pickSelect(page, '请选择车辆', '粤A22222');
    const monthInput = page.locator('.el-input-number input').first();
    await monthInput.click({ clickCount: 3 });
    await monthInput.fill('6');

    await page.locator('.el-form + .el-form-item .el-button--primary, .el-card .el-button--primary').first().click();
    await waitSuccess(page);
  });

  // ── 11. 更多临停订单 ──────────────────────────────────

  test('11. 补充临停订单', async ({ page }) => {
    const plates = ['粤A77701', '粤B88802', '粤A99903'];
    const lots = ['阳光花园停车场', '科技园停车场', '万达广场停车场'];

    for (let i = 0; i < plates.length; i++) {
      await go(page, '/parking/temp/form');
      await page.waitForSelector('.el-page-header', { timeout: 10000 });
      await pickSelect(page, '请选择停车场', lots[i]);
      await fillInput(page, '请输入车牌号', plates[i]);
      await page.locator('.el-form + .el-form-item .el-button--primary, .el-card .el-button--primary').first().click();
      await waitSuccess(page);
    }
  });

  // ── 12. 更多会员订单 ──────────────────────────────────

  test('12. 补充会员订单', async ({ page }) => {
    // 王五 - 铂金
    await go(page, '/parking/membership/form');
    await page.waitForSelector('.el-page-header', { timeout: 10000 });
    await pickSelect(page, '请选择停车场', '万达广场停车场');
    await pickSelect(page, '请选择客户', '王五');
    await page.waitForTimeout(500);
    await pickSelect(page, '请选择车辆', '粤A22222');
    await pickSelect(page, '请选择会员类型', '铂金');
    await page.locator('.el-form + .el-form-item .el-button--primary, .el-card .el-button--primary').first().click();
    await waitSuccess(page);

    // 赵六 - 银卡
    await go(page, '/parking/membership/form');
    await page.waitForSelector('.el-page-header', { timeout: 10000 });
    await pickSelect(page, '请选择停车场', '科技园停车场');
    await pickSelect(page, '请选择客户', '赵六');
    await page.waitForTimeout(500);
    await pickSelect(page, '请选择车辆', '粤B33333');
    await pickSelect(page, '请选择会员类型', '银卡');
    await page.locator('.el-form + .el-form-item .el-button--primary, .el-card .el-button--primary').first().click();
    await waitSuccess(page);
  });
});
