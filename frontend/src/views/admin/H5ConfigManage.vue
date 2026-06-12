<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminH5Config, updateAdminH5Config } from '@/api/adminConfig'
import { defaultH5Config } from '@/api/h5Config'
import type { H5Config } from '@/api/types'

const loading = ref(false)
const saving = ref(false)
const form = reactive<H5Config>({ ...defaultH5Config })

onMounted(loadConfig)

async function loadConfig() {
  loading.value = true
  try {
    Object.assign(form, defaultH5Config, await getAdminH5Config())
  } finally {
    loading.value = false
  }
}

function validate() {
  if (!form.siteTitle.trim()) return '请填写站点标题'
  if (!form.siteSubtitle.trim()) return '请填写站点副标题'
  if (!form.serviceWechat.trim()) return '请填写客服微信'
  if (!form.servicePhone.trim()) return '请填写联系电话'
  if (!form.workTime.trim()) return '请填写工作时间'
  if (!form.noticeText.trim()) return '请填写公告'
  return ''
}

async function save() {
  const message = validate()
  if (message) {
    ElMessage.warning(message)
    return
  }

  saving.value = true
  try {
    const saved = await updateAdminH5Config({
      siteTitle: form.siteTitle.trim(),
      siteSubtitle: form.siteSubtitle.trim(),
      serviceWechat: form.serviceWechat.trim(),
      servicePhone: form.servicePhone.trim(),
      workTime: form.workTime.trim(),
      noticeText: form.noticeText.trim(),
    })
    Object.assign(form, saved)
    ElMessage.success('H5 配置已保存')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section class="admin-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span>H5 页面配置</span>
          <el-button :loading="loading" @click="loadConfig">刷新</el-button>
        </div>
      </template>

      <el-form v-loading="loading" label-width="110px" :model="form" class="config-form">
        <el-form-item label="站点标题" required>
          <el-input v-model="form.siteTitle" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="站点副标题" required>
          <el-input v-model="form.siteSubtitle" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="客服微信" required>
          <el-input v-model="form.serviceWechat" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="form.servicePhone" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="工作时间" required>
          <el-input v-model="form.workTime" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="公告" required>
          <el-input v-model="form.noticeText" type="textarea" :rows="4" maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.config-form {
  max-width: 760px;
}
</style>
