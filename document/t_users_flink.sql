/*
 Navicat Premium Data Transfer

 Source Server         : 本地测试
 Source Server Type    : MySQL
 Source Server Version : 80018
 Source Host           : localhost:3306
 Source Schema         : test_flink

 Target Server Type    : MySQL
 Target Server Version : 80018
 File Encoding         : 65001

 Date: 03/03/2020 13:44:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_users_flink
-- ----------------------------
DROP TABLE IF EXISTS `t_users_flink`;
CREATE TABLE `t_users_flink`  (
  `ID` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键ID',
  `USERNAME` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `PASSWORD` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `TRUENAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `IDCARDNO` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `ISVALID` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否有效',
  `UPDATETIME` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `CREATETIME` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_users_flink
-- ----------------------------
INSERT INTO `t_users_flink` VALUES ('1', 'admin', 'admin', '管理员', '123456', '1', '2020-02-07 15:59:53', '2020-02-07 15:59:56');
INSERT INTO `t_users_flink` VALUES ('3', '222', '222', '222', '222', '1', '2020-02-27 09:11:50', '2020-02-27 09:11:53');
INSERT INTO `t_users_flink` VALUES ('4', '333', '333', '333', '333', '0', '2020-02-27 09:12:05', '2020-02-27 09:12:08');
INSERT INTO `t_users_flink` VALUES ('5', '444', '444', '444', '444', '0', '2020-02-27 09:12:31', '2020-02-27 09:12:35');

SET FOREIGN_KEY_CHECKS = 1;
