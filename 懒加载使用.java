�����صĶ�̬�������ϸ������
�������ǽ����ԣ�OrderDTO lazyLoadDTO(String oid);����������н�����
���������ڽ���һ�������ĵ�OrderDTO�����ص�ʱ�򣬿϶�ΨһҪ�����ֶξ��������������oid���������oid���ǿ�����װ�������Ķ�����������Ϣ��
���ǵ�һ����Ҫ���ľ��Ǵ���OrderDTO�������ʹ��cglib����
enhancer.setSuperclass(OrderDTO.class);
//������method.getName().startsWith("get")|| method.getName().startsWith("is")��ʱ�򣬵��ô�������������У�ԭ����ʲô�߼�������ʲô�߼�
enhancer.setCallbackFilter(FILTER);
���filter����һ�����������᷵��һ�����꣬��Ϊ��������ֻ��������callback�����Է��ص�����ֻ����0����1
Callback[] callbacks = new Callback[] { interceptor, NOOP };�����������callback
interceptor�����������ڴ��������ص�ʱ��ʹ�õ������������������صĵķ����У�
������method.getName().startsWith("get")|| method.getName().startsWith("is")��ʱ�򣬵��ô�������������У�
ԭ����ʲô�߼�������ʲô�߼�
Ȼ�󴴽��������
��OrderDTOInterceptor���ض������м������ԣ�
	//����һ��ʼ������Ǹ�oid
	private String oid;
	//����OrderDTO���������
	private OrderDTO orderDTO;
	/**
	 * �Ѽ��صı���Ϊ�������������أ�����һЩ�����Ѿ����ع������ǿ϶���Ҫ��һ�����������м�¼
	 */
	private Set<String> isLoad = new HashSet<>();
��
ע�⣺�������������ĵ���˼�����ܹ��ᴴ��3��������󣬷ֱ���OrderDTO�������OrderItemDetailDTO������󣨾������Ǵ������������Ĵ������ȡ�������ǵĶ���һ���ж����У�
��OrderDetailDTO�������OrderItemDetailDTO��������OrderDetailDTO������󶼻ᱻ���õ�OrderDTO�����������ȥ
�����ڽ��������ص�ʱ����һ����Ҫ��ע�⣺
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LazyLoad {

    //��Ӧ�ı�����������Ծ��Ǽ�����ͨ�ֶ����棬����������ֶν���get��ʱ����Ҫ���������ع��̣�
    String tableName() default "";
    //��ʵ������������ж��Ƿ�Ҫ���������������Ŀǰ��orderDto��ֻ��OrderDetailDTO��OrderItemDetailDTO��Ҫ�����������
    boolean isProxy() default false;
    //�����Ҫ���Ǻ������isProxy()���ʹ�á������ڴ�����������ʱ���Ӧ�����ȫ�޶���
    String className() default "";
    
}
����������ú��ˡ������Ƚ���һЩԤ���أ�һЩ�Ƚ���Ҫ�ı�����˵�б��ͷ��
����ִ�л�ȡorderDTO �������oid
String oid = orderDTO.getOrderDetail().getOid();
���������
orderDTO.getOrderDetail()��仰�ᴥ���������Ĵ���������MethodInterceptor�����������Ӷ���������һ����OrderDetail�������
�����õ�orderDTO �����������ȥ
ִ��getOid()Ҳ�ᱻ��������MethodInterceptor��������������ע�� @LazyLoad(tableName = "SO_ORD")��
����tableName��һ��hashMap�л�ȡ����Ӧ�����ش���ί����loader��
public class T_SO_ORD_Loader extends TableDataLoader<Ord>
���ί���������м�����Ҫ�ķ������ӿڣ���
public abstract class TableDataLoader<T> {
	//���T���Ƕ�Ӧ��ѯ������Ӧ��pojo�ࣨ�������ص�ʱ��ƴ�ӱ���ʹ�ã�
	public abstract String getTableName();
	//�����������Ҫ���þ����ڽ��в�ѯ��ʱ�� ��ȡ�ñ��rowmapper,�ѱ��е��ֶ�ת��Ϊ����
	//�����SO_ORDI���е��ֶ�����ת��ΪORDI����
	//�ڽ��������ص�ʱ�򣬲�ѯ�������ResultSetת����Tʱ��ʹ��
	//(List<T>) dalClient.queryForList("lazyLoad.queryTableByOid", paramMap, tableLoader.getRowMapper());
	public abstract RowMapper getRowMapper();
	//��Ҫ�ӱ��в�ѯ�������ֶ�,ƴ��sqlʱ����ʹ��
	public String getTableFieldStr() {
		return " * ";
	}
	//��data�е�������װ��orderDTO����ȥ
	public abstract void transforObj(List<T> data, OrderDTO orderDTO);

