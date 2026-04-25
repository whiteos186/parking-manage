# OpenClaw API Key 清理记录

更新日期：2026-04-18

## 1. 目标

清理本机 OpenClaw 配置中保存的 API key，避免继续通过旧的 API key 访问模型，同时保留后续切换到 ChatGPT OAuth 登录的空间。

## 2. 已完成操作

已从以下位置删除或清空 API key / API-key 认证档案：

- `C:\Users\Administrator\.openclaw\openclaw.json`
- `C:\Users\Administrator\.openclaw\openclaw.json.bak`
- `C:\Users\Administrator\.openclaw\agents\main\agent\models.json`
- `C:\Users\Administrator\.openclaw\agents\main\agent\auth-profiles.json`

同时处理了历史残留：

- 对两份 `sessions/*.jsonl.reset.*` 历史会话文件中的真实 key 做了脱敏替换
- 检查了当前 shell / 用户 / 机器环境变量，未发现 `OPENAI_API_KEY` 或 `ANTHROPIC_API_KEY`

## 3. 核验结果

核验方式：

- 在 `C:\Users\Administrator\.openclaw` 下按旧 key 精确搜索
- 复查当前 OpenClaw 主配置和 agent 配置
- 执行 `openclaw models status`

当前结果：

- 旧的 Anthropic key 已不存在
- 旧的 OpenAI key 已不存在
- OpenClaw 当前无可用的 API-key 认证档案

`openclaw models status` 的关键状态如下：

- 默认模型仍为 `openai/gpt-5.4`
- `Providers w/ OAuth/tokens (0): -`
- `Missing auth: openai`

这说明 OpenClaw 现在不会再使用之前保存的 API key。

## 4. 当前影响

由于默认模型仍然指向 `openai/gpt-5.4`，而本地已无对应 API key，所以当前 OpenClaw 不能继续按原方式调用 OpenAI 兼容接口。

这属于预期结果，目的是先把旧 key 从本机配置中移除干净。

## 5. 后续建议

如果下一步要切换到 ChatGPT 登录，建议执行：

```powershell
openclaw models auth login --provider openai-codex
openclaw config set agents.defaults.model.primary openai-codex/gpt-5.4
```

执行完成后，再用下面命令复查：

```powershell
openclaw models status
```

## 6. 说明

本次仅清理 API key 及其认证档案。

以下内容未处理：

- 企业微信渠道密钥
- Gateway token
- 其他非 API-key 类型凭据

如果后续需要，也可以单独再做一轮凭据清理和最小化配置整理。
