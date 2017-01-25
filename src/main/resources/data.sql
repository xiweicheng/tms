/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50614
Source Host           : localhost:3306
Source Database       : lhjz

Target Server Type    : MYSQL
Target Server Version : 50614
File Encoding         : 65001

Date: 2015-04-08 20:05:57
*/

SET FOREIGN_KEY_CHECKS=0;
SET NAMES 'utf8';

-- ----------------------------
-- Table structure for authorities
-- ----------------------------
--DROP TABLE IF EXISTS `authorities`;
--CREATE TABLE `authorities` (
--  `username` varchar(50) NOT NULL,
--  `authority` varchar(50) NOT NULL,
--  UNIQUE KEY `ix_auth_username` (`username`,`authority`),
--  CONSTRAINT `fk_authorities_users` FOREIGN KEY (`username`) REFERENCES `users` (`username`)
--) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of authorities
-- ----------------------------
INSERT INTO `authorities` (username, authority) VALUES ('super', 'ROLE_SUPER');
INSERT INTO `authorities` (username, authority) VALUES ('super', 'ROLE_ADMIN');
INSERT INTO `authorities` (username, authority) VALUES ('super', 'ROLE_USER');
INSERT INTO `authorities` (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO `authorities` (username, authority) VALUES ('admin', 'ROLE_USER');
INSERT INTO `authorities` (username, authority) VALUES ('user', 'ROLE_USER');

-- ----------------------------
-- Table structure for groups
-- ----------------------------
--DROP TABLE IF EXISTS `groups`;
--CREATE TABLE `groups` (
--  `id` bigint(20) NOT NULL AUTO_INCREMENT,
--  `group_name` varchar(50) NOT NULL,
--  PRIMARY KEY (`id`)
--) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of groups
-- ----------------------------

-- ----------------------------
-- Table structure for group_authorities
-- ----------------------------
--DROP TABLE IF EXISTS `group_authorities`;
--CREATE TABLE `group_authorities` (
--  `group_id` bigint(20) NOT NULL,
--  `authority` varchar(50) NOT NULL,
--  KEY `fk_group_authorities_group` (`group_id`),
--  CONSTRAINT `fk_group_authorities_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`)
--) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of group_authorities
-- ----------------------------

-- ----------------------------
-- Table structure for group_members
-- ----------------------------
--DROP TABLE IF EXISTS `group_members`;
--CREATE TABLE `group_members` (
--  `id` bigint(20) NOT NULL AUTO_INCREMENT,
--  `username` varchar(50) NOT NULL,
--  `group_id` bigint(20) NOT NULL,
--  PRIMARY KEY (`id`),
--  KEY `fk_group_members_group` (`group_id`),
--  CONSTRAINT `fk_group_members_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`)
--) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of group_members
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
--DROP TABLE IF EXISTS `users`;
--CREATE TABLE `users` (
--  `username` varchar(50) NOT NULL,
--  `password` varchar(255) NOT NULL,
--  `enabled` tinyint(1) NOT NULL,
--  PRIMARY KEY (`username`)
--) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of users (default password: 88888888)
-- ----------------------------
INSERT INTO `users` (username, password, enabled, mails, status, create_date, login_count, version) VALUES ('super', '$2a$10$gVkJuJuU0.yWP4GQq3745OYvoxav9zGcDYlDuk0lxnLASGaeDGtDW', '', 'super@tms.com', 'Bultin', '2015-04-25 10:01:51', '0', '0');
INSERT INTO `users` (username, password, enabled, mails, status, create_date, login_count, version) VALUES ('admin', '$2a$10$gVkJuJuU0.yWP4GQq3745OYvoxav9zGcDYlDuk0lxnLASGaeDGtDW', '', 'admin@tms.com', 'Normal', '2015-04-25 10:01:51', '0', '0');
INSERT INTO `users` (username, password, enabled, mails, status, create_date, login_count, version) VALUES ('user', '$2a$10$gVkJuJuU0.yWP4GQq3745OYvoxav9zGcDYlDuk0lxnLASGaeDGtDW', '', 'user@tms.com', 'Normal', '2015-04-25 10:01:51', '0', '0');

-- ----------------------------
-- Table structure for persistent_logins
-- ----------------------------
--DROP TABLE IF EXISTS `persistent_logins`;
--CREATE TABLE `persistent_logins` (
--  `series` varchar(64) NOT NULL,
--  `last_used` datetime NOT NULL,
--  `token` varchar(64) NOT NULL,
--  `username` varchar(64) NOT NULL,
--  PRIMARY KEY (`series`)
--) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of persistent_logins

-- ----------------------------
-- Records of file
-- ----------------------------

-- ----------------------------
-- Records of project
-- ----------------------------
INSERT INTO `project` (`id`, `create_date`, `creator`, `description`, `name`, `status`, `version`, `language_id`) VALUES ('1', '2016-05-17 15:55:49', 'admin', '团队协作', 'STEP', 'Normal', '0', '1');
INSERT INTO `project` (`id`, `create_date`, `creator`, `description`, `name`, `status`, `version`, `language_id`) VALUES ('2', '2016-05-17 15:56:46', 'admin', '新致云主站', 'WORK', 'Normal', '0', '1');
INSERT INTO `project` (`id`, `create_date`, `creator`, `description`, `name`, `status`, `version`, `language_id`) VALUES ('3', '2016-05-17 15:57:54', 'admin', '管理控制台', 'CONSOLE', 'Normal', '0', '1');


-- ----------------------------
-- Records of language
-- ----------------------------
INSERT INTO `language` (`id`, `create_date`, `creator`, `description`, `name`, `status`, `version`) VALUES ('1', '2016-05-17 15:59:25', 'admin', '中文', 'zh', 'Normal', '0');
INSERT INTO `language` (`id`, `create_date`, `creator`, `description`, `name`, `status`, `version`) VALUES ('2', '2016-05-17 16:00:30', 'admin', '日语', 'jp', 'Normal', '0');
INSERT INTO `language` (`id`, `create_date`, `creator`, `description`, `name`, `status`, `version`) VALUES ('3', '2016-05-17 16:00:30', 'admin', '英语', 'en', 'Normal', '0');


-- ----------------------------
-- Records of language_project
-- ----------------------------
INSERT INTO `language_project` (`language_id`, `project_id`) VALUES ('1', '1');
INSERT INTO `language_project` (`language_id`, `project_id`) VALUES ('1', '2');
INSERT INTO `language_project` (`language_id`, `project_id`) VALUES ('1', '3');
INSERT INTO `language_project` (`language_id`, `project_id`) VALUES ('2', '1');
INSERT INTO `language_project` (`language_id`, `project_id`) VALUES ('2', '2');
INSERT INTO `language_project` (`language_id`, `project_id`) VALUES ('2', '3');

-- ----------------------------
-- Records of watcher_project
-- ----------------------------
INSERT INTO `watcher_project` (`user_id`, `project_id`) VALUES ('super', '1');
INSERT INTO `watcher_project` (`user_id`, `project_id`) VALUES ('super', '2');
INSERT INTO `watcher_project` (`user_id`, `project_id`) VALUES ('super', '3');
INSERT INTO `watcher_project` (`user_id`, `project_id`) VALUES ('admin', '1');
INSERT INTO `watcher_project` (`user_id`, `project_id`) VALUES ('admin', '2');
INSERT INTO `watcher_project` (`user_id`, `project_id`) VALUES ('admin', '3');
