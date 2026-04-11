<template>
  <div class="app-container">
    <div class="page-title">月卡订单管理</div>
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单编号"
          clearable
          style="width: 220px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属停车场" prop="lotId">
        <el-select v-model="queryParams.lotId" clearable placeholder="请选择停车场">
          <el-option
            v-for="item in lotOptions"
            :key="item.lotId"
            :label="item.lotName"
            :value="item.lotId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" clearable filterable placeholder="请选择客户" style="width: 280px">
          <el-option
            v-for="item in customerOptions"
            :key="item.customerId"
            :label="formatCustomerOption(item)"
            :value="item.customerId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" clearable placeholder="请选择支付状态">
          <el-option
            v-for="item in payStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务状态" prop="bizStatus">
        <el-select v-model="queryParams.bizStatus" clearable placeholder="请选择业务状态">
          <el-option
            v-for="item in bizStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
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
          v-hasPermi="['parking:monthly:add']"
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
          v-hasPermi="['parking:monthly:edit']"
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
          v-hasPermi="['parking:monthly:remove']"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="orderList"
      empty-text="暂无月卡订单数据"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单编号" align="center" prop="orderNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="停车场" align="center" prop="lotName" min-width="140" show-overflow-tooltip />
      <el-table-column label="客户" align="center" min-width="220">
        <template slot-scope="scope">
          {{ customerLabel(scope.row.customerId) }}
        </template>
      </el-table-column>
      <el-table-column label="购买月数" align="center" prop="monthCount" width="90" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="到期时间" align="center" prop="endTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="应付金额" align="center" prop="payAmount" width="100">
        <template slot-scope="scope">¥{{ formatAmount(scope.row.payAmount) }}</template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="payStatusTagType(scope.row.payStatus)">
            {{ payStatusLabel(scope.row.payStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="bizStatusTagType(scope.row.bizStatus)">
            {{ bizStatusLabel(scope.row.bizStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['parking:monthly:edit']"
          >
            修改
          </el-button>
          <el-button
            v-if="scope.row.payStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-money"
            @click="handlePay(scope.row)"
            v-hasPermi="['parking:monthly:pay']"
          >
            支付
          </el-button>
          <el-button
            v-if="scope.row.bizStatus !== '3'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleCancel(scope.row)"
            v-hasPermi="['parking:monthly:cancel']"
          >
            取消
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['parking:monthly:remove']"
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
import { listParkingLotOptions } from '@/api/parking/lot'
import { listCustomerOptions } from '@/api/parking/customer'
import { listVehicleOptions } from '@/api/parking/vehicle'
import {
  cancelMonthlyOrder,
  delMonthlyOrder,
  listMonthlyOrders,
  payMonthlyOrder
} from '@/api/parking/monthlyOrder'
import {
  MONTHLY_ORDER_BIZ_STATUS_OPTIONS,
  MONTHLY_ORDER_PAY_STATUS_OPTIONS,
  findCustomerLabel,
  findLabel,
  findTagType,
  formatCustomerOption
} from '../options'

export default {
  name: 'ParkingMonthlyOrder',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      orderList: [],
      lotOptions: [],
      customerOptions: [],
      vehicleOptions: [],
      payStatusOptions: MONTHLY_ORDER_PAY_STATUS_OPTIONS,
      bizStatusOptions: MONTHLY_ORDER_BIZ_STATUS_OPTIONS,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: undefined,
        lotId: undefined,
        customerId: undefined,
        payStatus: undefined,
        bizStatus: undefined
      }
    }
  },
  created() {
    this.getLotOptions()
    this.getCustomerOptions()
    this.getVehicleOptions()
    this.getList()
  },
  methods: {
    getLotOptions() {
      listParkingLotOptions().then(response => {
        this.lotOptions = response.data || []
      })
    },
    getCustomerOptions() {
      listCustomerOptions().then(response => {
        this.customerOptions = response.data || []
      })
    },
    getVehicleOptions() {
      listVehicleOptions().then(response => {
        this.vehicleOptions = response.data || []
      })
    },
    getList() {
      this.loading = true
      listMonthlyOrders(this.queryParams).then(response => {
        this.orderList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.$router.push('/parking/monthly/form')
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.monthlyOrderId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      const monthlyOrderId = row ? row.monthlyOrderId : this.ids[0]
      this.$router.push({ path: '/parking/monthly/form', query: { id: monthlyOrderId } })
    },
    handleDelete(row) {
      const monthlyOrderIds = row.monthlyOrderId || this.ids
      this.$modal.confirm('是否确认删除月卡订单编号为"' + monthlyOrderIds + '"的数据项？').then(() => {
        return delMonthlyOrder(monthlyOrderIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handlePay(row) {
      this.$modal.confirm('是否确认支付月卡订单"' + row.orderNo + '"？').then(() => {
        return payMonthlyOrder({ monthlyOrderId: row.monthlyOrderId })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('支付成功')
      }).catch(() => {})
    },
    handleCancel(row) {
      this.$modal.confirm('是否确认取消月卡订单"' + row.orderNo + '"？').then(() => {
        return cancelMonthlyOrder({ monthlyOrderId: row.monthlyOrderId })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('取消成功')
      }).catch(() => {})
    },
    formatAmount(value) {
      const num = Number(value)
      if (Number.isNaN(num)) {
        return '0.00'
      }
      return num.toFixed(2)
    },
    formatCustomerOption(item) {
      return formatCustomerOption(item)
    },
    customerLabel(customerId) {
      return findCustomerLabel(this.customerOptions, customerId)
    },
    payStatusLabel(status) {
      return findLabel(MONTHLY_ORDER_PAY_STATUS_OPTIONS, status)
    },
    payStatusTagType(status) {
      return findTagType(MONTHLY_ORDER_PAY_STATUS_OPTIONS, status)
    },
    bizStatusLabel(status) {
      return findLabel(MONTHLY_ORDER_BIZ_STATUS_OPTIONS, status)
    },
    bizStatusTagType(status) {
      return findTagType(MONTHLY_ORDER_BIZ_STATUS_OPTIONS, status)
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
