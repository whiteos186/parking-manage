<template>
  <section class="parking-form-section">
    <div v-if="title" class="parking-form-section__title">{{ title }}</div>
    <el-row :gutter="gutter">
      <slot />
    </el-row>
  </section>
</template>

<script>
export default {
  name: 'FormSection',
  props: {
    title: String,
    gutter: { type: Number, default: 24 }
  }
}
</script>

<style scoped>
.parking-form-section {
  width: 100%;
  padding-bottom: 4px;
}

.parking-form-section + .parking-form-section {
  margin-top: 10px;
  padding-top: 18px;
  border-top: 1px solid #ebeef5;
}

.parking-form-section__title {
  margin: 0 0 18px 2px;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .parking-form-section__title {
    margin-bottom: 12px;
  }
}
</style>
