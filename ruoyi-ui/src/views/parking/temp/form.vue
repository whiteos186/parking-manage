<template>
  <form-page
    :title="isEdit ? '修改临停订单' : '登记入场'"
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
      <el-select v-model="form.customerId" filterable clearable placeholder="请选择客户" style="width: 360px">
        <el-option
          v-for="item in customerOptions"
          :key="item.customerId"
          :label="formatCustomerOption(item)"
          :value="item.customerId"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="车牌号" prop="vehiclePlateNo">
      <el-input v-model="form.vehiclePlateNo" placeholder="请输入车牌号" maxlength="20" show-word-limit style="width: 360px" />
    </el-form-item>
    <el-form-item label="入场时间" prop="inTime">
      <el-date-picker
        v-model="form.inTime"
        type="datetime"
        placeholder="请选择入场时间"
        value-format="yyyy-MM-dd HH:mm:ss"
        style="width: 360px"
      />
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
import { addTempOrder, getTempOrder, updateTempOrder } from '@/api/parking/tempOrder'
import { formatCustomerOption } from '../options'
import { FormPage } from '../components'

export default {
  name: 'TempOrderForm',
  components: { FormPage },
  data() {
    return {
      loading: false,
      submitting: false,
      lotOptions: [],
      customerOptions: [],
      form: {
        tempOrderId: undefined,
        lotId: undefined,
        spaceId: undefined,
        customerId: undefined,
        vehiclePlateNo: undefined,
        inTime: undefined,
        remark: undefined
      },
      rules: {
        lotId: [{ required: true, message: '所属停车场不能为空', trigger: 'change' }],
        vehiclePlateNo: [
          { required: true, message: '车牌号不能为空', trigger: 'blur' },
          { max: 20, message: '车牌号不能超过 20 个字符', trigger: 'blur' }
        ]
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
      getTempOrder(this.$route.query.id).then(response => {
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
      const req = this.isEdit ? updateTempOrder(this.form) : addTempOrder(this.form)
      req.then(() => {
        this.$modal.msgSuccess(this.isEdit ? '修改成功' : '登记成功')
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
