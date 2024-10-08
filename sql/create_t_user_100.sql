create database qiyu_live_user character set utf8mb3 collate utf8_bin;

use qiyu_live_user;

DELIMITER ;
CREATE
    DEFINER = `root`@`%` PROCEDURE `create_t_user_100`()
BEGIN

    DECLARE i INT;
    DECLARE table_name VARCHAR(30);
    DECLARE table_pre VARCHAR(30);
    DECLARE sql_text VARCHAR(3000);
    DECLARE table_body VARCHAR(2000);
    SET i = 0;
    SET table_name = '';

    SET sql_text = '';
    SET table_body = '(
  user_id bigint NOT NULL DEFAULT -1 COMMENT \'用户id\',
  nick_name varchar(35)  DEFAULT NULL COMMENT \'昵称\',
  avatar varchar(255)  DEFAULT NULL COMMENT \'头像\',
  true_name varchar(20)  DEFAULT NULL COMMENT \'真实姓名\',
  sex tinyint(1) DEFAULT NULL COMMENT \'性别 0男，1女\',
  born_date datetime DEFAULT NULL COMMENT \'出生时间\',
  work_city int(9) DEFAULT NULL COMMENT \'工作地\',
  born_city int(9) DEFAULT NULL COMMENT \'出生地\',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;';

    WHILE i < 100
        DO
            IF i < 10 THEN
                SET table_name = CONCAT('t_user_0', i);
            ELSE
                SET table_name = CONCAT('t_user_', i);
            END IF;

            SET sql_text = CONCAT('CREATE TABLE ', table_name, table_body);
            SELECT sql_text;
            SET @sql_text = sql_text;
            PREPARE stmt FROM @sql_text;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            SET i = i + 1;
        END WHILE;


END;;
DELIMITER ;

# 容量预估
select table_schema                            as '数据库',
       table_name                              as '表名',
       table_rows                              as '记录数',
       truncate(data_length / 1024 / 1024, 2)  as '数据容量(MB)',
       truncate(index_length / 1024 / 1024, 2) as '索引容量(MB)'
from information_schema.tables
where table_schema = 'qiyu_live_user'
order by data_length desc, index_length desc;



CREATE TABLE `t_user_tag`
(
    `user_id`     bigint NOT NULL DEFAULT -1 COMMENT '用户id',
    `tag_info_01` bigint NOT NULL DEFAULT '0' COMMENT '标签记录字段',
    `tag_info_02` bigint NOT NULL DEFAULT '0' COMMENT '标签记录字段',
    `tag_info_03` bigint NOT NULL DEFAULT '0' COMMENT '标签记录字段',
    `create_time` datetime        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT ='用户标签记录';