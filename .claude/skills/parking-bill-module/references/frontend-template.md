# Frontend Template

本文件提供 Vue 前端三大件(api.js / index.vue 列表页 / form.vue 独立编辑页)的填空式模板。
占位符参考 `backend-template.md` 的命名速查表。

---

## API 模板

文件路径: `ruoyi-ui/src/api/parking/{{resource}}.js`

```js
import request from '@/utils/request'

// 分页列表
export function listParking{{Entity}}s(query) {
  return request({
    url: '/parking/{{resource}}/list',
    method: 'get',
    params: query
  })
}

// 详情
export function getParking{{Entity}}({{pk}}) {
  return request({
    url: '/parking/{{resource}}/' + {{pk}},
    method: 'get'
  })
}

// 新增
export function addParking{{Entity}}(data) {
  return request({
    url: '/parking/{{resource}}',
    method: 'post',
    data: data
  })
}

// 修改
export function updateParking{{Entity}}(data) {
  return request({
    url: '/parking/{{resource}}',
    method: 'put',
    data: data
  })
}

// 批量逻辑删除
export function delParking{{Entity}}(ids) {
  return request({
    url: '/parking/{{resource}}/' + ids,
    method: 'delete'
  })
}

// 提交审核
export function submitParking{{Entity}}(data) {
  return request({
    url: '/parking/{{resource}}/submit',
    method: 'put',
    data: data
  })
}

// 审核通过
export function approveParking{{Entity}}(data) {
  return request({
    url: '/parking/{{resource}}/approve',
    method: 'put',
    data: data
  })
}

// 审核拒绝
export function rejectParking{{Entity}}(data) {
  return request({
    url: '/parking/{{resource}}/reject',
    method: 'put',
    data: data
  })
}

// 作废
export function voidParking{{Entity}}(data) {
  return request({
    url: '/parking/{{resource}}/void',
    method: 'put',
    data: data
  })
}
```

---

## options.js 追加片段

追加到 `ruoyi-ui/src/views/parking/options.js`:

```js
export const {{ENTITY}}_BILL_STATUS_OPTIONS = [
  { label: '草稿',     value: '00', tagType: 'info'    },
  { label: '待审核',   value: '10', tagType: 'warning' },
  { label: '审核通过', value: '20', tagType: 'success' },
  { label: '审核拒绝', value: '30', tagType: 'danger'  },
  { label: '已作废',   value: '90', tagType: 'info'    }
]

// BIZ: 其他业务字典(类型、等级等)追加在此
// export const {{ENTITY}}_TYPE_OPTIONS = [ ... ]
```

`findLabel` / `findTagType` 这两个工具函数已经在 options.js 文件里了,直接 import 用,不用重复写。

---

## 列表页模板 (index.vue)

文件路径: `ruoyi-ui/src/views/parking/{{resource}}/index.vue`

