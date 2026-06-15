# WeChat JSAPI Store

一个基于微信公众号 OAuth、微信支付 JSAPI、Spring Boot 和 Vue 3 实现的轻量级 H5 商品订购系统。

项目包含用户端 H5、订单支付流程和后台管理功能，适合作为微信生态内轻量商城的参考实现。

本仓库不包含任何真实商户配置、微信密钥、证书或生产数据。

## 功能列表

- 微信公众号 OAuth 登录与 H5 登录态
- 商品展示、价格与库存管理
- 收货资料维护和订单创建
- 微信支付 JSAPI 下单与支付回调
- 用户订单查询
- 管理员登录、商品管理、订单管理和配送信息维护
- H5 页面文案与服务信息配置
- 管理后台销售概览

## 技术栈

- 后端：Java 21、Spring Boot 3.5、Spring Security、MyBatis-Plus
- 数据库：MySQL 5.6+，MySQL Connector/J 8.0.33
- 微信集成：WxJava MP、微信支付 Java SDK
- 前端：Vue 3、TypeScript、Vite、Pinia、Vant、Element Plus
- 测试：JUnit 5、Mockito、Vitest、Playwright

## 系统架构

```text
WeChat browser
    |
    v
Nginx or another reverse proxy
    |-- static H5 and admin assets
    |-- /api/* ----------------------> Spring Boot
                                         |-- MySQL
                                         |-- WeChat MP OAuth
                                         `-- WeChat Pay JSAPI
```

前后端同源部署时，浏览器不会触发跨域请求。分离部署时，需要通过 `APP_CORS_ALLOWED_ORIGINS` 明确列出前端来源。

## 目录结构

```text
backend/                         Spring Boot application
  src/main/resources/sql/       Initial schema and reviewed migrations
frontend/                        Vue 3 application
deploy/nginx.example.conf        Example reverse-proxy configuration
docs/mysql56-compat.md           MySQL 5.6 verification guide
scripts/start.sh                 Local build and backend start helper
scripts/end.sh                   Local backend stop helper
package.sh                       Generic release-package builder
OPEN_SOURCE_READINESS.md         Publication checklist
SECURITY.md                      Security reporting guidance
```

## 环境要求

- JDK 21
- Node.js 20.19+ or 22.12+
- pnpm compatible with `frontend/pnpm-lock.yaml`
- MySQL 5.6+
- Maven Wrapper dependencies can be downloaded or are already cached
- A configured WeChat official account and WeChat Pay merchant account for real payment flows

## 本地运行

1. Create an empty database and a dedicated non-root database user.
2. Initialize the schema:

```bash
mysql -u wechat_store_app -p wechat_store \
  < backend/src/main/resources/sql/init.sql
```

3. Create local environment configuration:

```bash
cp .env.example .env
```

Replace every `replace_with_*` value. The checked-in examples are not usable credentials.

4. Start the backend and build the frontend:

```bash
./scripts/start.sh
```

For frontend hot reload:

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm dev
```

Stop the locally started backend:

```bash
./scripts/end.sh
```

## 环境变量

| Variable | Required | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | No | Defaults to `dev`; set `prod` explicitly in production |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | No | Database endpoint; defaults target local `wechat_store` |
| `DB_USERNAME`, `DB_PASSWORD` | Yes | Dedicated application database account |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Yes | Admin login |
| `ADMIN_TOKEN_SECRET` | Yes | Admin token signing secret, at least 32 random characters |
| `JWT_SECRET` | Yes | H5 token signing secret, at least 32 random characters |
| `H5_AUTH_TTL_SECONDS` | No | H5 token lifetime |
| `WECHAT_MP_APP_ID`, `WECHAT_MP_SECRET` | Yes | Official account OAuth credentials |
| `WECHAT_MP_TOKEN`, `WECHAT_MP_AES_KEY` | No | Official account message verification fields |
| `WECHAT_MP_OAUTH_CALLBACK_URL` | Yes | OAuth callback URL |
| `WECHAT_PAY_APP_ID`, `WECHAT_PAY_MCH_ID` | Yes | JSAPI payment identifiers |
| `WECHAT_PAY_API_V3_KEY` | Yes | WeChat Pay API v3 key |
| `WECHAT_PAY_PRIVATE_KEY_PATH` | Yes | Merchant private-key path outside the repository |
| `WECHAT_PAY_SERIAL_NO` | Yes | Merchant certificate serial number |
| `WECHAT_PAY_NOTIFY_URL` | Yes | Public payment notification URL |
| `UPLOAD_BASE_DIR` | No | Runtime upload directory |
| `APP_CORS_ALLOWED_ORIGINS` | No | Comma-separated origins; local Vite origins are the development default |
| `SPRINGDOC_ENABLED` | No | API documentation switch; defaults to `false` |