	//�ڽ��������صĹ����жԱ���в�ѯ��ʱ��ɸѡ�ֶ�
	public Map<String, Object> getParams() {
		HashMap<String, Object> param = new HashMap<>();
		param.put("activeFlag", OmsConstant.ACTIVED);
		return param;
	}
}
Ȼ��ʼִ�в�⣬����������������ί������ƴ��sql,��ѯ��oid����MethodInterceptor�������е�
List<Object> queryDtoTable = loadDTODao.queryDtoTableByOid(loader, oid);

{
	Ȼ��ʼִ��һ���Ƚ��ѵ��ж�:�����жϵ�ǰ��ȡ������Ҫ��Ҫ���ô�����Ȼ�����ǲ���Ҫ���õģ��������Ǽ���������Ҫ�����ô���ɣ�����Ӧ���������
��Ҫ����ע��LazyLoad��������Ҫ���ô���ǰ���ǣ�
public @interface LazyLoad {

    //��Ӧ�ı�����������Ծ��Ǽ�����ͨ�ֶ����棬����������ֶν���get��ʱ����Ҫ���������ع��̣�
    String tableName() "SO_ORD";
    //��ʵ������������ж��Ƿ�Ҫ���������������Ŀǰ��orderDto��ֻ��OrderDetailDTO��OrderItemDetailDTO��Ҫ�����������
    boolean isProxy() true;
    //�����Ҫ���Ǻ������isProxy()���ʹ�á������ڴ�����������ʱ���Ӧ�����ȫ�޶���
    String className() "��Ҫ����������ȫ�޶�����";
    
}
Ȼ����Ҫ�ж����Filed:if (field.getType() == List.class && null != annotation && annotation.isProxy()) 
�����List��ִ��
//��ȡ���������������
tring className = annotation.className();
���ȡ����Ҫ���ɴ����superClass.
�������ɼ��������Ǹ���queryDtoTable.size()
//�����������
			for (int i = 0; i < size; i++) {
				Object proxyBean = LoadDTOProxyFactory
						.createProxy(Class.forName(className), proxyDto.getOrderDetail().getOid(), proxyDto);
				list.add(proxyBean);
			}
����������������������ʹ������ˡ�
}

��ʵ����ִ��String oid = orderDTO.getOrderDetail().getOid();ʱ��getOid()���������ɴ�������ˣ���Ϊ��
 @LazyLoad(tableName = "SO_ORD")
    private String oid;
�������������������Ĳ��֮��Ĳ�������ʼִ��ί�����loader.transforObj(queryDtoTable, proxyDto);
���ǰѲ�ѯ����������List<T> queryDtoTable��װ������orderDTO����ȥ�������װ������ί�и�ҵ������Լ�ʵ�ֵġ�
��Ȼ������Ҳ����ͳһʵ�֣�ͨ�������ٽ�����@DTOMappingע�⣩��
@DTOMapping(className = "com.suning.oms.base.dao.entity.order.Ord", fieldName = "oid")
����ͨ������Ч�ʵͣ����޸�֪������������ҡ����Է���

���ܻ������ʣ�������OrderItemDetailDTO��������ȡ��һ��list���󡣱���˵����OrderItemDetailDTO�����payDetail���ԣ�
    @XStreamAlias("payDetails")
    @LazyLoad(tableName = "SO_ORDI_PAY")
    private List<PayDetailDTO> payDetail = new ArrayList<PayDetailDTO>();
	�����ִ�У�
	public List<PayDetailDTO> getPayDetail() {
        return payDetail;
    }
	��ͬ������MethodInterceptor������ @LazyLoad(tableName = "SO_ORDI_PAY")��SO_ORDI_PAY���ҵ�ί����T_SO_ORDI_PAY_Loader.
	��װsql����oidȥ��ѯSO_ORDI_PAY��Ȼ��ִ��ί�����public void transforObj(List<OrdiPay> data, OrderDTO orderDTO)