# qiyu-live-app

## 1. 用户数据存储剖析

### 1.1 字段分析

1. 读多写少的属性
   `userId`, `nickName`, `avatar`, `sex`, `phone`
2. 读多写多的属性
   `lastActiveTime`, `userFlag`

采用动静分离的设计方式，做到冷热字段分离。



### 1.2 分库分表

分表：所有分表存放在一个数据库中，可以连表查询，可以有事务操作，但是数据库连接有限。

分库：所有分表存放在不同的数据库中。数据库连接充足，不能做连表查询，跨数据库的事务操作完成不了（引入分布式事务升级了复杂度）。





