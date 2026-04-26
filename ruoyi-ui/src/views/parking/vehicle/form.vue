<template>
  <form-page
    :title="isEdit ? '修改车辆' : '新增车辆'"
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
import { listCustomerOptions } from '@/api/parking/customer'
import { addVehicle, getVehicle, updateVehicle } from '@/api/parking/vehicle'
import { formatCustomerOption } from '../options'
import { FormPage, SchemaForm } from '../components'

export default {
  name: 'ParkingVehicleForm',
  components: { FormPage, SchemaForm },
  dicts: ['parking_vehicle_type', 'parking_vehicle_status', 'parking_vehicle_default'],
  data() {
    return {
      loading: false,
      submitting: false,
      customerOptions: [],
      form: {
        vehicleId: undefined,
        customerId: undefined,
        plateNo: undefined,
        vehicleType: '1',
        brandName: undefined,
        vehicleColor: undefined,
        isDefault: '0',
        status: '0',
        remark: undefined
      },
      rules: {
        customerId: [{ required: true, message: '客户不能为空', trigger: 'change' }],
        plateNo: [
          { required: true, message: '车牌号不能为空', trigger: 'blur' },
          { max: 20, message: '车牌号不能超过 20 个字符', trigger: 'blur' }
        ],
        vehicleType: [{ required: true, message: '车辆类型不能为空', trigger: 'change' }]
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
          title: '车辆信息',
          fields: [
            {
              label: '客户',
              prop: 'customerId',
              type: 'select',
              attrs: { filterable: true, placeholder: '请选择客户' },
              options: this.customerOptions.map(item => ({
                label: item.optionLabel,
                value: item.customerId
              }))
            },
            {
              label: '车牌号',
              prop: 'plateNo',
              attrs: { placeholder: '请输入车牌号', maxlength: 20, 'show-word-limit': true }
            },
            {
              label: '车辆类型',
              prop: 'vehicleType',
              type: 'select',
              attrs: { placeholder: '请选择车辆类型' },
              options: this.dict.type.parking_vehicle_type
            },
            {
              label: '品牌',
              prop: 'brandName',
              attrs: { placeholder: '请输入品牌名称', maxlength: 50 }
            },
            {
              label: '颜色',
              prop: 'vehicleColor',
              attrs: { placeholder: '请输入车辆颜色', maxlength: 20 }
            },
            {
              label: '状态',
              prop: 'status',
              type: 'radio',
              options: this.dict.type.parking_vehicle_status
            },
            {
              label: '设为默认',
              prop: 'isDefault',
              type: 'radio',
              options: this.dict.type.parking_vehicle_default
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
    this.getCustomerOptions()
    if (this.isEdit) {
      this.loadData()
    }
  },
  methods: {
    getCustomerOptions() {
      listCustomerOptions().then(response => {
        this.customerOptions = (response.data || []).map(item => ({ ...item, optionLabel: formatCustomerOption(item) }))
      })
    },
    loadData() {
      this.loading = true
      getVehicle(this.$route.query.id).then(response => {
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
      const request = this.isEdit ? updateVehicle(this.form) : addVehicle(this.form)
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
