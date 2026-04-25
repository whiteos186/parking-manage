<template>
  <form-page
    :title="isEdit ? '修改月卡订单' : '新增月卡订单'"
    :model="form"
    :rules="rules"
    :loading="loading"
    :submitting="submitting"
    @submit="submitForm"
    @back="goBack"
  >
    <el-form-item label="所属停车场" prop="lotId">
      <el-select v-model="form.lotId" placeholder="请选择停车场" style="width: 360px">
        <el-option
          v-for="item in lotOptions"
          :key="item.lotId"
          :label="item.lotName"
          :value="item.lotId"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="客户" prop="customerId">
      <el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width: 360px" @change="handleCustomerChange">
        <el-option
          v-for="item in customerOptions"
          :key="item.customerId"
          :label="formatCustomerOption(item)"
          :value="item.customerId"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="车辆" prop="vehicleId">
      <el-select
        v-model="form.vehicleId"
        filterable
        clearable
        :disabled="!form.customerId"
        :placeholder="form.customerId ? '请选择车辆' : '请先选择客户'"
        style="width: 360px"
      >
        <el-option
          v-for="item in vehicleOptions"
          :key="item.vehicleId"
          :label="formatVehicleOption(item)"
          :value="item.vehicleId"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="购买月数" prop="monthCount">
      <el-input-number
        v-model="form.monthCount"
        :min="1"
        :precision="0"
        controls-position="right"
        style="width: 200px"
      />
      <span class="form-tip">个月</span>
    </el-form-item>
    <el-form-item label="开始时间" prop="startTime">
      <el-date-picker
        v-model="form.startTime"
        type="datetime"
        placeholder="默认为当前时间"
        value-format="yyyy-MM-dd HH:mm:ss"
        style="width: 360px"
      />
    </el-form-item>
    <el-form-item label="原始金额" prop="originalAmount">
      <el-input-number
        v-model="form.originalAmount"
        :min="0"
        :precision="2"
        :step="100"
        controls-position="right"
        style="width: 200px"
      />
      <span class="form-tip">元</span>
    </el-form-item>
    <el-form-item label="折扣金额" prop="discountAmount">
      <el-input-number
        v-model="form.discountAmount"
        :min="0"
        :precision="2"
        :step="10"
        controls-position="right"
        style="width: 200px"
      />
      <span class="form-tip">元</span>
    </el-form-item>
    <el-form-item label="备注" prop="remark">
      <el-input
        v-model="form.remark"
        type="textarea"
        :rows="3"
        placeholder="请输入备注"
        maxlength="500"
        show-word-limit
        style="width: 360px"
      />
    </el-form-item>
  </form-page>
</template>

<script>
import { listParkingLotOptions } from '@/api/parking/lot'
import { listCustomerOptions } from '@/api/parking/customer'
import { listVehicleOptions } from '@/api/parking/vehicle'
import { addMonthlyOrder, getMonthlyOrder, updateMonthlyOrder } from '@/api/parking/monthlyOrder'
import { formatCustomerOption, formatVehicleOption } from '../options'
import { FormPage } from '../components'

export default {
  name: 'MonthlyOrderForm',
  components: { FormPage },
  data() {
    return {
      loading: false,
      submitting: false,
      lotOptions: [],
      customerOptions: [],
      vehicleOptions: [],
      form: {
        monthlyOrderId: undefined,
        orderNo: undefined,
        customerId: undefined,
        vehicleId: undefined,
        lotId: undefined,
        monthCount: 1,
        startTime: undefined,
        originalAmount: 0,
        discountAmount: 0,
        remark: undefined
      },
      rules: {
        lotId: [{ required: true, message: '所属停车场不能为空', trigger: 'change' }],
        customerId: [{ required: true, message: '客户不能为空', trigger: 'change' }],
        vehicleId: [{ required: true, message: '车辆不能为空', trigger: 'change' }],
        monthCount: [{ required: true, message: '购买月数不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    isEdit() {
      return !!this.$route.query.id
    }
  },
  created() {
    this.getLotOptions()
    this.getCustomerOptions()
    if (this.isEdit) {
      this.loadData()
    }
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
    getVehicleOptions(customerId, preserveSelection = false) {
      if (!customerId) {
        this.vehicleOptions = []
        this.form.vehicleId = undefined
        return Promise.resolve()
      }
      return listVehicleOptions({ customerId }).then(response => {
        const options = response.data || []
        this.vehicleOptions = options
        const matched = options.some(item => String(item.vehicleId) === String(this.form.vehicleId))
        if (!preserveSelection || !matched) {
          this.form.vehicleId = undefined
        }
      })
    },
    loadData() {
      this.loading = true
      getMonthlyOrder(this.$route.query.id).then(response => {
        this.form = response.data
        return this.getVehicleOptions(this.form.customerId, true)
      }).finally(() => {
        this.loading = false
      })
    },
    handleCustomerChange(customerId) {
      this.getVehicleOptions(customerId, false)
    },
    goBack() {
      this.$router.go(-1)
    },
    submitForm() {
      this.submitting = true
      const req = this.isEdit ? updateMonthlyOrder(this.form) : addMonthlyOrder(this.form)
      req.then(() => {
        this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
        this.goBack()
      }).finally(() => {
        this.submitting = false
      })
    },
    formatCustomerOption(item) {
      return formatCustomerOption(item)
    },
    formatVehicleOption(item) {
      return formatVehicleOption(item)
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
