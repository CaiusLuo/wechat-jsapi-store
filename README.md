# WeChat JSAPI Store

微信 JSAPI 商城示例项目，包含 H5 端、管理员后台、微信公众号 OAuth 登录、微信支付 JSAPI 下单和回调处理。

## 需求手稿
<img width="2731" height="1320" alt="image" src="https://github.com/user-attachments/assets/ddd12b95-d2e9-48e2-bbc3-e67ce7ac8454" />

## 功能

- H5 商品浏览、详情、下单、支付、订单列表和订单详情
- 微信公众号 OAuth 登录和 H5 登录态
- 用户收货信息维护
- 管理员登录、商品管理、订单管理、H5 配置管理
- 订单发货、完成、取消和统计概览
- 管理员文件上传

## 技术栈

- 后端：Java 21、Spring Boot 3.5.14、Spring Security、MyBatis-Plus、Knife4j / SpringDoc
- 微信集成：WxJava MP、wechatpay-java
- 前端：Vue 3、TypeScript、Vite、Pinia、Element Plus、Vant、axios
- 测试：JUnit 5、Vitest、Playwright

## 目录

```text
backend/                  Spring Boot 后端
frontend/                 Vue 3 前端
deploy/nginx.example.conf 示例 Nginx 配置
scripts/start.sh          本地启动脚本
scripts/end.sh            本地停止脚本
package.sh                发布包脚本
```

## 本地运行

1. 复制环境文件并填写必填项：

```bash
cp .env.example .env
```

2. 启动本地全量构建和后端：

```bash
./scripts/start.sh
```

3. 前端热更新开发：

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm dev
```

4. 停止后端：

```bash
./scripts/end.sh
```

前端默认请求 `/api`；需要连接独立后端时，再设置 `VITE_API_BASE_URL`。

## 环境变量

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring 运行环境（dev/prod） |
| `DB_HOST` | `127.0.0.1` | 数据库地址 |
| `DB_PORT` | `3306` | 数据库端口 |
| `DB_NAME` | `wechat_store` | 数据库名 |
| `DB_USERNAME` | 无 | 数据库用户 |
| `DB_PASSWORD` | 无 | 数据库密码 |
| `ADMIN_USERNAME` | 无 | 管理员账号 |
| `ADMIN_PASSWORD` | 无 | 管理员密码 |
| `ADMIN_TOKEN_SECRET` | 无 | 管理员 Token 密钥 |
| `ADMIN_TOKEN_TTL_SECONDS` | `604800` | 管理员 Token 有效期 |
| `JWT_SECRET` | 无 | H5 Token 密钥 |
| `H5_AUTH_TTL_SECONDS` | `604800` | H5 登录态有效期 |
| `WECHAT_MP_APP_ID` | 无 | 微信公众号 AppID |
| `WECHAT_MP_SECRET` | 无 | 微信公众号 AppSecret |
| `WECHAT_MP_TOKEN` | 空 | 公众号消息校验 Token |
| `WECHAT_MP_AES_KEY` | 空 | 公众号消息加解密密钥 |
| `WECHAT_MP_OAUTH_CALLBACK_URL` | 无 | OAuth 回调地址 |
| `WECHAT_PAY_APP_ID` | 无 | 微信支付 AppID |
| `WECHAT_PAY_MCH_ID` | 无 | 微信支付商户号 |
| `WECHAT_PAY_API_V3_KEY` | 无 | 微信支付 API v3 密钥 |
| `WECHAT_PAY_PRIVATE_KEY_PATH` | 无 | 商户私钥文件路径 |
| `WECHAT_PAY_SERIAL_NO` | 无 | 商户证书序列号 |
| `WECHAT_PAY_NOTIFY_URL` | 无 | 支付通知地址 |
| `UPLOAD_BASE_DIR` | `./uploads` | 上传目录 |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | CORS 白名单 |
| `SPRINGDOC_ENABLED` | `false` | OpenAPI / Swagger 开关 |

## 构建与测试

```bash
cd backend && ./mvnw test
cd frontend && pnpm run type-check
cd frontend && pnpm run test:unit -- --run
cd frontend && pnpm run build
./package.sh
```

## 备注

- `scripts/start.sh` 会在未覆盖时把 `SPRINGDOC_ENABLED` 设为 `true`
- 微信支付私钥需放在仓库外部
- `backend/src/main/resources/sql/init.sql` 只用于全新空库初始化
