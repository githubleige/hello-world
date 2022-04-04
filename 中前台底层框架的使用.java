��ǰ̨�ײ��ܵ�ʹ�ã�
���Ⱥ������ű�
-- gomstpub.wf_node definition
�������������׼�ڵ����Ͻ�㡣��׼�ڵ�ָ��������ֻ��һ�����ִ���߼���
Ϊʲô��Ҫ�ֱ�׼������Ͻ�����֣���ʵ������ִ�е�λ���Ǳ�׼��㡣��׼�ڵ����Ͻ���������ǣ�Ϊ�˰�һЩ���п��ԾۺϵĽڵ����һ���������Լ���
���̵Ĳ��賤�ȣ���ά�������������ٴε��������߼��Ļ��Ϳ��ԣ�ֱ�Ӱ���Ͻ���ù���ʹ�á�ͬʱ��Ͻ�������е������߼����Բ�����ȥִ�У����ִ��Ч�ʡ�����
��ִ����Ͻ���ʱ���ǲ���ִ�л��Ǵ���ִ�У���ҵ��Ͳ�Ʒ������������ֻҪ�����ű��Ӧ����Ͻ���CND_CALL_TYPE�ֶ�������0����1�Ϳ�����
CREATE TABLE `wf_node` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `NODE_CODE` varchar(50) NOT NULL COMMENT '�ڵ����',
  `NODE_NAME` varchar(256) NOT NULL COMMENT '�ڵ�����',
  `NODE_TYPE` int(1) NOT NULL COMMENT '�ڵ����� 0 ��׼�ڵ� 1 ��Ͻڵ�',
  `NODE_STATE` int(1) NOT NULL COMMENT '�ڵ�״̬ 0 δ���� 1 ����',
  `ROLLBACK_FLAG` int(1) NOT NULL COMMENT '�Ƿ�֧�ֻع� 0 ��֧�� 1 ֧��',
  `CND_CALL_TYPE` int(1) NOT NULL COMMENT '��Ͻڵ�ִ�з�ʽ 0 ���� 1 ����',
  `NODE_DESCRIPTION` varchar(512) DEFAULT NULL COMMENT '�ڵ�����',
  `WF_VERSION` varchar(100) NOT NULL DEFAULT '' COMMENT '�汾��',
  `CREATED_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '����ʱ��',
  `LAST_UPD_TIME` timestamp NOT NULL DEFAULT '2018-12-10 00:00:00' COMMENT '����ʱ��',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UX1_WF_NODE` (`NODE_CODE`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12283 DEFAULT CHARSET=utf8mb4 COMMENT='��������ڵ��';

-- gomstpub.wf_cnd_item definition
Ȼ���о�����Ͻ���ˣ�������Ͻ������ڻ���������չ�����ģ������ڵ���е���Ͻ����Ҫ�����ű��н������ã�����Ͻ���Ӧ�ı�׼�ڵ����ó�����ͬʱ
��������Ͻ���еı�׼�ڵ��ִ�в��衣�����Ͻ������ı�׼�ڵ㲻�ܲ���ִ�У�����������Ҫ����˳��Ž���˳��ִ�С��ڽ���ģ������ʱ�����ǻᷢ��
��Ͻ���������һ�� private final Map<Integer, StandardWorkflowNode> cndNodes = new TreeMap<>();���ԣ���Ҫ������������Ͻڵ��Ӧ�Ļ������ġ�
key��ִ�е�˳��ţ�value��StandardWorkflowNode
CREATE TABLE `wf_cnd_item` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `CND_CODE` varchar(50) NOT NULL COMMENT '��Ͻڵ����',
  `BND_CODE` varchar(50) NOT NULL COMMENT '��׼�ڵ����',
  `STEP_NUM` int(10) NOT NULL COMMENT '�����',
  `CREATED_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '����ʱ��',
  `LAST_UPD_TIME` timestamp NOT NULL DEFAULT '2018-12-10 00:00:00' COMMENT '����ʱ��',
  `WF_VERSION` varchar(100) NOT NULL DEFAULT '' COMMENT '�汾��',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UX1_WF_CND_ITEM` (`CND_CODE`,`BND_CODE`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12283 DEFAULT CHARSET=utf8mb4 COMMENT='��Ͻڵ������';
Ȼ�����ִ�в�����ˡ���Ӧ��WorkflowStep�����м����Ƚ���Ҫ�����ԣ�stepNum��nextStepNumY��nextStepNumN��Ȼ�����ά��һ��private BaseWorkflowNode node;
����ִ��Ҳ����ִ��BaseWorkflowNode����� protected IWorkflowNodeExecutable workflowNodeExecutable;

-- gomstpub.wf_step definition

CREATE TABLE `wf_step` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `WF_ID` bigint(15) DEFAULT NULL COMMENT 'ģ��id',
  `WF_CODE` varchar(50) NOT NULL '���̱���',
  `NODE_CODE` varchar(50) NOT NULL COMMENT '�ڵ����',
  `STEP_NUM` int(10) NOT NULL COMMENT '�����',
  `NEXT_STEP_N` int(10) NOT NULL COMMENT '����ֵΪNʱ����һ�������',
  `NEXT_STEP_Y` int(10) NOT NULL COMMENT '����ֵΪYʱ����һ�������',
  `ROLLBACK_FLAG` int(1) NOT NULL COMMENT '�Ƿ�֧�ֻع� 0 ��֧�� 1 ֧��',
  `CREATED_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '����ʱ��',
  `LAST_UPD_TIME` timestamp NOT NULL DEFAULT '2018-12-10 00:00:00' COMMENT '����ʱ��',
  `WF_VERSION` varchar(100) NOT NULL DEFAULT '' COMMENT '�汾��',
  PRIMARY KEY (`ID`),
  KEY `IX1_WF_STEP` (`WF_ID`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=24571 DEFAULT CHARSET=utf8mb4 COMMENT='���̲��趨���';

-- gomstpub.wf_definition definition

CREATE TABLE `wf_definition` (
  `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `WF_CODE` varchar(50) NOT NULL COMMENT '���̱���',
  `WF_ID` int(11) DEFAULT NULL COMMENT 'ģ��id',
  `WF_NAME` varchar(256) NOT NULL COMMENT '��������',
  `WF_VERSION` varchar(100) NOT NULL COMMENT '���̰汾',
  `WF_STATE` int(1) NOT NULL COMMENT '����״̬ 0 δ���� 1 ����',
  `WF_TYPE_CODE` varchar(50) NOT NULL COMMENT '�������ͱ���',
  `BIZ_SCENE_CODE` varchar(2048) NOT NULL COMMENT 'ҵ�񳡾�����',
  `WF_DESCRIPTION` varchar(512) DEFAULT NULL COMMENT '��������',
  `CREATED_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '����ʱ��',
  `LAST_UPD_TIME` timestamp NOT NULL DEFAULT '2018-12-10 00:00:00' COMMENT '����ʱ��',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UX1_WF_DEFINITION` (`WF_CODE`,`WF_VERSION`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1531 DEFAULT CHARSET=utf8mb4 COMMENT='����ģ�嶨���';
('P_ORD_SBMT_OLN',1,'�����ύ���׹���׼��','GOMST_20211011_10',1,'ORD_SBMT','���ϱ�׼,����ƴ��,�����ں�,������,���±�׼,���¼��ָ�','�����ύ���׹���׼��','2021-09-28 11:25:32.0','2021-09-28 11:25:32.0')



@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface WorkflowNode {
    /**
     * �ڵ����
     */
    public String nodeCode();
}
���Ƕ�����һ��ע�⣬���ע����Ҫ��spring���ʹ�ã����������ʵ��ApplicationListener<ApplicationContextEvent>��������������ɺ����ǻᴥ��
onApplicationEvent���������еļ���������̾�����������ɵģ����ǻ�ɨ�����е�bean,����ע��WorkflowNode���εĽ��ȫ�����Ž�һ��map����ȥ��
//����һ��map,map��key�ڵ�@WorkflowNode(nodeCode = "ND_SO_CHK_MN")�е�nodeCode����ֵ��
//Object����bean��Ӧ�Ľڵ���������beanһ����ʵ����IWorkflowNodeExecutable�ӿڵ��ࣩ
public static final Map<String, Object> EXECUTABLES = new HashMap<>();��
���map���ں����������ʱ��ʹ�ã���WorkflowNode���εĽ��һ����Ҫʵ��һ����ڣ�
public interface IWorkflowNodeExecutable extends Serializable {
	//ʵ�ֵ�ҵ���߼���ͬʱWorkflowDataContextWrapper�����и�����workflowDataContext��workflowDataContext�����������ִ����Ҫ�����ݣ������װ��List<OrderDTO>���ݶ���
    int execute(WorkflowDataContextWrapper var1);
	//�ع��߼���Ҳ�ǽ���ҵ��ȥʵ��
    int rollback(WorkflowDataContextWrapper var1);
}
������������ʱ�򣬾�������ʵ��IWorkflowNodeExecutable�Ľ�㸳ֵ��BndWorkflowNode��protected IWorkflowNodeExecutable workflowNodeExecutable;���Ե�
Ȼ������ִ�е�ʱ��Ҳ�ǻ�ȡ�����workflowNodeExecutable��ִ�еġ�

���ǵĽ��̳й�ϵ�������ģ�
BaseWorkflowNode�ǻ���㣬���涨����һЩ�������ԣ�protected String nodeCode;��protected int nodeType;
��BaseWorkflowNode����������ֱ�Ӽ̳е㣺
BndWorkflowNode:����һ���ǳ���Ҫ�Ļ��࣬������һ���ǳ���Ҫ�����Ծ������н��ʵ���඼����ʵ�ֵĽӿڣ�protected IWorkflowNodeExecutable workflowNodeExecutable;
CndWorkflowNode��������Ͻ�㡣������һ��map������һ��cndExecuteType���ԡ�map������Ҫ������װ��Ͻ������Ļ������ģ�cndExecuteType��ʾ��Ͻ��
����Ļ����ڵ㼯��ִ�з�ʽ�ǲ��л��Ǵ��е�
BndWorkflowNode���滹�м̳��ࣺ
StandardWorkflowNode������һ���������������ҵ������еĽ�㶼��ʹ����������а�װ��
SopWorkflowNode�����ǿ�ʼ������������ʼ���һ�����ǻ��ڵײ��Լ�ʵ�֡�ֱ�ӷ�װ�ͺ���
EopfWorkflowNode������ִ���쳣��������;�нڵ㷵��ʧ�ܣ��Ľ�������װ��������������лع��߼����ѻع���������Ľ��ȫ���ó���������ִ�н�������
�ع��߼����ع��������Ⱥ�˳��ģ�
EopWorkflowNode����������������ִ�еĽ���߼�����Ҫ����ִ�н����Ĺ��Ӻ����ġ�

���Ƿ�װ�������߼����ǣ���������Ϣ�浽��������ű��У���ҵ���߼�����ʵ��IWorkflowNodeExecutable�ӿڵ�ʵ���࣬��Ҫʹ��@WorkflowNodeע�⡣
1���ѻ����������������spring��װ��һ��һ����StandardWorkflowNode��
2��������Ͻ���Ȱ�����װ��CndWorkflowNode�����������map�����ǿյģ�������ı�׼�ڵ��װ�ú��ڸ���wf_cnd_item��Ͻ����ӱ�׼�ڵ������л�ȡ��Ӧ��
StandardWorkflowNode�����ݱ������õĲ��裬�ŵ���Ͻ�������map������
3��Ȼ����Ǹ���wf_definition������ݣ������ݳ�����󣬷�װ��WorkflowTemplate����Ž�map������key�����̱��룬value��WorkflowTemplate���󡣵���
WorkflowTemplate���������private final Map<String, WorkflowStep> workflowSteps = new ConcurrentHashMap<>();ִ�в������Ի��ǿյ�
4��Ȼ����Ǹ���wf_step����������������װWorkflowTemplate�����workflowSteps���ԡ�WorkflowStep������һ����Ҫ����private BaseWorkflowNode node;��װǰ��
��װ�õĽ�㡣Ȼ��ѷ�װ�õ�WorkflowStep�Ž�WorkflowTemplate�����map�����У�����key�ǲ�����ж�Ӧ�Ĳ���š�ͬʱҲ��װ��nextStepNumY��nextStepNumN���Լ�
��������Ӧ�Ľ���ǲ�����Ͻ��
����һ�����������̲��輯�Ͼͺ��ˣ�
// ģ�弯��
 private static final Map<String, WorkflowTemplate> WORKFLOW_TEMPLATES = new ConcurrentHashMap<>();
 ���������ִ��ֻ��Ҫ�������̱���Ϳ����ˡ����ܻ�ȡһ��������ִ�в���
 
 
 
 ����������ǰ�úͺ��ù��������Ӻ�������
 //workflowCode:�Ǵ����̱���
 //workflowDataContext  ����һ��WorkflowInstanceִ��ʵ�������в��蹲��Ķ��������з�װdto����List<OrderDTO> Ҳ���������Ķ���
 ��ڷ���com.suning.zhongtai.wf.bootstrap.WorkflowEngine#prepareStart(String workflowCode, IWorkflowDataContext workflowDataContext)
 ����һ������ʵ��������ʵ����������˺ܶ���Ҫ�����ԣ�
 //����ʵ��id��uuid
private String wfInstanceId;
//���̻ع�����ÿִ��һ������ͻ��ж�һ����������Ƿ���Իع�������ǿ��Իع���if (nodeRollbackFlag && stepRollbackFlag) ����ô�ͷ���
//private List<WorkflowStep> rollbackList = new ArrayList<>();���������Ͻ���Ƚ��鷳�����ж���Ͻ���Ƿ�֧�ֻع���Ȼ������������еı�׼�ڵ㲽��
//ִ�а���Ͻ������֧�ֻع��ı�׼����ó���������Щ��׼�ڵ�ƴ���µĲ������ع�������ע���ʱƴ�ӵ��²�������Ľ�㶼�Ǳ�׼�ڵ㡣cndstep.getStepNum()_childstandardStepNum;
//��ִ�е�EOPF����ʱ�򣬻��������������еĻع������ó�������ִ��
private IWorkflowRollbackManager workflowRollbackManager;
//������־�������ࣩ.���ǰѽ���Ƿ�ִ�гɹ����Լ�ִ�����̷���һ�������У�������ִ�������ǻ�ִ��CLM_LOGGER.info("{}", workflowClmLogBean);
������ִ��һ������ӡһ�������Ƿ��ں���һ���ӡ
private IWorkflowLog workflowLog;
/**
  * ����Ҫ�����е����ݶ��󶼷�װ�������������
  */
//��������������(��һ������ʵ�����н�������ã������װ��List<OrderDTO>���ݶ���)
private IWorkflowDataContext dataContext;

//���̹���ִ��������
private WorkflowHookContext hookContext;
//����ģ��
private WorkflowTemplate workflowTemplate;
 //���̽���������ִ�б�ʶ����eopf����EOP������Ϊtrue��
private boolean instanceEndHookHasRun = false;
//ʵ������ʱ����(��ʼʱ��ִ�иö����ֲ�ʽ������ֹ�ظ��µ����µ���ɺ����)����ʵ��ִ��ǰ����Ҫͨ��
//public WorkflowExecuteBuilder addInstanceStartHook(WorkflowHook hook, boolean ... isAffect)���������i_s_h���Ϳ�ʼ���Ӻ���
//public WorkflowExecuteBuilder addInstanceEndHook(WorkflowHook hook, boolean ... isAffect)���������i_f_h���ͽ������Ӻ���
private final Map<String, List<WorkflowHookWrapper>> instanceRuntimeHooks = new HashMap<>();

���Ӻ�����Ӻ�����ʵ����ʼ����ɺ󣬿�ʼ�������̣�
 //ִ�п�ʼ���ӣ��൱�ڼӷֲ�ʽ����
 //ͨ������ģ���ȡ��ʼ����
 WorkflowStep startStep = workflowTemplate.findStartStep();
 //ִ�п�ʼ����
 int res = startStep.execute(this);
 WorkflowStep nextStep = workflowTemplate.findNextStep(startStep, res);
 //�ж��¸������ǲ��ǽ������裨ROP��EOP��EOPF��
 while (!isEndStep(nextStep)) {
     //������ǽ���Step�������ִ���¸�����
     int nextStepRes = nextStep.execute(this);
     //�жϵ�ǰstep�Ƿ���Ҫ�ع�������Ҫ���ݷ���ֵ���ж��Ƿ���Ҫ�ع���
     stepRollbackProcess(nextStep);
     nextStep = workflowTemplate.findNextStep(nextStep, nextStepRes);
 }
 //ִ�н�������
 nextStep.execute(this);
  //ִ�����̽�������,����EOP����EOPF�п�ʼִ�е�((����ʱ��ִ�иö����ֲ�ʽ���Ľ�������ֹ��;��ʧЧ�����µ��ظ��µ�))
workflowInstance.runHooks(WorkflowConstant.WORKFLOW_INSTANCE_END);
//���ù���ִ�гɹ����Ϊtrue
workflowInstance.setInstanceEndHookHasRun(true);
����



