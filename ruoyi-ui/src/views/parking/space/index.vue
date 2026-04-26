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
      <el-form-item label="车位编码" prop="spaceCode">
        <el-input
          v-model="queryParams.spaceCode"
          placeholder="请输入车位编码"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_space_status"
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
          v-hasPermi="['parking:space:add']"
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
          v-hasPermi="['parking:space:edit']"
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
          v-hasPermi="['parking:space:remove']"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <data-table
      :loading="loading"
      :data="spaceList"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      empty-text="暂无车位数据"
      @selection-change="handleSelectionChange"
      @pagination="getList"
    >
      <el-table-column label="车位编号" align="center" prop="spaceId" width="110" />
      <el-table-column label="所属停车场" align="center" prop="lotName" min-width="160" show-overflow-tooltip />
      <el-table-column label="车位编码" align="center" prop="spaceCode" width="140" />
      <el-table-column label="区域" align="center" prop="areaName" width="100" />
      <el-table-column label="楼层" align="center" prop="floorNo" width="100" />
      <el-table-column label="车位类型" align="center" width="120">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_space_type" :value="scope.row.spaceType" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_space_status" :value="scope.row.status" />
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
    </data-table>

  </div>
</template>

<script>
import { listParkingLotOptions } from '@/api/parking/lot'
import { delParkingSpace, listParkingSpaces } from '@/api/parking/space'
import { SearchForm, DataTable } from '../components'

export default {
  name: 'ParkingSpace',
  components: { SearchForm, DataTable },
  dicts: ['parking_space_status', 'parking_space_type'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      spaceList: [],
      lotOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        spaceCode: undefined,
        status: undefined
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
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleAdd() {
      this.$router.push('/parking/space/form')
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.spaceId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      const spaceId = row.spaceId || this.ids[0]
      this.$router.push({ path: '/parking/space/form', query: { id: spaceId } })
    },
    handleDelete(row) {
      const spaceIds = row.spaceId || this.ids
      this.$modal.confirm('是否确认删除车位编号为"' + spaceIds + '"的数据项？').then(() => {
        return delParkingSpace(spaceIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>
