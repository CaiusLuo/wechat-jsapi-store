# Frontend

Vue 3 H5 and admin frontend for WeChat JSAPI Store.

```bash
pnpm install --frozen-lockfile
pnpm dev
pnpm run type-check
pnpm run test:unit -- --run
pnpm run build
```

The default API base is `/api`. Set `VITE_API_BASE_URL` only when a separate development API endpoint is required.
