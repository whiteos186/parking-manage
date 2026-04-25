<template>
  <div class="app-container">
    <el-alert
      title="停车场价格字段已降级为兼容字段，实际计费、折扣和支付默认规则统一以本页设置为准。"
      type="warning"
      :closable="false"
      show-icon
      class="mb16"
    />

    <el-form ref="form" :model="form" :rules="rules" label-width="150px" v-loading="loading">
      <el-card shadow="never" class="settings-card">
        <div slot="header" class="card-header">
          <span>计费规则</span>
        </div>
        <el-row :gutter="24">
          <el-col :xs="24" :lg="12">
            <el-form-item label="月卡全局价格" prop="monthlyPrice">
              <el-input-number
                v-model="form.monthlyPrice"
                :min="0"
                :precision="2"
                :step="50"
                controls-position="right"
                style="width: 220px"
              />
              <span class="form-tip">元 / 月</span>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="临停小时单价" prop="tempHourPrice">
              <el-input-number
                v-model="form.tempHourPrice"
                :min="0"
                :precision="2"
                :step="1"
                controls-position="right"
                style="width: 220px"
              />
              <span class="form-tip">元 / 小时</span>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="免费分钟数" prop="tempFreeMinutes">
              <el-input-number
                v-model="form.tempFreeMinutes"
                :min="0"
                :step="5"
                controls-position="right"
                style="width: 220px"
              />
              <span class="form-tip">分钟</span>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="计费步长" prop="tempBillingStepMinutes">
              <el-input-number
                v-model="form.tempBillingStepMinutes"
                :min="1"
                :step="5"
                controls-position="right"
                style="width: 220px"
              />
              <span class="form-tip">分钟</span>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="向上取整" prop="tempRoundUpEnabled">
              <el-switch v-model="form.tempRoundUpEnabled" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="临停封顶金额" prop="tempDailyCapAmount">
              <el-input-number
                v-model="form.tempDailyCapAmount"
                :min="0"
                :precision="2"
                :step="10"
                controls-position="right"
                style="width: 220px"
              />
              <span class="form-tip">0 表示不启用封顶</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card shadow="never" class="settings-card">
        <div slot="header" class="card-header">
          <span>会员折扣</span>
        </div>
        <el-row :gutter="24">
          <el-col :xs="24" :lg="12">
            <el-form-item label="启用会员折扣" prop="memberDiscountEnabled">
              <el-switch v-model="form.memberDiscountEnabled" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="银卡折扣率" prop="silverDiscountRate">
              <el-input-number
                v-model="form.silverDiscountRate"
                :min="0"
                :max="1"
                :precision="2"
                :step="0.01"
                :disabled="!form.memberDiscountEnabled"
                controls-position="right"
                style="width: 220px"
              />
              <span class="form-tip">0.05 = 优惠 5%</span>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="金卡折扣率" prop="goldDiscountRate">
              <el-input-number
                v-model="form.goldDiscountRate"
                :min="0"
                :max="1"
                :precision="2"
                :step="0.01"
                :disabled="!form.memberDiscountEnabled"
                controls-position="right"
                style="width: 220px"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="铂金折扣率" prop="platinumDiscountRate">
              <el-input-number
                v-model="form.platinumDiscountRate"
                :min="0"
                :max="1"
                :precision="2"
                :step="0.01"
                :disabled="!form.memberDiscountEnabled"
                controls-position="right"
                style="width: 220px"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card shadow="never" class="settings-card">
        <div slot="header" class="card-header">
          <span>支付与业务开关</span>
          <el-button type="text" @click="$router.push('/system/dict')">去字典管理</el-button>
        </div>
        <el-row :gutter="24">
          <el-col :xs="24" :lg="12">
            <el-form-item label="默认支付渠道" prop="defaultPaymentChannel">
              <el-select v-model="form.defaultPaymentChannel" placeholder="请选择默认支付渠道" style="width: 220px">
                <el-option
                  v-for="item in dict.type.parking_payment_channel"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="启用现金支付" prop="cashEnabled">
              <el-switch v-model="form.cashEnabled" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-form-item label="取消后自动退款" prop="cancelAutoRefundEnabled">
              <el-switch v-model="form.cancelAutoRefundEnabled" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <div class="dialog-footer">
        <el-button
          type="primary"
          :loading="submitting"
          @click="submitForm"
          v-hasPermi="['parking:settings:edit']"
        >
          保存设置
        </el-button>
        <el-button @click="loadSettings">重新加载</el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { getParkingSettings, updateParkingSettings } from '@/api/parking/settings'

