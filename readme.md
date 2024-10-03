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

### 1.3 容量预估

```sql
create table t_user
(
    user_id     bigint   default -1                not null comment '用户id'
        primary key,
    nick_name   varchar(35)                        null comment '昵称',
    avatar      varchar(255)                       null comment '头像',
    true_name   varchar(20)                        null comment '真实姓名',
    sex         tinyint(1)                         null comment '性别 0男，1女',
    born_date   datetime                           null comment '出生时间',
    work_city   int                                null comment '工作地',
    born_city   int                                null comment '出生地',
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);
```

单行记录占用大小：8+35+255+20+1+8+4+4+8+8=351b.

考虑冗余：（351 + 100） * 1亿 大约45GB

考虑索引：450GB

假设以后还有分表：450 * 5 = 2250GB.



采用sql做容量预估:

```sql
select table_schema                            as '数据库',
       table_name                              as '表名',
       table_rows                              as '记录数',
       truncate(data_length / 1024 / 1024, 2)  as '数据容量(MB)',
       truncate(index_length / 1024 / 1024, 2) as '索引容量(MB)'
from information_schema.tables
where table_schema = 'qiyu_live_user'
order by data_length desc, index_length desc;
```

### 1.4 ShardingJDBC

在ShardingJDBC中，分片路由是将查询请求根据配置好的分片策略映射到对应的数据库表中的过程。ShardingJDBC提供了多种不同的路由策略，下面详细介绍几种常见的路由方式，并通过例子进行说明。

1. **直接路由**

直接路由适用于明确指定目标库和表的场景，通常是在SQL中直接指定了表名或通过强制路由规则确定。ShardingJDBC不会对表进行分片计算，而是直接将请求路由到目标表。

**例子：**

```sql
SELECT * FROM user_0001 WHERE user_id = 123;
```
在这个例子中，`user_0001`表已经在SQL中明确指定，因此ShardingJDBC会直接路由到`user_0001`表，而不会根据`user_id`进行分片计算。

2. **标准路由**

标准路由是ShardingJDBC最常见的路由方式，通常基于分片键（如主键、订单ID等）来确定目标分片表。标准路由依赖分片算法，根据SQL中的条件计算目标库或表。

**例子：**
```sql
SELECT * FROM user WHERE user_id = 123;
```
假设我们根据`user_id`字段进行分片，有4个表`user_0000`, `user_0001`, `user_0002`, `user_0003`。通过分片算法（如`user_id % 4`），`user_id = 123`会被路由到`user_0003`表。

3. **笛卡尔积路由**

笛卡尔积路由是针对多个分片条件的复杂查询。ShardingJDBC会分别计算每个条件的目标库和表，然后对所有可能的组合进行计算，产生一个笛卡尔积。这种路由方式会带来较多的查询表组合，适用于较复杂的查询条件。

**例子：**

```sql
SELECT * FROM order WHERE user_id = 123 AND order_id = 456;
```
假设`user_id`分片在`user_0000`到`user_0003`表中，`order_id`分片在`order_0000`到`order_0003`表中，ShardingJDBC会计算`user_id`对应的表与`order_id`对应的表，然后生成可能的表组合，形成笛卡尔积。

4. **广播路由**

广播路由是指ShardingJDBC会将查询或操作广播到所有的数据库或表中，适用于全库或全表查询的场景。

**全库路由**

将SQL请求广播到每个数据库中的指定表。

**例子：**
```sql
SELECT * FROM config;
```
假设`config`表存在于每一个数据库中，ShardingJDBC会将该查询路由到所有数据库的`config`表，返回所有结果。

**全库表路由**

将SQL请求广播到所有数据库中的所有分片表。

**例子：**
```sql
SELECT * FROM user;
```
假设`user`表被分片到多个库，每个库有多个分片表，ShardingJDBC会将查询请求发送到所有数据库的所有分片表中，汇总返回结果。

**全实例路由**

全实例路由是针对每个数据库实例的操作。对于系统管理类的SQL语句，如`SHOW VARIABLES`，可能会在所有数据库实例上执行。

**例子：**

```sql
SHOW VARIABLES;
```
ShardingJDBC会将该命令发送到每个数据库实例，获取所有实例的结果。

**单播路由**

单播路由只向单个分片数据库或表发送SQL请求，通常用于通过分片键明确定位到唯一的目标表的情况。

**例子：**
```sql
SELECT * FROM user WHERE user_id = 1000;
```
假设根据`user_id`分片规则，`user_id = 1000`会路由到`user_0002`表，这时ShardingJDBC只会发送请求到`user_0002`表，而不会查询其他表。

**阻断路由**

阻断路由通常在查询结果不需要访问任何实际表时发生。比如SQL中通过常量条件或不需要访问数据的查询，ShardingJDBC会提前拦截不必要的请求，不会执行SQL。

**例子：**
```sql
SELECT 1;
```
这是一个常量查询，不涉及任何实际表，ShardingJDBC会直接返回结果`1`，不执行数据库操作。
