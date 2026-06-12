<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()
const activeMenu = computed(() => route.path)

function logout() {
  adminStore.logout()
  router.replace('/admin/login')
}
</script>

<template>
  <el-container class="admin-shell">
    <el-aside width="208px" class="aside">
      <div class="brand">购书后台</div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/admin/dashboard">Dashboard</el-menu-item>
        <el-menu-item index="/admin/books">书籍管理</el-menu-item>
        <el-menu-item index="/admin/config">H5 配置</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <h1>{{ route.meta.title || '后台管理' }}</h1>
        <el-button plain @click="logout">退出登录</el-button>
      </el-header>
      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
  background: #f5f7fa;
}

.aside {
  border-right: 1px solid #e6eaf0;
  background: #fff;
}

.brand {
  display: flex;
  height: 60px;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #edf0f3;
  color: #17212b;
  font-size: 18px;
  font-weight: 700;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6eaf0;
  background: #fff;
}

.header h1 {
  margin: 0;
  font-size: 18px;
}

.main {
  padding: 18px;
}
</style>