export default {
  name: 'ParkingSettings',
  dicts: ['parking_payment_channel'],
  data() {
    return {
      loading: false,
      submitting: false,
      form: this.getDefaultForm(),
      rules: {
        monthlyPrice: [
          { required: true, message: '请输入月卡全局价格', trigger: 'blur' }
        ],
        tempHourPrice: [
          { required: true, message: '请输入临停小时单价', trigger: 'blur' }
        ],
        tempFreeMinutes: [
          { required: true, message: '请输入免费分钟数', trigger: 'blur' }
        ],
        tempBillingStepMinutes: [
          { required: true, message: '请输入计费步长', trigger: 'blur' }
        ],
        tempDailyCapAmount: [
          { required: true, message: '请输入临停封顶金额', trigger: 'blur' }
        ],
        silverDiscountRate: [
          { required: true, message: '请输入银卡折扣率', trigger: 'blur' }
        ],
        goldDiscountRate: [
          { required: true, message: '请输入金卡折扣率', trigger: 'blur' }
        ],
        platinumDiscountRate: [
          { required: true, message: '请输入铂金折扣率', trigger: 'blur' }
        ],
        defaultPaymentChannel: [
          { required: true, message: '请选择默认支付渠道', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.loadSettings()
  },
  methods: {
    getDefaultForm() {
      return {
        monthlyPrice: 0,
        tempHourPrice: 0,
        tempFreeMinutes: 0,
        tempBillingStepMinutes: 30,
        tempRoundUpEnabled: true,
        tempDailyCapAmount: 0,
        memberDiscountEnabled: true,
        silverDiscountRate: 0.05,
        goldDiscountRate: 0.10,
        platinumDiscountRate: 0.15,
        defaultPaymentChannel: undefined,
        cashEnabled: true,
        cancelAutoRefundEnabled: false
      }
    },
    loadSettings() {
      this.loading = true
      getParkingSettings().then(response => {
        const data = response.data || {}
        const pricing = data.pricing || {}
        const memberDiscount = data.memberDiscount || {}
        const payment = data.payment || {}
        const switches = data.switches || {}
        this.form = {
          monthlyPrice: pricing.monthlyPrice ?? 0,
          tempHourPrice: pricing.tempHourPrice ?? 0,
          tempFreeMinutes: pricing.tempFreeMinutes ?? 0,
          tempBillingStepMinutes: pricing.tempBillingStepMinutes ?? 30,
          tempRoundUpEnabled: pricing.tempRoundUpEnabled ?? true,
          tempDailyCapAmount: pricing.tempDailyCapAmount ?? 0,
          memberDiscountEnabled: memberDiscount.memberDiscountEnabled ?? true,
          silverDiscountRate: memberDiscount.silverRate ?? 0.05,
          goldDiscountRate: memberDiscount.goldRate ?? 0.10,
          platinumDiscountRate: memberDiscount.platinumRate ?? 0.15,
          defaultPaymentChannel: payment.defaultChannel,
          cashEnabled: payment.cashEnabled ?? true,
          cancelAutoRefundEnabled: switches.cancelAutoRefundEnabled ?? false
        }
      }).finally(() => {
        this.loading = false
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        this.submitting = true
        updateParkingSettings(this.form).then(() => {
          this.$modal.msgSuccess('保存成功')
          this.loadSettings()
        }).finally(() => {
          this.submitting = false
        })
      })
    }
  }
}
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}

.settings-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
