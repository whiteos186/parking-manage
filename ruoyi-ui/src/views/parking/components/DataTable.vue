<template>
  <div>
    <el-table
      v-loading="loading"
      :data="data"
      :empty-text="emptyText"
      @selection-change="val => $emit('selection-change', val)"
    >
      <el-table-column v-if="showSelection" type="selection" width="55" align="center" />
      <slot />
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="innerPage"
      :limit.sync="innerLimit"
      @pagination="$emit('pagination')"
    />
  </div>
</template>

<script>
export default {
  name: 'DataTable',
  props: {
    loading: Boolean,
    data: { type: Array, default: () => [] },
    total: { type: Number, default: 0 },
    page: { type: Number, default: 1 },
    limit: { type: Number, default: 10 },
    emptyText: { type: String, default: '暂无数据' },
    showSelection: { type: Boolean, default: true }
  },
  computed: {
    innerPage: {
      get() { return this.page },
      set(v) { this.$emit('update:page', v) }
    },
    innerLimit: {
      get() { return this.limit },
      set(v) { this.$emit('update:limit', v) }
    }
  }
}
</script>
