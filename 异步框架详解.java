异步框架的整体架构设计详解：
异步框架的使用场景主要就是在结点执行失败的情况下，不通过从头再来
首先是AP表结构：
CREATE TABLE `ap_status_aims_0004` (
  `ID` varchar(40) NOT NULL COMMENT '编号,一般存放订单行号',
  `CATEGORY` varchar(20) NOT NULL COMMENT '分类,一个ID下有多个情况则需要用此字段做区分',
  `STATUS` int(11) DEFAULT NULL COMMENT '状态',
  `TIMES` int(11) DEFAULT '0' COMMENT '发送次数',
  `MESSAGE` varchar(2048) DEFAULT NULL COMMENT '信息内容',//HashMap<String, String> message = new HashMap();
  `CREATED_BY` varchar(20) DEFAULT NULL COMMENT '创建者',
  `CREATED_TIME` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `LAST_UPD_BY` varchar(20) DEFAULT NULL COMMENT '修改者',
  `LAST_UPD_TIME` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '修改时间',
  PRIMARY KEY (`ID`,`CATEGORY`),//主键
  KEY `IX_AP_STATUS_AIMS_01` (`CREATED_TIME`) USING BTREE,
  KEY `IX_AP_STATUS_AIMS_02` (`STATUS`,`CREATED_TIME`) USING BTREE,
  KEY `IX_AP_STATUS_AIMS_03` (`CATEGORY`,`STATUS`,`CREATED_TIME`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回滚活动库存资源';
STATUS是一个枚举状态值：
public enum IStatusValue {
    REDO_L3(-13),重试3次
    REDO_L2(-12),重试两次
    REDO_L1(-11),重试一次
    REDO(-10),
    DOING(-1),正在执行（已进入队列）
    TODO(0),待处理（定时任务捞这个状态的数据）
    DONE(1),成功完成
    QUIT(2),
    ERROR(3),业务失败（无需重试）
    OVER(4),重试次数过多
    WASTE(9);已经执行成功很久的数据，待删除
}
定义两种注解：
@Inherited
@Target(TYPE)
@Retention(RUNTIME)
public @interface AsynchronousProcess {

    //存入哪张AP表
    String table();

    //支付场景（一个异步框架要明确自己的适用场景，例如：category = "WF_APH_OLN_SUPER",随单购异步框架）
    String category();

    //可以重试的最大次数(一般情况下设置为3)
    int retryTimes() default 0;

    //注解用来解释要不要加分布式锁（有的异步框架执行前需要加分布式锁，例如扣库存）
    boolean exclusive() default false;

    //是否可以异步执行（默认是异步的）
    boolean async() default true;
}
还有一种注解是基于MQ实现的异步框架：
（异步框架分为两种，一种是基于线程池的阻塞队列实现的，还有一种是基于mq实现的。具体是采用哪种，把任务优先
放到自己所处的app对应的线程池的阻塞队列中，判断：pollSize * 1.0D / (double)maxPoolSize > 0.6D。一定要大于核心线程数和maxPoolSize的比值，记住
线程池的策略是先放到阻塞队列中，阻塞队列放满了，在启用最大线程数。所以当超过这个阈值的话，可以肯定这个app对应的异步任务已经很多了，这个时候
我们可以把他放到同一个unit内部队列mq中，别的app有时间取消费他。防止线程池处理不过来）
@MessageQueue(producer = "omsPrivateJmsTemplate", consumers = "windQConnectionFactory", concurrency = "10-10", queue = "AP_PRIVATE_QUEUE_001")
public @interface MessageQueue {
	//消息队列对应的JmsTemplate，客户端操作工具。是一个bean的名字
    String producer();
	//对应内部broker对应的队列名字
    String queue();
	//消费者，就是一个连接工厂。连接到对应的内部broker。windQConnectionFactory+queue就可以确定消费者消费的具体内容
    String[] consumers();
    //可以理解为在消费端不可能用一个线程来处理调度broker发送过来的信息。
    // （肯定是根据任务的多少来增加消费端的处理线程）。
    // 2相当于线程池中的核心线程，10相当于最大的线程数
    String concurrency() default "2-10";

}
基于线程池实现的异步框架的基类接口是：abstract class ObjectBasedAsynchronousProcessor<E>
基于MQ实现的异步框架的基类接口是：abstract class MessageQueueAsynchronousProcessor
在业务代码中根据需要看具体实现哪种接口
启动ob异步框架的的具体流程：
1、创建IStatus元数据
IStatus iStatus = IStatusUtils.newIStatus(oid, orderDetail.getMemInCardNo());
2、启动执行
AP.oneStepProcess("abnormalOrderSyncToPANGUXAsynchronousProcessor", iStatus, orderDTO);
3、进入oneStepProcess方法，注意异步框架的基类是实现了BeanNameAware这个接口的，所以可以通过this.beanName获取到当前执行异步框架在容器中的名字的：
AsynchronousProcessEngine.newInstance(processorName, status).persist().process(order);
存表的逻辑是，通过获取异步框架的注解可以获取到存表的表名和场景，通过上下文可以获取到id,所以存表
这行记录的状态需要看该异步框架在scm中配置的降级的状态，如果为降级那么状态阻断就是：TODO(0)，直接等job来捞取
如果该异步框架没有降级，那么状态就是 DOING(-1),正在做。
然后开始执行process(order)，首先判断该异步框架的降级状态，如果为降级：打印一行info日志就返回，该任务等待job来捞取
然后就通过该异步框架的注解async来判断是主线程执行还是丢到线程池里面去执行：一般情况下的都是丢到线程池里面去执行
ThreadPoolExecutor threadPoolExecutor = ThreadPools.ASYNCHRONOUS_PROCESS_THREAD_POOL;
ThreadPoolUtils.monitor(this.getClass(), threadPoolExecutor);
通过monitor方法返回的是true还是false来决定丢到阻塞队列中还是丢到mq中，这里面主要判断的一句就是pollSize * 1.0D / (double)maxPoolSize
如果不需要丢到mq。那么我们就丢到线程池里面去执行：
执行前会做一些前置检查：目前这条记录在表中的状态和以及重试次数.同时在执行前我们还生成了一个uuid，并把uuid方法ThreadLocal里面。在打印日志的时候可以使用，
标志着这个订单执行的唯一实例，在执行完会remove掉。同时这个uuid也会如表：
{"sub-message":"该返券订单正在进行补款，请稍等！","uuid":"5031f46624664fc793a111920ba715f2"}'
然后开始真正的执行逻辑，根据返回的状态，去更新ap表或者增加重试次数。上面这是在ob的执行情况

下面解释mq的执行状况：
如果判断pollSize * 1.0D / (double)maxPoolSize已经超过阈值了，那么就开始执行mq的业务逻辑。

如果有MessageQueue注解修饰的异步框架就发送到私有队列（一般比较重要的任务用私有队列）中，如果没有私有队列就发送到公共队列中去，这里公共队列总共有10个。发送的负载均衡是轮询发送。
同时在一个unit里面的6台机器，全部都监听了这个内部队列，获取到就开始执行。执行逻辑和上面的一样

job来进行的逻辑，在每个异步框架里面会有一个run方法，job就是来执行这个run方法的：
this.doJob(IStatusValue.TODO, 0, count, processNumPerThread, dutsParam); 当前时间之前
this.doJob(IStatusValue.DOING, this.getIntConfigValue(config, "job.delay.doing", 5), count, processNumPerThread, dutsParam);5分钟之前
this.doJob(IStatusValue.REDO_L1, this.getIntConfigValue(config, "job.delay.redo.L1", 5), count, processNumPerThread, dutsParam);5分钟之前
this.doJob(IStatusValue.REDO_L2, this.getIntConfigValue(config, "job.delay.redo.L2", 10), count, processNumPerThread, dutsParam);10分钟之前
this.doJob(IStatusValue.REDO_L3, this.getIntConfigValue(config, "job.delay.redo.L3", 15), count, processNumPerThread, dutsParam);15分钟之前
抽取TODO状态的条件：当前时间之前new Date(System.currentTimeMillis() - (long)(60 * 1000 * delay));

然后使用心得线程池进行执行：
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




