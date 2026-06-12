<template>
  <RouterView />
</template>

<style>
:root {
  color: var(--color-text-main);
  background: var(--color-bg);
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB",
    "Microsoft YaHei", sans-serif;
}

html,
body,
#app {
  width: 100%;
  min-width: 0;
  overflow-x: hidden;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

body {
  margin: 0;
  background: var(--color-bg);
}

a {
  color: inherit;
  text-decoration: none;
}

#app {
  min-height: 100vh;
  min-height: 100dvh;
}
</style>
