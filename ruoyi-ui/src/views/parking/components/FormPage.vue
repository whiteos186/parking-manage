<template>
  <div class="app-container">
    <el-page-header @back="$emit('back')" :content="title" style="margin-bottom: 20px" />
    <el-card>
      <el-form ref="innerForm" :model="model" :rules="rules" :label-width="labelWidth" v-loading="loading">
        <slot />
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
          <el-button @click="$emit('back')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'FormPage',
  props: {
    title: { type: String, required: true },
    model: { type: Object, required: true },
    rules: Object,
    loading: Boolean,
    submitting: Boolean,
    labelWidth: { type: String, default: '100px' }
  },
  methods: {
    handleSubmit() {
      this.$refs.innerForm.validate(valid => {
        if (valid) this.$emit('submit')
      })
    },
    resetFields() {
      this.$refs.innerForm && this.$refs.innerForm.resetFields()
    }
  }
}
</script>
