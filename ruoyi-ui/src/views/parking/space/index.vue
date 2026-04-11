<template>
  <div class="app-container">
    <div class="page-title">车位管理</div>
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="所属停车场" prop="lotId">
        <el-select v-model="queryParams.lotId" clearable placeholder="请选择停车场">
          <el-option
            v-for="item in lotOptions"
            :key="item.lotId"
            :label="item.lotName"
            :value="item.lotId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="车位编码" prop="spaceCode">
        <el-input
          v-model="queryParams.spaceCode"
          placeholder="请输入车位编码"
          clearable
          style="width: 220px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option
            v-for="item in spaceStatusOptions"
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
          v-hasPermi="['parking:space:add']"
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
          v-hasPermi="['parking:space:edit']"
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
          v-hasPermi="['parking:space:remove']"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      v-loading="loading"
      :data="spaceList"
      empty-text="暂无车位数据"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="车位编号" align="center" prop="spaceId" width="110" />
      <el-table-column label="所属停车场" align="center" prop="lotName" min-width="160" show-overflow-tooltip />
      <el-table-column label="车位编码" align="center" prop="spaceCode" width="140" />
      <el-table-column label="区域" align="center" prop="areaName" width="100" />
      <el-table-column label="楼层" align="center" prop="floorNo" width="100" />
      <el-table-column label="车位类型" align="center" width="120">
        <template slot-scope="scope">
          {{ spaceTypeLabel(scope.row.spaceType) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="spaceStatusTagType(scope.row.status)">
            {{ spaceStatusLabel(scope.row.status) }}
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
            v-hasPermi="['parking:space:edit']"
          >
            修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['parking:space:remove']"
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
        <el-form-item label="所属停车场" prop="lotId">
          <el-select v-model="form.lotId" placeholder="请选择停车场" style="width: 100%">
            <el-option
              v-for="item in lotOptions"
              :key="item.lotId"
              :label="item.lotName"
              :value="item.lotId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="车位编码" prop="spaceCode">
          <el-input v-model="form.spaceCode" placeholder="请输入车位编码" maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="区域" prop="areaName">
          <el-input v-model="form.areaName" placeholder="请输入区域" maxlength="64" />
        </el-form-item>
        <el-form-item label="楼层" prop="floorNo">
          <el-input v-model="form.floorNo" placeholder="请输入楼层" maxlength="20" />
        </el-form-item>
        <el-form-item label="车位类型" prop="spaceType">
          <el-select v-model="form.spaceType" placeholder="请选择车位类型" style="width: 100%">
            <el-option
              v-for="item in spaceTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="item in spaceStatusOptions"
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
import { listParkingLotOptions } from '@/api/parking/lot'
import { addParkingSpace, delParkingSpace, getParkingSpace, listParkingSpaces, updateParkingSpace } from '@/api/parking/space'
import { SPACE_STATUS_OPTIONS, SPACE_TYPE_OPTIONS, findLabel, findTagType } from '../options'

export default {
  name: 'ParkingSpace',
  data() {
    return {
      loading: true,
      submitting: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      spaceList: [],
      lotOptions: [],
      title: '',
      open: false,
      spaceTypeOptions: SPACE_TYPE_OPTIONS,
      spaceStatusOptions: SPACE_STATUS_OPTIONS,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        spaceCode: undefined,
        status: undefined
      },
      form: {},
      rules: {
        lotId: [
          { required: true, message: '所属停车场不能为空', trigger: 'change' }
        ],
        spaceCode: [
          { required: true, message: '车位编码不能为空', trigger: 'blur' },
          { max: 32, message: '车位编码不能超过 32 个字符', trigger: 'blur' }
        ],
        spaceType: [
          { required: true, message: '车位类型不能为空', trigger: 'change' }
        ],
        status: [
          { required: true, message: '状态不能为空', trigger: 'change' }
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
      listParkingSpaces(this.queryParams).then(response => {
        this.spaceList = response.rows
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
        spaceId: undefined,
        lotId: undefined,
        spaceCode: undefined,
        areaName: undefined,
        floorNo: undefined,
        spaceType: '1',
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
      this.title = '新增车位'
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.spaceId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      this.reset()
      const spaceId = row.spaceId || this.ids[0]
      getParkingSpace(spaceId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改车位'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        this.submitting = true
        const request = this.form.spaceId ? updateParkingSpace(this.form) : addParkingSpace(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.spaceId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    handleDelete(row) {
      const spaceIds = row.spaceId || this.ids
      this.$modal.confirm('是否确认删除车位编号为“' + spaceIds + '”的数据项？').then(() => {
        return delParkingSpace(spaceIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    spaceTypeLabel(type) {
      return findLabel(SPACE_TYPE_OPTIONS, type)
    },
    spaceStatusLabel(status) {
      return findLabel(SPACE_STATUS_OPTIONS, status)
    },
    spaceStatusTagType(status) {
      return findTagType(SPACE_STATUS_OPTIONS, status)
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
</style>
