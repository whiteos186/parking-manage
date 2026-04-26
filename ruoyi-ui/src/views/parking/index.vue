<template>
  <div class="app-container parking-overview" v-loading="loading">
    <div class="overview-toolbar">
      <div class="overview-toolbar__main">
        <div class="overview-toolbar__title">工作台</div>
        <div class="overview-toolbar__meta">
          <el-tag size="mini" :type="roleTagType">{{ roleLabel }}</el-tag>
          <span>{{ welcomeText }}</span>
          <span>更新时间：{{ lastRefreshText }}</span>
        </div>
      </div>
      <el-button
        v-hasPermi="['parking:overview:list']"
        size="mini"
        type="primary"
        icon="el-icon-refresh"
        :loading="loading"
        @click="getOverviewData"
      >刷新</el-button>
    </div>

    <el-row :gutter="12" class="mb12">
      <el-col v-for="card in statCards" :key="card.key" :xs="12" :sm="12" :lg="6" class="metric-col">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-card__icon" :class="`metric-card__icon--${card.color}`">
            <i :class="card.icon" />
          </div>
          <div class="metric-card__body">
            <div class="metric-card__title">{{ card.title }}</div>
            <div class="metric-card__value">{{ card.value }}</div>
            <div class="metric-card__hint">{{ card.hint }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" type="flex" class="mb12 equal-card-row">
      <el-col :md="24" :lg="16" class="mb12 equal-card-col">
        <el-card shadow="hover" class="panel-card">
          <div slot="header" class="card-header">
            <div>
              <span>车位状态分布</span>
              <span class="card-header__sub">占用率 {{ occupancyRate }}%</span>
            </div>
            <el-tag size="mini" :type="pressureTagType">{{ pressureLabel }}</el-tag>
          </div>
          <div class="space-panel">
            <div ref="spaceChart" class="echarts-donut" />
            <div class="space-breakdown">
              <div v-for="item in spaceBreakdown" :key="item.key" class="space-breakdown__item">
                <div class="space-breakdown__top">
                  <span class="space-breakdown__name">
                    <i class="space-breakdown__dot" :style="{ backgroundColor: item.color }" />
                    {{ item.label }}
                  </span>
                  <span class="space-breakdown__value">{{ item.value }} 个</span>
                </div>
                <el-progress :percentage="item.percent" :color="item.color" :show-text="false" />
                <div class="space-breakdown__percent">{{ item.percent }}%</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :md="24" :lg="8" class="mb12 equal-card-col">
        <el-card shadow="hover" class="panel-card quick-panel">
          <div slot="header" class="card-header">
            <span>快捷入口</span>
            <el-tooltip content="配置快捷入口" placement="top">
              <el-button type="text" icon="el-icon-setting" @click="openQuickConfig" />
            </el-tooltip>
          </div>
          <div class="quick-grid">
            <div class="quick-grid__item" @click="getOverviewData">
              <div class="quick-grid__icon" :style="{ background: iconGradient('success') }">
                <i class="el-icon-refresh" />
              </div>
              <div class="quick-grid__label">刷新数据</div>
            </div>
            <div
              v-for="item in quickActions"
              :key="item.key"
              class="quick-grid__item"
              @click="$router.push(item.path)"
            >
              <div class="quick-grid__icon" :style="{ background: iconGradient(item.color) }">
                <svg-icon :icon-class="item.icon" />
              </div>
              <div class="quick-grid__label">{{ item.title }}</div>
            </div>
          </div>
          <div v-if="quickActions.length === 0" class="empty-hint">暂无可用入口</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" type="flex" class="equal-card-row">
      <el-col :md="24" :lg="14" class="mb12 equal-card-col">
        <el-card shadow="hover" class="panel-card">
          <div slot="header" class="card-header">
            <div>
              <span>停车场占用排行 TOP {{ topLots.length || TOP_LOT_LIMIT }}</span>
              <span class="card-header__sub">按实时占用率排序</span>
            </div>
            <el-button v-hasPermi="['parking:lot:list']" type="text" @click="goLotPage">
              查看全部 <i class="el-icon-arrow-right" />
            </el-button>
          </div>
          <div v-if="topLots.length" v-loading="lotLoading" class="lot-rank-board">
            <div class="lot-rank-summary">
              <div class="lot-rank-summary__main">
                <div class="lot-rank-summary__label">最高占用</div>
                <div class="lot-rank-summary__name">{{ topLots[0].lotName || '-' }}</div>
                <div class="lot-rank-summary__meta">
                  <span>占用 {{ lotOccupied(topLots[0]) }}</span>
                  <span>空闲 {{ lotAvailable(topLots[0]) }}</span>
                  <span>总计 {{ lotTotal(topLots[0]) }}</span>
                </div>
              </div>
              <div class="lot-rank-summary__rate" :style="{ color: occupancyColor(lotOccupancy(topLots[0])) }">
                {{ lotOccupancy(topLots[0]) }}%
              </div>
            </div>

            <div class="lot-rank-list">
              <div
                v-for="(lot, index) in topLots"
                :key="lot.lotId || index"
                class="lot-rank-row"
                :class="`lot-rank-row--${occupancyTone(lotOccupancy(lot))}`"
              >
                <div class="lot-rank-row__rank" :class="`is-rank-${index + 1}`">{{ index + 1 }}</div>
                <div class="lot-rank-row__content">
                  <div class="lot-rank-row__top">
                    <span class="lot-rank-row__name">{{ lot.lotName || '-' }}</span>
                    <span class="lot-rank-row__tag">{{ occupancyLabel(lotOccupancy(lot)) }}</span>
                  </div>
                  <div class="lot-rank-row__bar">
                    <span
                      class="lot-rank-row__bar-fill"
                      :style="{ width: lotOccupancy(lot) + '%', backgroundColor: occupancyColor(lotOccupancy(lot)) }"
                    />
                  </div>
                  <div class="lot-rank-row__bottom">
                    <span>占用 {{ lotOccupied(lot) }}/{{ lotTotal(lot) }}</span>
                    <span>空闲 {{ lotAvailable(lot) }}</span>
                    <span v-if="Number(lot.lockedSpaceCount) || Number(lot.disabledSpaceCount)">
                      异常 {{ (Number(lot.lockedSpaceCount) || 0) + (Number(lot.disabledSpaceCount) || 0) }}
                    </span>
                  </div>
                </div>
                <div class="lot-rank-row__rate" :style="{ color: occupancyColor(lotOccupancy(lot)) }">
                  {{ lotOccupancy(lot) }}%
                </div>
              </div>
            </div>
          </div>
          <div v-else v-loading="lotLoading" class="empty-hint empty-hint--large">暂无停车场数据</div>
        </el-card>
      </el-col>

      <el-col :md="24" :lg="10" class="mb12 equal-card-col">
        <el-card shadow="hover" class="panel-card recent-panel">
          <div slot="header" class="card-header">
            <div>
              <span>最近订单</span>
              <span class="card-header__sub">最新 {{ recentOrders.length || RECENT_ORDER_LIMIT }} 条业务</span>
            </div>
            <el-button type="text" @click="goPaymentPage">
              支付流水 <i class="el-icon-arrow-right" />
            </el-button>
          </div>
          <div v-if="recentOrders.length" class="order-list" v-loading="recentLoading">
            <div
              v-for="order in recentOrders"
              :key="`${order.orderType}-${order.orderNo}`"
              class="order-list__item"
              @click="openRecentOrder(order)"
            >
              <div class="order-list__left">
                <el-tag size="mini" :type="recentOrderTypeTag(order.orderType)">
                  {{ recentOrderTypeLabel(order.orderType) }}
                </el-tag>
                <div>
                  <div class="order-list__no">{{ order.orderNo || '-' }}</div>
                  <div class="order-list__meta">{{ order.lotName || '-' }} · {{ parseTime(order.createTime) || '-' }}</div>
                </div>
              </div>
              <div class="order-list__right">
                <div class="order-list__amount">¥{{ formatPrice(order.payAmount) }}</div>
                <el-tag size="mini" :type="recentPayStatusTag(order.payStatus)">
                  {{ recentPayStatusLabel(order.payStatus) }}
                </el-tag>
              </div>
            </div>
          </div>
          <div v-else v-loading="recentLoading" class="empty-hint empty-hint--large">暂无订单数据</div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="配置快捷入口" :visible.sync="showQuickConfig" width="560px" append-to-body>
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
        <div v-if="menuItems.length === 0" class="empty-hint empty-hint--large">暂无可配置菜单</div>
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
import { getParkingOverviewStats, getParkingRecentOrders, getParkingTopLots } from '@/api/parking/overview'
import { getQuickConfig, saveQuickConfig as apiSaveQuickConfig } from '@/api/parking/userConfig'
import { findLabel, findTagType } from './options'

const QUICK_COLORS = ['primary', 'success', 'warning', 'danger', 'info']
const TOP_LOT_LIMIT = 5
const RECENT_ORDER_LIMIT = 6

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
  dicts: ['parking_payment_status'],
  data() {
    return {
      TOP_LOT_LIMIT,
      RECENT_ORDER_LIMIT,
      loading: false,
      lotLoading: false,
      recentLoading: false,
      overviewStats: { ...EMPTY_STATS },
      topLots: [],
      recentOrders: [],
      chart: null,
      showQuickConfig: false,
      configuredKeys: [],
      pendingKeys: [],
      lastRefreshTime: null
    }
  },
  computed: {
    ...mapGetters(['sidebarRouters', 'roles', 'nickName']),
    welcomeText() {
      return `欢迎，${this.nickName || '管理员'}`
    },
    roleLabel() {
      if ((this.roles || []).includes('admin')) return '系统管理员'
      if ((this.roles || []).includes('lot_admin')) return '停车场管理员'
      if ((this.roles || []).includes('customer')) return '客户'
      return '运营人员'
    },
    roleTagType() {
      if ((this.roles || []).includes('admin')) return 'danger'
      if ((this.roles || []).includes('lot_admin')) return 'success'
      return 'info'
    },
    lastRefreshText() {
      return this.lastRefreshTime ? this.parseTime(this.lastRefreshTime) : '待刷新'
    },
    occupancyRate() {
      const total = Number(this.overviewStats.totalSpaceCount) || 0
      if (total <= 0) return 0
      return Math.round(((Number(this.overviewStats.occupiedSpaceCount) || 0) / total) * 100)
    },
    pressureLabel() {
      if (this.occupancyRate >= 90) return '高负荷'
      if (this.occupancyRate >= 70) return '较繁忙'
      return '平稳'
    },
    pressureTagType() {
      if (this.occupancyRate >= 90) return 'danger'
      if (this.occupancyRate >= 70) return 'warning'
      return 'success'
    },
    statCards() {
      return [
        { key: 'lot', title: '停车场总数', value: this.formatNumber(this.overviewStats.lotCount), hint: '当前可管理车场', icon: 'el-icon-office-building', color: 'primary' },
        { key: 'total', title: '车位总数', value: this.formatNumber(this.overviewStats.totalSpaceCount), hint: `空闲 ${this.formatNumber(this.overviewStats.availableSpaceCount)} 个`, icon: 'el-icon-s-grid', color: 'info' },
        { key: 'occupied', title: '已占用车位', value: this.formatNumber(this.overviewStats.occupiedSpaceCount), hint: `占用率 ${this.occupancyRate}%`, icon: 'el-icon-truck', color: 'danger' },
        { key: 'todayRevenue', title: '今日营收', value: `¥${this.formatPrice(this.overviewStats.todayRevenue)}`, hint: `入场 ${this.formatNumber(this.overviewStats.todayEntryCount)} / 出场 ${this.formatNumber(this.overviewStats.todayExitCount)}`, icon: 'el-icon-coin', color: 'warning' }
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
      return buckets.map(item => {
        const value = Number(item.value) || 0
        return {
          ...item,
          value,
          percent: total > 0 ? Math.round((value / total) * 100) : 0
        }
      })
    },
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
          for (const route of (routes || [])) {
            if (route.hidden) continue
            const seg = route.path.startsWith('/') ? route.path : parentPath + '/' + route.path
            const path = normalize(seg)
            if (route.children && route.children.length) {
              flatten(route.children, path)
            } else if (route.meta && route.meta.title && path !== currentPath) {
              items.push({
                key: path,
                path,
                title: route.meta.title,
                icon: route.meta.icon || 'dashboard',
                color: QUICK_COLORS[colorIdx++ % QUICK_COLORS.length]
              })
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
      return this.menuGroups.reduce((rows, group) => rows.concat(group.items), [])
    },
    quickActions() {
      return this.menuItems.filter(item => this.configuredKeys.includes(item.key))
    }
  },
  watch: {
    spaceBreakdown() {
      this.updateChart()
    }
  },
  created() {
    this.loadQuickConfig()
    this.getOverviewData()
  },
  mounted() {
    this.$nextTick(() => {
      this.initChart()
    })
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chart) {
      this.chart.dispose()
    }
  },
  methods: {
    getOverviewData() {
      this.loading = true
      Promise.all([this.getStats(), this.getTopLots(), this.getRecentOrders()])
        .then(() => {
          this.lastRefreshTime = new Date()
        })
        .finally(() => {
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
      return getParkingTopLots(TOP_LOT_LIMIT)
        .then(res => { this.topLots = res.data || [] })
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
        value: item.value,
        percent: item.percent,
        itemStyle: { color: item.color }
      }))
      const total = data.reduce((sum, item) => sum + item.value, 0)
      this.chart.clear()
      this.chart.setOption({
        title: [{
          text: String(total),
          subtext: '总车位',
          left: '31%',
          top: '38%',
          textAlign: 'center',
          textStyle: { fontSize: 24, fontWeight: 'bold', color: '#303133' },
          subtextStyle: { fontSize: 13, color: '#909399' }
        }],
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 0,
          top: 'center',
          itemWidth: 10,
          itemHeight: 10,
          textStyle: { fontSize: 13, color: '#606266' },
          formatter: name => {
            const item = data.find(row => row.name === name)
            return item ? `${name}  ${item.value} (${item.percent}%)` : name
          }
        },
        series: [{
          type: 'pie',
          radius: ['48%', '68%'],
          center: ['32%', '50%'],
          stillShowZeroSum: true,
          avoidLabelOverlap: true,
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 13, fontWeight: 'bold' },
            scaleSize: 5
          },
          data
        }]
      })
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    },
    formatNumber(value) {
      const num = Number(value) || 0
      return num.toLocaleString()
    },
    lotTotal(row) {
      return Number(row.totalSpaceCount) || 0
    },
    lotAvailable(row) {
      return Number(row.availableSpaceCount) || 0
    },
    lotOccupied(row) {
      return Number(row.occupiedSpaceCount) || Math.max(this.lotTotal(row) - this.lotAvailable(row), 0)
    },
    lotOccupancy(row) {
      const rate = Number(row.occupancyRate)
      if (!Number.isNaN(rate)) return Math.max(0, Math.min(Math.round(rate), 100))
      const total = this.lotTotal(row)
      if (total <= 0) return 0
      return Math.round((this.lotOccupied(row) / total) * 100)
    },
    occupancyTone(pct) {
      if (pct >= 90) return 'danger'
      if (pct >= 70) return 'warning'
      if (pct > 0) return 'active'
      return 'idle'
    },
    occupancyLabel(pct) {
      if (pct >= 90) return '高负荷'
      if (pct >= 70) return '较繁忙'
      if (pct > 0) return '平稳'
      return '空闲'
    },
    occupancyColor(pct) {
      if (pct >= 90) return '#f56c6c'
      if (pct >= 70) return '#e6a23c'
      return '#67c23a'
    },
    iconGradient(color) {
      const map = {
        primary: 'linear-gradient(135deg, #409eff, #66b1ff)',
        success: 'linear-gradient(135deg, #67c23a, #85ce61)',
        warning: 'linear-gradient(135deg, #e6a23c, #ebb563)',
        danger: 'linear-gradient(135deg, #f56c6c, #f78989)',
        info: 'linear-gradient(135deg, #909399, #b1b3b8)'
      }
      return map[color] || map.primary
    },
    recentOrderTypeLabel(value) {
      return findLabel(RECENT_ORDER_TYPE_OPTIONS, value)
    },
    recentOrderTypeTag(value) {
      return findTagType(RECENT_ORDER_TYPE_OPTIONS, value)
    },
    recentPayStatusOptions() {
      const type = this.dict && this.dict.type ? this.dict.type.parking_payment_status : null
      return type && type.length ? type : RECENT_PAY_STATUS_OPTIONS
    },
    recentPayStatusLabel(value) {
      return findLabel(this.recentPayStatusOptions(), value)
    },
    recentPayStatusTag(value) {
      return findTagType(this.recentPayStatusOptions(), value)
    },
    openRecentOrder(order) {
      const map = {
        temp: '/operations/temp',
        monthly: '/operations/monthly',
        membership: '/operations/membership'
      }
      this.$router.push({ path: map[order.orderType] || '/operations/payment' })
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
          if (saved) {
            this.configuredKeys = JSON.parse(saved)
            return
          }
        } catch (e) {
          // ignore invalid local data
        }
        this.applyDefaultConfig()
      }
      getQuickConfig().then(res => {
        const raw = res.data
        if (raw) {
          try {
            this.configuredKeys = JSON.parse(raw)
            return
          } catch (e) {
            // ignore invalid remote data
          }
        }
        applyLocalOrDefault()
      }).catch(() => {
        applyLocalOrDefault()
      })
    },
    applyDefaultConfig() {
      const prefixes = ['/archives', '/operations', '/customers']
      this.configuredKeys = this.menuItems
        .filter(item => prefixes.some(prefix => item.path.startsWith(prefix)))
        .map(item => item.key)
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
      this.pendingKeys = this.menuItems.map(item => item.key)
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
  background: #f4f6f9;
  min-height: calc(100vh - 84px);

  .mb12 { margin-bottom: 12px; }

  .overview-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
    padding: 14px 16px;
    background: #fff;
    border: 1px solid #ebeef5;
    border-radius: 6px;
  }

  .overview-toolbar__title {
    color: #303133;
    font-size: 20px;
    font-weight: 600;
    line-height: 1.3;
  }

  .overview-toolbar__meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    margin-top: 6px;
    color: #909399;
    font-size: 12px;
  }

  .metric-col {
    margin-bottom: 12px;
  }

  .metric-card {
    height: 124px;

    ::v-deep .el-card__body {
      height: 100%;
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 18px 20px;
      box-sizing: border-box;
    }
  }

  .metric-card__icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 24px;
    flex-shrink: 0;

    &--primary { background: linear-gradient(135deg, #409eff, #66b1ff); }
    &--info { background: linear-gradient(135deg, #909399, #b1b3b8); }
    &--success { background: linear-gradient(135deg, #67c23a, #85ce61); }
    &--warning { background: linear-gradient(135deg, #e6a23c, #ebb563); }
    &--danger { background: linear-gradient(135deg, #f56c6c, #f78989); }
  }

  .metric-card__body {
    min-width: 0;
  }

  .metric-card__title,
  .metric-card__hint {
    color: #909399;
    font-size: 12px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .metric-card__value {
    margin: 7px 0 5px;
    color: #303133;
    font-size: 26px;
    font-weight: 600;
    line-height: 1.15;
    white-space: nowrap;
  }

  .equal-card-row {
    align-items: stretch;
    flex-wrap: wrap;
  }

  .equal-card-col {
    display: flex;
    flex-direction: column;
  }

  .panel-card {
    height: 100%;
    border-radius: 6px;
    display: flex;
    flex-direction: column;

    ::v-deep .el-card__header {
      padding: 13px 16px;
      flex-shrink: 0;
    }

    ::v-deep .el-card__body {
      flex: 1;
      padding: 16px;
    }
  }

  .card-header {
    min-height: 28px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    color: #303133;
    font-weight: 600;
  }

  .card-header__sub {
    margin-left: 8px;
    color: #909399;
    font-size: 12px;
    font-weight: 400;
  }

  .space-panel {
    display: grid;
    grid-template-columns: minmax(260px, 1fr) 260px;
    align-items: center;
    gap: 12px;
  }

  .echarts-donut {
    width: 100%;
    height: 282px;
  }

  .space-breakdown {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .space-breakdown__top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  .space-breakdown__name {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: #606266;
    font-size: 13px;
  }

  .space-breakdown__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .space-breakdown__value {
    color: #303133;
    font-size: 13px;
    font-weight: 600;
  }

  .space-breakdown__percent {
    margin-top: 3px;
    color: #909399;
    font-size: 12px;
    text-align: right;
  }

  .quick-panel {
    min-height: 340px;
  }

  .quick-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
  }

  .quick-grid__item {
    min-height: 76px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 7px;
    padding: 10px 6px;
    border: 1px solid #ebeef5;
    border-radius: 6px;
    background: #fafafa;
    cursor: pointer;
    transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;

    &:hover {
      border-color: #c6e2ff;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.12);
      transform: translateY(-1px);
    }
  }

  .quick-grid__icon {
    width: 34px;
    height: 34px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 17px;
    flex-shrink: 0;
  }

  .quick-grid__label {
    width: 100%;
    color: #606266;
    font-size: 12px;
    font-weight: 500;
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .lot-rank-board {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .lot-rank-summary {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 16px 18px;
    border-radius: 6px;
    background: linear-gradient(135deg, #f7fbff 0%, #f9fafc 100%);
    border: 1px solid #e8f1ff;
  }

  .lot-rank-summary__main {
    min-width: 0;
  }

  .lot-rank-summary__label {
    margin-bottom: 6px;
    color: #909399;
    font-size: 12px;
  }

  .lot-rank-summary__name {
    color: #303133;
    font-size: 18px;
    font-weight: 600;
    line-height: 1.35;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .lot-rank-summary__meta {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 8px;
    color: #909399;
    font-size: 12px;
  }

  .lot-rank-summary__rate {
    flex-shrink: 0;
    font-size: 38px;
    font-weight: 700;
    line-height: 1;
  }

  .lot-rank-list {
    display: flex;
    flex-direction: column;
  }

  .lot-rank-row {
    position: relative;
    display: grid;
    grid-template-columns: 34px minmax(0, 1fr) 54px;
    align-items: center;
    gap: 12px;
    padding: 11px 2px 12px;
    border-top: 1px solid #ebeef5;

    &:first-child {
      border-top: 0;
    }

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 12px;
      bottom: 12px;
      width: 3px;
      border-radius: 999px;
      background: transparent;
    }

    &--danger::before { background: #f56c6c; }
    &--warning::before { background: #e6a23c; }
    &--active::before { background: #67c23a; }
    &--idle::before { background: #c0c4cc; }
  }

  .lot-rank-row__rank {
    width: 28px;
    height: 28px;
    margin-left: 8px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background: #f2f6fc;
    color: #606266;
    font-size: 13px;
    font-weight: 700;
    flex-shrink: 0;

    &.is-rank-1 {
      background: #ecf5ff;
      color: #409eff;
    }

    &.is-rank-2 {
      background: #f0f9eb;
      color: #67c23a;
    }

    &.is-rank-3 {
      background: #fdf6ec;
      color: #e6a23c;
    }
  }

  .lot-rank-row__content {
    min-width: 0;
  }

  .lot-rank-row__top,
  .lot-rank-row__bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
  }

  .lot-rank-row__name {
    min-width: 0;
    color: #303133;
    font-size: 14px;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .lot-rank-row__tag {
    flex-shrink: 0;
    color: #909399;
    font-size: 12px;
  }

  .lot-rank-row__bar {
    height: 8px;
    margin: 8px 0 6px;
    overflow: hidden;
    border-radius: 999px;
    background: #edf1f7;
  }

  .lot-rank-row__bar-fill {
    display: block;
    height: 100%;
    min-width: 2px;
    border-radius: inherit;
    transition: width 0.2s;
  }

  .lot-rank-row__bottom {
    justify-content: flex-start;
    color: #909399;
    font-size: 12px;
  }

  .lot-rank-row__rate {
    justify-self: end;
    font-size: 18px;
    font-weight: 700;
  }

  .recent-panel {
    min-height: 420px;
  }

  .order-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .order-list__item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    min-height: 58px;
    padding: 10px 12px;
    border: 1px solid #ebeef5;
    border-radius: 6px;
    background: #fff;
    cursor: pointer;
    transition: border-color 0.2s, background 0.2s;

    &:hover {
      border-color: #c6e2ff;
      background: #f8fbff;
    }
  }

  .order-list__left {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .order-list__no {
    max-width: 220px;
    color: #303133;
    font-size: 13px;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .order-list__meta {
    max-width: 240px;
    margin-top: 4px;
    color: #909399;
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .order-list__right {
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 5px;
  }

  .order-list__amount {
    color: #303133;
    font-size: 14px;
    font-weight: 600;
  }

  .empty-hint {
    color: #909399;
    font-size: 13px;
    text-align: center;
  }

  .empty-hint--large {
    min-height: 180px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

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
    color: #909399;
    font-size: 13px;
  }

  &__group {
    margin-bottom: 16px;

    &:last-child { margin-bottom: 0; }
  }

  &__group-title {
    margin-bottom: 8px;
    padding-left: 8px;
    border-left: 3px solid #409eff;
    color: #606266;
    font-size: 12px;
    font-weight: 600;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 8px;
  }

  &__card {
    position: relative;
    min-height: 76px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px 6px;
    border: 1px solid #e4e7ed;
    border-radius: 6px;
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
    color: #fff;
    font-size: 15px;
  }

  &__card-label {
    max-width: 100%;
    color: #606266;
    font-size: 12px;
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__check {
    position: absolute;
    top: 5px;
    right: 6px;
    color: #409eff;
    font-size: 12px;
    font-weight: bold;
  }
}

@media (max-width: 1200px) {
  .parking-overview {
    .space-panel {
      grid-template-columns: 1fr;
    }

    .quick-grid {
      grid-template-columns: repeat(4, minmax(0, 1fr));
    }
  }
}

@media (max-width: 768px) {
  .parking-overview {
    .overview-toolbar {
      align-items: flex-start;
      flex-direction: column;
    }

    .metric-card__value {
      font-size: 22px;
    }

    .quick-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }

    .order-list__item {
      align-items: flex-start;
      flex-direction: column;
    }

    .order-list__right {
      width: 100%;
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
    }
  }

  .quick-config {
    &__grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }
}
</style>
