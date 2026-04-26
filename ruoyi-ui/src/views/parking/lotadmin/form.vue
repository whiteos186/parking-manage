<template>
  <form-page
    :title="isEdit ? '修改管理员绑定' : '新增管理员绑定'"
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
import { addLotAdmin, getLotAdmin, listLotAdminUserOptions, updateLotAdmin } from '@/api/parking/lotAdmin'
import { FormPage, SchemaForm } from '../components'

export default {
  name: 'ParkingLotAdminForm',
  components: { FormPage, SchemaForm },
  dicts: ['parking_lot_admin_status'],
  data() {
    return {
      loading: false,
      submitting: false,
      lotOptions: [],
      userOptions: [],
      form: {
        lotAdminId: undefined,
        lotId: undefined,
        userId: undefined,
        status: '0',
        remark: undefined
      },
      rules: {
        lotId: [{ required: true, message: '所属停车场不能为空', trigger: 'change' }],
        userId: [{ required: true, message: '用户不能为空', trigger: 'change' }],
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
          title: '绑定信息',
          fields: [
            {
              label: '所属停车场',
              prop: 'lotId',
              type: 'select',
              attrs: { placeholder: '请选择停车场' },
              options: this.lotOptions.map(item => ({ label: item.lotName, value: item.lotId }))
            },
            {
              label: '用户',
              prop: 'userId',
              type: 'select',
              attrs: { filterable: true, placeholder: '请选择用户' },
              options: this.userOptions.map(item => ({
                label: this.userOptionLabel(item),
                value: item.userId
              }))
            },
            {
              label: '状态',
              prop: 'status',
              type: 'radio',
              options: this.dict.type.parking_lot_admin_status
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
    this.getUserOptions()
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
    getUserOptions() {
      listLotAdminUserOptions().then(response => {
        this.userOptions = response.data || []
      }).catch(() => {
        this.userOptions = []
      })
    },
    userOptionLabel(item) {
      if (!item) {
        return ''
      }
      const nickName = item.nickName || item.userName || ''
      return `${nickName}（${item.userName} #${item.userId}）`
    },
    loadData() {
      this.loading = true
      getLotAdmin(this.$route.query.id).then(response => {
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
      const request = this.isEdit ? updateLotAdmin(this.form) : addLotAdmin(this.form)
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
