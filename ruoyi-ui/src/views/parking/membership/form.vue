<template>
  <form-page
    :title="isEdit ? '修改会员订单' : '新增会员订单'"
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
import { addMembershipOrder, getMembershipOrder, updateMembershipOrder } from '@/api/parking/membershipOrder'
import { formatCustomerOption, formatVehicleOption } from '../options'
import { FormPage, SchemaForm } from '../components'

export default {
  name: 'MembershipOrderForm',
  components: { FormPage, SchemaForm },
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
            },
            {
              label: '会员类型',
              prop: 'membershipType',
              type: 'select',
              attrs: { placeholder: '请选择会员类型' },
              options: this.dict.type.parking_membership_type
            }
          ]
        },
        {
          title: '有效期与金额',
          fields: [
            {
              label: '有效期开始',
              prop: 'validStartTime',
              type: 'datetime',
              attrs: { placeholder: '请选择有效期开始时间', 'value-format': 'yyyy-MM-dd HH:mm:ss' }
            },
            {
              label: '有效期结束',
              prop: 'validEndTime',
              type: 'datetime',
              attrs: { placeholder: '请选择有效期结束时间', 'value-format': 'yyyy-MM-dd HH:mm:ss' }
            },
            {
              label: '原价',
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
            },
            {
              label: '应付金额',
              prop: 'payAmount',
              type: 'number',
              unit: '元',
              tip: '留空则自动计算原价 - 折扣',
              attrs: { min: 0, precision: 2, step: 100, 'controls-position': 'right' }
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
    formatVehicleOption(item) {
      return formatVehicleOption(item)
    }
  }
}
</script>