Required configuration has no checked-in weak fallback. Missing required values cause application startup to fail.

## 数据库初始化

`backend/src/main/resources/sql/init.sql` is the schema source of truth and is only for a brand-new empty environment. Existing databases must use separately reviewed migration scripts.

Current schema highlights:

- `book.original_price` stores the optional original price.
- `wx_user` includes receiver name, phone, school, province, city, district, and detail address.
- `order_info` snapshots the school and delivery address and includes `tracking_company` and `tracking_no`.
- Order state flow is `CREATED -> PAID -> DELIVERING -> FINISHED`, with `CANCELLED` as the cancellation state.
- Payment status values are `0` pending, `1` success, and `2` closed.
- Business indexes include book status/sort, order user/status/create/pay time, order-item order/book, and payment order/status.
- `payment_record.order_no` and `payment_record.transaction_id` are unique.

Do not import `init.sql` into an existing production database.

## 微信公众平台配置

Configure the official account web authorization domain and set `WECHAT_MP_OAUTH_CALLBACK_URL` to the public callback endpoint:

```text
https://mall.example.com/api/wx/oauth/callback
```

The application uses `snsapi_userinfo` by default to enrich nickname and avatar. OAuth access tokens and full OpenIDs are not logged.

## 微信支付配置

- Keep the merchant private key outside the repository.
- Inject the API v3 key and merchant identifiers through environment variables or a secret manager.
- Configure the notification endpoint, for example:

```text
https://mall.example.com/api/pay/notify
```

- Verify HTTPS, merchant certificate serial number, AppID, merchant ID, and callback reachability before enabling payments.

## 构建方式

Backend:

```bash
cd backend
./mvnw clean test
./mvnw clean package -DskipTests
```

Frontend:

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm run type-check
pnpm run test:unit -- --run
pnpm run build
```

Generic release package:

```bash
./package.sh
```

The generated package is written under `release/`, which is ignored by Git.

## 部署示例

Suggested layout:

```text
/opt/wechat-jsapi-store/public/
/opt/wechat-jsapi-store/backend/app.jar
/opt/wechat-jsapi-store/uploads/
/opt/wechat-jsapi-store/certs/
```

Use `deploy/nginx.example.conf` as a starting point. Review it before use; scripts in this repository do not create or modify `/etc/nginx`.

Production must explicitly set:

```dotenv
SPRING_PROFILES_ACTIVE=prod
SPRINGDOC_ENABLED=false
APP_CORS_ALLOWED_ORIGINS=https://mall.example.com
```

## 安全注意事项

- Never commit `.env`, certificates, private keys, database dumps, logs, uploads, or production screenshots.
- Use long, independent random values for `JWT_SECRET` and `ADMIN_TOKEN_SECRET`.
- Do not use a MySQL root account for the application.
- Keep API documentation disabled in production.
- Do not configure CORS as `*` when credentials are enabled.
- Store production secrets in a secret manager where possible.
- Rotate any credential that may have been exposed before publishing.

## 测试命令

```bash
cd backend && ./mvnw clean test
cd frontend && pnpm run type-check
cd frontend && pnpm run test:unit -- --run
```

End-to-end tests require a running application and a Playwright browser installation:

```bash
cd frontend
pnpm run test:e2e
```

## 已知限制

- Real OAuth and JSAPI payment flows require valid WeChat platform configuration and cannot be fully exercised offline.
- `init.sql` is not a migration framework.
- The sample Nginx configuration does not provision TLS certificates.
- Uploaded files are stored on the local filesystem unless the implementation is extended.
- MySQL 5.6 verification depends on an external database or Docker environment.
