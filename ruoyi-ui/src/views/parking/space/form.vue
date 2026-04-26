<template>
  <form-page
    :title="isEdit ? '修改车位' : '新增车位'"
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
import { addParkingSpace, getParkingSpace, updateParkingSpace } from '@/api/parking/space'
import { FormPage, SchemaForm } from '../components'

export default {
  name: 'ParkingSpaceForm',
  components: { FormPage, SchemaForm },
  dicts: ['parking_space_status', 'parking_space_type'],
  data() {
    return {
      loading: false,
      submitting: false,
      lotOptions: [],
      form: {
        spaceId: undefined,
        lotId: undefined,
        spaceCode: undefined,
        areaName: undefined,
        floorNo: undefined,
        spaceType: '1',
        status: '0',
        remark: undefined
      },
      rules: {
        lotId: [{ required: true, message: '所属停车场不能为空', trigger: 'change' }],
        spaceCode: [
          { required: true, message: '车位编码不能为空', trigger: 'blur' },
          { max: 32, message: '车位编码不能超过 32 个字符', trigger: 'blur' }
        ],
        spaceType: [{ required: true, message: '车位类型不能为空', trigger: 'change' }],
        status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
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
          title: '车位信息',
          fields: [
            {
              label: '所属停车场',
              prop: 'lotId',
              type: 'select',
              attrs: { placeholder: '请选择停车场' },
              options: this.lotOptions.map(item => ({ label: item.lotName, value: item.lotId }))
            },
            {
              label: '车位编码',
              prop: 'spaceCode',
              attrs: { placeholder: '请输入车位编码', maxlength: 32, 'show-word-limit': true }
            },
            {
              label: '区域',
              prop: 'areaName',
              attrs: { placeholder: '请输入区域', maxlength: 64 }
            },
            {
              label: '楼层',
              prop: 'floorNo',
              attrs: { placeholder: '请输入楼层', maxlength: 20 }
            },
            {
              label: '车位类型',
              prop: 'spaceType',
              type: 'select',
              attrs: { placeholder: '请选择车位类型' },
              options: this.dict.type.parking_space_type
            },
            {
              label: '状态',
              prop: 'status',
              type: 'radio',
              options: this.dict.type.parking_space_status
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
    loadData() {
      this.loading = true
      getParkingSpace(this.$route.query.id).then(response => {
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
      const request = this.isEdit ? updateParkingSpace(this.form) : addParkingSpace(this.form)
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
