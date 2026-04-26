<template>
  <form-page
    :title="isEdit ? '修改客户档案' : '新增客户档案'"
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
import { addCustomer, getCustomer, updateCustomer } from '@/api/parking/customer'
import { FormPage, SchemaForm } from '../components'

export default {
  name: 'ParkingCustomerForm',
  components: { FormPage, SchemaForm },
  dicts: ['parking_customer_type', 'parking_customer_status'],
  data() {
    return {
      loading: false,
      submitting: false,
      form: {
        customerId: undefined,
        customerCode: undefined,
        customerName: undefined,
        mobile: undefined,
        customerType: '1',
        status: '0',
        remark: undefined
      },
      rules: {
        customerCode: [{ required: true, message: '客户编号不能为空', trigger: 'blur' }],
        customerName: [{ required: true, message: '客户名称不能为空', trigger: 'blur' }],
        mobile: [{ required: true, message: '手机号不能为空', trigger: 'blur' }],
        customerType: [{ required: true, message: '客户类型不能为空', trigger: 'change' }]
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
          title: '客户信息',
          fields: [
            {
              label: '客户编号',
              prop: 'customerCode',
              attrs: { placeholder: '请输入客户编号', maxlength: 32, 'show-word-limit': true }
            },
            {
              label: '客户名称',
              prop: 'customerName',
              attrs: { placeholder: '请输入客户名称', maxlength: 50, 'show-word-limit': true }
            },
            {
              label: '手机号',
              prop: 'mobile',
              attrs: { placeholder: '请输入手机号', maxlength: 20 }
            },
            {
              label: '客户类型',
              prop: 'customerType',
              type: 'select',
              attrs: { placeholder: '请选择客户类型' },
              options: this.dict.type.parking_customer_type
            },
            {
              label: '状态',
              prop: 'status',
              type: 'radio',
              options: this.dict.type.parking_customer_status
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
    if (this.isEdit) {
      this.loadData()
    }
  },
  methods: {
    loadData() {
      this.loading = true
      getCustomer(this.$route.query.id).then(response => {
        this.form = Object.assign({}, this.form, response.data)
      }).finally(() => {
        this.loading = false
      })
    },
    goBack() {
      this.$router.go(-1)
    },
    submitForm() {
      this.submitting = true
      const request = this.isEdit ? updateCustomer(this.form) : addCustomer(this.form)
      request.then(() => {
        this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
        this.goBack()
      }).finally(() => {
        this.submitting = false
      })
    }
  }
}
</script>