```vue
<template>
  <div class="app-container">
    <!-- 查询表单 -->
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="单据编号" prop="billNo">
        <el-input v-model="queryParams.billNo" placeholder="请输入单据编号"
                  clearable style="width: 200px"
                  @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="所属停车场" prop="lotId">
        <el-select v-model="queryParams.lotId" clearable placeholder="请选择停车场" style="width: 180px">
          <el-option v-for="item in lotOptions" :key="item.lotId"
                     :label="item.lotName" :value="item.lotId" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="billStatus">
        <el-select v-model="queryParams.billStatus" clearable placeholder="全部状态" style="width: 140px">
          <el-option v-for="item in billStatusOptions" :key="item.value"
                     :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <!-- BIZ: 其他过滤条件 -->
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮行 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini"
                   @click="handleAdd"
                   v-hasPermi="['parking:{{resource}}:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini"
                   :disabled="multiple" @click="handleDelete"
                   v-hasPermi="['parking:{{resource}}:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="list" empty-text="暂无{{模块中文}}数据"
              @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="单据编号"  prop="billNo"   min-width="180" />
      <el-table-column label="停车场"    prop="lotName"  min-width="140" />
      <!-- BIZ: 业务列 -->
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="billStatusTagType(scope.row.billStatus)">
            {{ billStatusLabel(scope.row.billStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280">
        <template slot-scope="scope">
          <el-button v-if="canEdit(scope.row)" size="mini" type="text" icon="el-icon-edit"
                     @click="handleUpdate(scope.row)"
                     v-hasPermi="['parking:{{resource}}:edit']">修改</el-button>
          <el-button v-if="canSubmit(scope.row)" size="mini" type="text" icon="el-icon-upload2"
                     @click="handleSubmit(scope.row)"
                     v-hasPermi="['parking:{{resource}}:submit']">提交</el-button>
          <el-button v-if="canAudit(scope.row)" size="mini" type="text" icon="el-icon-check"
                     @click="handleApprove(scope.row)"
                     v-hasPermi="['parking:{{resource}}:approve']">通过</el-button>
          <el-button v-if="canAudit(scope.row)" size="mini" type="text" icon="el-icon-close"
                     @click="openRejectDialog(scope.row)"
                     v-hasPermi="['parking:{{resource}}:reject']">拒绝</el-button>
          <el-button v-if="canVoid(scope.row)" size="mini" type="text" icon="el-icon-remove-outline"
                     @click="handleVoid(scope.row)"
                     v-hasPermi="['parking:{{resource}}:void']">作废</el-button>
          <el-button v-if="canDelete(scope.row)" size="mini" type="text" icon="el-icon-delete"
                     @click="handleDelete(scope.row)"
                     v-hasPermi="['parking:{{resource}}:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total"
                :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize"
                @pagination="getList" />

    <!-- 审核拒绝弹框(只有一个意见输入,允许用 el-dialog) -->
    <el-dialog title="审核拒绝" :visible.sync="rejectOpen" width="480px" append-to-body>
      <el-form ref="rejectForm" :model="rejectForm" :rules="rejectRules" label-width="80px">
        <el-form-item label="单据号">
          <span>{{ rejectForm.billNo }}</span>
        </el-form-item>
        <el-form-item label="意见" prop="auditRemark">
          <el-input v-model="rejectForm.auditRemark" type="textarea" :rows="3"
                    placeholder="请填写拒绝原因" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="submitting" @click="submitReject">确 定</el-button>
        <el-button @click="rejectOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listParking{{Entity}}s,
  delParking{{Entity}},
  submitParking{{Entity}},
  approveParking{{Entity}},
  rejectParking{{Entity}},
  voidParking{{Entity}}
} from '@/api/parking/{{resource}}'
import { listParkingLotOptions } from '@/api/parking/lot'
import {
  {{ENTITY}}_BILL_STATUS_OPTIONS,
  findLabel,
  findTagType
} from '@/views/parking/options'

export default {
  name: 'Parking{{Entity}}',
  data() {
    return {
      loading: true,
      submitting: false,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      list: [],
      lotOptions: [],
      billStatusOptions: {{ENTITY}}_BILL_STATUS_OPTIONS,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        billNo: undefined,
        lotId: undefined,
        billStatus: undefined
        // BIZ: 其他过滤字段
      },
      rejectOpen: false,
      rejectForm: {},
      rejectRules: {
        auditRemark: [
          { required: true, message: '请填写拒绝原因', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getLotOptions()
    this.getList()
  },
  methods: {
    getLotOptions() {
      listParkingLotOptions().then(response => {
        this.lotOptions = response.data || []
      })
    },
    getList() {
      this.loading = true
      listParking{{Entity}}s(this.queryParams).then(response => {
        this.list = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.{{pk}})
      this.multiple = !selection.length
    },

    // ---- 路由跳转到独立编辑页 ----
    handleAdd() {
      this.$router.push('/parking/{{resource}}/form')
    },
    handleUpdate(row) {
      this.$router.push({ path: '/parking/{{resource}}/form', query: { id: row.{{pk}} } })
    },

    // ---- 业务动作 ----
    handleSubmit(row) {
      this.$modal.confirm('确定提交单据 "' + row.billNo + '" 进入审核?').then(() => {
        return submitParking{{Entity}}({ {{pk}}: row.{{pk}} })
      }).then(() => {
        this.$modal.msgSuccess('提交成功')
        this.getList()
      }).catch(() => {})
    },
    handleApprove(row) {
      this.$modal.confirm('确定审核通过 "' + row.billNo + '"?').then(() => {
        return approveParking{{Entity}}({ {{pk}}: row.{{pk}} })
      }).then(() => {
        this.$modal.msgSuccess('审核通过')
        this.getList()
      }).catch(() => {})
    },
    openRejectDialog(row) {
      this.rejectForm = { {{pk}}: row.{{pk}}, billNo: row.billNo, auditRemark: '' }
      this.rejectOpen = true
    },
    submitReject() {
      this.$refs.rejectForm.validate(valid => {
        if (!valid) return
        this.submitting = true
        rejectParking{{Entity}}(this.rejectForm).then(() => {
          this.$modal.msgSuccess('已拒绝')
          this.rejectOpen = false
          this.getList()
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    handleVoid(row) {
      this.$modal.confirm('确定作废单据 "' + row.billNo + '"?作废后不可恢复').then(() => {
        return voidParking{{Entity}}({ {{pk}}: row.{{pk}} })
      }).then(() => {
        this.$modal.msgSuccess('已作废')
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      const ids = row.{{pk}} ? [row.{{pk}}] : this.ids
      this.$modal.confirm('是否确认删除选中的 ' + ids.length + ' 条记录?').then(() => {
        return delParking{{Entity}}(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },

    // ---- 行级状态判断 ----
    canEdit(row)   { return row.billStatus === '00' || row.billStatus === '30' },
    canSubmit(row) { return row.billStatus === '00' || row.billStatus === '30' },
    canAudit(row)  { return row.billStatus === '10' },
    canVoid(row)   { return row.billStatus === '20' },
    canDelete(row) { return row.billStatus === '00' || row.billStatus === '30' },

    // ---- 字典标签 ----
    billStatusLabel(v)    { return findLabel({{ENTITY}}_BILL_STATUS_OPTIONS, v) },
    billStatusTagType(v)  { return findTagType({{ENTITY}}_BILL_STATUS_OPTIONS, v) }
  }
}
</script>
```

