export interface PageDef {
  route: string;
  titleText?: string;
  loadedSelector: string;
  name: string;
  isForm?: boolean;
}

export const PAGES: Record<string, PageDef> = {
  overview: {
    route: '/parking',
    name: '工作台',
    loadedSelector: '.parking-overview .stat-card',
  },
  lot: {
    route: '/archives/lot',
    name: '停车场管理',
    titleText: '停车场管理',
    loadedSelector: '.app-container .el-table',
  },
  space: {
    route: '/archives/space',
    name: '车位管理',
    titleText: '车位管理',
    loadedSelector: '.app-container .el-table',
  },
  lotadmin: {
    route: '/archives/lotadmin',
    name: '管理员绑定',
    titleText: '停车场管理员绑定',
    loadedSelector: '.app-container .el-table',
  },
  settings: {
    route: '/archives/settings',
    name: '停车设置',
    titleText: '停车设置',
    loadedSelector: '.app-container .el-form',
  },
  temp: {
    route: '/operations/temp',
    name: '临停订单',
    titleText: '临停订单管理',
    loadedSelector: '.app-container .el-table',
  },
  tempForm: {
    route: '/parking/temp/form',
    name: '临停订单表单',
    isForm: true,
    loadedSelector: '.el-page-header',
  },
  monthly: {
    route: '/operations/monthly',
    name: '月卡订单',
    titleText: '月卡订单管理',
    loadedSelector: '.app-container .el-table',
  },
  monthlyForm: {
    route: '/parking/monthly/form',
    name: '月卡订单表单',
    isForm: true,
    loadedSelector: '.el-page-header',
  },
  membership: {
    route: '/operations/membership',
    name: '会员订单',
    titleText: '会员订单管理',
    loadedSelector: '.app-container .el-table',
  },
  membershipForm: {
    route: '/parking/membership/form',
    name: '会员订单表单',
    isForm: true,
    loadedSelector: '.el-page-header',
  },
  payment: {
    route: '/operations/payment',
    name: '支付流水',
    titleText: '支付流水',
    loadedSelector: '.app-container .el-table',
  },
  paymentForm: {
    route: '/parking/payment/form',
    name: '支付表单',
    isForm: true,
    loadedSelector: '.el-page-header',
  },
  customer: {
    route: '/customers/customer',
    name: '客户档案',
    titleText: '客户档案',
    loadedSelector: '.app-container .el-table',
  },
  vehicle: {
    route: '/customers/vehicle',
    name: '用户车辆',
    titleText: '用户车辆管理',
    loadedSelector: '.app-container .el-table',
  },
};
