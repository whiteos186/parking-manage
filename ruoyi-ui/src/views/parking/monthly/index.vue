<template>
  <div class="app-container">
    <search-form :model="queryParams" :visible="showSearch" @search="handleQuery" @reset="handleQuery">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单编号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属停车场" prop="lotId">
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
        <el-select v-model="queryParams.payStatus" clearable placeholder="请选择支付状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_monthly_pay_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务状态" prop="bizStatus">
        <el-select v-model="queryParams.bizStatus" clearable placeholder="请选择业务状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_monthly_biz_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </search-form>

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
          type="primary"
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
          type="primary"
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

    <data-table
      :loading="loading"
      :data="orderList"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      empty-text="暂无月卡订单数据"
      @selection-change="handleSelectionChange"
      @pagination="getList"
    >
      <el-table-column label="订单编号" align="center" prop="orderNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="停车场" align="center" prop="lotName" min-width="140" show-overflow-tooltip />
      <el-table-column label="客户" align="center" min-width="220">
        <template slot-scope="scope">
          {{ customerLabel(scope.row.customerId) }}
        </template>
      </el-table-column>
      <el-table-column label="车牌号" align="center" prop="vehiclePlateNo" width="120" show-overflow-tooltip />
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
        <template slot-scope="scope">¥{{ formatPrice(scope.row.payAmount) }}</template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_monthly_pay_status" :value="scope.row.payStatus" />
        </template>
      </el-table-column>
      <el-table-column label="业务状态" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_monthly_biz_status" :value="scope.row.bizStatus" />
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
    </data-table>
  </div>
</template>

<script>
import { listParkingLotOptions } from '@/api/parking/lot'
import { listCustomerOptions } from '@/api/parking/customer'
import { listVehicleOptions } from '@/api/parking/vehicle'
import { cancelMonthlyOrder, delMonthlyOrder, listMonthlyOrders, payMonthlyOrder } from '@/api/parking/monthlyOrder'
import { findCustomerLabel, formatCustomerOption, formatPrice } from '../options'
import { SearchForm, DataTable } from '../components'

export default {
  name: 'ParkingMonthlyOrder',
  components: { SearchForm, DataTable },
  dicts: ['parking_monthly_pay_status', 'parking_monthly_biz_status'],
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
      this.$modal.confirm('是否确认删除月卡订单编号为 "' + monthlyOrderIds + '" 的数据项？').then(() => {
        return delMonthlyOrder(monthlyOrderIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handlePay(row) {
      this.$modal.confirm('是否确认支付月卡订单 "' + row.orderNo + '"？').then(() => {
        return payMonthlyOrder({ monthlyOrderId: row.monthlyOrderId })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('支付成功')
      }).catch(() => {})
    },
    handleCancel(row) {
      this.$modal.confirm('是否确认取消月卡订单 "' + row.orderNo + '"？').then(() => {
        return cancelMonthlyOrder({ monthlyOrderId: row.monthlyOrderId })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('取消成功')
      }).catch(() => {})
    },
    formatPrice,
    formatCustomerOption(item) {
      return formatCustomerOption(item)
    },
    customerLabel(customerId) {
      return findCustomerLabel(this.customerOptions, customerId)
    }
  }
}
</script>