---

## 独立编辑页模板 (form.vue)

文件路径: `ruoyi-ui/src/views/parking/{{resource}}/form.vue`

```vue
<template>
  <div class="app-container">
    <el-page-header @back="goBack"
                    :content="isEdit ? '修改{{模块中文}}' : '新增{{模块中文}}'"
                    style="margin-bottom: 20px" />

    <el-form ref="form" :model="form" :rules="rules" label-width="120px" v-loading="loading">

      <!-- ===== 基本信息 ===== -->
      <el-divider content-position="left">基本信息</el-divider>
      <el-form-item label="单据编号" prop="billNo">
        <el-input v-model="form.billNo" :disabled="true"
                  placeholder="保存时自动生成" style="width: 320px" />
      </el-form-item>
      <el-form-item label="所属停车场" prop="lotId">
        <el-select v-model="form.lotId" :disabled="readonly"
                   placeholder="请选择停车场" style="width: 320px">
          <el-option v-for="item in lotOptions" :key="item.lotId"
                     :label="item.lotName" :value="item.lotId" />
        </el-select>
      </el-form-item>

      <!-- ===== 业务信息 ===== -->
      <el-divider content-position="left">业务信息</el-divider>
      <!-- BIZ: 业务字段 -->
      <!-- 示例:
      <el-form-item label="设备编码" prop="equipmentCode">
        <el-input v-model="form.equipmentCode" :disabled="readonly" style="width: 320px" />
      </el-form-item>
      <el-form-item label="预估金额" prop="estimatedAmount">
        <el-input-number v-model="form.estimatedAmount" :disabled="readonly"
                         :min="0" :precision="2" :step="100"
                         controls-position="right" style="width: 200px" />
        <span class="form-tip">元</span>
      </el-form-item>
      <el-form-item label="计划时间" prop="scheduleTime">
        <el-date-picker v-model="form.scheduleTime" :disabled="readonly"
                        type="datetime"
                        value-format="yyyy-MM-dd HH:mm:ss"
                        placeholder="请选择计划时间"
                        style="width: 320px" />
      </el-form-item>
      -->

      <!-- ===== 其他 ===== -->
      <el-divider content-position="left">其他</el-divider>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" :disabled="readonly"
                  type="textarea" :rows="3"
                  placeholder="请输入备注"
                  maxlength="500" show-word-limit
                  style="width: 520px" />
      </el-form-item>

      <!-- 审核信息(只读) -->
      <template v-if="isEdit && form.auditTime">
        <el-divider content-position="left">审核记录</el-divider>
        <el-form-item label="审核人">
          <span>{{ form.auditUser }}</span>
        </el-form-item>
        <el-form-item label="审核时间">
          <span>{{ form.auditTime }}</span>
        </el-form-item>
        <el-form-item label="审核意见">
          <span>{{ form.auditRemark }}</span>
        </el-form-item>
      </template>

      <!-- 底部操作 -->
      <el-form-item>
        <el-button v-if="!readonly" type="primary" :loading="submitting" @click="submitForm">确 定</el-button>
        <el-button @click="goBack">{{ readonly ? '返 回' : '取 消' }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {
  getParking{{Entity}},
  addParking{{Entity}},
  updateParking{{Entity}}
} from '@/api/parking/{{resource}}'
import { listParkingLotOptions } from '@/api/parking/lot'

export default {
  name: 'Parking{{Entity}}Form',
  data() {
    return {
      loading: false,
      submitting: false,
      lotOptions: [],
      form: {
        {{pk}}: undefined,
        billNo: undefined,
        lotId: undefined,
        // BIZ: 业务字段初值
        billStatus: '00',
        remark: undefined
      },
      rules: {
        lotId: [{ required: true, message: '所属停车场不能为空', trigger: 'change' }]
        // BIZ: 业务字段规则
      }
    }
  },
  computed: {
    isEdit() {
      return !!this.$route.query.id
    },
    /** 仅草稿/审核拒绝状态可编辑,其他状态是只读查看 */
    readonly() {
      if (!this.isEdit) return false
      const s = this.form.billStatus
      return s !== '00' && s !== '30'
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
      getParking{{Entity}}(this.$route.query.id).then(response => {
        this.form = response.data
      }).finally(() => {
        this.loading = false
      })
    },
    goBack() {
      this.$router.go(-1)
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        const req = this.isEdit
          ? updateParking{{Entity}}(this.form)
          : addParking{{Entity}}(this.form)
        req.then(() => {
          this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
          this.goBack()
        }).finally(() => {
          this.submitting = false
        })
      })
    }
  }
}
</script>
```

