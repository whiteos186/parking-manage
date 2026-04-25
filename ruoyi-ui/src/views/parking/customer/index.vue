<template>
  <div class="app-container">
    <search-form :model="queryParams" :visible="showSearch" @search="handleQuery" @reset="handleQuery">
      <el-form-item label="客户编号" prop="customerCode">
        <el-input
          v-model="queryParams.customerCode"
          placeholder="请输入客户编号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input
          v-model="queryParams.customerName"
          placeholder="请输入客户名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入手机号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户类型" prop="customerType">
        <el-select v-model="queryParams.customerType" clearable placeholder="请选择客户类型" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_customer_type"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 160px">
          <el-option
            v-for="item in dict.type.parking_customer_status"
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
          v-hasPermi="['parking:customer:add']"
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
          v-hasPermi="['parking:customer:edit']"
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
          v-hasPermi="['parking:customer:remove']"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <data-table
      :loading="loading"
      :data="customerList"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @selection-change="handleSelectionChange"
      @pagination="getList"
    >
      <el-table-column label="编号" align="center" prop="customerCode" width="140" />
      <el-table-column label="客户名称" align="center" prop="customerName" min-width="160" show-overflow-tooltip />
      <el-table-column label="手机号" align="center" prop="mobile" width="140" />
      <el-table-column label="客户类型" align="center" width="120">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_customer_type" :value="scope.row.customerType" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.parking_customer_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['parking:customer:edit']"
          >
            修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['parking:customer:remove']"
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
      <el-form-item label="客户编号" prop="customerCode">
        <el-input v-model="form.customerCode" placeholder="请输入客户编号" maxlength="32" show-word-limit />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="form.customerName" placeholder="请输入客户名称" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="form.mobile" placeholder="请输入手机号" maxlength="20" />
      </el-form-item>
      <el-form-item label="客户类型" prop="customerType">
        <el-select v-model="form.customerType" placeholder="请选择客户类型" style="width: 100%">
          <el-option
            v-for="item in dict.type.parking_customer_type"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio v-for="item in dict.type.parking_customer_status" :key="item.value" :label="item.value">
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
import { addCustomer, delCustomer, getCustomer, listCustomers, updateCustomer } from '@/api/parking/customer'
import { SearchForm, DataTable, FormDialog } from '../components'

export default {
  name: 'ParkingCustomer',
  components: { SearchForm, DataTable, FormDialog },
  dicts: ['parking_customer_type', 'parking_customer_status'],
  data() {
    return {
      loading: true,
      submitting: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      customerList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        customerCode: undefined,
        customerName: undefined,
        mobile: undefined,
        customerType: undefined,
        status: undefined
      },
      form: {},
      rules: {
        customerCode: [
          { required: true, message: '客户编号不能为空', trigger: 'blur' }
        ],
        customerName: [
          { required: true, message: '客户名称不能为空', trigger: 'blur' }
        ],
        mobile: [
          { required: true, message: '手机号不能为空', trigger: 'blur' }
        ],
        customerType: [
          { required: true, message: '客户类型不能为空', trigger: 'change' }
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
      listCustomers(this.queryParams).then(response => {
        this.customerList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    reset() {
      this.form = {
        customerId: undefined,
        customerCode: undefined,
        customerName: undefined,
        mobile: undefined,
        customerType: '1',
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
      this.title = '新增客户档案'
    },
    handleUpdate(row) {
      this.reset()
      const customerId = row.customerId || this.ids[0]
      getCustomer(customerId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改客户档案'
      })
    },
    handleDelete(row) {
      const customerIds = row.customerId || this.ids
      this.$modal.confirm('是否确认删除客户档案编号为 "' + customerIds + '" 的数据项？').then(() => {
        return delCustomer(customerIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.customerId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    submitForm() {
      this.submitting = true
      const request = this.form.customerId ? updateCustomer(this.form) : addCustomer(this.form)
      request.then(() => {
        this.$modal.msgSuccess(this.form.customerId ? '修改成功' : '新增成功')
        this.open = false
        this.getList()
      }).finally(() => {
        this.submitting = false
      })
    }
  }
}
</script>
