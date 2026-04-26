<template>
  <form-page
    :title="isEdit ? '修改月卡订单' : '新增月卡订单'"
    :model="form"
    :rules="rules"
    :loading="loading"
    :submitting="submitting"
    label-width="96px"
    @submit="submitForm"
    @back="goBack"
  >
    <schema-form :model="form" :schema="schema" />
  </form-page>
</template>

<script>
import { listParkingLotOptions } from '@/api/parking/lot'
import { listCustomerOptions } from '@/api/parking/customer'
import { listVehicleOptions } from '@/api/parking/vehicle'
import { addMonthlyOrder, getMonthlyOrder, updateMonthlyOrder } from '@/api/parking/monthlyOrder'
import { formatCustomerOption, formatVehicleOption } from '../options'
import { FormPage, SchemaForm } from '../components'

export default {
  name: 'MonthlyOrderForm',
  components: { FormPage, SchemaForm },
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
    },
    schema() {
      return [
        {
          title: '订单信息',
          fields: [
            {
              label: '所属停车场',
              prop: 'lotId',
              type: 'select',
              attrs: { placeholder: '请选择停车场' },
              options: this.lotOptions.map(item => ({ label: item.lotName, value: item.lotId }))
            },
            {
              label: '客户',
              prop: 'customerId',
              type: 'select',
              attrs: { filterable: true, placeholder: '请选择客户' },
              on: { change: this.handleCustomerChange },
              options: this.customerOptions.map(item => ({
                label: item.optionLabel,
                value: item.customerId
              }))
            },
            {
              label: '车辆',
              prop: 'vehicleId',
              type: 'select',
              attrs: {
                filterable: true,
                clearable: true,
                disabled: !this.form.customerId,
                placeholder: this.form.customerId ? '请选择车辆' : '请先选择客户'
              },
              options: this.vehicleOptions.map(item => ({
                label: formatVehicleOption(item),
                value: item.vehicleId
              }))
            }
          ]
        },
        {
          title: '有效期与金额',
          fields: [
            {
              label: '购买月数',
              prop: 'monthCount',
              type: 'number',
              unit: '个月',
              attrs: { min: 1, precision: 0, 'controls-position': 'right' }
            },
            {
              label: '开始时间',
              prop: 'startTime',
              type: 'datetime',
              attrs: { placeholder: '默认为当前时间', 'value-format': 'yyyy-MM-dd HH:mm:ss' }
            },
            {
              label: '原始金额',
              prop: 'originalAmount',
              type: 'number',
              unit: '元',
              attrs: { min: 0, precision: 2, step: 100, 'controls-position': 'right' }
            },
            {
              label: '折扣金额',
              prop: 'discountAmount',
              type: 'number',
              unit: '元',
              attrs: { min: 0, precision: 2, step: 10, 'controls-position': 'right' }
            }
          ]
        },
        {
          fields: [
            {
              label: '备注',
              prop: 'remark',
              type: 'textarea',
              wide: true,
              attrs: { rows: 4, placeholder: '请输入备注', maxlength: 500, 'show-word-limit': true }
            }
          ]
        }
      ]
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
        this.customerOptions = (response.data || []).map(item => ({ ...item, optionLabel: formatCustomerOption(item) }))
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
    formatVehicleOption(item) {
      return formatVehicleOption(item)
    }
  }
}
</script>
