<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelAdminOrder,
  getAdminOrderDetail,
  listAdminOrders,
  markOrderDelivering,
  markOrderFinished,
} from '@/api/adminOrder'
import type { OrderDetail, OrderStatus } from '@/api/types'
import { formatMoney, formatOrderStatus, normalizeList, orderStatusType } from '@/utils/format'
import AdminOrderDetail from './OrderDetail.vue'

const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const operatingOrderNo = ref<string | null>(null)
const rows = ref<OrderDetail[]>([])
const total = ref(0)
const currentOrder = ref<OrderDetail | null>(null)
const query = reactive({
  orderNo: '',
  phone: '',
  receiverName: '',
  status: '' as OrderStatus | '',
  page: 1,
  size: 10,
})

onMounted(fetchOrders)

function buildQuery() {
  return {
    ...(query.orderNo.trim() ? { orderNo: query.orderNo.trim() } : {}),
    ...(query.phone.trim() ? { phone: query.phone.trim() } : {}),
    ...(query.receiverName.trim() ? { receiverName: query.receiverName.trim() } : {}),
    ...(query.status ? { status: query.status } : {}),
    page: query.page,
    size: query.size,
  }
}

async function fetchOrders() {
  if (loading.value) return
  loading.value = true
  try {
    const result = await listAdminOrders(buildQuery())
    rows.value = normalizeList<OrderDetail>(result)
    total.value = Array.isArray(result) ? result.length : result.total || rows.value.length
  } finally {
    loading.value = false
  }
}

function search() {
  if (loading.value) return
  query.page = 1
  fetchOrders()
}

function resetQuery() {
  if (loading.value) return
  query.orderNo = ''
  query.phone = ''
  query.receiverName = ''
  query.status = ''
  query.page = 1
  fetchOrders()
}

async function openDetail(orderNo: string) {
  detailVisible.value = true
  detailLoading.value = true
  currentOrder.value = null
  try {
    currentOrder.value = await getAdminOrderDetail(orderNo)
  } finally {
    detailLoading.value = false
  }
}

function getErrorMessage(error: unknown) {
  if (error && typeof error === 'object' && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    if (response?.data?.message) return response.data.message
  }
  if (error instanceof Error) return error.message
  return '操作失败，请稍后重试'
}

function isCancelConfirm(error: unknown) {
  return error === 'cancel' || error === 'close'
}

async function confirmOrderAction(order: OrderDetail, message: string, action: () => Promise<void>) {
  if (operatingOrderNo.value) return
  operatingOrderNo.value = order.orderNo
  try {
    await ElMessageBox.confirm(message, '操作确认', { type: 'warning' })
    await action()
    ElMessage.success('操作成功')
    await fetchOrders()
  } catch (error) {
    if (!isCancelConfirm(error)) {
      ElMessage.error(getErrorMessage(error))
    }
  } finally {
    operatingOrderNo.value = null
  }
}

function cancelOrder(order: OrderDetail) {
  if (order.status !== 'CREATED') return
  return confirmOrderAction(order, '确认取消该订单？', () => cancelAdminOrder(order.orderNo, true))
}

function deliverOrder(order: OrderDetail) {
  if (order.status !== 'PAID') return
  if (operatingOrderNo.value) return
  operatingOrderNo.value = order.orderNo
  return ElMessageBox.prompt('顺丰订单号（选填）', '发货确认', {
    confirmButtonText: '确认发货',
    cancelButtonText: '取消',
    inputPlaceholder: '可不填直接确认发货',
  })
    .then(async ({ value }) => {
      await markOrderDelivering(order.orderNo, { trackingNo: value?.trim() || undefined }, true)
      ElMessage.success('操作成功')
      await fetchOrders()
    })
    .catch((error) => {
      if (!isCancelConfirm(error)) {
        ElMessage.error(getErrorMessage(error))
      }
    })
    .finally(() => {
      operatingOrderNo.value = null
    })
}

function finishOrder(order: OrderDetail) {
  if (order.status !== 'DELIVERING') return
  return confirmOrderAction(order, '确认将订单标记为已完成？', () => markOrderFinished(order.orderNo, true))
}

function getStatusText(order: OrderDetail) {
  return formatOrderStatus(order.status, order.statusText)
}

function getStatusType(status: OrderStatus) {
  return orderStatusType[status] || 'info'
}
</script>

<template>
  <section class="admin-page">
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="订单号">
          <el-input v-model="query.orderNo" clearable placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="query.phone" clearable placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="收货人">
          <el-input v-model="query.receiverName" clearable placeholder="请输入收货人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 180px">
            <el-option label="待支付" value="CREATED" />
            <el-option label="待发货" value="PAID" />
            <el-option label="配送中" value="DELIVERING" />
            <el-option label="已完成" value="FINISHED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" :disabled="loading" @click="search">查询</el-button>
          <el-button :loading="loading" :disabled="loading" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="orderNo" label="订单号" min-width="190" />
        <el-table-column prop="receiverName" label="收货人" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="payAmount" label="金额" width="120">
          <template #default="{ row }">¥{{ formatMoney(row.payAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.orderNo)">详情</el-button>
            <el-button
              v-if="row.status === 'CREATED'"
              link
              type="danger"
              :loading="operatingOrderNo === row.orderNo"
              :disabled="Boolean(operatingOrderNo)"
              @click="cancelOrder(row)"
            >
              取消订单
            </el-button>
            <el-button
              v-else-if="row.status === 'PAID'"
              link
              type="primary"
              :loading="operatingOrderNo === row.orderNo"
              :disabled="Boolean(operatingOrderNo)"
              @click="deliverOrder(row)"
            >
              标记发货
            </el-button>
            <el-button
              v-else-if="row.status === 'DELIVERING'"
              link
              type="success"
              :loading="operatingOrderNo === row.orderNo"
              :disabled="Boolean(operatingOrderNo)"
              @click="finishOrder(row)"
            >
              标记完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        layout="total, prev, pager, next, sizes"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        @change="fetchOrders"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="820px">
      <div v-loading="detailLoading">
        <AdminOrderDetail :order="currentOrder" />
      </div>
    </el-dialog>
  </section>
</template>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
}

.pagination {
  justify-content: flex-end;
  margin-top: 16px;
}

</style>
