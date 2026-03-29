<template>
  <div class="app-container">
    <el-card>
      <div slot="header">Parking Module Placeholder</div>
      <p>This is the initial parking module scaffold page.</p>
      <el-button
        v-hasPermi="['parking:overview:query']"
        :loading="loading"
        type="primary"
        size="mini"
        @click="checkHealth"
      >
        Check /parking/health
      </el-button>
      <p v-if="healthError" class="health-error">{{ healthError }}</p>
      <pre v-if="healthResult" class="health-result">{{ healthResult }}</pre>
    </el-card>
  </div>
</template>

<script>
import { getParkingHealth } from '@/api/parking/health'

export default {
  name: 'ParkingPlaceholder',
  data() {
    return {
      loading: false,
      healthResult: '',
      healthError: ''
    }
  },
  methods: {
    checkHealth() {
      this.loading = true
      this.healthError = ''
      getParkingHealth()
        .then(response => {
          this.healthResult = JSON.stringify(response.data, null, 2)
        })
        .catch(error => {
          this.healthError = error && error.message ? error.message : 'Health check failed'
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style scoped>
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
