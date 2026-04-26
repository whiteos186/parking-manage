<template>
  <el-dialog :title="title" :visible.sync="innerOpen" :width="width" @close="handleClose">
    <el-form ref="innerForm" class="parking-dialog-form" :model="model" :rules="rules" :label-width="labelWidth">
      <slot />
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'FormDialog',
  props: {
    open: Boolean,
    title: String,
    width: { type: String, default: '680px' },
    model: { type: Object, required: true },
    rules: Object,
    submitting: Boolean,
    labelWidth: { type: String, default: '100px' }
  },
  computed: {
    innerOpen: {
      get() { return this.open },
      set(v) { this.$emit('update:open', v) }
    }
  },
  methods: {
    handleSubmit() {
      this.$refs.innerForm.validate(valid => {
        if (valid) this.$emit('submit')
      })
    },
    handleCancel() {
      this.innerOpen = false
    },
    handleClose() {
      this.$emit('cancel')
    },
    resetFields() {
      this.$refs.innerForm && this.$refs.innerForm.resetFields()
    }
  }
}
</script>

<style scoped>
.parking-dialog-form ::v-deep .el-input,
.parking-dialog-form ::v-deep .el-select,
.parking-dialog-form ::v-deep .el-date-editor.el-input,
.parking-dialog-form ::v-deep .el-date-editor.el-input__inner,
.parking-dialog-form ::v-deep .el-textarea {
  width: 100%;
}

.parking-dialog-form ::v-deep .el-input-number {
  width: 100%;
}

.parking-dialog-form ::v-deep .el-input-number .el-input__inner {
  text-align: left;
}
</style>
