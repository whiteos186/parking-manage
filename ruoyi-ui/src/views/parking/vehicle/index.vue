<template>
  <div class="app-container">
    <search-form :model="queryParams" :visible="showSearch" @search="handleQuery" @reset="handleQuery">
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
      <el-form-item label="车牌号" prop="plateNo">
        <el-input
          v-model="queryParams.plateNo"
          placeholder="请输入车牌号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="车辆类型" prop="vehicleType">
        <el-select v-model="queryParams.vehicleType" clearable placeholder="请选择车辆类型" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_vehicle_type"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_vehicle_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="是否默认" prop="isDefault">
        <el-select v-model="queryParams.isDefault" clearable placeholder="请选择" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_vehicle_default"
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
          v-hasPermi="['parking:vehicle:add']"
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
          v-hasPermi="['parking:vehicle:edit']"
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
          v-hasPermi="['parking:vehicle:remove']"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <data-table
      :loading="loading"
      :data="vehicleList"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      empty-text="暂无车辆数据"
      @selection-change="handleSelectionChange"
      @pagination="getList"
    >
      <el-table-column label="车辆编号" align="center" prop="vehicleId" width="110" />
      <el-table-column label="客户" align="center" min-width="220">
        <template slot-scope="scope">
          {{ customerLabel(scope.row.customerId) }}
        </template>
      </el-table-column>
      <el-table-column label="车牌号" align="center" prop="plateNo" width="130" />
      <el-table-column label="车辆类型" align="center" width="110">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_vehicle_type" :value="scope.row.vehicleType" />
        </template>
      </el-table-column>
      <el-table-column label="品牌" align="center" prop="brandName" width="120" show-overflow-tooltip />
      <el-table-column label="颜色" align="center" prop="vehicleColor" width="90" />
      <el-table-column label="是否默认" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_vehicle_default" :value="scope.row.isDefault" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_vehicle_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="绑定时间" align="center" prop="bindTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.bindTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['parking:vehicle:edit']"
          >
            修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-s-flag"
            :disabled="scope.row.isDefault === '1'"
            @click="handleSetDefault(scope.row)"
            v-hasPermi="['parking:vehicle:setDefault']"
          >
            设为默认
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['parking:vehicle:remove']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </data-table>

    <form-dialog
      :open.sync="open"
      :title="title"
      :model="form"
      :rules="rules"
      :submitting="submitting"
      @submit="submitForm"
      @cancel="reset"
    >
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width: 100%">
          <el-option
            v-for="item in customerOptions"
            :key="item.customerId"
            :label="formatCustomerOption(item)"
            :value="item.customerId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="车牌号" prop="plateNo">
        <el-input v-model="form.plateNo" placeholder="请输入车牌号" maxlength="20" show-word-limit />
      </el-form-item>
      <el-form-item label="车辆类型" prop="vehicleType">
        <el-select v-model="form.vehicleType" placeholder="请选择车辆类型" style="width: 100%">
          <el-option
            v-for="item in dict.type.parking_vehicle_type"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="品牌" prop="brandName">
        <el-input v-model="form.brandName" placeholder="请输入品牌名称" maxlength="50" />
      </el-form-item>
      <el-form-item label="颜色" prop="vehicleColor">
        <el-input v-model="form.vehicleColor" placeholder="请输入车辆颜色" maxlength="20" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio v-for="item in dict.type.parking_vehicle_status" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="设为默认" prop="isDefault">
        <el-radio-group v-model="form.isDefault">
          <el-radio v-for="item in dict.type.parking_vehicle_default" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </form-dialog>
  </div>
</template>

<script>
import { listCustomerOptions } from '@/api/parking/customer'
import { addVehicle, delVehicle, getVehicle, listVehicles, setDefaultVehicle, updateVehicle } from '@/api/parking/vehicle'
import { findCustomerLabel, formatCustomerOption } from '../options'
import { SearchForm, DataTable, FormDialog } from '../components'

export default {
  name: 'ParkingUserVehicle',
  components: { SearchForm, DataTable, FormDialog },
  dicts: ['parking_vehicle_type', 'parking_vehicle_status', 'parking_vehicle_default'],
  data() {
    return {
      loading: true,
      submitting: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      vehicleList: [],
      customerOptions: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        customerId: undefined,
        plateNo: undefined,
        vehicleType: undefined,
        status: undefined,
        isDefault: undefined
      },
      form: {},
      rules: {
        customerId: [
          { required: true, message: '客户不能为空', trigger: 'change' }
        ],
        plateNo: [
          { required: true, message: '车牌号不能为空', trigger: 'blur' },
          { max: 20, message: '车牌号不能超过 20 个字符', trigger: 'blur' }
        ],
        vehicleType: [
          { required: true, message: '车辆类型不能为空', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getCustomerOptions()
    this.getList()
  },
  methods: {
    getCustomerOptions() {
      listCustomerOptions().then(response => {
        this.customerOptions = response.data || []
      })
    },
    getList() {
      this.loading = true
      listVehicles(this.queryParams).then(response => {
        this.vehicleList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    reset() {
      this.form = {
        vehicleId: undefined,
        customerId: undefined,
        plateNo: undefined,
        vehicleType: '1',
        brandName: undefined,
        vehicleColor: undefined,
        isDefault: '0',
        status: '0',
        remark: undefined
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增车辆'
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.vehicleId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      this.reset()
      const vehicleId = row.vehicleId || this.ids[0]
      getVehicle(vehicleId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改车辆'
      })
    },
    submitForm() {
      this.submitting = true
      const request = this.form.vehicleId ? updateVehicle(this.form) : addVehicle(this.form)
      request.then(() => {
        this.$modal.msgSuccess(this.form.vehicleId ? '修改成功' : '新增成功')
        this.open = false
        this.getList()
      }).finally(() => {
        this.submitting = false
      })
    },
    handleDelete(row) {
      const vehicleIds = row.vehicleId || this.ids
      this.$modal.confirm('是否确认删除车辆编号为 "' + vehicleIds + '" 的数据项？').then(() => {
        return delVehicle(vehicleIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleSetDefault(row) {
      this.$modal.confirm('是否将车牌 "' + row.plateNo + '" 设为默认车辆？').then(() => {
        return setDefaultVehicle(row.vehicleId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('设置成功')
      }).catch(() => {})
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
