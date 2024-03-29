中前台底层框架的使用：
首先核心四张表：
-- gomstpub.wf_node definition
基础结点表包括标准节点和组合结点。标准节点指的是里面只有一个结点执行逻辑。
为什么需要分标准结点和组合结点两种，其实基本的执行单位都是标准结点。标准节点和组合结点的区别就是，为了把一些具有可以聚合的节点放在一起，这样可以减少
流程的步骤长度，好维护。其他流程再次调用相似逻辑的话就可以，直接把组合结点拿过来使用。同时组合结点里面有的相似逻辑可以并发的去执行，提高执行效率。具体
再执行组合结点的时候是并发执行还是串行执行，由业务和产品量决定。配置只要在这张表对应的组合结点的CND_CALL_TYPE字段来配置0还是1就可以了
CREATE TABLE `wf_node` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `NODE_CODE` varchar(50) NOT NULL COMMENT '节点编码',
  `NODE_NAME` varchar(256) NOT NULL COMMENT '节点名称',
  `NODE_TYPE` int(1) NOT NULL COMMENT '节点类型 0 标准节点 1 组合节点',
  `NODE_STATE` int(1) NOT NULL COMMENT '节点状态 0 未启用 1 启用',
  `ROLLBACK_FLAG` int(1) NOT NULL COMMENT '是否支持回滚 0 不支持 1 支持',
  `CND_CALL_TYPE` int(1) NOT NULL COMMENT '组合节点执行方式 0 串行 1 并行',
  `NODE_DESCRIPTION` varchar(512) DEFAULT NULL COMMENT '节点描述',
  `WF_VERSION` varchar(100) NOT NULL DEFAULT '' COMMENT '版本号',
  `CREATED_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `LAST_UPD_TIME` timestamp NOT NULL DEFAULT '2018-12-10 00:00:00' COMMENT '更新时间',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UX1_WF_NODE` (`NODE_CODE`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12283 DEFAULT CHARSET=utf8mb4 COMMENT='流程引擎节点表';

-- gomstpub.wf_cnd_item definition
然后还有就是组合结点了，这张组合结点表是在基础结点表扩展出来的，基础节点表中的组合结点需要在这张表中进行配置，把组合结点对应的标准节点配置出来。同时
给出在组合结点中的标准节点的执行步骤。如果组合结点里面的标准节点不能并发执行，我们这里需要根据顺序号进行顺序执行。在进行模板填充的时候，我们会发现
组合结点里面多了一个 private final Map<Integer, StandardWorkflowNode> cndNodes = new TreeMap<>();属性，主要就是用来放组合节点对应的基础结点的。
key是执行的顺序号，value是StandardWorkflowNode
CREATE TABLE `wf_cnd_item` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `CND_CODE` varchar(50) NOT NULL COMMENT '组合节点编码',
  `BND_CODE` varchar(50) NOT NULL COMMENT '标准节点编码',
  `STEP_NUM` int(10) NOT NULL COMMENT '步骤号',
  `CREATED_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `LAST_UPD_TIME` timestamp NOT NULL DEFAULT '2018-12-10 00:00:00' COMMENT '更新时间',
  `WF_VERSION` varchar(100) NOT NULL DEFAULT '' COMMENT '版本号',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UX1_WF_CND_ITEM` (`CND_CODE`,`BND_CODE`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12283 DEFAULT CHARSET=utf8mb4 COMMENT='组合节点子项表';
然后就是执行步骤表了。对应的WorkflowStep里面有几个比较重要的属性：stepNum、nextStepNumY、nextStepNumN，然后就是维护一个private BaseWorkflowNode node;
真正执行也是先执行BaseWorkflowNode里面的 protected IWorkflowNodeExecutable workflowNodeExecutable;

-- gomstpub.wf_step definition

CREATE TABLE `wf_step` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `WF_ID` bigint(15) DEFAULT NULL COMMENT '模板id',
  `WF_CODE` varchar(50) NOT NULL '流程编码',
  `NODE_CODE` varchar(50) NOT NULL COMMENT '节点编码',
  `STEP_NUM` int(10) NOT NULL COMMENT '步骤号',
  `NEXT_STEP_N` int(10) NOT NULL COMMENT '返回值为N时的下一个步骤号',
  `NEXT_STEP_Y` int(10) NOT NULL COMMENT '返回值为Y时的下一个步骤号',
  `ROLLBACK_FLAG` int(1) NOT NULL COMMENT '是否支持回滚 0 不支持 1 支持',
  `CREATED_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `LAST_UPD_TIME` timestamp NOT NULL DEFAULT '2018-12-10 00:00:00' COMMENT '更新时间',
  `WF_VERSION` varchar(100) NOT NULL DEFAULT '' COMMENT '版本号',
  PRIMARY KEY (`ID`),
  KEY `IX1_WF_STEP` (`WF_ID`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=24571 DEFAULT CHARSET=utf8mb4 COMMENT='流程步骤定义表';

-- gomstpub.wf_definition definition

CREATE TABLE `wf_definition` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `WF_CODE` varchar(50) NOT NULL COMMENT '流程编码',
  `WF_ID` int(11) DEFAULT NULL COMMENT '模板id',
  `WF_NAME` varchar(256) NOT NULL COMMENT '流程名称',
  `WF_VERSION` varchar(100) NOT NULL COMMENT '流程版本',
  `WF_STATE` int(1) NOT NULL COMMENT '流程状态 0 未启用 1 启用',
  `WF_TYPE_CODE` varchar(50) NOT NULL COMMENT '流程类型编码',
  `BIZ_SCENE_CODE` varchar(2048) NOT NULL COMMENT '业务场景编码',
  `WF_DESCRIPTION` varchar(512) DEFAULT NULL COMMENT '流程描述',
  `CREATED_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `LAST_UPD_TIME` timestamp NOT NULL DEFAULT '2018-12-10 00:00:00' COMMENT '更新时间',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UX1_WF_DEFINITION` (`WF_CODE`,`WF_VERSION`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1531 DEFAULT CHARSET=utf8mb4 COMMENT='流程模板定义表';
('P_ORD_SBMT_OLN',1,'订单提交（易购标准）','GOMST_20211011_10',1,'ORD_SBMT','线上标准,线上拼购,线上融合,零售云,线下标准,线下家乐福','订单提交（易购标准）','2021-09-28 11:25:32.0','2021-09-28 11:25:32.0')



@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface WorkflowNode {
    /**
     * 节点编码
     */
    public String nodeCode();
}
我们定义了一个注解，这个注解需要和spring配合使用，根启动类会实现ApplicationListener<ApplicationContextEvent>，在容器加载完成后，我们会触发
onApplicationEvent方法，所有的加载填充流程就是在这里完成的，我们会扫描所有的bean,把用注解WorkflowNode修饰的结点全部都放进一个map里面去，
//这是一个map,map的key节点@WorkflowNode(nodeCode = "ND_SO_CHK_MN")中的nodeCode属性值，
//Object就是bean对应的节点对象（这里的bean一定是实现了IWorkflowNodeExecutable接口的类）
public static final Map<String, Object> EXECUTABLES = new HashMap<>();：
这个map会在后面的填充结点的时候使用，用WorkflowNode修饰的结点一定需要实现一个借口，
public interface IWorkflowNodeExecutable extends Serializable {
	//实现的业务逻辑，同时WorkflowDataContextWrapper里面有个属性workflowDataContext，workflowDataContext里面放着上文执行需要的数据，里面封装了List<OrderDTO>数据对象
    int execute(WorkflowDataContextWrapper var1);
	//回滚逻辑，也是交给业务去实现
    int rollback(WorkflowDataContextWrapper var1);
}
我们在填充结点的时候，就是拿着实现IWorkflowNodeExecutable的结点赋值给BndWorkflowNode的protected IWorkflowNodeExecutable workflowNodeExecutable;属性的
然后结点在执行的时候也是获取到这个workflowNodeExecutable来执行的。

我们的结点继承关系是这样的：
BaseWorkflowNode是基结点，里面定义了一些公共属性：protected String nodeCode;、protected int nodeType;
在BaseWorkflowNode下面有两个直接继承点：
BndWorkflowNode:这是一个非常重要的基类，里面有一个非常重要的属性就是所有结点实现类都必须实现的接口：protected IWorkflowNodeExecutable workflowNodeExecutable;
CndWorkflowNode：这是组合结点。里面有一个map容器和一个cndExecuteType属性。map容器主要用来承装组合结点包含的基础结点的，cndExecuteType表示组合结点
里面的基础节点集合执行方式是并行还是串行的
BndWorkflowNode下面还有继承类：
StandardWorkflowNode：这是一般结点的容器，我们业务代码中的结点都是使用这个来进行包装的
SopWorkflowNode：这是开始结点的容器，开始结点一般我们会在底层自己实现。直接封装就好了
EopfWorkflowNode：这是执行异常（或者中途有节点返回失败）的结束结点包装容器，这里面会有回滚逻辑。把回滚容器里面的结点全部拿出来，并发执行结点里面的
回滚逻辑（回滚不存在先后顺序的）
EopWorkflowNode：这是正常结束会执行的结点逻辑，主要就是执行结束的钩子函数的。

我们封装的整体逻辑就是：把所有信息存到上面的四张表中，把业务逻辑交给实现IWorkflowNodeExecutable接口的实现类，需要使用@WorkflowNode注解。
1、把基础结点表抽出来，结合spring封装成一个一个的StandardWorkflowNode。
2、对于组合结点先把他封装成CndWorkflowNode，但是里面的map容器是空的，待上面的标准节点封装好后，在根据wf_cnd_item组合结点表，从标准节点容器中获取对应的
StandardWorkflowNode，根据表中配置的步骤，放到组合结点里面的map容器中
3、然后就是根据wf_definition表抽数据，把数据抽出来后，封装成WorkflowTemplate对象放进map容器。key是流程编码，value是WorkflowTemplate对象。但是
WorkflowTemplate对象里面的private final Map<String, WorkflowStep> workflowSteps = new ConcurrentHashMap<>();执行步骤属性还是空的
4、然后就是根据wf_step表抽出来的数据来封装WorkflowTemplate里面的workflowSteps属性。WorkflowStep里面有一个重要属性private BaseWorkflowNode node;封装前面
组装好的结点。然后把封装好的WorkflowStep放进WorkflowTemplate里面的map容器中，其中key是步骤表中对应的步骤号。同时也封装了nextStepNumY和nextStepNumN。以及
这个步骤对应的结点是不是组合结点
至此一个完整的流程步骤集合就好了：
// 模板集合
 private static final Map<String, WorkflowTemplate> WORKFLOW_TEMPLATES = new ConcurrentHashMap<>();
 后面的流程执行只需要给我流程编码就可以了。我能获取一个完整的执行步骤
 
 
 
 流程启动的前置和后置工作（钩子函数）：
 //workflowCode:是传流程编码
 //workflowDataContext  这是一个WorkflowInstance执行实例中所有步骤共享的对象，里面有封装dto对象：List<OrderDTO> 也就是上下文对象
 入口方法com.suning.zhongtai.wf.bootstrap.WorkflowEngine#prepareStart(String workflowCode, IWorkflowDataContext workflowDataContext)
 创建一个流程实例：流程实例里面包含了很多重要的属性：
 //流程实例id，uuid
private String wfInstanceId;
//流程回滚管理。每执行一个步骤就会判断一下这个步骤是否可以回滚。如果是可以回滚（if (nodeRollbackFlag && stepRollbackFlag) ）那么就放入
//private List<WorkflowStep> rollbackList = new ArrayList<>();。如果是组合结点会比较麻烦，先判断组合结点是否支持回滚，然后遍历里面所有的标准节点步骤
//执行把组合结点里面支持回滚的标准结点拿出来，把这些标准节点拼成新的步骤放入回滚容器。注意此时拼接的新步骤里面的结点都是标准节点。cndstep.getStepNum()_childstandardStepNum;
//在执行到EOPF结点的时候，会把这个容器中所有的回滚步骤拿出来并发执行
private IWorkflowRollbackManager workflowRollbackManager;
//流程日志（工具类）.我们把结点是否执行成功，以及执行流程放在一个容器中，在流程执行完我们会执行CLM_LOGGER.info("{}", workflowClmLogBean);
并不是执行一个，打印一个，而是放在后面一起打印
private IWorkflowLog workflowLog;
/**
  * 很重要，所有的数据对象都封装在这个上下文中
  */
//流程数据上下文(被一个流程实例所有结点所公用，里面封装了List<OrderDTO>数据对象)
private IWorkflowDataContext dataContext;

//流程钩子执行上下文
private WorkflowHookContext hookContext;
//流程模板
private WorkflowTemplate workflowTemplate;
 //流程结束钩子已执行标识（在eopf或者EOP中设置为true）
private boolean instanceEndHookHasRun = false;
//实例运行时钩子(开始时候执行该订单分布式锁，防止重复下单，下单完成后解锁)。在实例执行前，需要通过
//public WorkflowExecuteBuilder addInstanceStartHook(WorkflowHook hook, boolean ... isAffect)函数来添加i_s_h类型开始钩子函数
//public WorkflowExecuteBuilder addInstanceEndHook(WorkflowHook hook, boolean ... isAffect)函数来添加i_f_h类型结束钩子函数
private final Map<String, List<WorkflowHookWrapper>> instanceRuntimeHooks = new HashMap<>();

钩子函数添加和运行实例初始化完成后，开始启动流程：
 //执行开始钩子（相当于加分布式锁）
 //通过流程模板获取开始步骤
 WorkflowStep startStep = workflowTemplate.findStartStep();
 //执行开始步骤
 int res = startStep.execute(this);
 WorkflowStep nextStep = workflowTemplate.findNextStep(startStep, res);
 //判断下个步骤是不是结束步骤（ROP、EOP、EOPF）
 while (!isEndStep(nextStep)) {
     //如果不是结束Step，则继续执行下个步骤
     int nextStepRes = nextStep.execute(this);
     //判断当前step是否需要回滚（不需要根据返回值来判断是否需要回滚）
     stepRollbackProcess(nextStep);
     nextStep = workflowTemplate.findNextStep(nextStep, nextStepRes);
 }
 //执行结束步骤
 nextStep.execute(this);
  //执行流程结束钩子,是在EOP或者EOPF中开始执行的((结束时候执行该订单分布式锁的解锁，防止中途锁失效，导致的重复下单))
workflowInstance.runHooks(WorkflowConstant.WORKFLOW_INSTANCE_END);
//设置钩子执行成功标记为true
workflowInstance.setInstanceEndHookHasRun(true);
结束



