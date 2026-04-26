<template>
  <div class="app-container">
    <search-form :model="queryParams" :visible="showSearch" @search="handleQuery" @reset="resetDateRange">
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
            :label="item.optionLabel"
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
        <el-select v-model="queryParams.payStatus" clearable placeholder="请选择支付状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_temp_pay_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务状态" prop="bizStatus">
        <el-select v-model="queryParams.bizStatus" clearable placeholder="请选择业务状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_temp_biz_status"
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
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 240px"
        />
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
          v-hasPermi="['parking:temp:add']"
        >
          登记入场
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
          v-hasPermi="['parking:temp:edit']"
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
          v-hasPermi="['parking:temp:remove']"
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
      empty-text="暂无临停订单数据"
      @selection-change="handleSelectionChange"
      @pagination="getList"
    >
      <el-table-column label="订单编号" align="center" prop="tempOrderId" width="90" />
      <el-table-column label="订单号" align="center" prop="orderNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="停车场" align="center" prop="lotName" min-width="140" show-overflow-tooltip />
      <el-table-column label="客户名称" align="center" prop="customerName" min-width="140" show-overflow-tooltip />
      <el-table-column label="车牌号" align="center" prop="vehiclePlateNo" width="120" />
      <el-table-column label="入场时间" align="center" prop="inTime" width="170" />
      <el-table-column label="出场时间" align="center" prop="outTime" width="170" />
      <el-table-column label="时长(分钟)" align="center" prop="parkingDurationMin" width="100" />
      <el-table-column label="应收金额" align="center" prop="feeAmount" width="100">
        <template slot-scope="scope">¥{{ formatPrice(scope.row.feeAmount) }}</template>
      </el-table-column>
      <el-table-column label="实付金额" align="center" prop="payAmount" width="100">
        <template slot-scope="scope">¥{{ formatPrice(scope.row.payAmount) }}</template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_temp_pay_status" :value="scope.row.payStatus" />
        </template>
      </el-table-column>
      <el-table-column label="业务状态" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_temp_biz_status" :value="scope.row.bizStatus" />
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
    </data-table>

    <el-dialog title="确认支付" :visible.sync="settleOpen" width="400px" append-to-body @close="cancelSettle">
      <el-form :model="settleForm" label-width="90px">
        <el-form-item label="订单号">
          <span>{{ settleForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="车牌号">
          <span>{{ settleForm.vehiclePlateNo }}</span>
        </el-form-item>
        <el-form-item label="应付金额">
          <span>¥{{ formatPrice(settleForm.payAmount) }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="settling" @click="submitSettle">确认已收款</el-button>
        <el-button @click="cancelSettle">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listParkingLotOptions } from '@/api/parking/lot'
import { listCustomerOptions } from '@/api/parking/customer'
import { delTempOrder, entryTempOrder, exitTempOrder, listTempOrders, settleTempOrder } from '@/api/parking/tempOrder'
import { formatCustomerOption } from '../options'
import { SearchForm, DataTable } from '../components'

export default {
  name: 'ParkingTempOrder',
  components: { SearchForm, DataTable },
  dicts: ['parking_temp_pay_status', 'parking_temp_biz_status'],
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
      settleForm: {}
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
        this.customerOptions = (response.data || []).map(item => ({ ...item, optionLabel: formatCustomerOption(item) }))
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
    resetDateRange() {
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
      this.$modal.confirm('确认车辆"' + row.vehiclePlateNo + '"入场？').then(() => {
        return entryTempOrder(row.tempOrderId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('入场登记成功')
      }).catch(() => {})
    },
    handleExit(row) {
      this.$modal.confirm('确认车辆"' + row.vehiclePlateNo + '"出场？出场后将自动计算费用。').then(() => {
        return exitTempOrder(row.tempOrderId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('出场登记成功，请确认支付')
      }).catch(() => {})
    },
    handleSettle(row) {
      if (row.bizStatus !== '2') {
        this.$modal.msgWarning('请先登记出场并完成费用计算')
        return
      }
      if (row.payAmount === undefined || row.payAmount === null) {
        this.$modal.msgWarning('当前订单尚未完成费用计算')
        return
      }
      this.settleForm = {
        tempOrderId: row.tempOrderId,
        orderNo: row.orderNo,
        vehiclePlateNo: row.vehiclePlateNo,
        payAmount: row.payAmount
      }
      this.settleOpen = true
    },
    submitSettle() {
      this.settling = true
      settleTempOrder(this.settleForm).then(() => {
        this.$modal.msgSuccess('结算成功')
        this.settleOpen = false
        this.getList()
      }).finally(() => {
        this.settling = false
      })
    },
    handleDelete(row) {
      const tempOrderIds = row.tempOrderId || this.ids
      this.$modal.confirm('是否确认删除临停订单编号为 "' + tempOrderIds + '" 的数据项？').then(() => {
        return delTempOrder(tempOrderIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>
