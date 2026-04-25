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
      <el-form-item label="会员类型" prop="membershipType">
        <el-select v-model="queryParams.membershipType" clearable placeholder="请选择会员类型" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_membership_type"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" clearable placeholder="请选择支付状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_membership_pay_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务状态" prop="bizStatus">
        <el-select v-model="queryParams.bizStatus" clearable placeholder="请选择业务状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_membership_biz_status"
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
          v-hasPermi="['parking:membership:add']"
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
          v-hasPermi="['parking:membership:edit']"
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
          v-hasPermi="['parking:membership:remove']"
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
      empty-text="暂无会员订单数据"
      @selection-change="handleSelectionChange"
      @pagination="getList"
    >
      <el-table-column label="订单编号" align="center" prop="orderNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="停车场" align="center" prop="lotName" min-width="160" show-overflow-tooltip />
      <el-table-column label="客户" align="center" min-width="220">
        <template slot-scope="scope">
          {{ customerLabel(scope.row.customerId) }}
        </template>
      </el-table-column>
      <el-table-column label="会员类型" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_membership_type" :value="scope.row.membershipType" />
        </template>
      </el-table-column>
      <el-table-column label="有效期开始" align="center" prop="validStartTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.validStartTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="有效期结束" align="center" prop="validEndTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.validEndTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="应付金额" align="center" prop="payAmount" width="110">
        <template slot-scope="scope">¥{{ formatPrice(scope.row.payAmount) }}</template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_membership_pay_status" :value="scope.row.payStatus" />
        </template>
      </el-table-column>
      <el-table-column label="业务状态" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_membership_biz_status" :value="scope.row.bizStatus" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['parking:membership:edit']"
          >
            修改
          </el-button>
          <el-button
            v-if="scope.row.payStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-s-finance"
            @click="handlePay(scope.row)"
            v-hasPermi="['parking:membership:pay']"
          >
            支付
          </el-button>
          <el-button
            v-if="scope.row.bizStatus !== '3'"
            size="mini"
            type="text"
            icon="el-icon-circle-close"
            @click="handleCancel(scope.row)"
            v-hasPermi="['parking:membership:cancel']"
          >
            取消
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['parking:membership:remove']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </data-table>

    <el-dialog title="订单支付" :visible.sync="payOpen" width="400px" append-to-body @close="payCancel">
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="订单编号">
          <span>{{ payForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="应付金额" prop="payAmount">
          <el-input-number
            v-model="payForm.payAmount"
            :min="0"
            :precision="2"
            controls-position="right"
            style="width: 200px"
          />
          <span class="form-tip">元</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="submitting" @click="submitPay">确认支付</el-button>
        <el-button @click="payCancel">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listParkingLotOptions } from '@/api/parking/lot'
import { listCustomerOptions } from '@/api/parking/customer'
import { listVehicleOptions } from '@/api/parking/vehicle'
import { cancelMembershipOrder, delMembershipOrder, listMembershipOrders, payMembershipOrder } from '@/api/parking/membershipOrder'
import { findCustomerLabel, formatCustomerOption, formatPrice } from '../options'
import { SearchForm, DataTable } from '../components'

export default {
  name: 'ParkingMembershipOrder',
  components: { SearchForm, DataTable },
  dicts: ['parking_membership_type', 'parking_membership_pay_status', 'parking_membership_biz_status'],
  data() {
    return {
      loading: true,
      submitting: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      orderList: [],
      lotOptions: [],
      customerOptions: [],
      vehicleOptions: [],
      payOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: undefined,
        lotId: undefined,
        customerId: undefined,
        membershipType: undefined,
        payStatus: undefined,
        bizStatus: undefined
      },
      payForm: {}
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
      listMembershipOrders(this.queryParams).then(response => {
        this.orderList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    payCancel() {
      this.payOpen = false
      this.payForm = {}
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleAdd() {
      this.$router.push('/parking/membership/form')
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.membershipOrderId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      const membershipOrderId = row ? row.membershipOrderId : this.ids[0]
      this.$router.push({ path: '/parking/membership/form', query: { id: membershipOrderId } })
    },
    handleDelete(row) {
      const orderIds = row.membershipOrderId || this.ids
      this.$modal.confirm('是否确认删除会员订单编号为 "' + orderIds + '" 的数据项？').then(() => {
        return delMembershipOrder(orderIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handlePay(row) {
      this.payForm = {
        membershipOrderId: row.membershipOrderId,
        orderNo: row.orderNo,
        payAmount: row.payAmount
      }
      this.payOpen = true
    },
    submitPay() {
      this.submitting = true
      payMembershipOrder(this.payForm).then(() => {
        this.$modal.msgSuccess('支付成功')
        this.payOpen = false
        this.getList()
      }).finally(() => {
        this.submitting = false
      })
    },
    handleCancel(row) {
      this.$modal.confirm('是否确认取消会员订单 "' + row.orderNo + '"？').then(() => {
        return cancelMembershipOrder({ membershipOrderId: row.membershipOrderId })
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

<style scoped>
.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
