<template>
  <div class="app-container">
    <div class="page-title">停车场管理</div>
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="停车场名称" prop="lotName">
        <el-input
          v-model="queryParams.lotName"
          placeholder="请输入停车场名称"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option
            v-for="item in lotStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['parking:lot:add']"
        >
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['parking:lot:edit']"
        >
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['parking:lot:remove']"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="lotList"
      empty-text="暂无停车场数据"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="停车场编号" align="center" prop="lotId" width="110" />
      <el-table-column label="停车场名称" align="center" prop="lotName" min-width="160" show-overflow-tooltip />
      <el-table-column label="地址" align="center" prop="lotAddress" min-width="220" show-overflow-tooltip />
      <el-table-column label="总车位数" align="center" prop="totalSpaceCount" width="100" />
      <el-table-column label="空闲车位数" align="center" prop="availableSpaceCount" width="110" />
      <el-table-column label="占用率" align="center" width="160">
        <template slot-scope="scope">
          <el-progress
            :percentage="lotOccupancy(scope.row)"
            :color="occupancyColor"
            :stroke-width="10"
          />
        </template>
      </el-table-column>
      <el-table-column label="月租价格" align="center" prop="monthlyPrice" width="100">
        <template slot-scope="scope">¥{{ formatPrice(scope.row.monthlyPrice) }}</template>
      </el-table-column>
      <el-table-column label="临停单价" align="center" prop="tempHourPrice" width="100">
        <template slot-scope="scope">¥{{ formatPrice(scope.row.tempHourPrice) }}/h</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="scope">
          <el-tag :type="lotStatusTagType(scope.row.status)">
            {{ lotStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['parking:lot:edit']"
          >
            修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['parking:lot:remove']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body @close="cancel">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="停车场名称" prop="lotName">
          <el-input v-model="form.lotName" placeholder="请输入停车场名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="停车场地址" prop="lotAddress">
          <el-input
            v-model="form.lotAddress"
            type="textarea"
            :rows="2"
            placeholder="请输入停车场地址"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="月租价格" prop="monthlyPrice">
          <el-input-number
            v-model="form.monthlyPrice"
            :min="0"
            :precision="2"
            :step="50"
            controls-position="right"
            style="width: 200px"
          />
          <span class="form-tip">元/月</span>
        </el-form-item>
        <el-form-item label="临停单价" prop="tempHourPrice">
          <el-input-number
            v-model="form.tempHourPrice"
            :min="0"
            :precision="2"
            :step="1"
            controls-position="right"
            style="width: 200px"
          />
          <span class="form-tip">元/小时</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="item in lotStatusOptions"
              :key="item.value"
              :label="item.value"
            >
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="submitting" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addParkingLot, delParkingLot, getParkingLot, listParkingLots, updateParkingLot } from '@/api/parking/lot'
import { LOT_STATUS_OPTIONS, findLabel, findTagType } from '../options'

export default {
  name: 'ParkingLot',
  data() {
    return {
      loading: true,
      submitting: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      lotList: [],
      title: '',
      open: false,
      lotStatusOptions: LOT_STATUS_OPTIONS,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotName: undefined,
        status: undefined
      },
      form: {},
      rules: {
        lotName: [
          { required: true, message: '停车场名称不能为空', trigger: 'blur' },
          { max: 100, message: '停车场名称不能超过 100 个字符', trigger: 'blur' }
        ],
        lotAddress: [
          { required: true, message: '停车场地址不能为空', trigger: 'blur' },
          { max: 255, message: '停车场地址不能超过 255 个字符', trigger: 'blur' }
        ],
        monthlyPrice: [
          { required: true, message: '月租价格不能为空', trigger: 'blur' }
        ],
        tempHourPrice: [
          { required: true, message: '临停单价不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listParkingLots(this.queryParams).then(response => {
        this.lotList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        lotId: undefined,
        lotName: undefined,
        lotAddress: undefined,
        monthlyPrice: 0,
        tempHourPrice: 0,
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增停车场'
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.lotId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      this.reset()
      const lotId = row.lotId || this.ids[0]
      getParkingLot(lotId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改停车场'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        this.submitting = true
        const request = this.form.lotId ? updateParkingLot(this.form) : addParkingLot(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.lotId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    handleDelete(row) {
      const lotIds = row.lotId || this.ids
      this.$modal.confirm('是否确认删除停车场编号为“' + lotIds + '”的数据项？').then(() => {
        return delParkingLot(lotIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    lotOccupancy(row) {
      const total = Number(row.totalSpaceCount) || 0
      if (total <= 0) {
        return 0
      }
      const available = Number(row.availableSpaceCount) || 0
      const occupied = Math.max(total - available, 0)
      return Math.round((occupied / total) * 100)
    },
    occupancyColor(percentage) {
      if (percentage >= 90) return '#f56c6c'
      if (percentage >= 70) return '#e6a23c'
      return '#67c23a'
    },
    formatPrice(value) {
      const num = Number(value)
      if (Number.isNaN(num)) {
        return '0.00'
      }
      return num.toFixed(2)
    },
    lotStatusLabel(status) {
      return findLabel(LOT_STATUS_OPTIONS, status)
    },
    lotStatusTagType(status) {
      return findTagType(LOT_STATUS_OPTIONS, status)
    }
  }
}
</script>

<style scoped>
.page-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}

.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
