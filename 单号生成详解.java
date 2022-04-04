LAST_INSERT_ID函数：
该函数有两种形式：LAST_INSERT_ID(), LAST_INSERT_ID(expr)。
无参的形式会返回最近一次执行INSERT语句时auto_increment的值；带expr的形式会返回表达式的值，并且该值会被记住，在下一次调用LAST_INSERT_ID()时也返回该值。
CREATE TABLE user (

id INT AUTO_INCREMENT PRIMARY KEY,

name VARCHAR(50) NOT NULL

);
INSERT INTO user(name) VALUES('张三');
INSERT INTO user(name) VALUES('李四');
无参函数的返回值：
SELECT LAST_INSERT_ID();此时得到的结果是2。
注意：如果是一条语句插入多条值，则返回的是插入第一条时自动生成的ID，而不是最后一条的。

最终我们生成ID是通过：
CREATE TABLE `sys_sequence` (
  `NAME` varchar(50) NOT NULL,
  `CURRENT_VALUE` bigint(20) NOT NULL,
  `END_VALUE` bigint(20) NOT NULL,
  PRIMARY KEY (`NAME`,`CURRENT_VALUE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
我们的END_VALUE设置的是：999 9999L（7个9）
在获取段id的时候：
UPDATE SYS_SEQUENCE
			SET CURRENT_VALUE = LAST_INSERT_ID(CURRENT_VALUE + 1)
			WHERE
				NAME = 'NEW_OMS_ORDERID_SEQ'
			AND CURRENT_VALUE < END_VALUE
			ORDER BY
				CURRENT_VALUE ASC
			LIMIT 1
SELECT LAST_INSERT_ID();

获取到CURRENT_VALUE后，进行拼接处理
seq=String.valueOf(getNextLongSequence());
private String createNextOmsOrderId(String seq) {
        StringBuilder sb = new StringBuilder();
        sb.append("000 0000 000").append(seq);
		//在最前面插入两个0，表示正向订单。那么就获得了12位的订单号。订单行号是在后面加数字，第一行，orderid+01。最多不能是99
        sb.delete(0, sb.length() - 10).insert(0, "00");
        return sb.toString();
}


平台单号和平台行号生成（这里我们以生产环境为例进行解析，比较复杂）,${sg_orderid}是通过会员号算出的业务库的表号拼接，sg_orderid+（routeNum-1），
就是表名字，主要这张表是维护在业务库的，routeNum就是路由号，（会员号取模4096然后+1），不够四位前面补0，ip对应就是jboss的IP：
核心sql:
DELETE FROM ${sg_orderid} WHERE ROUTE_NUMBER = :routeNum AND IP = :ip
INSERT INTO ${tableName} (IP, ROUTE_NUMBER) VALUES (:ip, :routeNum)
SELECT ROW_ID FROM ${tableName} WHERE ROUTE_NUMBER = :routeNum AND IP = :ip
表结构：
CREATE TABLE `sg_orderid_0255` (
  `ROW_ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增序列',
  `IP` varchar(50) NOT NULL COMMENT '应用IP',
  `ROUTE_NUMBER` varchar(4) NOT NULL COMMENT '路由号',
  PRIMARY KEY (`ROW_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=100003128 DEFAULT CHARSET=utf8mb4 COMMENT='平台号生成序列表'
注意在生产环境下每张业务表会对应6条记录。就是那6个jboss
在生产环境下还要维护一个序列化生成器的集合：
private static Map<String ,OrderIdsSeqGenerator> shardOrderIdsSeqGeneratorMap = new HashMap<>();
OrderIdsSeqGenerator是一个完整的序列化生成器，这个map在生产环境下应该有64个key。比如说0号unit,map对应的key是：0,64,128，……4032。总共64个
首先可以肯定的是能路由到1号unit里面来，会员号取模4096肯定属于（0,64,128，……4032）其中之一，如果这个时候在一个unit中维护一张公共表，来生成序列号，
就会产生浪费，为什么：看平台号的组成
2（固定，系统编号）+1/0(机房号，江北还是雨花)+10位数据库（靠${sg_orderid}业务表自增来维持，不够10位前面补0）*100（系统缓存维持100个）+（会员号取模4096然后+1)
如果维护在公共库中，那么这10位的自增序列。会被64个尾数不同的组会员公用，但是组会员之间的后四位本来就不同，所以这样不浪费了剩下63份数据，
所以应该把后四位相同的维护放到公用一个递增id上面来，这样数据就扩大64倍。
同时我在每一个jboss中都维护一个相同后四位的递增序列，不同后四位（总共64个）分别维护，所以上面的map中总64个key.
在每张业务表中，${sg_orderid}会有6条记录。分别对应那6台jboss在内存中维护的10位自增id.且这6条记录的自增id是连续的






