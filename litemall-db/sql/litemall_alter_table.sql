ALTER TABLE `litemall`.`litemall_user` ADD COLUMN `secret` VARCHAR(100) NULL COMMENT 'etm secret' AFTER `session_key`, ADD COLUMN `address` VARCHAR(100) NULL COMMENT 'etm address' AFTER `secret`; 
ALTER TABLE `litemall`.`litemall_user` ADD COLUMN `pay_password` VARCHAR(65) DEFAULT '' NULL COMMENT '支付密码' AFTER `password`, ADD COLUMN `email` VARCHAR(65) DEFAULT '' NULL COMMENT '电子邮件' AFTER `pay_password`;

CREATE TABLE `litemall_draw` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) NOT NULL,
  `secret` varchar(128) NOT NULL,
  `amount` varchar(64) DEFAULT NULL,
  `address` varchar(128) NOT NULL,
  `transactionId1` varchar(128) DEFAULT NULL,
  `transactionId2` varchar(128) DEFAULT NULL,
  `transactionId3` varchar(128) DEFAULT NULL,
  `add_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
