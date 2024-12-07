-- ----------------------------
-- 区域表结构
-- ----------------------------
DROP TABLE IF EXISTS `sys_region`;
CREATE TABLE `sys_region` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `region_code` varchar(15) NOT NULL COMMENT '区域编码',
  `region_name` varchar(100) NOT NULL COMMENT '区域名称',
  `region_level` tinyint(1) NOT NULL COMMENT '区域级别：1->省；2->市；3->区/县；4->乡镇；5->街道/社区',
  `parent_code` varchar(15) DEFAULT NULL COMMENT '父级区域编码',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0->禁用；1->启用',
  `sort` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_code` (`region_code`),
  KEY `idx_parent_code` (`parent_code`),
  KEY `idx_region_level` (`region_level`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='区域表'; 

ALTER TABLE pms_product
ADD COLUMN region_code varchar(15) COMMENT '区域编码';