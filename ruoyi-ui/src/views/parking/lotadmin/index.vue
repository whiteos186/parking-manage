<template>
  <div class="app-container">
    <search-form :model="queryParams" :visible="showSearch" @search="handleQuery" @reset="handleQuery">
      <el-form-item label="所属停车场" prop="lotId">
        <el-select v-model="queryParams.lotId" clearable placeholder="请选择停车场" style="width: 160px">
          <el-option
            v-for="item in lotOptions"
            :key="item.lotId"
            :label="item.lotName"
            :value="item.lotId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="用户" prop="userId">
        <el-select
          v-model="queryParams.userId"
          clearable
          filterable
          placeholder="请选择用户"
          style="width: 280px"
        >
          <el-option
            v-for="item in userOptions"
            :key="item.userId"
            :label="userOptionLabel(item)"
            :value="item.userId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_lot_admin_status"
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
          v-hasPermi="['parking:lotadmin:add']"
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
          v-hasPermi="['parking:lotadmin:edit']"
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
          v-hasPermi="['parking:lotadmin:remove']"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <data-table
      :loading="loading"
      :data="lotAdminList"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      empty-text="暂无绑定数据"
      @selection-change="handleSelectionChange"
      @pagination="getList"
    >
      <el-table-column label="绑定编号" align="center" prop="lotAdminId" width="110" />
      <el-table-column label="停车场名称" align="center" prop="lotName" min-width="160" show-overflow-tooltip />
      <el-table-column label="用户编号" align="center" prop="userId" width="110" />
      <el-table-column label="登录账号" align="center" prop="userName" width="140" show-overflow-tooltip />
      <el-table-column label="用户昵称" align="center" prop="nickName" min-width="140" show-overflow-tooltip />
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_lot_admin_status" :value="scope.row.status" />
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
            v-hasPermi="['parking:lotadmin:edit']"
          >
            修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['parking:lotadmin:remove']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </data-table>

    <form-dialog
      :open.sync="open"
      :title="title"
      width="480px"
      :model="form"
      :rules="rules"
      :submitting="submitting"
      @submit="submitForm"
      @cancel="reset"
    >
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
      <el-form-item label="用户" prop="userId">
        <el-select
          v-model="form.userId"
          filterable
          placeholder="请选择用户"
          style="width: 100%"
        >
          <el-option
            v-for="item in userOptions"
            :key="item.userId"
            :label="userOptionLabel(item)"
            :value="item.userId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio
            v-for="item in dict.type.parking_lot_admin_status"
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
import { listParkingLotOptions } from '@/api/parking/lot'
import {
  addLotAdmin,
  delLotAdmin,
  getLotAdmin,
  listLotAdmins,
  listLotAdminUserOptions,
  updateLotAdmin
} from '@/api/parking/lotAdmin'
import { SearchForm, DataTable, FormDialog } from '../components'

export default {
  name: 'ParkingLotAdmin',
  components: { SearchForm, DataTable, FormDialog },
  dicts: ['parking_lot_admin_status'],
  data() {
    return {
      loading: true,
      submitting: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      lotAdminList: [],
      lotOptions: [],
      userOptions: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        userId: undefined,
        status: undefined
      },
      form: {},
      rules: {
        lotId: [
          { required: true, message: '所属停车场不能为空', trigger: 'change' }
        ],
        userId: [
          { required: true, message: '用户不能为空', trigger: 'change' }
        ],
        status: [
          { required: true, message: '状态不能为空', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getLotOptions()
    this.getUserOptions()
    this.getList()
  },
  methods: {
    getLotOptions() {
      listParkingLotOptions().then(response => {
        this.lotOptions = response.data || []
      })
    },
    getUserOptions() {
      listLotAdminUserOptions().then(response => {
        this.userOptions = response.data || []
      }).catch(() => {
        this.userOptions = []
      })
    },
    userOptionLabel(item) {
      if (!item) {
        return ''
      }
      const nickName = item.nickName || item.userName || ''
      return `${nickName}（${item.userName} #${item.userId}）`
    },
    getList() {
      this.loading = true
      listLotAdmins(this.queryParams).then(response => {
        this.lotAdminList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    reset() {
      this.form = {
        lotAdminId: undefined,
        lotId: undefined,
        userId: undefined,
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
      this.title = '新增管理员绑定'
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.lotAdminId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      this.reset()
      const lotAdminId = row.lotAdminId || this.ids[0]
      getLotAdmin(lotAdminId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改管理员绑定'
      })
    },
    submitForm() {
      this.submitting = true
      const request = this.form.lotAdminId ? updateLotAdmin(this.form) : addLotAdmin(this.form)
      request.then(() => {
        this.$modal.msgSuccess(this.form.lotAdminId ? '修改成功' : '新增成功')
        this.open = false
        this.getList()
      }).finally(() => {
        this.submitting = false
      })
    },
    handleDelete(row) {
      const lotAdminIds = row.lotAdminId || this.ids
      this.$modal.confirm('是否确认删除管理员绑定编号为 "' + lotAdminIds + '" 的数据项？').then(() => {
        return delLotAdmin(lotAdminIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>
