<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
  type GridComponentOption,
  type LegendComponentOption,
  type TooltipComponentOption,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { BarSeriesOption, LineSeriesOption } from 'echarts/charts'
import { getAdminDashboardOverview } from '@/api/adminDashboard'
import type { AdminDashboardOverview } from '@/api/types'
import { formatMoney } from '@/utils/format'

type DashboardChartOption = echarts.ComposeOption<
  | BarSeriesOption
  | LineSeriesOption
  | GridComponentOption
  | LegendComponentOption
  | TooltipComponentOption
>

interface TooltipPoint {
  axisValueLabel?: string
  marker?: string
  seriesName?: string
  value?: number | string
}

echarts.use([BarChart, LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const loading = ref(false)
const overview = ref<AdminDashboardOverview | null>(null)
const chartRef = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null

const statCards = computed(() => [
  {
    label: '今日订单',
    value: overview.value?.todayOrderCount ?? 0,
    hint: '今日创建订单',
  },
  {
    label: '待发货',
    value: overview.value?.pendingDeliveryCount ?? 0,
    hint: '已支付待处理',
  },
  {
    label: '在售书籍',
    value: overview.value?.onSaleBookCount ?? 0,
    hint: '当前上架书籍',
  },
])

const weeklySales = computed(() => overview.value?.weeklySales ?? [])
const chartEmpty = computed(() =>
  weeklySales.value.every((item) => Number(item.salesVolume || 0) === 0 && Number(item.salesAmount || 0) === 0),
)

onMounted(async () => {
  window.addEventListener('resize', resizeChart)
  await loadOverview()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
  chart = null
})

async function loadOverview() {
  loading.value = true
  try {
    overview.value = await getAdminDashboardOverview()
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || chartEmpty.value) {
    chart?.dispose()
    chart = null
    return
  }

  chart ??= echarts.init(chartRef.value)
  const option: DashboardChartOption = {
    color: ['#1f3a5f', '#d9480f'],
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const points = (Array.isArray(params) ? params : [params]) as TooltipPoint[]
        const title = points[0]?.axisValueLabel || ''
        const rows = points.map((point) => {
          const value =
            point.seriesName === '销售额' ? `¥${formatMoney(point.value)}` : String(point.value ?? 0)
          return `${point.marker || ''}${point.seriesName || ''}：${value}`
        })
        return [title, ...rows].filter(Boolean).join('<br/>')
      },
    },
    legend: {
      top: 0,
      right: 0,
      data: ['销量', '销售额'],
    },
    grid: {
      left: 42,
      right: 54,
      top: 48,
      bottom: 30,
    },
    xAxis: {
      type: 'category',
      data: weeklySales.value.map((item) => item.date.slice(5)),
      axisTick: { alignWithLabel: true },
    },
    yAxis: [
      {
        type: 'value',
        name: '销量',
        minInterval: 1,
      },
      {
        type: 'value',
        name: '销售额',
        axisLabel: {
          formatter: '¥{value}',
        },
      },
    ],
    series: [
      {
        name: '销量',
        type: 'bar',
        barMaxWidth: 28,
        data: weeklySales.value.map((item) => Number(item.salesVolume || 0)),
      },
      {
        name: '销售额',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbolSize: 7,
        data: weeklySales.value.map((item) => Number(item.salesAmount || 0)),
      },
    ],
  }
  chart.setOption(option, true)
}

function resizeChart() {
  chart?.resize()
}
</script>

<template>
  <section v-loading="loading" class="dashboard">
    <el-row :gutter="16" class="stat-grid">
      <el-col v-for="card in statCards" :key="card.label" :xs="24" :sm="8">
        <el-card shadow="never" class="stat-card">
          <p>{{ card.label }}</p>
          <strong>{{ card.value }}</strong>
          <span>{{ card.hint }}</span>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <div class="chart-head">
        <div>
          <h2>最近一周销售趋势</h2>
          <p>销量与销售额按有效支付订单统计</p>
        </div>
        <el-button plain :loading="loading" @click="loadOverview">刷新</el-button>
      </div>

      <el-empty v-if="chartEmpty" description="最近一周暂无销售数据" />
      <div v-else ref="chartRef" class="sales-chart" />

      <div v-if="!chartEmpty" class="sales-summary">
        <span>周销量：{{ weeklySales.reduce((sum, item) => sum + Number(item.salesVolume || 0), 0) }}</span>
        <span>
          周销售额：￥{{
            formatMoney(weeklySales.reduce((sum, item) => sum + Number(item.salesAmount || 0), 0))
          }}
        </span>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.dashboard {
  display: grid;
  gap: 16px;
}

.stat-grid {
  row-gap: 16px;
}

.stat-card {
  border-radius: 8px;
}

.stat-card p {
  margin: 0 0 10px;
  color: #687583;
}

.stat-card strong {
  display: block;
  font-size: 28px;
}

.stat-card span {
  display: block;
  margin-top: 8px;
  color: #8a95a3;
  font-size: 13px;
}

.chart-card {
  border-radius: 8px;
}

.chart-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.chart-head h2 {
  margin: 0;
  color: #17212b;
  font-size: 18px;
}

.chart-head p {
  margin: 6px 0 0;
  color: #687583;
  font-size: 13px;
}

.sales-chart {
  height: 360px;
}

.sales-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
  margin-top: 12px;
  color: #46515c;
  font-size: 14px;
}

@media (max-width: 640px) {
  .chart-head {
    align-items: stretch;
    flex-direction: column;
  }

  .sales-chart {
    height: 300px;
  }
}
</style>
