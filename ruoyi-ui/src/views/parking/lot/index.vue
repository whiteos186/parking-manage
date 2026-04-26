<template>
  <div class="app-container">
    <search-form :model="queryParams" :visible="showSearch" @search="handleQuery" @reset="handleQuery">
      <el-form-item label="停车场名称" prop="lotName">
        <el-input
          v-model="queryParams.lotName"
          placeholder="请输入停车场名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_lot_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </search-form>

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
          type="primary"
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
          type="primary"
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
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-setting"
          size="mini"
          v-hasPermi="['parking:settings:list']"
          @click="$router.push('/archives/settings')"
        >
          停车设置
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <data-table
      :loading="loading"
      :data="lotList"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      empty-text="暂无停车场数据"
      @selection-change="handleSelectionChange"
      @pagination="getList"
    >
      <el-table-column label="停车场编号" align="center" prop="lotId" width="110" />
      <el-table-column label="停车场名称" align="center" prop="lotName" min-width="160" show-overflow-tooltip />
      <el-table-column label="地址" align="center" prop="lotAddress" min-width="220" show-overflow-tooltip />
      <el-table-column label="总车位数" align="center" prop="totalSpaceCount" width="100" />
      <el-table-column label="空闲车位数" align="center" prop="availableSpaceCount" width="110" />
      <el-table-column label="占用率" align="center" width="100">
        <template slot-scope="scope">
          <span>{{ lotOccupancy(scope.row) }}%</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_lot_status" :value="scope.row.status" />
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
    </data-table>

    <form-dialog
      :open.sync="open"
      :title="title"
      :model="form"
      :rules="rules"
      :submitting="submitting"
      @submit="submitForm"
      @cancel="reset"
    >
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
      <el-form-item label="总车位数" prop="totalSpaceCount">
        <el-input-number
          v-model="form.totalSpaceCount"
          :min="1"
          :max="99999"
          :precision="0"
          :step="10"
          controls-position="right"
          style="width: 220px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio
            v-for="item in dict.type.parking_lot_status"
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
    </form-dialog>
  </div>
</template>

<script>
import { addParkingLot, delParkingLot, getParkingLot, listParkingLots, updateParkingLot } from '@/api/parking/lot'
import { SearchForm, DataTable, FormDialog } from '../components'

export default {
  name: 'ParkingLot',
  components: { SearchForm, DataTable, FormDialog },
  dicts: ['parking_lot_status'],
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
        totalSpaceCount: [
          { required: true, message: '总车位数不能为空', trigger: 'blur' },
          { type: 'number', min: 1, message: '总车位数必须大于 0', trigger: 'blur' }
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
    reset() {
      this.form = {
        lotId: undefined,
        lotName: undefined,
        lotAddress: undefined,
        totalSpaceCount: 100,
        monthlyPrice: undefined,
        tempHourPrice: undefined,
        status: '0',
        remark: undefined
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
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
      this.submitting = true
      const request = this.form.lotId ? updateParkingLot(this.form) : addParkingLot(this.form)
      request.then(() => {
        this.$modal.msgSuccess(this.form.lotId ? '修改成功' : '新增成功')
        this.open = false
        this.getList()
      }).finally(() => {
        this.submitting = false
      })
    },
    handleDelete(row) {
      const lotIds = row.lotId || this.ids
      this.$modal.confirm('是否确认删除停车场编号为 "' + lotIds + '" 的数据项？').then(() => {
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
      return Math.round((Math.max(total - available, 0) / total) * 100)
    }
  }
}
</script>

<style scoped>
.mb12 {
  margin-bottom: 12px;
}
</style>
