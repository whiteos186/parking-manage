<template>
  <el-col :xs="colXs" :sm="colSm" :md="colMd" :lg="colLg" :xl="colXl">
    <el-form-item :label="label" :prop="prop">
      <div class="parking-form-field" :class="{ 'has-unit': unit }">
        <slot />
        <span v-if="unit" class="parking-form-field__unit">{{ unit }}</span>
      </div>
      <div v-if="tip || $slots.tip" class="parking-form-field__tip">
        <slot name="tip">{{ tip }}</slot>
      </div>
    </el-form-item>
  </el-col>
</template>

<script>
export default {
  name: 'FormField',
  props: {
    label: { type: String, required: true },
    prop: String,
    unit: String,
    tip: String,
    wide: Boolean,
    full: Boolean,
    xs: { type: Number, default: 24 },
    sm: { type: Number, default: 24 },
    md: { type: Number, default: 12 },
    lg: { type: Number, default: 6 },
    xl: { type: Number, default: 6 }
  },
  computed: {
    colXs() {
      return this.full ? 24 : this.xs
    },
    colSm() {
      return this.full ? 24 : this.sm
    },
    colMd() {
      if (this.full || this.wide) return 24
      return this.md
    },
    colLg() {
      if (this.full) return 24
      if (this.wide) return 24
      return this.lg
    },
    colXl() {
      if (this.full) return 24
      if (this.wide) return 24
      return this.xl
    }
  }
}
</script>

<style scoped>
.parking-form-field {
  width: 100%;
}

.parking-form-field.has-unit {
  display: flex;
  align-items: center;
}

.parking-form-field ::v-deep .el-input,
.parking-form-field ::v-deep .el-select,
.parking-form-field ::v-deep .el-date-editor.el-input,
.parking-form-field ::v-deep .el-date-editor.el-input__inner,
.parking-form-field ::v-deep .el-textarea {
  width: 100%;
}

.parking-form-field ::v-deep .el-input-number {
  width: 100%;
}

.parking-form-field.has-unit ::v-deep .el-input-number {
  flex: 1;
  min-width: 0;
}

.parking-form-field ::v-deep .el-input-number .el-input__inner {
  text-align: left;
}

.parking-form-field__unit {
  flex: 0 0 auto;
  margin-left: 8px;
  color: #606266;
}

.parking-form-field__tip {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  line-height: 18px;
}
</style>
