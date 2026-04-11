<template>
  <div class="app-container">
    <div class="page-title">临停订单管理</div>
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch">
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
      <el-form-item label="车牌号" prop="vehiclePlateNo">
        <el-input
          v-model="queryParams.vehiclePlateNo"
          placeholder="请输入车牌号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
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
      <el-form-item label="入场时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="-"
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
          v-hasPermi="['parking:temp:add']"
        >
          登记入场
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
          v-hasPermi="['parking:temp:edit']"
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
          v-hasPermi="['parking:temp:remove']"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="orderList"
      empty-text="暂无临停订单数据"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单编号" align="center" prop="tempOrderId" width="90" />
      <el-table-column label="订单号" align="center" prop="orderNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="停车场" align="center" prop="lotName" min-width="140" show-overflow-tooltip />
      <el-table-column label="客户" align="center" min-width="220">
        <template slot-scope="scope">
          {{ customerLabel(scope.row.customerId) }}
        </template>
      </el-table-column>
      <el-table-column label="车牌号" align="center" prop="vehiclePlateNo" width="120" />
      <el-table-column label="入场时间" align="center" prop="inTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.inTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出场时间" align="center" prop="outTime" width="170">
        <template slot-scope="scope">
          <span>{{ scope.row.outTime ? parseTime(scope.row.outTime) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="时长(分钟)" align="center" prop="parkingDurationMin" width="100" />
      <el-table-column label="应收金额" align="center" prop="feeAmount" width="100">
        <template slot-scope="scope">¥{{ formatPrice(scope.row.feeAmount) }}</template>
      </el-table-column>
      <el-table-column label="实付金额" align="center" prop="payAmount" width="100">
        <template slot-scope="scope">¥{{ formatPrice(scope.row.payAmount) }}</template>
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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['parking:temp:edit']"
          >
            修改
          </el-button>
          <el-button
            v-if="scope.row.bizStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-upload2"
            @click="handleEntry(scope.row)"
            v-hasPermi="['parking:temp:entry']"
          >
            入场
          </el-button>
          <el-button
            v-if="scope.row.bizStatus === '1'"
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleExit(scope.row)"
            v-hasPermi="['parking:temp:exit']"
          >
            出场
          </el-button>
          <el-button
            v-if="scope.row.bizStatus === '2'"
            size="mini"
            type="text"
            icon="el-icon-money"
            @click="handleSettle(scope.row)"
            v-hasPermi="['parking:temp:settle']"
          >
            确认支付
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['parking:temp:remove']"
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

    <!-- 支付确认对话框 -->
    <el-dialog title="确认支付" :visible.sync="settleOpen" width="400px" append-to-body @close="cancelSettle">
      <el-form ref="settleForm" :model="settleForm" label-width="90px">
        <el-form-item label="订单号">
          <span>{{ settleForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="车牌号">
          <span>{{ settleForm.vehiclePlateNo }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="settling" @click="submitSettle">确认已收款</el-button>
        <el-button @click="cancelSettle">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listParkingLotOptions } from '@/api/parking/lot'
import { listCustomerOptions } from '@/api/parking/customer'
import { delTempOrder, entryTempOrder, exitTempOrder, listTempOrders, settleTempOrder } from '@/api/parking/tempOrder'
import {
  TEMP_ORDER_BIZ_STATUS_OPTIONS,
  TEMP_ORDER_PAY_STATUS_OPTIONS,
  findCustomerLabel,
  findLabel,
  findTagType,
  formatCustomerOption
} from '../options'

export default {
  name: 'ParkingTempOrder',
  data() {
    return {
      loading: true,
      settling: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      orderList: [],
      lotOptions: [],
      customerOptions: [],
      settleOpen: false,
      dateRange: [],
      payStatusOptions: TEMP_ORDER_PAY_STATUS_OPTIONS,
      bizStatusOptions: TEMP_ORDER_BIZ_STATUS_OPTIONS,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        customerId: undefined,
        vehiclePlateNo: undefined,
        payStatus: undefined,
        bizStatus: undefined,
        inTimeStart: undefined,
        inTimeEnd: undefined
      },
      settleForm: {},
      settleRules: {
        feeAmount: [
          { type: 'number', min: 0, message: '金额不能为负数', trigger: 'blur' }
        ],
        discountAmount: [
          { type: 'number', min: 0, message: '优惠金额不能为负数', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getLotOptions()
    this.getCustomerOptions()
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
    getList() {
      this.loading = true
      const params = Object.assign({}, this.queryParams)
      if (this.dateRange && this.dateRange.length === 2) {
        params.inTimeStart = this.dateRange[0]
        params.inTimeEnd = this.dateRange[1]
      } else {
        params.inTimeStart = undefined
        params.inTimeEnd = undefined
      }
      listTempOrders(params).then(response => {
        this.orderList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    cancelSettle() {
      this.settleOpen = false
      this.settleForm = {}
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.dateRange = []
      this.handleQuery()
    },
    handleAdd() {
      this.$router.push('/parking/temp/form')
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.tempOrderId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      const tempOrderId = row ? row.tempOrderId : this.ids[0]
      this.$router.push({ path: '/parking/temp/form', query: { id: tempOrderId } })
    },
    handleEntry(row) {
      this.$modal.confirm('确认车辆「' + row.vehiclePlateNo + '」入场？').then(() => {
        return entryTempOrder(row.tempOrderId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('入场登记成功')
      }).catch(() => {})
    },
    handleExit(row) {
      this.$modal.confirm('确认车辆「' + row.vehiclePlateNo + '」出场？出场后将自动计算费用。').then(() => {
        return exitTempOrder(row.tempOrderId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('出场登记成功，请确认支付')
      }).catch(() => {})
    },
    handleSettle(row) {
      this.settleForm = {
        tempOrderId: row.tempOrderId,
        orderNo: row.orderNo,
        vehiclePlateNo: row.vehiclePlateNo,
        feeAmount: 0,
        discountAmount: 0
      }
      this.settleOpen = true
    },
    submitSettle() {
      this.$refs.settleForm.validate(valid => {
        if (!valid) return
        this.settling = true
        settleTempOrder(this.settleForm).then(() => {
          this.$modal.msgSuccess('结算成功')
          this.settleOpen = false
          this.getList()
        }).finally(() => {
          this.settling = false
        })
      })
    },
    handleDelete(row) {
      const tempOrderIds = row.tempOrderId || this.ids
      this.$modal.confirm('是否确认删除临停订单编号为"' + tempOrderIds + '"的数据项？').then(() => {
        return delTempOrder(tempOrderIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    formatPrice(value) {
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
      return findLabel(TEMP_ORDER_PAY_STATUS_OPTIONS, status)
    },
    payStatusTagType(status) {
      return findTagType(TEMP_ORDER_PAY_STATUS_OPTIONS, status)
    },
    bizStatusLabel(status) {
      return findLabel(TEMP_ORDER_BIZ_STATUS_OPTIONS, status)
    },
    bizStatusTagType(status) {
      return findTagType(TEMP_ORDER_BIZ_STATUS_OPTIONS, status)
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

.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
