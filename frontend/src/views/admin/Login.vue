<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})

async function submit() {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    await adminStore.login(form.username.trim(), form.password)
    await router.replace(String(route.query.redirect || '/admin/books'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <h1>购书后台</h1>
      <p>书籍、H5 配置与订单管理</p>

      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            show-password
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button class="login-button" type="primary" :loading="loading" @click="submit">登录</el-button>
      </el-form>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  background: #f2f5f8;
}

.login-panel {
  width: min(420px, 100%);
  padding: 28px;
  border: 1px solid #e5eaf0;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 18px 50px rgb(31 41 55 / 8%);
}

h1 {
  margin: 0;
  color: #17212b;
  font-size: 24px;
}

p {
  margin: 8px 0 24px;
  color: #687583;
}

.login-button {
  width: 100%;
}
</style>
