LAST_INSERT_ID������
�ú�����������ʽ��LAST_INSERT_ID(), LAST_INSERT_ID(expr)��
�޲ε���ʽ�᷵�����һ��ִ��INSERT���ʱauto_increment��ֵ����expr����ʽ�᷵�ر��ʽ��ֵ�����Ҹ�ֵ�ᱻ��ס������һ�ε���LAST_INSERT_ID()ʱҲ���ظ�ֵ��
CREATE TABLE user (

id INT AUTO_INCREMENT PRIMARY KEY,

name VARCHAR(50) NOT NULL

);
INSERT INTO user(name) VALUES('����');
INSERT INTO user(name) VALUES('����');
�޲κ����ķ���ֵ��
SELECT LAST_INSERT_ID();��ʱ�õ��Ľ����2��
ע�⣺�����һ�����������ֵ���򷵻ص��ǲ����һ��ʱ�Զ����ɵ�ID�����������һ���ġ�

������������ID��ͨ����
CREATE TABLE `sys_sequence` (
  `NAME` varchar(50) NOT NULL,
  `CURRENT_VALUE` bigint(20) NOT NULL,
  `END_VALUE` bigint(20) NOT NULL,
  PRIMARY KEY (`NAME`,`CURRENT_VALUE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
���ǵ�END_VALUE���õ��ǣ�999 9999L��7��9��
�ڻ�ȡ��id��ʱ��
UPDATE SYS_SEQUENCE
			SET CURRENT_VALUE = LAST_INSERT_ID(CURRENT_VALUE + 1)
			WHERE
				NAME = 'NEW_OMS_ORDERID_SEQ'
			AND CURRENT_VALUE < END_VALUE
			ORDER BY
				CURRENT_VALUE ASC
			LIMIT 1
SELECT LAST_INSERT_ID();

��ȡ��CURRENT_VALUE�󣬽���ƴ�Ӵ���
seq=String.valueOf(getNextLongSequence());
private String createNextOmsOrderId(String seq) {
        StringBuilder sb = new StringBuilder();
        sb.append("000 0000 000").append(seq);
		//����ǰ���������0����ʾ���򶩵�����ô�ͻ����12λ�Ķ����š������к����ں�������֣���һ�У�orderid+01����಻����99
        sb.delete(0, sb.length() - 10).insert(0, "00");
        return sb.toString();
}


ƽ̨���ź�ƽ̨�к����ɣ�������������������Ϊ�����н������Ƚϸ��ӣ�,${sg_orderid}��ͨ����Ա�������ҵ���ı��ƴ�ӣ�sg_orderid+��routeNum-1����
���Ǳ����֣���Ҫ���ű���ά����ҵ���ģ�routeNum����·�ɺţ�����Ա��ȡģ4096Ȼ��+1����������λǰ�油0��ip��Ӧ����jboss��IP��
����sql:
DELETE FROM ${sg_orderid} WHERE ROUTE_NUMBER = :routeNum AND IP = :ip
INSERT INTO ${tableName} (IP, ROUTE_NUMBER) VALUES (:ip, :routeNum)
SELECT ROW_ID FROM ${tableName} WHERE ROUTE_NUMBER = :routeNum AND IP = :ip
��ṹ��
CREATE TABLE `sg_orderid_0255` (
  `ROW_ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '��������',
  `IP` varchar(50) NOT NULL COMMENT 'Ӧ��IP',
  `ROUTE_NUMBER` varchar(4) NOT NULL COMMENT '·�ɺ�',
  PRIMARY KEY (`ROW_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=100003128 DEFAULT CHARSET=utf8mb4 COMMENT='ƽ̨���������б�'
ע��������������ÿ��ҵ�����Ӧ6����¼��������6��jboss
�����������»�Ҫά��һ�����л��������ļ��ϣ�
private static Map<String ,OrderIdsSeqGenerator> shardOrderIdsSeqGeneratorMap = new HashMap<>();
OrderIdsSeqGenerator��һ�����������л������������map������������Ӧ����64��key������˵0��unit,map��Ӧ��key�ǣ�0,64,128������4032���ܹ�64��
���ȿ��Կ϶�������·�ɵ�1��unit����������Ա��ȡģ4096�϶����ڣ�0,64,128������4032������֮һ��������ʱ����һ��unit��ά��һ�Ź��������������кţ�
�ͻ�����˷ѣ�Ϊʲô����ƽ̨�ŵ����
2���̶���ϵͳ��ţ�+1/0(�����ţ����������껨)+10λ���ݿ⣨��${sg_orderid}ҵ���������ά�֣�����10λǰ�油0��*100��ϵͳ����ά��100����+����Ա��ȡģ4096Ȼ��+1)
���ά���ڹ������У���ô��10λ���������С��ᱻ64��β����ͬ�����Ա���ã��������Ա֮��ĺ���λ�����Ͳ�ͬ�������������˷���ʣ��63�����ݣ�
����Ӧ�ðѺ���λ��ͬ��ά���ŵ�����һ������id���������������ݾ�����64����
ͬʱ����ÿһ��jboss�ж�ά��һ����ͬ����λ�ĵ������У���ͬ����λ���ܹ�64�����ֱ�ά�������������map����64��key.
��ÿ��ҵ����У�${sg_orderid}����6����¼���ֱ��Ӧ��6̨jboss���ڴ���ά����10λ����id.����6����¼������id��������






