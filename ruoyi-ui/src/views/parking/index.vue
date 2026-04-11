<template>
  <div class="app-container parking-overview" v-loading="loading">

    <!-- Top section: stat cards left, quick actions right -->
    <el-row :gutter="12" type="flex" class="mb12 top-row">
      <!-- Left: stat + biz cards stacked -->
      <el-col :md="24" :lg="16" class="stat-col">
        <!-- Top stat cards -->
        <el-row :gutter="12" type="flex" class="stat-row mb12">
          <el-col v-for="card in statCards" :key="card.key" :xs="12" :sm="12" :lg="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-card__icon" :class="`stat-card__icon--${card.color}`">
                <i :class="card.icon" />
              </div>
              <div class="stat-card__body">
                <div class="stat-card__title">{{ card.title }}</div>
                <div class="stat-card__value">{{ card.value }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <!-- Business KPI cards -->
        <el-row :gutter="12" type="flex" class="stat-row">
          <el-col v-for="card in bizCards" :key="card.key" :xs="12" :sm="12" :lg="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-card__icon" :class="`stat-card__icon--${card.color}`">
                <i :class="card.icon" />
              </div>
              <div class="stat-card__body">
                <div class="stat-card__title">{{ card.title }}</div>
                <div class="stat-card__value">{{ card.display }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>

      <!-- Right: quick actions -->
      <el-col :md="24" :lg="8" class="quick-col">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="card-header">
            <span>快捷入口</span>
            <el-tooltip content="配置快捷入口" placement="top">
              <el-button type="text" icon="el-icon-setting" @click="openQuickConfig" />
            </el-tooltip>
          </div>
          <div class="quick-grid">
            <!-- Static: refresh overview data -->
            <div class="quick-grid__item" @click="getOverviewData">
              <div class="quick-grid__icon" :style="{ background: iconGradient('success') }">
                <i class="el-icon-refresh" />
              </div>
              <div class="quick-grid__label">刷新数据</div>
            </div>
            <!-- Dynamic: from menu management -->
            <div
              v-for="item in quickActions"
              :key="item.key"
              class="quick-grid__item"
              :class="`quick-grid__item--${item.color}`"
              @click="$router.push(item.path)"
            >
              <div class="quick-grid__icon" :style="{ background: iconGradient(item.color) }">
                <svg-icon :icon-class="item.icon" />
              </div>
              <div class="quick-grid__label">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ECharts donut -->
    <el-row :gutter="12" class="mb12">
      <el-col :md="24" :lg="11">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="card-header">
            <span>车位状态分布</span>
            <el-button
              v-hasPermi="['parking:overview:list']"
              size="mini"
              type="primary"
              icon="el-icon-refresh"
              :loading="loading"
              @click="getOverviewData"
            >刷新</el-button>
          </div>
          <div ref="spaceChart" class="echarts-donut" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <!-- Top lots bar chart -->
      <el-col :md="24" :lg="12" class="mb12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>停车场实时占用 TOP {{ topLots.length || 5 }}</span>
            <el-button v-hasPermi="['parking:lot:list']" type="text" @click="goLotPage">
              查看全部 <i class="el-icon-arrow-right" />
            </el-button>
          </div>
          <div ref="lotChart" v-loading="lotLoading" class="echarts-bar" />
        </el-card>
      </el-col>

      <!-- Recent orders bar chart -->
      <el-col :md="24" :lg="12" class="mb12">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>最近订单 TOP {{ recentOrders.length || 5 }}</span>
          </div>
          <div ref="orderChart" v-loading="recentLoading" class="echarts-bar" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick actions config dialog -->
    <el-dialog title="配置快捷入口" :visible.sync="showQuickConfig" width="500px" append-to-body>
      <div class="quick-config">
        <div class="quick-config__toolbar">
          <span class="quick-config__hint">已选 {{ pendingKeys.length }} / {{ menuItems.length }} 项</span>
          <div>
            <el-button size="mini" type="text" @click="selectAllConfig">全选</el-button>
            <el-divider direction="vertical" />
            <el-button size="mini" type="text" @click="clearAllConfig">清空</el-button>
          </div>
        </div>
        <div v-for="group in menuGroups" :key="group.title" class="quick-config__group">
          <div class="quick-config__group-title">{{ group.title }}</div>
          <div class="quick-config__grid">
            <div
              v-for="item in group.items"
              :key="item.key"
              class="quick-config__card"
              :class="{ 'is-checked': pendingKeys.includes(item.key) }"
              @click="toggleConfigKey(item.key)"
            >
              <div class="quick-config__card-icon" :style="{ background: iconGradient(item.color) }">
                <svg-icon :icon-class="item.icon" />
              </div>
              <span class="quick-config__card-label">{{ item.title }}</span>
              <i v-if="pendingKeys.includes(item.key)" class="el-icon-check quick-config__check" />
            </div>
          </div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="showQuickConfig = false">取消</el-button>
        <el-button type="primary" @click="saveQuickConfig">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { mapGetters } from 'vuex'
import { getParkingOverviewStats, getParkingRecentOrders } from '@/api/parking/overview'
import { getQuickConfig, saveQuickConfig as apiSaveQuickConfig } from '@/api/parking/userConfig'
import { listParkingLots } from '@/api/parking/lot'
import { LOT_STATUS_OPTIONS, findLabel, findTagType } from './options'

const QUICK_COLORS = ['primary', 'success', 'danger', 'warning', 'info']

const TOP_LOT_LIMIT = 5
const RECENT_ORDER_LIMIT = 5

const RECENT_ORDER_TYPE_OPTIONS = [
  { label: '临停', value: 'temp', tagType: 'primary' },
  { label: '月卡', value: 'monthly', tagType: 'warning' },
  { label: '会员', value: 'membership', tagType: 'danger' }
]

const RECENT_PAY_STATUS_OPTIONS = [
  { label: '未支付', value: '0', tagType: 'info' },
  { label: '已支付', value: '1', tagType: 'success' },
  { label: '已退款', value: '2', tagType: 'warning' },
  { label: '已关闭', value: '3', tagType: 'info' }
]

const EMPTY_STATS = {
  lotCount: 0,
  totalSpaceCount: 0,
  availableSpaceCount: 0,
  occupiedSpaceCount: 0,
  disabledSpaceCount: 0,
  lockedSpaceCount: 0,
  todayEntryCount: 0,
  todayExitCount: 0,
  todayRevenue: 0,
  activeTempOrderCount: 0
}

export default {
  name: 'ParkingOverview',
  data() {
    return {
      loading: false,
      lotLoading: false,
      recentLoading: false,
      overviewStats: { ...EMPTY_STATS },
      topLots: [],
      recentOrders: [],
      chart: null,
      lotChart: null,
      orderChart: null,
      showQuickConfig: false,
      configuredKeys: [],
      pendingKeys: []
    }
  },
  computed: {
    ...mapGetters(['sidebarRouters']),
    statCards() {
      return [
        { key: 'lot', title: '停车场总数', value: this.overviewStats.lotCount, icon: 'el-icon-office-building', color: 'primary' },
        { key: 'total', title: '车位总数', value: this.overviewStats.totalSpaceCount, icon: 'el-icon-s-grid', color: 'info' },
        { key: 'available', title: '空闲车位', value: this.overviewStats.availableSpaceCount, icon: 'el-icon-circle-check', color: 'success' },
        { key: 'occupied', title: '已占用车位', value: this.overviewStats.occupiedSpaceCount, icon: 'el-icon-truck', color: 'danger' }
      ]
    },
    bizCards() {
      return [
        { key: 'todayEntry', title: '今日入场', display: this.overviewStats.todayEntryCount, icon: 'el-icon-bottom', color: 'primary' },
        { key: 'todayExit', title: '今日出场', display: this.overviewStats.todayExitCount, icon: 'el-icon-top', color: 'success' },
        { key: 'todayRevenue', title: '今日营收', display: '¥' + Number(this.overviewStats.todayRevenue || 0).toFixed(2), icon: 'el-icon-coin', color: 'warning' },
        { key: 'activeTemp', title: '进行中临停', display: this.overviewStats.activeTempOrderCount, icon: 'el-icon-time', color: 'danger' }
      ]
    },
    spaceBreakdown() {
      const total = Number(this.overviewStats.totalSpaceCount) || 0
      const buckets = [
        { key: 'available', label: '空闲', value: this.overviewStats.availableSpaceCount, color: '#67c23a' },
        { key: 'occupied', label: '占用', value: this.overviewStats.occupiedSpaceCount, color: '#f56c6c' },
        { key: 'locked', label: '锁定', value: this.overviewStats.lockedSpaceCount, color: '#e6a23c' },
        { key: 'disabled', label: '停用', value: this.overviewStats.disabledSpaceCount, color: '#909399' }
      ]
      return buckets.map(b => ({
        ...b,
        percent: total > 0 ? Math.round((Number(b.value || 0) / total) * 100) : 0
      }))
    },
    // Group sidebarRouters by top-level menu section, flatten each to leaf pages
    menuGroups() {
      const normalize = p => p.replace(/\/+/g, '/')
      const currentPath = normalize(this.$route ? this.$route.path : '')
      const groups = []
      let colorIdx = 0
      for (const topRoute of (this.sidebarRouters || [])) {
        if (topRoute.hidden) continue
        const groupTitle = (topRoute.meta && topRoute.meta.title) ? topRoute.meta.title : ''
        const topPath = normalize(topRoute.path.startsWith('/') ? topRoute.path : '/' + topRoute.path)
        const items = []
        const flatten = (routes, parentPath) => {
          for (const r of (routes || [])) {
            if (r.hidden) continue
            const seg = r.path.startsWith('/') ? r.path : parentPath + '/' + r.path
            const path = normalize(seg)
            if (r.children && r.children.length) {
              flatten(r.children, path)
            } else if (r.meta && r.meta.title && path !== currentPath) {
              items.push({ key: path, path, title: r.meta.title, icon: r.meta.icon || 'dashboard', color: QUICK_COLORS[colorIdx++ % QUICK_COLORS.length] })
            }
          }
        }
        flatten(topRoute.children || [], topPath)
        if (items.length > 0) {
          groups.push({ title: groupTitle, items })
        }
      }
      return groups
    },
    menuItems() {
      return this.menuGroups.flatMap(g => g.items)
    },
    quickActions() {
      return this.menuItems.filter(item => this.configuredKeys.includes(item.key))
    }
  },
  watch: {
    spaceBreakdown() {
      this.updateChart()
    },
    topLots() {
      this.updateLotChart()
    },
    recentOrders() {
      this.updateOrderChart()
    }
  },
  created() {
    this.loadQuickConfig()
    this.getOverviewData()
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
      this.initLotChart()
      this.initOrderChart()
    })
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    ;[this.chart, this.lotChart, this.orderChart].forEach(c => c && c.dispose())
  },
  methods: {
    getOverviewData() {
      this.loading = true
      Promise.all([this.getStats(), this.getTopLots(), this.getRecentOrders()]).finally(() => {
        this.loading = false
      })
    },
    getStats() {
      return getParkingOverviewStats()
        .then(res => { this.overviewStats = { ...EMPTY_STATS, ...(res.data || {}) } })
        .catch(() => { this.overviewStats = { ...EMPTY_STATS } })
    },
    getTopLots() {
      this.lotLoading = true
      return listParkingLots({ pageNum: 1, pageSize: TOP_LOT_LIMIT })
        .then(res => { this.topLots = res.rows || [] })
        .catch(() => { this.topLots = [] })
        .finally(() => { this.lotLoading = false })
    },
    getRecentOrders() {
      this.recentLoading = true
      return getParkingRecentOrders(RECENT_ORDER_LIMIT)
        .then(res => { this.recentOrders = res.data || [] })
        .catch(() => { this.recentOrders = [] })
        .finally(() => { this.recentLoading = false })
    },
    initChart() {
      if (!this.$refs.spaceChart) return
      this.chart = echarts.init(this.$refs.spaceChart)
      this.updateChart()
    },
    updateChart() {
      if (!this.chart) return
      const data = this.spaceBreakdown.map(item => ({
        name: item.label,
        value: Number(item.value) || 0,
        itemStyle: { color: item.color }
      }))
      const total = data.reduce((s, d) => s + d.value, 0)
      this.chart.setOption({
        title: [{
          text: String(total),
          subtext: '总车位',
          left: '29%',
          top: '38%',
          textAlign: 'center',
          textStyle: { fontSize: 22, fontWeight: 'bold', color: '#303133' },
          subtextStyle: { fontSize: 13, color: '#909399' }
        }],
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: '2%',
          top: 'center',
          itemWidth: 10,
          itemHeight: 10,
          textStyle: { fontSize: 13 },
          formatter: name => {
            const item = data.find(d => d.name === name)
            return item ? `${name}   ${item.value} (${item.percent || 0}%)` : name
          }
        },
        series: [{
          type: 'pie',
          radius: ['46%', '66%'],
          center: ['30%', '50%'],
          avoidLabelOverlap: false,
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 13, fontWeight: 'bold' },
            scaleSize: 5
          },
          data
        }]
      })
    },
    iconGradient(color) {
      const map = {
        primary: 'linear-gradient(135deg, #409eff, #66b1ff)',
        success: 'linear-gradient(135deg, #67c23a, #85ce61)',
        warning: 'linear-gradient(135deg, #e6a23c, #ebb563)',
        danger: 'linear-gradient(135deg, #f56c6c, #f78989)',
        info: 'linear-gradient(135deg, #909399, #b1b3b8)',
        gray: 'linear-gradient(135deg, #c0c4cc, #dcdfe6)'
      }
      return map[color] || map.primary
    },
    handleResize() {
      ;[this.chart, this.lotChart, this.orderChart].forEach(c => c && c.resize())
    },
    initLotChart() {
      if (!this.$refs.lotChart) return
      this.lotChart = echarts.init(this.$refs.lotChart)
      this.updateLotChart()
    },
    updateLotChart() {
      if (!this.lotChart) return
      const lots = this.topLots
      if (!lots.length) {
        this.lotChart.setOption({ series: [], title: { text: '暂无停车场数据', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 13, fontWeight: 'normal' } } })
        return
      }
      const names = lots.map(l => l.lotName || '-')
      const availableData = lots.map(l => Number(l.availableSpaceCount) || 0)
      const occupiedData = lots.map(l => Math.max((Number(l.totalSpaceCount) || 0) - (Number(l.availableSpaceCount) || 0), 0))
      this.lotChart.setOption({
        title: { text: '' },
        grid: { left: 16, right: 16, top: 36, bottom: 16, containLabel: true },
        legend: {
          data: [
            { name: '空闲', itemStyle: { color: '#67c23a' } },
            { name: '占用', itemStyle: { color: '#f56c6c' } }
          ],
          top: 4, right: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: params => {
            const idx = params[0].dataIndex
            const lot = lots[idx]
            const occ = this.lotOccupancy(lot)
            const occupied = occupiedData[idx]
            return `<b>${lot.lotName}</b><br>空闲：${lot.availableSpaceCount}<br>占用：${occupied}<br>总计：${lot.totalSpaceCount}<br>占用率：${occ}%`
          }
        },
        xAxis: {
          type: 'category',
          data: names,
          axisLabel: { fontSize: 12, color: '#303133', overflow: 'truncate', width: 80 },
          axisTick: { show: false }
        },
        yAxis: {
          type: 'value',
          name: '车位数',
          nameTextStyle: { fontSize: 11, color: '#909399' },
          axisLabel: { fontSize: 11 },
          splitLine: { lineStyle: { type: 'dashed', color: '#e8e8e8' } }
        },
        series: [
          {
            name: '空闲',
            type: 'bar',
            stack: 'space',
            data: availableData,
            itemStyle: { color: '#67c23a' },
            barMaxWidth: 60,
            label: {
              show: true,
              position: 'inside',
              formatter: params => params.value > 0 ? params.value : '',
              fontSize: 12,
              color: '#fff'
            }
          },
          {
            name: '占用',
            type: 'bar',
            stack: 'space',
            data: occupiedData,
            itemStyle: { color: '#f56c6c', borderRadius: [4, 4, 0, 0] },
            barMaxWidth: 60,
            label: {
              show: true,
              position: 'top',
              formatter: (params) => {
                const occ = this.lotOccupancy(lots[params.dataIndex])
                return `${occ}%`
              },
              fontSize: 12,
              color: '#606266'
            }
          }
        ]
      })
    },
    initOrderChart() {
      if (!this.$refs.orderChart) return
      this.orderChart = echarts.init(this.$refs.orderChart)
      this.updateOrderChart()
    },
    updateOrderChart() {
      if (!this.orderChart) return
      const orders = this.recentOrders
      if (!orders.length) {
        this.orderChart.setOption({ title: { text: '暂无订单数据', left: 'center', top: 'center', textStyle: { color: '#909399', fontSize: 13, fontWeight: 'normal' } } })
        return
      }
      const typeColor = { temp: '#409eff', monthly: '#e6a23c', membership: '#f56c6c' }
      const typeLabel = { temp: '临停', monthly: '月卡', membership: '会员' }
      const xLabels = orders.map((o, i) => `${typeLabel[o.orderType] || '-'}${i + 1}`)
      this.orderChart.setOption({
        title: { text: '' },
        grid: { left: 55, right: 16, top: 36, bottom: 28 },
        legend: {
          data: [
            { name: '临停', itemStyle: { color: '#409eff' } },
            { name: '月卡', itemStyle: { color: '#e6a23c' } },
            { name: '会员', itemStyle: { color: '#f56c6c' } }
          ],
          top: 4, right: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: params => {
            const o = orders[params[0].dataIndex]
            return [
              `<b>${typeLabel[o.orderType] || o.orderType}</b>`,
              `订单号：${o.orderNo}`,
              `停车场：${o.lotName || '-'}`,
              `金额：¥${Number(o.payAmount || 0).toFixed(2)}`,
              `状态：${this.recentPayStatusLabel(o.payStatus)}`,
              `时间：${this.parseTime(o.createTime)}`
            ].join('<br>')
          }
        },
        xAxis: {
          type: 'category',
          data: xLabels,
          axisLabel: { fontSize: 12, color: '#606266' },
          axisTick: { show: false }
        },
        yAxis: {
          type: 'value',
          axisLabel: { formatter: '¥{value}', fontSize: 11 },
          splitLine: { lineStyle: { type: 'dashed', color: '#e8e8e8' } }
        },
        series: [{
          type: 'bar',
          data: orders.map(o => ({
            value: Number(o.payAmount) || 0,
            name: typeLabel[o.orderType],
            itemStyle: { color: typeColor[o.orderType] || '#909399', borderRadius: [4, 4, 0, 0] }
          })),
          barMaxWidth: 48,
          label: { show: true, position: 'top', formatter: params => `¥${params.value.toFixed(2)}`, fontSize: 11, color: '#606266' }
        }]
      })
    },
    lotOccupancy(row) {
      const total = Number(row.totalSpaceCount) || 0
      if (total <= 0) return 0
      const available = Number(row.availableSpaceCount) || 0
      return Math.round((Math.max(total - available, 0) / total) * 100)
    },
    occupancyColor(pct) {
      if (pct >= 90) return '#f56c6c'
      if (pct >= 70) return '#e6a23c'
      return '#67c23a'
    },
    lotStatusLabel(status) { return findLabel(LOT_STATUS_OPTIONS, status) },
    lotStatusTagType(status) { return findTagType(LOT_STATUS_OPTIONS, status) },
    recentOrderTypeLabel(v) { return findLabel(RECENT_ORDER_TYPE_OPTIONS, v) },
    recentOrderTypeTag(v) { return findTagType(RECENT_ORDER_TYPE_OPTIONS, v) },
    recentPayStatusLabel(v) { return findLabel(RECENT_PAY_STATUS_OPTIONS, v) },
    recentPayStatusTag(v) { return findTagType(RECENT_PAY_STATUS_OPTIONS, v) },
    formatAmount(v) {
      const n = Number(v)
      return Number.isNaN(n) ? '0.00' : n.toFixed(2)
    },
    goLotPage() { this.$router.push({ path: '/archives/lot' }) },
    goSpacePage() { this.$router.push({ path: '/archives/space' }) },
    goTempPage() { this.$router.push({ path: '/operations/temp' }) },
    goMonthlyPage() { this.$router.push({ path: '/operations/monthly' }) },
    goMembershipPage() { this.$router.push({ path: '/operations/membership' }) },
    goPaymentPage() { this.$router.push({ path: '/operations/payment' }) },
    goCustomerPage() { this.$router.push({ path: '/customers/customer' }) },
    goVehiclePage() { this.$router.push({ path: '/customers/vehicle' }) },
    goLotAdminPage() { this.$router.push({ path: '/archives/lotadmin' }) },
    loadQuickConfig() {
      const applyLocalOrDefault = () => {
        try {
          const saved = localStorage.getItem('parking_quick_config')
          if (saved) { this.configuredKeys = JSON.parse(saved); return }
        } catch { /* ignore */ }
        this.applyDefaultConfig()
      }
      getQuickConfig().then(res => {
        const raw = res.data
        if (raw) {
          try { this.configuredKeys = JSON.parse(raw); return } catch { /* ignore */ }
        }
        // API 返回空（表未存数据），走 localStorage 或默认
        applyLocalOrDefault()
      }).catch(() => {
        // API 不可用，走 localStorage 或默认
        applyLocalOrDefault()
      })
    },
    applyDefaultConfig() {
      const PARKING_PREFIXES = ['/archives', '/operations', '/customers']
      this.configuredKeys = this.menuItems
        .filter(m => PARKING_PREFIXES.some(p => m.path.startsWith(p)))
        .map(m => m.key)
    },
    openQuickConfig() {
      this.pendingKeys = [...this.configuredKeys]
      this.showQuickConfig = true
    },
    saveQuickConfig() {
      this.configuredKeys = [...this.pendingKeys]
      apiSaveQuickConfig(this.configuredKeys).then(() => {
        localStorage.setItem('parking_quick_config', JSON.stringify(this.configuredKeys))
        this.showQuickConfig = false
        this.$message.success('配置已保存')
      }).catch(() => {
        this.$message.error('保存失败，请稍后重试')
      })
    },
    selectAllConfig() {
      this.pendingKeys = this.menuItems.map(m => m.key)
    },
    clearAllConfig() {
      this.pendingKeys = []
    },
    toggleConfigKey(key) {
      const idx = this.pendingKeys.indexOf(key)
      if (idx === -1) this.pendingKeys.push(key)
      else this.pendingKeys.splice(idx, 1)
    }
  }
}
</script>

