---
name: e2e-test
description: 运行 Playwright E2E 测试，自动遍历停车管理系统全部 15 个页面，捕获浏览器控制台错误和未处理异常，分析定位源码问题并修复。当用户要求跑 E2E 测试、检查前端页面报错、冒烟测试、验证页面加载是否正常时使用此 skill。
---

# E2E 自动化测试 Skill

运行 Playwright 测试覆盖全部 15 个停车管理页面，自动捕获控制台错误，分析并修复。

测试文件就在本 skill 目录下（`.claude/skills/e2e-test/`），无需额外项目目录。

## 前置条件

运行前确认：
1. **前端 dev server** 运行中：`http://localhost:80`（在 `ruoyi-ui/` 下 `npm run dev`）
2. **后端 API** 运行中：`http://localhost:8080`（Spring Boot）
3. **验证码已关闭**：`sys_config` 表中 `sys.account.captchaEnabled = 'false'`

Playwright CLI 已全局安装（`npm install -g @playwright/test`），无本地依赖。
如果 Chromium 未下载：
```bash
playwright install chromium
```

## 工作流

### Phase 1 — 运行测试

```bash
cd D:/code/parking-management-system/.claude/skills/e2e-test && NODE_PATH="$(npm root -g)" playwright test 2>&1
```

可选参数：
- 只跑某个页面：`NODE_PATH="$(npm root -g)" playwright test lot.spec.ts`
- 有头模式（可看到浏览器）：`NODE_PATH="$(npm root -g)" playwright test --headed`
- 调试模式：`NODE_PATH="$(npm root -g)" playwright test --debug`

### Phase 2 — 读取 JSON 报告

```bash
Read .claude/skills/e2e-test/reports/results.json
```

解析 JSON，关注：
- `suites[].specs[].tests[].results[].attachments` — 名为 `console-errors` 的附件包含控制台错误详情
- `suites[].specs[].tests[].results[].error.message` — 错误摘要
- `suites[].specs[].tests[].results[].status` — `passed` / `failed`

### Phase 3 — 分析和分类错误

对每个唯一的控制台错误，判断：

| 错误类型 | 特征 | 修复方向 |
|----------|------|---------|
| JS 运行时错误 | `Cannot read properties of undefined` | Vue 组件中加 optional chaining 或默认值 |
| Vue 警告 | `[Vue warn]: Error in render` | 检查 data/computed 定义 |
| API 404 | `404 Not Found on /dev-api/xxx` | 检查后端 Controller 路由或前端 API 模块 |
| API 500 | `500 Internal Server Error` | 后端问题，委托 backend agent |
| 类型错误 | `TypeError: xxx is not a function` | 检查 import 或 methods 定义 |

### Phase 4 — 修复源码

在 `ruoyi-ui/src/` 中修复，规则：
- 匹配现有代码风格（Vue 2 Options API + Element UI）
- 不添加新依赖
- 不重构无关代码
- API 错误（后端返回 500）需要检查后端端点，必要时委托 backend agent

### Phase 5 — 重跑验证

```bash
cd D:/code/parking-management-system/.claude/skills/e2e-test && NODE_PATH="$(npm root -g)" playwright test 2>&1
```

重复 Phase 2-4 直到全部通过。

### Phase 6 — 汇报

输出格式：
```
E2E 测试结果：
- 测试页面：15
- 通过：X
- 失败：Y（原 Z 个，已修复 N 个）
- 发现并修复的控制台错误：
  - [文件:行号] 错误描述 → 修复方式
  - ...
- 残留问题（如有）：
  - ...
```

## 关键文件位置

| 用途 | 路径 |
|------|------|
| Playwright 配置 | `.claude/skills/e2e-test/playwright.config.ts` |
| 登录 setup | `.claude/skills/e2e-test/tests/auth.setup.ts` |
| 控制台 fixture | `.claude/skills/e2e-test/fixtures/test-with-console.ts` |
| 页面定义 | `.claude/skills/e2e-test/helpers/page-map.ts` |
| 测试用例 | `.claude/skills/e2e-test/tests/*.spec.ts` |
| JSON 报告 | `.claude/skills/e2e-test/reports/results.json` |
| Vue 页面 | `ruoyi-ui/src/views/parking/` |
| API 模块 | `ruoyi-ui/src/api/parking/` |
| 请求工具 | `ruoyi-ui/src/utils/request.js` |

## 禁止事项

- 不要修改 `playwright.config.ts` 来跳过失败的测试 — 修复源码
- 不要在 Vue 组件中加 `try/catch` 来静默控制台错误 — 修复根因
- 不要关闭 fixture 的错误检测
