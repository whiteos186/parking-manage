<template>
  <div class="app-container">
    <el-row :gutter="12" class="mb8">
      <el-col :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">Parking Lots</div>
          <div class="stat-value">{{ overviewStats.lotCount }}</div>
        </el-card>
      </el-col>
      <el-col :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">Total Spaces</div>
          <div class="stat-value">{{ overviewStats.totalSpaceCount }}</div>
        </el-card>
      </el-col>
      <el-col :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">Available Spaces</div>
          <div class="stat-value">{{ overviewStats.availableSpaceCount }}</div>
        </el-card>
      </el-col>
      <el-col :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">Occupied Spaces</div>
          <div class="stat-value">{{ overviewStats.occupiedSpaceCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :span="24" class="mb8">
        <el-card>
          <div slot="header" class="card-header">
            <span>Overview Actions</span>
            <div>
              <el-button
                v-hasPermi="['parking:overview:list']"
                size="mini"
                type="primary"
                icon="el-icon-refresh"
                @click="getOverviewData"
              >
                Refresh Overview
              </el-button>
              <el-button
                v-hasPermi="['parking:overview:query']"
                :loading="healthLoading"
                size="mini"
                @click="checkHealth"
              >
                Diagnostic Health Check
              </el-button>
            </div>
          </div>
          <p v-if="healthError" class="health-error">{{ healthError }}</p>
          <pre v-if="healthResult" class="health-result">{{ healthResult }}</pre>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :span="24" class="mb8">
        <el-card>
          <div slot="header">Parking Lots</div>
          <el-form ref="lotQueryForm" :model="lotQueryParams" size="small" :inline="true">
            <el-form-item label="Lot Name" prop="lotName">
              <el-input
                v-model="lotQueryParams.lotName"
                placeholder="Enter lot name"
                clearable
                @keyup.enter.native="handleLotQuery"
              />
            </el-form-item>
            <el-form-item label="Status" prop="status">
              <el-select v-model="lotQueryParams.status" clearable placeholder="Select status">
                <el-option label="Normal" value="0" />
                <el-option label="Disabled" value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleLotQuery">Search</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetLotQuery">Reset</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="lotLoading" :data="lotList">
            <el-table-column label="Lot ID" align="center" prop="lotId" width="100" />
            <el-table-column label="Lot Name" align="center" prop="lotName" />
            <el-table-column label="Address" align="center" prop="lotAddress" min-width="220" />
            <el-table-column label="Total" align="center" prop="totalSpaceCount" width="100" />
            <el-table-column label="Available" align="center" prop="availableSpaceCount" width="100" />
            <el-table-column label="Status" align="center" width="110">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
                  {{ scope.row.status === '0' ? 'Normal' : 'Disabled' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="lotTotal > 0"
            :total="lotTotal"
            :page.sync="lotQueryParams.pageNum"
            :limit.sync="lotQueryParams.pageSize"
            @pagination="getLotList"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :span="24">
        <el-card>
          <div slot="header">Parking Spaces</div>
          <el-form ref="spaceQueryForm" :model="spaceQueryParams" size="small" :inline="true">
            <el-form-item label="Lot" prop="lotId">
              <el-select v-model="spaceQueryParams.lotId" clearable placeholder="Select lot">
                <el-option
                  v-for="item in lotOptions"
                  :key="item.lotId"
                  :label="item.lotName"
                  :value="item.lotId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="Space Code" prop="spaceCode">
              <el-input
                v-model="spaceQueryParams.spaceCode"
                placeholder="Enter space code"
                clearable
                @keyup.enter.native="handleSpaceQuery"
              />
            </el-form-item>
            <el-form-item label="Status" prop="status">
              <el-select v-model="spaceQueryParams.status" clearable placeholder="Select status">
                <el-option label="Free" value="0" />
                <el-option label="Occupied" value="1" />
                <el-option label="Disabled" value="2" />
                <el-option label="Locked" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleSpaceQuery">Search</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetSpaceQuery">Reset</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="spaceLoading" :data="spaceList">
            <el-table-column label="Space ID" align="center" prop="spaceId" width="100" />
            <el-table-column label="Lot" align="center" prop="lotName" />
            <el-table-column label="Space Code" align="center" prop="spaceCode" />
            <el-table-column label="Area" align="center" prop="areaName" width="100" />
            <el-table-column label="Floor" align="center" prop="floorNo" width="100" />
            <el-table-column label="Type" align="center" width="120">
              <template slot-scope="scope">
                {{ renderSpaceType(scope.row.spaceType) }}
              </template>
            </el-table-column>
            <el-table-column label="Status" align="center" width="120">
              <template slot-scope="scope">
                <el-tag :type="spaceStatusType(scope.row.status)">
                  {{ renderSpaceStatus(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <pagination
            v-show="spaceTotal > 0"
            :total="spaceTotal"
            :page.sync="spaceQueryParams.pageNum"
            :limit.sync="spaceQueryParams.pageSize"
            @pagination="getSpaceList"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getParkingHealth } from '@/api/parking/health'
import { getParkingOverviewStats, listParkingLots, listParkingSpaces } from '@/api/parking/overview'

export default {
  name: 'ParkingOverview',
  data() {
    return {
      healthLoading: false,
      healthResult: '',
      healthError: '',
      overviewStats: {
        lotCount: 0,
        totalSpaceCount: 0,
        availableSpaceCount: 0,
        occupiedSpaceCount: 0,
        disabledSpaceCount: 0,
        lockedSpaceCount: 0
      },
      lotLoading: false,
      lotTotal: 0,
      lotList: [],
      lotOptions: [],
      lotQueryParams: {
        pageNum: 1,
        pageSize: 10,
        lotName: undefined,
        status: undefined
      },
      spaceLoading: false,
      spaceTotal: 0,
      spaceList: [],
      spaceQueryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        spaceCode: undefined,
        status: undefined
      }
    }
  },
  created() {
    this.getOverviewData()
    this.getLotOptions()
  },
  methods: {
    getOverviewData() {
      this.getStats()
      this.getLotList()
      this.getSpaceList()
    },
    getStats() {
      getParkingOverviewStats().then(response => {
        this.overviewStats = response.data || this.overviewStats
      })
    },
    getLotOptions() {
      listParkingLots({
        pageNum: 1,
        pageSize: 100,
        status: '0'
      }).then(response => {
        this.lotOptions = response.rows || []
      })
    },
    getLotList() {
      this.lotLoading = true
      listParkingLots(this.lotQueryParams).then(response => {
        this.lotList = response.rows
        this.lotTotal = response.total
      }).finally(() => {
        this.lotLoading = false
      })
    },
    handleLotQuery() {
      this.lotQueryParams.pageNum = 1
      this.getLotList()
    },
    resetLotQuery() {
      this.resetForm('lotQueryForm')
      this.handleLotQuery()
    },
    getSpaceList() {
      this.spaceLoading = true
      listParkingSpaces(this.spaceQueryParams).then(response => {
        this.spaceList = response.rows
        this.spaceTotal = response.total
      }).finally(() => {
        this.spaceLoading = false
      })
    },
    handleSpaceQuery() {
      this.spaceQueryParams.pageNum = 1
      this.getSpaceList()
    },
    resetSpaceQuery() {
      this.resetForm('spaceQueryForm')
      this.handleSpaceQuery()
    },
    renderSpaceType(type) {
      const map = { '1': 'Standard', '2': 'Charging', '3': 'Accessible', '4': 'VIP' }
      return map[type] || '-'
    },
    renderSpaceStatus(status) {
      const map = { '0': 'Free', '1': 'Occupied', '2': 'Disabled', '3': 'Locked' }
      return map[status] || '-'
    },
    spaceStatusType(status) {
      const map = { '0': 'success', '1': 'danger', '2': 'info', '3': 'warning' }
      return map[status] || 'info'
    },
    checkHealth() {
      this.healthLoading = true
      this.healthError = ''
      getParkingHealth()
        .then(response => {
          this.healthResult = JSON.stringify(response.data, null, 2)
        })
        .catch(error => {
          this.healthError = error && error.message ? error.message : 'Health check failed'
        })
        .finally(() => {
          this.healthLoading = false
        })
    }
  }
}
</script>

<style scoped>
.stat-card {
  min-height: 110px;
}

.stat-title {
  color: #909399;
  font-size: 13px;
}

.stat-value {
  margin-top: 12px;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.health-result {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
}
.health-error {
  margin-top: 8px;
  color: #f56c6c;
}
</style>
