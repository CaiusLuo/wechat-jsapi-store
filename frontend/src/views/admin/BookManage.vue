<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus'
import {
  createAdminBook,
  deleteAdminBook,
  listAdminBooks,
  updateAdminBook,
  updateAdminBookStatus,
  uploadAdminFile,
} from '@/api/adminBook'
import type { Book, BookPayload } from '@/api/types'
import { formatMoney, normalizeList } from '@/utils/format'
import { displayOriginalPrice, formatDiscount } from '@/utils/pricing'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const rows = ref<Book[]>([])
const total = ref(0)
const query = reactive({
  name: '',
  status: '' as '' | 0 | 1,
  page: 1,
  size: 10,
})
const form = reactive<BookPayload>({
  name: '',
  subtitle: '',
  coverUrl: '',
  originalPrice: 0,
  price: 0,
  intro: '',
  stock: 0,
  sort: 0,
  status: 1,
})

onMounted(fetchBooks)

function buildQuery() {
  return {
    ...(query.name.trim() ? { name: query.name.trim() } : {}),
    ...(query.status === 0 || query.status === 1 ? { status: query.status } : {}),
    page: query.page,
    size: query.size,
  }
}

async function fetchBooks() {
  loading.value = true
  try {
    const result = await listAdminBooks(buildQuery())
    rows.value = normalizeList<Book>(result)
    total.value = result.total || rows.value.length
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  void fetchBooks()
}

function resetForm() {
  editingId.value = null
  form.name = ''
  form.subtitle = ''
  form.coverUrl = ''
  form.originalPrice = 0
  form.price = 0
  form.intro = ''
  form.stock = 0
  form.sort = 0
  form.status = 1
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(book: Book) {
  editingId.value = book.id
  form.name = book.name
  form.subtitle = book.subtitle || ''
  form.coverUrl = book.coverUrl || ''
  form.originalPrice = Number(displayOriginalPrice(book))
  form.price = Number(book.price || 0)
  form.intro = book.intro || ''
  form.stock = Number(book.stock || 0)
  form.sort = Number(book.sort || 0)
  form.status = Number(book.status ?? 1) === 1 ? 1 : 0
  dialogVisible.value = true
}

function validate() {
  if (!form.name.trim()) return '请填写书籍名称'
  if (Number(form.originalPrice) <= 0) return '原价必须大于 0'
  if (Number(form.price) <= 0) return '折扣价必须大于 0'
  if (Number(form.price) > Number(form.originalPrice)) return '折扣价不能高于原价'
  if (Number(form.stock) < 0) return '库存不能小于 0'
  return ''
}

function cleanOptional(value?: string) {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}

function buildPayload(): BookPayload {
  return {
    name: form.name.trim(),
    subtitle: cleanOptional(form.subtitle),
    coverUrl: cleanOptional(form.coverUrl),
    originalPrice: Number(form.originalPrice).toFixed(2),
    price: Number(form.price).toFixed(2),
    intro: cleanOptional(form.intro),
    stock: Number(form.stock || 0),
    sort: Number(form.sort || 0),
    status: Number(form.status) === 1 ? 1 : 0,
  }
}

async function save() {
  const message = validate()
  if (message) {
    ElMessage.warning(message)
    return
  }

  const payload = buildPayload()
  saving.value = true
  try {
    if (editingId.value) {
      await updateAdminBook(editingId.value, payload)
    } else {
      await createAdminBook(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await fetchBooks()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(book: Book) {
  const nextStatus: 0 | 1 = Number(book.status) === 1 ? 0 : 1
  await updateAdminBookStatus(book.id, nextStatus)
  ElMessage.success(nextStatus === 1 ? '已上架' : '已下架')
  await fetchBooks()
}

async function removeBook(book: Book) {
  await ElMessageBox.confirm(`确认删除《${book.name}》？`, '删除确认', { type: 'warning' })
  await deleteAdminBook(book.id)
  ElMessage.success('已删除')
  await fetchBooks()
}

async function uploadCover(options: UploadRequestOptions) {
  const result = await uploadAdminFile(options.file)
  form.coverUrl = result.publicUrl
  ElMessage.success('封面上传成功')
  options.onSuccess(result)
}

function getDiscountText(book: Book) {
  return formatDiscount(book.price, book.originalPrice) || '-'
}
</script>

<template>
  <section class="admin-page">
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="书名">
          <el-input v-model="query.name" clearable placeholder="请输入书名" @clear="search" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px" @change="search">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="fetchBooks">刷新</el-button>
          <el-button type="success" @click="openCreate">新增书籍</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="rows" border>
        <el-table-column label="封面" width="92">
          <template #default="{ row }">
            <div class="cover-cell">
              <img v-if="row.coverUrl" :src="row.coverUrl" :alt="row.name" />
              <span v-else>默认</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="书名" min-width="170" />
        <el-table-column prop="subtitle" label="副标题" min-width="170" show-overflow-tooltip />
        <el-table-column prop="originalPrice" label="原价" width="110">
          <template #default="{ row }">¥{{ formatMoney(displayOriginalPrice(row)) }}</template>
        </el-table-column>
        <el-table-column prop="price" label="折扣价" width="110">
          <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
        </el-table-column>
        <el-table-column label="折扣" width="90">
          <template #default="{ row }">{{ getDiscountText(row) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'">
              {{ Number(row.status) === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="intro" label="简介" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="Number(row.status) === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ Number(row.status) === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="removeBook(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        layout="total, prev, pager, next, sizes"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        @change="fetchBooks"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑书籍' : '新增书籍'" width="660px">
      <el-form label-width="90px" :model="form">
        <el-form-item label="书名" required>
          <el-input v-model="form.name" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="封面">
          <div class="cover-form">
            <el-upload :show-file-list="false" accept=".jpg,.jpeg,.png,.webp" :http-request="uploadCover">
              <el-button>上传封面</el-button>
            </el-upload>
            <el-input v-model="form.coverUrl" clearable placeholder="上传后自动填入，也可手动填写 /uploads/..." />
          </div>
        </el-form-item>
        <el-form-item label="原价" required>
          <el-input-number v-model="form.originalPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="折扣价" required>
          <el-input-number v-model="form.price" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="form.stock" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :precision="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
}

.cover-cell {
  display: grid;
  width: 48px;
  height: 64px;
  place-items: center;
  overflow: hidden;
  border-radius: 4px;
  color: #687583;
  background: #eef2f6;
  font-size: 12px;
}

.cover-cell img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-form {
  display: grid;
  width: 100%;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 10px;
}

.pagination {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
