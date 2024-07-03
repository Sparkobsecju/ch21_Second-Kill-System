/* `seckill_item` 資料表的結構 */
DROP TABLE IF EXISTS `seckill_item`;
CREATE TABLE `seckill_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(50) NOT NULL,
  `price` float(6,2) NOT NULL COMMENT '原價',
  `seckill_price` float(6,2) NOT NULL COMMENT '優惠價格',
  `inventory` int(11) NOT NULL COMMENT '庫存',
  `img` varchar(250) DEFAULT NULL COMMENT '商品圖片',
	`start_time` datetime DEFAULT NULL COMMENT '優惠開始時間',
	`end_time` datetime DEFAULT NULL COMMENT '優惠結束時間',
	`create_time` datetime NOT NULL DEFAULT NOW(0) COMMENT '商品創建時間',
  PRIMARY KEY (`id`),
  KEY `INDEX_TITLE` (`title`),
  INDEX `INDEX_START_TIME`(`start_time`),
  INDEX `INDEX_END_TIME`(`end_time`)
) ENGINE=InnoDB;

/* `seckill_order` 資料表的結構 */
DROP TABLE IF EXISTS `seckill_order`;
CREATE TABLE `seckill_order` (
  `item_id` int(11) NOT NULL COMMENT '商品ID',
  `mobile` varchar(20) NOT NULL COMMENT '用戶手機號',
  `money` float(6,2) NULL COMMENT '支付金額',
	`create_time` datetime(3) DEFAULT NOW(3) COMMENT '訂單創建時間',
	`payment_time` datetime(3) DEFAULT NULL ON UPDATE NOW(3) COMMENT '訂單支付時間',
	`state` tinyint NOT NULL DEFAULT 0 COMMENT '訂單狀態：-1無效，0未支付，1已支付',
  PRIMARY KEY (`item_id`, `mobile`)
) ENGINE=InnoDB;

