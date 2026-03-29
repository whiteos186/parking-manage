-- ----------------------------
-- Parking menu bootstrap
-- ----------------------------

-- Safe for repeated local bootstrap: remove parking menu rows owned by this script first.
delete from sys_menu where menu_id in (2102, 2101, 2100);

-- Top-level parking directory.
insert into sys_menu values (
  2100, '停车管理', 0, 6, 'parking', null, '', '',
  1, 0, 'M', '0', '0', '', 'car',
  'admin', sysdate(), '', null, 'Parking module directory'
);

-- Parking landing page (existing placeholder page in ruoyi-ui/src/views/parking/index.vue).
insert into sys_menu values (
  2101, '停车概览', 2100, 1, 'index', 'parking/index', '', '',
  1, 0, 'C', '0', '0', 'parking:overview:list', 'dashboard',
  'admin', sysdate(), '', null, 'Parking landing page'
);

-- Function/button permission for page query action.
insert into sys_menu values (
  2102, '停车概览查询', 2101, 1, '', '', '', '',
  1, 0, 'F', '0', '0', 'parking:overview:query', '#',
  'admin', sysdate(), '', null, 'Parking overview query permission'
);
