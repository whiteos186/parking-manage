<template>
  <div>
    <form-section
      v-for="(section, sectionIndex) in schema"
      :key="section.key || section.title || sectionIndex"
      :title="section.title"
    >
      <form-field
        v-for="field in visibleFields(section.fields)"
        :key="field.prop"
        :label="field.label"
        :prop="field.prop"
        :unit="field.unit"
        :tip="field.tip"
        :wide="field.wide"
        :full="field.full"
        :xs="field.xs"
        :sm="field.sm"
        :md="field.md"
        :lg="field.lg"
        :xl="field.xl"
      >
        <el-select
          v-if="field.type === 'select'"
          v-model="model[field.prop]"
          v-bind="field.attrs"
          v-on="field.on || {}"
        >
          <el-option
            v-for="item in field.options || []"
            :key="optionValue(item)"
            :label="optionLabel(item)"
            :value="optionValue(item)"
            :disabled="item.disabled"
          />
        </el-select>
        <el-radio-group
          v-else-if="field.type === 'radio'"
          v-model="model[field.prop]"
          v-bind="field.attrs"
          v-on="field.on || {}"
        >
          <el-radio
            v-for="item in field.options || []"
            :key="optionValue(item)"
            :label="optionValue(item)"
            :disabled="item.disabled"
          >
            {{ optionLabel(item) }}
          </el-radio>
        </el-radio-group>
        <el-input-number
          v-else-if="field.type === 'number'"
          v-model="model[field.prop]"
          v-bind="field.attrs"
          v-on="field.on || {}"
        />
        <el-date-picker
          v-else-if="field.type === 'datetime'"
          v-model="model[field.prop]"
          type="datetime"
          v-bind="field.attrs"
          v-on="field.on || {}"
        />
        <el-switch
          v-else-if="field.type === 'switch'"
          v-model="model[field.prop]"
          v-bind="field.attrs"
          v-on="field.on || {}"
        />
        <el-input
          v-else
          v-model="model[field.prop]"
          :type="field.type === 'textarea' ? 'textarea' : undefined"
          v-bind="field.attrs"
          v-on="field.on || {}"
        />
      </form-field>
    </form-section>
  </div>
</template>

<script>
import FormField from './FormField'
import FormSection from './FormSection'

export default {
  name: 'SchemaForm',
  components: { FormField, FormSection },
  props: {
    model: { type: Object, required: true },
    schema: { type: Array, default: () => [] }
  },
  methods: {
    visibleFields(fields = []) {
      return fields.filter(field => !field.hidden)
    },
    optionLabel(item) {
      return item.label !== undefined ? item.label : item.dictLabel
    },
    optionValue(item) {
      return item.value !== undefined ? item.value : item.dictValue
    }
  }
}
</script>