---

## 前端注意事项

1. **按钮级权限**: 每个业务按钮都必须有 `v-hasPermi="['parking:{{resource}}:<action>']"`,否则 `ParkingModuleSmokeTest` 会红
2. **中文文案**: 所有 placeholder / 按钮文字 / empty-text / 消息提示都必须中文硬编码,不要用 `this.$t(...)`
3. **分组分隔**: 字段多的表单必须用 `<el-divider content-position="left">` 分组(基本信息 / 业务信息 / 审核记录 / 其他),可读性是表单页的第一要义
4. **只读态**: `readonly` computed 覆盖"审核中/通过/作废"三种状态,这些态下打开 form.vue 只做**查看**,不显示确定按钮
5. **级联下拉**: 选客户后加载车辆、选分类后加载子分类这类,写成独立方法 `getXxxOptions(parentId, preserveSelection)`,参考 `membership/form.vue` 的 `handleCustomerChange` 模式
6. **返回**: 统一用 `this.$router.go(-1)`,不要 `push('/parking/xxx')`,因为浏览器历史栈才是最直观的返回语义
7. **保存跳转**: 保存成功后 `goBack()`,不要自动跳到详情页(用户可能想继续新增下一条)

---

## 常见 Element UI 输入组件选型

| 字段类型 | 组件 | 关键属性 |
|---|---|---|
| 短文本 | `el-input` | `style="width: 320px"` |
| 长文本/备注 | `el-input type="textarea"` | `:rows="3"` + `maxlength="500"` + `show-word-limit` |
| 整数 | `el-input-number` | `:min="0"` + `:step="1"` |
| 金额 | `el-input-number` | `:min="0"` + `:precision="2"` + `:step="100"` + `controls-position="right"` + `<span class="form-tip">元</span>` |
| 单选下拉 | `el-select` + `el-option` | `filterable`(选项 > 10) + `clearable` |
| 多选下拉 | `el-select multiple` | `collapse-tags` |
| 级联选择 | `el-cascader` | `:options` + `clearable` |
| 日期 | `el-date-picker type="date"` | `value-format="yyyy-MM-dd"` |
| 日期时间 | `el-date-picker type="datetime"` | `value-format="yyyy-MM-dd HH:mm:ss"` |
| 时间区间 | `el-date-picker type="daterange"` | `start-placeholder="开始"` + `end-placeholder="结束"` |
| 开关 | `el-switch` | `active-value="1"` + `inactive-value="0"`(字符串保持一致) |
| 单选按钮 | `el-radio-group` + `el-radio` | value 用字符串 |
| 上传 | `el-upload` | 参考 `src/views/parking/lot/index.vue` |

日期字段 `value-format` 必须精确对应后端 `@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")`,否则反序列化会失败。
