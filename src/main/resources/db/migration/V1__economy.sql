CREATE TABLE IF NOT EXISTS `balance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `account_id` varchar(128) NOT NULL,
  `name` varchar(128) NOT NULL,
  `balance` decimal(10,2) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `account_id` (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;