<template>
  <div class="app-container">
    <div class="page-title">支付流水</div>
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="订单号" prop="bizOrderNo">
        <el-input
          v-model="queryParams.bizOrderNo"
          placeholder="请输入业务订单号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单类型" prop="bizOrderType">
        <el-select v-model="queryParams.bizOrderType" clearable placeholder="请选择订单类型" style="width: 130px">
          <el-option
            v-for="item in bizTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支付渠道" prop="payChannel">
        <el-select v-model="queryParams.payChannel" clearable placeholder="请选择支付渠道" style="width: 130px">
          <el-option
            v-for="item in channelOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" clearable placeholder="请选择支付状态" style="width: 130px">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="停车场" prop="lotId">
        <el-select v-model="queryParams.lotId" clearable placeholder="请选择停车场" style="width: 160px">
          <el-option
            v-for="item in lotOptions"
            :key="item.lotId"
            :label="item.lotName"
            :value="item.lotId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" clearable filterable placeholder="请选择客户" style="width: 260px">
          <el-option
            v-for="item in customerOptions"
            :key="item.customerId"
            :label="formatCustomerOption(item)"
            :value="item.customerId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支付时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['parking:payment:add']"
        >
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['parking:payment:edit']"
        >
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['parking:payment:remove']"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="paymentList"
      empty-text="暂无支付流水数据"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="流水编号" align="center" prop="paymentId" width="90" />
      <el-table-column label="业务订单号" align="center" prop="bizOrderNo" min-width="160" show-overflow-tooltip />
      <el-table-column label="订单类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="findTagType(bizTypeOptions, scope.row.bizOrderType)">
            {{ findLabel(bizTypeOptions, scope.row.bizOrderType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="客户" align="center" min-width="220">
        <template slot-scope="scope">
          {{ customerLabel(scope.row.customerId) }}
        </template>
      </el-table-column>
      <el-table-column label="停车场" align="center" prop="lotName" min-width="140" show-overflow-tooltip />
      <el-table-column label="支付渠道" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="findTagType(channelOptions, scope.row.payChannel)">
            {{ findLabel(channelOptions, scope.row.payChannel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付金额" align="center" width="110">
        <template slot-scope="scope">¥{{ formatAmount(scope.row.payAmount) }}</template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="findTagType(statusOptions, scope.row.payStatus)">
            {{ findLabel(statusOptions, scope.row.payStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="交易号" align="center" prop="tradeNo" min-width="160" show-overflow-tooltip />
      <el-table-column label="支付时间" align="center" prop="payTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.payTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款时间" align="center" prop="refundTime" width="170">
        <template slot-scope="scope">
          <span>{{ scope.row.refundTime ? parseTime(scope.row.refundTime) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['parking:payment:edit']"
          >
            修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-refresh-left"
            :disabled="scope.row.payStatus !== '1'"
            @click="handleRefund(scope.row)"
            v-hasPermi="['parking:payment:refund']"
          >
            退款
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['parking:payment:remove']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { delPayment, listPayments, refundPayment } from '@/api/parking/payment'
import { listCustomerOptions } from '@/api/parking/customer'
import { listParkingLotOptions } from '@/api/parking/lot'
import {
  PAYMENT_BIZ_TYPE_OPTIONS,
  PAYMENT_CHANNEL_OPTIONS,
  PAYMENT_STATUS_OPTIONS,
  findCustomerLabel,
  findLabel,
  findTagType,
  formatCustomerOption
} from '../options'

export default {
  name: 'ParkingPayment',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      paymentList: [],
      customerOptions: [],
      dateRange: [],
      lotOptions: [],
      bizTypeOptions: PAYMENT_BIZ_TYPE_OPTIONS,
      channelOptions: PAYMENT_CHANNEL_OPTIONS,
      statusOptions: PAYMENT_STATUS_OPTIONS,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        bizOrderNo: undefined,
        bizOrderType: undefined,
        payChannel: undefined,
        payStatus: undefined,
        lotId: undefined,
        customerId: undefined
      }
    }
  },
  created() {
    this.getList()
    this.getLotOptions()
    this.getCustomerOptions()
  },
  methods: {
    getList() {
      this.loading = true
      const params = Object.assign({}, this.queryParams)
      if (this.dateRange && this.dateRange.length === 2) {
        params['params[payTimeStart]'] = this.dateRange[0]
        params['params[payTimeEnd]'] = this.dateRange[1]
      }
      listPayments(params).then(response => {
        this.paymentList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    getLotOptions() {
      listParkingLotOptions().then(response => {
        this.lotOptions = response.data
      })
    },
    getCustomerOptions() {
      listCustomerOptions().then(response => {
        this.customerOptions = response.data || []
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.$router.push('/parking/payment/form')
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.paymentId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      const paymentId = row ? row.paymentId : this.ids[0]
      this.$router.push({ path: '/parking/payment/form', query: { id: paymentId } })
    },
    handleDelete(row) {
      const paymentIds = row.paymentId || this.ids
      this.$modal.confirm('是否确认删除支付流水编号为"' + paymentIds + '"的数据项？').then(() => {
        return delPayment(paymentIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleRefund(row) {
      this.$modal.confirm('是否确认对支付流水"' + row.bizOrderNo + '"执行退款操作？').then(() => {
        return refundPayment({ paymentId: row.paymentId })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('退款成功')
      }).catch(() => {})
    },
    formatAmount(value) {
      const num = Number(value)
      if (Number.isNaN(num)) {
        return '0.00'
      }
      return num.toFixed(2)
    },
    findLabel(options, value) {
      return findLabel(options, value)
    },
    findTagType(options, value) {
      return findTagType(options, value)
    },
    formatCustomerOption(item) {
      return formatCustomerOption(item)
    },
    customerLabel(customerId) {
      return findCustomerLabel(this.customerOptions, customerId)
    }
  }
}
</script>

<style scoped>
.page-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}
</style>