<style lang="scss" scoped>
.parking-overview {
  .mb12 { margin-bottom: 12px; }

  // ---- stat cards ----
  .stat-card {
    // When stretched by flex parent, make body fill the card height and center content
    display: flex;
    flex-direction: column;

    ::v-deep .el-card__body {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 18px 20px;
    }
  }

  .stat-card__icon {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    color: #fff;
    flex-shrink: 0;
    &--primary { background: linear-gradient(135deg, #409eff, #66b1ff); }
    &--info    { background: linear-gradient(135deg, #909399, #b1b3b8); }
    &--success { background: linear-gradient(135deg, #67c23a, #85ce61); }
    &--warning { background: linear-gradient(135deg, #e6a23c, #ebb563); }
    &--danger  { background: linear-gradient(135deg, #f56c6c, #f78989); }
  }

  .stat-card__title {
    color: #909399;
    font-size: 13px;
  }

  .stat-card__value {
    margin-top: 6px;
    color: #303133;
    font-size: 26px;
    font-weight: 600;
    line-height: 1.2;
  }

  // ---- top-row: stretch both cols to the same height ----
  .top-row {
    align-items: stretch !important;
    flex-wrap: wrap;
    min-height: 260px;
  }

  .stat-col {
    display: flex;
    flex-direction: column;
  }

  // Each stat-row takes an equal share of the left column's height
  .stat-row {
    flex: 1;
    align-items: stretch !important;

    ::v-deep .el-col {
      display: flex;
      flex-direction: column;
    }

    .stat-card {
      flex: 1;
    }
  }

  .quick-col {
    // 作为绝对定位容器；自身无内容高度 → 不参与 flex 高度竞争
    // 高度由 align-items:stretch 跟随 stat-col 决定
    position: relative;

    ::v-deep .el-card {
      // 填充 stat-col 确定的高度，超出内容滚动
      position: absolute;
      top: 0;
      bottom: 0;
      left: 6px;   // 补偿 gutter padding
      right: 6px;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    ::v-deep .el-card__body {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
    }
  }

  // chart-card 不再需要 height:100%（由绝对定位控制）
  .chart-card {
    height: auto !important;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .echarts-donut {
    width: 100%;
    height: 280px;
  }

  // ---- quick actions grid (5 columns) ----
  .quick-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
    padding: 2px 0;
  }

  .quick-grid__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 12px 4px;
    border-radius: 10px;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s, background 0.2s;
    background: #f5f7fa;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      background: #ecf5ff;
    }

    &:active {
      transform: translateY(0);
    }
  }

  .quick-grid__icon {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    color: #fff;
  }

  .quick-grid__label {
    font-size: 12px;
    color: #606266;
    text-align: center;
    font-weight: 500;
    white-space: nowrap;
  }

  .echarts-bar {
    width: 100%;
    height: 260px;
  }
}

// ---- quick config dialog (append-to-body, outside .parking-overview) ----
.quick-config {
  &__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 6px;
  }
  &__hint {
    font-size: 13px;
    color: #909399;
  }
  &__group {
    margin-bottom: 16px;
    &:last-child { margin-bottom: 0; }
  }
  &__group-title {
    font-size: 12px;
    color: #606266;
    font-weight: 600;
    margin-bottom: 8px;
    padding-left: 8px;
    border-left: 3px solid #409eff;
  }
  &__grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }
  &__card {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px 6px 10px;
    border-radius: 8px;
    border: 2px solid #e4e7ed;
    background: #fafafa;
    cursor: pointer;
    transition: border-color 0.2s, background 0.2s;
    &:hover {
      border-color: #c6e2ff;
      background: #f0f7ff;
    }
    &.is-checked {
      border-color: #409eff;
      background: #ecf5ff;
    }
  }
  &__card-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    color: #fff;
  }
  &__card-label {
    font-size: 12px;
    color: #606266;
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
  }
  &__check {
    position: absolute;
    top: 4px;
    right: 5px;
    font-size: 12px;
    color: #409eff;
    font-weight: bold;
  }
}
</style>
