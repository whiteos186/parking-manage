<template>
  <form-page
    :title="isEdit ? '修改会员订单' : '新增会员订单'"
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
    <el-form-item label="会员类型" prop="membershipType">
      <el-select v-model="form.membershipType" placeholder="请选择会员类型" style="width: 360px">
        <el-option
          v-for="item in dict.type.parking_membership_type"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="有效期开始" prop="validStartTime">
      <el-date-picker
        v-model="form.validStartTime"
        type="datetime"
        placeholder="请选择有效期开始时间"
        value-format="yyyy-MM-dd HH:mm:ss"
        style="width: 360px"
      />
    </el-form-item>
    <el-form-item label="有效期结束" prop="validEndTime">
      <el-date-picker
        v-model="form.validEndTime"
        type="datetime"
        placeholder="请选择有效期结束时间"
        value-format="yyyy-MM-dd HH:mm:ss"
        style="width: 360px"
      />
    </el-form-item>
    <el-form-item label="原价" prop="originalAmount">
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
    <el-form-item label="应付金额" prop="payAmount">
      <el-input-number
        v-model="form.payAmount"
        :min="0"
        :precision="2"
        :step="100"
        controls-position="right"
        style="width: 200px"
      />
      <span class="form-tip">元（留空则自动计算原价 - 折扣）</span>
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
import { addMembershipOrder, getMembershipOrder, updateMembershipOrder } from '@/api/parking/membershipOrder'
import { formatCustomerOption, formatVehicleOption } from '../options'
import { FormPage } from '../components'

export default {
  name: 'MembershipOrderForm',
  components: { FormPage },
  dicts: ['parking_membership_type'],
  data() {
    return {
      loading: false,
      submitting: false,
      lotOptions: [],
      customerOptions: [],
      vehicleOptions: [],
      form: {
        membershipOrderId: undefined,
        orderNo: undefined,
        customerId: undefined,
        vehicleId: undefined,
        lotId: undefined,
        membershipType: '1',
        validStartTime: undefined,
        validEndTime: undefined,
        originalAmount: 0,
        discountAmount: 0,
        payAmount: undefined,
        remark: undefined
      },
      rules: {
        lotId: [{ required: true, message: '所属停车场不能为空', trigger: 'change' }],
        customerId: [{ required: true, message: '客户不能为空', trigger: 'change' }],
        vehicleId: [{ required: true, message: '车辆不能为空', trigger: 'change' }],
        membershipType: [{ required: true, message: '会员类型不能为空', trigger: 'change' }]
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
      getMembershipOrder(this.$route.query.id).then(response => {
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
      const req = this.isEdit ? updateMembershipOrder(this.form) : addMembershipOrder(this.form)
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
