懒加载的动态代理的详细剖析：
这里我们仅仅以：OrderDTO lazyLoadDTO(String oid);这个方法进行解析。
首先我们在进行一个上下文的OrderDTO懒加载的时候，肯定唯一要传的字段就是这个订单单号oid。根据这个oid我们可以组装出完整的订单上下文信息。
我们第一步需要做的就是创建OrderDTO代理对象（使用cglib）：
enhancer.setSuperclass(OrderDTO.class);
//方法是method.getName().startsWith("get")|| method.getName().startsWith("is")的时候，调用代理方法，否则放行，原来是什么逻辑，就是什么逻辑
enhancer.setCallbackFilter(FILTER);
这个filter就是一个过滤器，会返回一个坐标，因为我们这里只设置两个callback，所以返回的坐标只能是0或者1
Callback[] callbacks = new Callback[] { interceptor, NOOP };这就是那两个callback
interceptor是真正我们在处理懒加载的时候使用的拦截器。拦截器拦截的的方法有：
方法是method.getName().startsWith("get")|| method.getName().startsWith("is")的时候，调用代理方法，否则放行，
原来是什么逻辑，就是什么逻辑
然后创建代理对象。
（OrderDTOInterceptor拦截对象中有几个属性：
	//就是一开始传入的那个oid
	private String oid;
	//代理OrderDTO，代理对象
	private OrderDTO orderDTO;
	/**
	 * 已加载的表，因为我们这是懒加载，所以一些东西已经加载过的我们肯定需要用一个容器来进行记录
	 */
	private Set<String> isLoad = new HashSet<>();
）
注意：按照我们上下文的意思我们总共会创建3个代理对象，分别是OrderDTO代理对象、OrderItemDetailDTO代理对象（具体我们创建几个这样的代理对象，取决于我们的订单一共有多少行）
、OrderDetailDTO代理对象。OrderItemDetailDTO代理对象和OrderDetailDTO代理对象都会被设置到OrderDTO代理对象里面去
我们在进行懒加载的时候有一个重要的注解：
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LazyLoad {

    //对应的表名（这个属性就是加载普通字段上面，表明在这个字段进行get的时候需要进行懒加载过程）
    String tableName() default "";
    //其实这个代理属性判断是否要继续创建代理对象（目前在orderDto中只有OrderDetailDTO和OrderItemDetailDTO需要穿件代理对象）
    boolean isProxy() default false;
    //这个主要就是和上面的isProxy()配合使用。表明在创建代理对象的时候对应的类的全限定名
    String className() default "";
    
}
代理对象设置好了。我们先进行一些预加载（一些比较重要的表，比如说行表和头表）
首先执行获取orderDTO 代理对象oid
String oid = orderDTO.getOrderDetail().getOid();
代码分析：
orderDTO.getOrderDetail()这句话会触发代理对象的代理方法，被MethodInterceptor拦截下来。从而创建代理一个的OrderDetail代理对象
并设置到orderDTO 代理对象里面去
执行getOid()也会被代理对象的MethodInterceptor拦截下来，解析注解 @LazyLoad(tableName = "SO_ORD")。
根据tableName从一个hashMap中获取这表对应懒加载处理委派类loader：
public class T_SO_ORD_Loader extends TableDataLoader<Ord>
这个委派类里用有几个重要的方法（接口）：
public abstract class TableDataLoader<T> {
	//这个T就是对应查询表名对应的pojo类（在懒加载的时候拼接表名使用）
	public abstract String getTableName();
	//这个方法的主要作用就是在进行查询的时候 获取该表的rowmapper,把表中的字段转化为对象。
	//例如把SO_ORDI表中的字段设置转化为ORDI对象
	//在进行懒加载的时候，查询出结果集ResultSet转换成T时候使用
	//(List<T>) dalClient.queryForList("lazyLoad.queryTableByOid", paramMap, tableLoader.getRowMapper());
	public abstract RowMapper getRowMapper();
	//需要从表中查询出来的字段,拼接sql时候里使用
	public String getTableFieldStr() {
		return " * ";
	}
	//将data中的数据组装到orderDTO里面去
	public abstract void transforObj(List<T> data, OrderDTO orderDTO);

	//在进行懒加载的过程中对表进行查询的时候筛选字段
	public Map<String, Object> getParams() {
		HashMap<String, Object> param = new HashMap<>();
		param.put("activeFlag", OmsConstant.ACTIVED);
		return param;
	}
}
然后开始执行查库，就是利用上面的这个委派类来拼接sql,查询。oid是在MethodInterceptor本来就有的
List<Object> queryDtoTable = loadDTODao.queryDtoTableByOid(loader, oid);

{
	然后开始执行一个比较难的判断:就是判断当前获取的属性要不要设置代理（当然这里是不需要设置的，但是我们假设这里需要在设置代理吧，我们应该如何做）
需要借助注解LazyLoad，首先需要设置代理前提是：
public @interface LazyLoad {

    //对应的表名（这个属性就是加载普通字段上面，表明在这个字段进行get的时候需要进行懒加载过程）
    String tableName() "SO_ORD";
    //其实这个代理属性判断是否要继续创建代理对象（目前在orderDto中只有OrderDetailDTO和OrderItemDetailDTO需要穿件代理对象）
    boolean isProxy() true;
    //这个主要就是和上面的isProxy()配合使用。表明在创建代理对象的时候对应的类的全限定名
    String className() "需要被代理的类的全限定名字";
    
}
然后还需要判断这个Filed:if (field.getType() == List.class && null != annotation && annotation.isProxy()) 
如果是List，执行
//获取创建代理对象类型
tring className = annotation.className();
会获取到需要生成代理的superClass.
具体生成几个，我们根据queryDtoTable.size()
//创建代理对象
			for (int i = 0; i < size; i++) {
				Object proxyBean = LoadDTOProxyFactory
						.createProxy(Class.forName(className), proxyDto.getOrderDetail().getOid(), proxyDto);
				list.add(proxyBean);
			}
好了我们现在这个代理对象就创建好了。
}

其实我们执行String oid = orderDTO.getOrderDetail().getOid();时候getOid()不会在生成代理对象了，因为：
 @LazyLoad(tableName = "SO_ORD")
    private String oid;
所以我们这里继续上面的查库之后的操作，开始执行委托类的loader.transforObj(queryDtoTable, proxyDto);
就是把查询出来的数据List<T> queryDtoTable组装到代理orderDTO里面去，这个组装过程是委托给业务代码自己实现的。
当然在这里也可以统一实现，通过反射再借助（@DTOMapping注解）：
@DTOMapping(className = "com.suning.oms.base.dao.entity.order.Ord", fieldName = "oid")
但是通过反射效率低，且无感知，代码出错不好找。所以放弃

可能还有疑问：就是在OrderItemDetailDTO代理对象获取另一个list对象。比如说处在OrderItemDetailDTO里面的payDetail属性：
    @XStreamAlias("payDetails")
    @LazyLoad(tableName = "SO_ORDI_PAY")
    private List<PayDetailDTO> payDetail = new ArrayList<PayDetailDTO>();
	那如果执行：
	public List<PayDetailDTO> getPayDetail() {
        return payDetail;
    }
	会同样触发MethodInterceptor，根据 @LazyLoad(tableName = "SO_ORDI_PAY")中SO_ORDI_PAY，找到委托类T_SO_ORDI_PAY_Loader.
	组装sql根据oid去查询SO_ORDI_PAY。然后执行委托类的public void transforObj(List<OrdiPay> data, OrderDTO orderDTO)