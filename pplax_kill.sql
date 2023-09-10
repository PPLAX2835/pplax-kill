drop database if exists pplax_kill;

create database pplax_kill;

use pplax_kill;

/*
Navicat MySQL Data Transfer

Source Server         : localhost-mysql-pplax_kill
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : pplax_kill

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2019-02-24 22:08:10
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order` (
  `kill_id` bigint(20) NOT NULL COMMENT '秒杀商品id',
  `user_phone` bigint(20) NOT NULL COMMENT '用户手机号',
  `state` tinyint(4) NOT NULL DEFAULT '-1' COMMENT '状态标示:-1:无效 0:成功 1:已付款 2:已发货',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`kill_id`,`user_phone`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';



/*
Navicat MySQL Data Transfer

Source Server         : localhost-mysql-pplax_kill
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : pplax_kill

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2019-02-24 21:50:58
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for pplax_kill
-- ----------------------------
DROP TABLE IF EXISTS `pplax_kill`;
CREATE TABLE `pplax_kill` (
  `kill_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  `name` varchar(120) CHARACTER SET utf8 NOT NULL COMMENT '商品名称',
  `inventory` int(11) NOT NULL COMMENT '库存数量',
  `start_time` datetime NOT NULL COMMENT '秒杀开启时间',
  `end_time` datetime NOT NULL COMMENT '秒杀结束时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `version` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`kill_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1004 DEFAULT CHARSET=utf8mb4 COMMENT='秒杀库存表';

-- ----------------------------
-- Records of pplaxkill
-- ----------------------------
INSERT INTO `pplax_kill` VALUES ('1000', '商品0', '119990', '2019-01-17 14:29:10', '2019-12-20 00:00:00', '2019-01-13 21:28:31', '11');
INSERT INTO `pplax_kill` VALUES ('1001', '商品1', '188', '2019-01-17 14:29:12', '2019-12-20 00:00:00', '2019-01-13 21:28:31', '12');
INSERT INTO `pplax_kill` VALUES ('1002', '商品2', '289', '2019-01-17 14:29:18', '2019-12-20 00:00:00', '2019-01-13 21:28:31', '11');
INSERT INTO `pplax_kill` VALUES ('1003', '商品3', '394', '2019-01-17 14:29:22', '2019-12-20 00:00:00', '2019-01-13 21:28:31', '6');
