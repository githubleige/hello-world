�첽��ܵ�����ܹ������⣺
�첽��ܵ�ʹ�ó�����Ҫ�����ڽ��ִ��ʧ�ܵ�����£���ͨ����ͷ����
������AP��ṹ��
CREATE TABLE `ap_status_aims_0004` (
  `ID` varchar(40) NOT NULL COMMENT '���,һ���Ŷ����к�',
  `CATEGORY` varchar(20) NOT NULL COMMENT '����,һ��ID���ж���������Ҫ�ô��ֶ�������',
  `STATUS` int(11) DEFAULT NULL COMMENT '״̬',
  `TIMES` int(11) DEFAULT '0' COMMENT '���ʹ���',
  `MESSAGE` varchar(2048) DEFAULT NULL COMMENT '��Ϣ����',//HashMap<String, String> message = new HashMap();
  `CREATED_BY` varchar(20) DEFAULT NULL COMMENT '������',
  `CREATED_TIME` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '����ʱ��',
  `LAST_UPD_BY` varchar(20) DEFAULT NULL COMMENT '�޸���',
  `LAST_UPD_TIME` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '�޸�ʱ��',
  PRIMARY KEY (`ID`,`CATEGORY`),//����
  KEY `IX_AP_STATUS_AIMS_01` (`CREATED_TIME`) USING BTREE,
  KEY `IX_AP_STATUS_AIMS_02` (`STATUS`,`CREATED_TIME`) USING BTREE,
  KEY `IX_AP_STATUS_AIMS_03` (`CATEGORY`,`STATUS`,`CREATED_TIME`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='�ع�������Դ';
STATUS��һ��ö��״ֵ̬��
public enum IStatusValue {
    REDO_L3(-13),����3��
    REDO_L2(-12),��������
    REDO_L1(-11),����һ��
    REDO(-10),
    DOING(-1),����ִ�У��ѽ�����У�
    TODO(0),��������ʱ���������״̬�����ݣ�
    DONE(1),�ɹ����
    QUIT(2),
    ERROR(3),ҵ��ʧ�ܣ��������ԣ�
    OVER(4),���Դ�������
    WASTE(9);�Ѿ�ִ�гɹ��ܾõ����ݣ���ɾ��
}
��������ע�⣺
@Inherited
@Target(TYPE)
@Retention(RUNTIME)
public @interface AsynchronousProcess {

    //��������AP��
    String table();

    //֧��������һ���첽���Ҫ��ȷ�Լ������ó��������磺category = "WF_APH_OLN_SUPER",�浥���첽��ܣ�
    String category();

    //�������Ե�������(һ�����������Ϊ3)
    int retryTimes() default 0;

    //ע����������Ҫ��Ҫ�ӷֲ�ʽ�����е��첽���ִ��ǰ��Ҫ�ӷֲ�ʽ��������ۿ�棩
    boolean exclusive() default false;

    //�Ƿ�����첽ִ�У�Ĭ�����첽�ģ�
    boolean async() default true;
}
����һ��ע���ǻ���MQʵ�ֵ��첽��ܣ�
���첽��ܷ�Ϊ���֣�һ���ǻ����̳߳ص���������ʵ�ֵģ�����һ���ǻ���mqʵ�ֵġ������ǲ������֣�����������
�ŵ��Լ�������app��Ӧ���̳߳ص����������У��жϣ�pollSize * 1.0D / (double)maxPoolSize > 0.6D��һ��Ҫ���ں����߳�����maxPoolSize�ı�ֵ����ס
�̳߳صĲ������ȷŵ����������У��������з����ˣ�����������߳��������Ե����������ֵ�Ļ������Կ϶����app��Ӧ���첽�����Ѿ��ܶ��ˣ����ʱ��
���ǿ��԰����ŵ�ͬһ��unit�ڲ�����mq�У����app��ʱ��ȡ����������ֹ�̳߳ش���������
@MessageQueue(producer = "omsPrivateJmsTemplate", consumers = "windQConnectionFactory", concurrency = "10-10", queue = "AP_PRIVATE_QUEUE_001")
public @interface MessageQueue {
	//��Ϣ���ж�Ӧ��JmsTemplate���ͻ��˲������ߡ���һ��bean������
    String producer();
	//��Ӧ�ڲ�broker��Ӧ�Ķ�������
    String queue();
	//�����ߣ�����һ�����ӹ��������ӵ���Ӧ���ڲ�broker��windQConnectionFactory+queue�Ϳ���ȷ�����������ѵľ�������
    String[] consumers();
    //�������Ϊ�����Ѷ˲�������һ���߳����������broker���͹�������Ϣ��
    // ���϶��Ǹ�������Ķ������������Ѷ˵Ĵ����̣߳���
    // 2�൱���̳߳��еĺ����̣߳�10�൱�������߳���
    String concurrency() default "2-10";

}
�����̳߳�ʵ�ֵ��첽��ܵĻ���ӿ��ǣ�abstract class ObjectBasedAsynchronousProcessor<E>
����MQʵ�ֵ��첽��ܵĻ���ӿ��ǣ�abstract class MessageQueueAsynchronousProcessor
��ҵ������и�����Ҫ������ʵ�����ֽӿ�
����ob�첽��ܵĵľ������̣�
1������IStatusԪ����
IStatus iStatus = IStatusUtils.newIStatus(oid, orderDetail.getMemInCardNo());
2������ִ��
AP.oneStepProcess("abnormalOrderSyncToPANGUXAsynchronousProcessor", iStatus, orderDTO);
3������oneStepProcess������ע���첽��ܵĻ�����ʵ����BeanNameAware����ӿڵģ����Կ���ͨ��this.beanName��ȡ����ǰִ���첽����������е����ֵģ�
AsynchronousProcessEngine.newInstance(processorName, status).persist().process(order);
�����߼��ǣ�ͨ����ȡ�첽��ܵ�ע����Ի�ȡ�����ı����ͳ�����ͨ�������Ŀ��Ի�ȡ��id,���Դ��
���м�¼��״̬��Ҫ�����첽�����scm�����õĽ�����״̬�����Ϊ������ô״̬��Ͼ��ǣ�TODO(0)��ֱ�ӵ�job����ȡ
������첽���û�н�������ô״̬���� DOING(-1),��������
Ȼ��ʼִ��process(order)�������жϸ��첽��ܵĽ���״̬�����Ϊ��������ӡһ��info��־�ͷ��أ�������ȴ�job����ȡ
Ȼ���ͨ�����첽��ܵ�ע��async���ж������߳�ִ�л��Ƕ����̳߳�����ȥִ�У�һ������µĶ��Ƕ����̳߳�����ȥִ��
ThreadPoolExecutor threadPoolExecutor = ThreadPools.ASYNCHRONOUS_PROCESS_THREAD_POOL;
ThreadPoolUtils.monitor(this.getClass(), threadPoolExecutor);
ͨ��monitor�������ص���true����false�������������������л��Ƕ���mq�У���������Ҫ�жϵ�һ�����pollSize * 1.0D / (double)maxPoolSize
�������Ҫ����mq����ô���ǾͶ����̳߳�����ȥִ�У�
ִ��ǰ����һЩǰ�ü�飺Ŀǰ������¼�ڱ��е�״̬���Լ����Դ���.ͬʱ��ִ��ǰ���ǻ�������һ��uuid������uuid����ThreadLocal���档�ڴ�ӡ��־��ʱ�����ʹ�ã�
��־���������ִ�е�Ψһʵ������ִ�����remove����ͬʱ���uuidҲ�����
{"sub-message":"�÷�ȯ�������ڽ��в�����Եȣ�","uuid":"5031f46624664fc793a111920ba715f2"}'
Ȼ��ʼ������ִ���߼������ݷ��ص�״̬��ȥ����ap������������Դ���������������ob��ִ�����

�������mq��ִ��״����
����ж�pollSize * 1.0D / (double)maxPoolSize�Ѿ�������ֵ�ˣ���ô�Ϳ�ʼִ��mq��ҵ���߼���

�����MessageQueueע�����ε��첽��ܾͷ��͵�˽�ж��У�һ��Ƚ���Ҫ��������˽�ж��У��У����û��˽�ж��оͷ��͵�����������ȥ�����﹫�������ܹ���10�������͵ĸ��ؾ�������ѯ���͡�
ͬʱ��һ��unit�����6̨������ȫ��������������ڲ����У���ȡ���Ϳ�ʼִ�С�ִ���߼��������һ��

job�����е��߼�����ÿ���첽����������һ��run������job������ִ�����run�����ģ�
this.doJob(IStatusValue.TODO, 0, count, processNumPerThread, dutsParam); ��ǰʱ��֮ǰ
this.doJob(IStatusValue.DOING, this.getIntConfigValue(config, "job.delay.doing", 5), count, processNumPerThread, dutsParam);5����֮ǰ
this.doJob(IStatusValue.REDO_L1, this.getIntConfigValue(config, "job.delay.redo.L1", 5), count, processNumPerThread, dutsParam);5����֮ǰ
this.doJob(IStatusValue.REDO_L2, this.getIntConfigValue(config, "job.delay.redo.L2", 10), count, processNumPerThread, dutsParam);10����֮ǰ
this.doJob(IStatusValue.REDO_L3, this.getIntConfigValue(config, "job.delay.redo.L3", 15), count, processNumPerThread, dutsParam);15����֮ǰ
��ȡTODO״̬����������ǰʱ��֮ǰnew Date(System.currentTimeMillis() - (long)(60 * 1000 * delay));

Ȼ��ʹ���ĵ��̳߳ؽ���ִ�У�
 (new SimplePaginationExecutor<IStatus>(list, processNumPerThread, ThreadPools.ASYNCHRONOUS_PROCESS_JOB_THREAD_POOL) {
                    protected void callback(List<IStatus> subList) {
                        if (!CollectionUtils.isEmpty(subList)) {
                            Iterator var2 = subList.iterator();

                            while(var2.hasNext()) {
                                IStatus status = (IStatus)var2.next();
                                status.setMessage(AsynchronousProcessor.this.append(status));
                                AsynchronousProcessor.this.processIStatus(status, AsynchronousProcessor.this.default_process_callback);
                            }

                        }
                    }
                }).execute();




