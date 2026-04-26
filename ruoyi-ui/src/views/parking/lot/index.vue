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
      <el-table-column label="创建时间" align="center" prop="createTime" width="170" />
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

  </div>
</template>

<script>
import { delParkingLot, listParkingLots } from '@/api/parking/lot'
import { SearchForm, DataTable } from '../components'

export default {
  name: 'ParkingLot',
  components: { SearchForm, DataTable },
  dicts: ['parking_lot_status'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      lotList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotName: undefined,
        status: undefined
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
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleAdd() {
      this.$router.push('/parking/lot/form')
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.lotId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      const lotId = row.lotId || this.ids[0]
      this.$router.push({ path: '/parking/lot/form', query: { id: lotId } })
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
