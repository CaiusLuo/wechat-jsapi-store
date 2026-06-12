<script setup lang="ts">
import type { OrderDetail } from '@/api/types'
import { formatAddress, formatMoney, formatOrderStatus, formatPayStatus } from '@/utils/format'

defineProps<{
  order: OrderDetail | null
}>()
</script>

<template>
  <el-empty v-if="!order" description="暂无订单详情" />
  <section v-else class="admin-order-detail">
    <el-descriptions title="订单基础信息" :column="2" border>
      <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ formatOrderStatus(order.status, order.statusText) }}</el-descriptions-item>
      <el-descriptions-item label="订单金额">¥{{ formatMoney(order.totalAmount || order.payAmount) }}</el-descriptions-item>
      <el-descriptions-item label="支付金额">¥{{ formatMoney(order.payAmount) }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ order.createTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="发货时间">{{ order.deliverTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="完成时间">{{ order.finishTime || '-' }}</el-descriptions-item>
    </el-descriptions>

    <el-descriptions title="收货信息" :column="2" border>
      <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ order.phone }}</el-descriptions-item>
      <el-descriptions-item label="学校">{{ order.school || '-' }}</el-descriptions-item>
      <el-descriptions-item label="地址">
        {{ formatAddress([order.province, order.city, order.district, order.detailAddress]) || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="备注">{{ order.remark || '-' }}</el-descriptions-item>
    </el-descriptions>

    <el-descriptions title="物流信息" :column="2" border>
      <el-descriptions-item label="物流公司">{{ order.trackingCompany || '-' }}</el-descriptions-item>
      <el-descriptions-item label="顺丰订单号">{{ order.trackingNo || '-' }}</el-descriptions-item>
    </el-descriptions>

    <el-descriptions title="支付信息" :column="2" border>
      <el-descriptions-item label="微信交易号">{{ order.payment?.transactionId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="支付状态">
        {{ formatPayStatus(order.payment?.payStatus ?? order.payStatus, order.payment?.payStatusText || order.payStatusText) }}
      </el-descriptions-item>
      <el-descriptions-item label="支付金额">
        {{ order.payment ? `¥${formatMoney(order.payment.amount)}` : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="支付时间">{{ order.payment?.payTime || order.payTime || '-' }}</el-descriptions-item>
    </el-descriptions>

    <el-table :data="order.items || []" border>
      <el-table-column prop="bookName" label="书名" min-width="180" />
      <el-table-column prop="price" label="单价" width="120">
        <template #default="{ row }">¥{{ formatMoney(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="subtotal" label="小计" width="120">
        <template #default="{ row }">¥{{ formatMoney(row.subtotal) }}</template>
      </el-table-column>
    </el-table>
  </section>
</template>

<style scoped>
.admin-order-detail {
  display: grid;
  gap: 18px;
}
</style>
