<template>
  <form-page
    :title="isEdit ? '修改停车场' : '新增停车场'"
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
import { addParkingLot, getParkingLot, updateParkingLot } from '@/api/parking/lot'
import { FormPage, SchemaForm } from '../components'

export default {
  name: 'ParkingLotForm',
  components: { FormPage, SchemaForm },
  dicts: ['parking_lot_status'],
  data() {
    return {
      loading: false,
      submitting: false,
      form: {
        lotId: undefined,
        lotName: undefined,
        lotAddress: undefined,
        totalSpaceCount: 100,
        monthlyPrice: undefined,
        tempHourPrice: undefined,
        status: '0',
        remark: undefined
      },
      rules: {
        lotName: [
          { required: true, message: '停车场名称不能为空', trigger: 'blur' },
          { max: 100, message: '停车场名称不能超过 100 个字符', trigger: 'blur' }
        ],
        lotAddress: [
          { required: true, message: '停车场地址不能为空', trigger: 'blur' },
          { max: 255, message: '停车场地址不能超过 255 个字符', trigger: 'blur' }
        ],
        totalSpaceCount: [
          { required: true, message: '总车位数不能为空', trigger: 'blur' },
          { type: 'number', min: 1, message: '总车位数必须大于 0', trigger: 'blur' }
        ]
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
          title: '停车场信息',
          fields: [
            {
              label: '停车场名称',
              prop: 'lotName',
              attrs: { placeholder: '请输入停车场名称', maxlength: 100, 'show-word-limit': true }
            },
            {
              label: '总车位数',
              prop: 'totalSpaceCount',
              type: 'number',
              attrs: { min: 1, max: 99999, precision: 0, step: 10, 'controls-position': 'right' }
            },
            {
              label: '状态',
              prop: 'status',
              type: 'radio',
              options: this.dict.type.parking_lot_status
            },
            {
              label: '停车场地址',
              prop: 'lotAddress',
              type: 'textarea',
              wide: true,
              attrs: { rows: 3, placeholder: '请输入停车场地址', maxlength: 255, 'show-word-limit': true }
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
      getParkingLot(this.$route.query.id).then(response => {
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
      const request = this.isEdit ? updateParkingLot(this.form) : addParkingLot(this.form)
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
