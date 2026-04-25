<template>
  <form-page
    :title="isEdit ? '修改支付流水' : '新增支付流水'"
    :model="form"
    :rules="rules"
    :loading="loading"
    :submitting="submitting"
    @submit="submitForm"
    @back="goBack"
  >
    <el-form-item label="业务订单号" prop="bizOrderNo">
      <el-input
        v-model="form.bizOrderNo"
        placeholder="请输入业务订单号"
        maxlength="32"
        show-word-limit
        style="width: 360px"
      />
    </el-form-item>
    <el-form-item label="订单类型" prop="bizOrderType">
      <el-select v-model="form.bizOrderType" placeholder="请选择订单类型" style="width: 360px">
        <el-option
          v-for="item in dict.type.parking_payment_biz_type"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="停车场" prop="lotId">
      <el-select v-model="form.lotId" clearable placeholder="请选择停车场" style="width: 360px">
        <el-option
          v-for="item in lotOptions"
          :key="item.lotId"
          :label="item.lotName"
          :value="item.lotId"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="客户" prop="customerId">
      <el-select v-model="form.customerId" filterable clearable placeholder="请选择客户" style="width: 360px">
        <el-option
          v-for="item in customerOptions"
          :key="item.customerId"
          :label="formatCustomerOption(item)"
          :value="item.customerId"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="支付渠道" prop="payChannel">
      <el-select v-model="form.payChannel" placeholder="请选择支付渠道" style="width: 360px">
        <el-option
          v-for="item in dict.type.parking_payment_channel"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="支付金额" prop="payAmount">
      <el-input-number
        v-model="form.payAmount"
        :min="0"
        :precision="2"
        :step="1"
        controls-position="right"
        style="width: 200px"
      />
      <span class="form-tip">元</span>
    </el-form-item>
    <el-form-item label="支付状态" prop="payStatus">
      <el-select v-model="form.payStatus" placeholder="请选择支付状态" style="width: 360px">
        <el-option
          v-for="item in dict.type.parking_payment_status"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="交易号" prop="tradeNo">
      <el-input v-model="form.tradeNo" placeholder="留空则自动生成" maxlength="64" show-word-limit style="width: 360px" />
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
import { addPayment, getPayment, updatePayment } from '@/api/parking/payment'
import { listCustomerOptions } from '@/api/parking/customer'
import { listParkingLotOptions } from '@/api/parking/lot'
import { formatCustomerOption } from '../options'
import { FormPage } from '../components'

export default {
  name: 'PaymentForm',
  components: { FormPage },
  dicts: ['parking_payment_biz_type', 'parking_payment_channel', 'parking_payment_status'],
  data() {
    return {
      loading: false,
      submitting: false,
      lotOptions: [],
      customerOptions: [],
      form: {
        paymentId: undefined,
        bizOrderNo: undefined,
        bizOrderType: undefined,
        customerId: undefined,
        lotId: undefined,
        payChannel: '1',
        payAmount: 0,
        payStatus: '0',
        tradeNo: undefined,
        remark: undefined
      },
      rules: {
        bizOrderNo: [
          { required: true, message: '业务订单号不能为空', trigger: 'blur' },
          { max: 32, message: '业务订单号不能超过 32 个字符', trigger: 'blur' }
        ],
        bizOrderType: [{ required: true, message: '订单类型不能为空', trigger: 'change' }],
        payAmount: [{ required: true, message: '支付金额不能为空', trigger: 'blur' }],
        customerId: [{ required: true, message: '客户不能为空', trigger: 'change' }]
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
    loadData() {
      this.loading = true
      getPayment(this.$route.query.id).then(response => {
        this.form = response.data
      }).finally(() => {
        this.loading = false
      })
    },
    goBack() {
      this.$router.go(-1)
    },
    submitForm() {
      this.submitting = true
      const req = this.isEdit ? updatePayment(this.form) : addPayment(this.form)
      req.then(() => {
        this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
        this.goBack()
      }).finally(() => {
        this.submitting = false
      })
    },
    formatCustomerOption(item) {
      return formatCustomerOption(item)
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
