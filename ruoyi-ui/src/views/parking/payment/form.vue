<template>
  <form-page
    :title="isEdit ? '修改支付流水' : '新增支付流水'"
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
import { addPayment, getPayment, updatePayment } from '@/api/parking/payment'
import { listCustomerOptions } from '@/api/parking/customer'
import { listParkingLotOptions } from '@/api/parking/lot'
import { formatCustomerOption } from '../options'
import { FormPage, SchemaForm } from '../components'

export default {
  name: 'PaymentForm',
  components: { FormPage, SchemaForm },
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
    },
    schema() {
      return [
        {
          title: '业务信息',
          fields: [
            {
              label: '业务订单号',
              prop: 'bizOrderNo',
              attrs: { placeholder: '请输入业务订单号', maxlength: 32, 'show-word-limit': true }
            },
            {
              label: '订单类型',
              prop: 'bizOrderType',
              type: 'select',
              attrs: { placeholder: '请选择订单类型' },
              options: this.dict.type.parking_payment_biz_type
            },
            {
              label: '停车场',
              prop: 'lotId',
              type: 'select',
              attrs: { clearable: true, placeholder: '请选择停车场' },
              options: this.lotOptions.map(item => ({ label: item.lotName, value: item.lotId }))
            },
            {
              label: '客户',
              prop: 'customerId',
              type: 'select',
              attrs: { filterable: true, clearable: true, placeholder: '请选择客户' },
              options: this.customerOptions.map(item => ({
                label: item.optionLabel,
                value: item.customerId
              }))
            }
          ]
        },
        {
          title: '支付信息',
          fields: [
            {
              label: '支付渠道',
              prop: 'payChannel',
              type: 'select',
              attrs: { placeholder: '请选择支付渠道' },
              options: this.dict.type.parking_payment_channel
            },
            {
              label: '支付金额',
              prop: 'payAmount',
              type: 'number',
              unit: '元',
              attrs: { min: 0, precision: 2, step: 1, 'controls-position': 'right' }
            },
            {
              label: '支付状态',
              prop: 'payStatus',
              type: 'select',
              attrs: { placeholder: '请选择支付状态' },
              options: this.dict.type.parking_payment_status
            },
            {
              label: '交易号',
              prop: 'tradeNo',
              attrs: { placeholder: '留空则自动生成', maxlength: 64, 'show-word-limit': true }
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
    }
  }
}
</script>
