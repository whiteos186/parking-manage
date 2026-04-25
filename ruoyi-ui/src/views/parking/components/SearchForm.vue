<template>
  <el-form
    ref="innerForm"
    :model="model"
    size="small"
    :inline="true"
    v-show="visible"
  >
    <slot />
    <el-form-item>
      <el-button type="primary" icon="el-icon-search" size="mini" @click="$emit('search')">搜索</el-button>
      <el-button icon="el-icon-refresh" size="mini" @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  name: 'SearchForm',
  props: {
    model: { type: Object, required: true },
    visible: { type: Boolean, default: true }
  },
  methods: {
    handleReset() {
      this.$refs.innerForm.resetFields()
      this.$emit('reset')
    },
    resetFields() {
      this.$refs.innerForm.resetFields()
    }
  }
}
</script>
