# springboot-mybatis-redis
springboot整合redis,mybatis。redis缓存，mybatis sql拦截

## 应用介绍
本应用通过springboot结合redis，mysql实现，实现redis查询缓存。当缓存中没有数据时，会先去查询数据库，然后将数据加载到缓存中，下一次查询的时候就可以取查询缓存，
本文介绍SpringBoot 如何使用redis做缓存，如何对redis缓存进行定制化配置(如key的有效期)以及初始化redis做缓存。使用具体的代码介绍了@Cacheable，@CacheEvict，@CachePut，@CacheConfig等注解及其属性的用法。以及实现自定义注解

## 知识点讲解

### Spring缓存相关的注解
注解|说明
---|:--:
@Cacheable|方法执行前先看缓存中是否有数据，如果有直接返回。如果没有就调用方法，并将方法返回值放入缓存
@CachePut|无论怎样都会执行方法，并将方法返回值放入缓存
@CacheEvict|将数据从缓存中删除
@Caching|可通过此注解组合多个注解策略在一个方法上面

### 继承redis缓存
#### 添加SpringBoot redis依赖
```java
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-redis</artifactId>
			<version>1.4.3.RELEASE</version>
		</dependency>
```

#### 配置application.yml
```java
spring:
    redis:
        database:  0
        host:  127.0.0.1
        port:  6379
        password: root
        jedis:
          pool:
            max-active: 8
            max-idle: 8
            max-wait:
            min-idle: 0
        timeout: 100000

```
#### 缓存配置类
一般来讲我们使用key属性就可以满足大部分要求，但是如果你还想更好的自定义key，可以实现keyGenerator。通过继承CachingConfigurerSupport，并且加上@EnabelCaching,也可以在SpringBoot启动类上加上@
```java

@Configuration
@EnableCaching
public class RedisConfiguration extends CachingConfigurerSupport{

	// 缓存数据时Key的生成器，可以依据业务和技术场景自行定制
    //类名加方法名
	@Bean
    public KeyGenerator myKeyGenerator() {
        System.out.println("Redis cache Config keyGenerator()");
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                //类名+方法名
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };

    }


    
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}


```
##### 相应的注解说明

@Cacheable可以标记在一个方法上，也可以标记在一个类上。当标记在一个方法上时表示该方法是支持缓存的，当标记在一个类上时则表示该类所有的方法都是支持缓存的.@Cacheable可以指定三个属性，value、key和condition。<br>
value属性指定Cache名称,使用key属性自定义key,自定义策略是指我们可以通过Spring的EL表达式来指定我们的key。这里的EL表达式可以使用方法参数及它们对应的属性。使用方法参数时我们可以直接使用“#参数名”或者“#p参数index”。<br>
除了上述使用方法参数作为key之外，Spring还为我们提供了一个root对象可以用来生成key。通过该root对象我们可以获取到以下信息。
属性名称|描述|示例
---|:--:|---:
methodName|当前方法名|#root.methodName
method|当前方法|#root.method.name
target|当前被调用的对象|#root.target
targetClass|当前被调用的对象的class|	#root.targetClass
args|当前方法参数组成的数组|#root.args[0]
caches|当前被调用的方法使用的Cache|#root.caches[0].name
 当我们要使用root对象的属性作为key时我们也可以将“#root”省略，因为Spring默认使用的就是root对象的属性。如：
 ```java
    @Cacheable(value={"users", "xxx"}, key="caches[1].name")
   public User find(User user) {
      returnnull;
   }
 ```
##### condition属性指定发生的条件
 有的时候我们可能并不希望缓存一个方法所有的返回结果。通过condition属性可以实现这一功能。condition属性默认为空，表示将缓存所有的调用情形。其值是通过SpringEL表达式来指定的，当为true时表示进行缓存处理；当为false时表示不进行缓存处理，即每次调用该方法时该方法都会执行一次。如下示例表示只有当user的id为偶数时才会进行缓存。
 ```JAVA
   @Cacheable(value={"users"}, key="#user.id", condition="#user.id%2==0")
   public User find(User user) {
      System.out.println("find user by user " + user);
      return user;
   }
 ```
 
 #### 查询Service
```java
@Service
//@CacheConfig(cacheNames="userCache",keyGenerator = "myKeyGenerator") // 本类内方法指定使用缓存时，默认的名称就是userCache
//注意可以在类上面通过 @CacheConfig 配置全局缓存名称，方法上面如果也配置了就会覆盖。
@CacheConfig(cacheNames = "cacheName")
@Transactional(propagation=Propagation.REQUIRED,readOnly=false,rollbackFor=Exception.class)
public class User2Service {

	@Autowired
	private UserMapper userMapper;
	
	// 因为必须要有返回值，才能保存到数据库中，如果保存的对象的某些字段是需要数据库生成的，
   //那保存对象进数据库的时候，就没必要放到缓存了
	//@CachePut(key="#p0.id")  //#p0表示@Cacheable注解方法的第一个参数，这里都是id(1....)
	//必须要有返回值，否则没数据放到缓存中
	@Cacheable(key = "#p0.id")
	public User insertUser(User u){
		userMapper.insert(u);
		//u对象中可能只有只几个有效字段，其他字段值靠数据库生成，比如id
		return this.userMapper.find(u.getId());
	}
	
	// Both 'key' and 'keyGenerator' attributes have been set.会报错
	//所以有了key，就不能再加keyGenerator属性。
	@CachePut(key="#p0.id")
	public User updateUser(User u){
		userMapper.update(u);
		//可能只是更新某几个字段而已，所以查次数据库把数据全部拿出来全部
		return this.userMapper.find(u.getId());
	}
	
	@Cacheable(key="#p0") // @Cacheable 会先查询缓存，如果缓存中存在，则不执行方法
	public User findById(String id){
		System.err.println("根据id=" + id +"获取用户对象，从数据库中获取");
		return this.userMapper.find(id);
	}
	
	@CacheEvict(key="#p0")  //删除缓存名称为userCache,key等于指定的id对应的缓存
	public void deleteById(String id){
		System.out.println("根据id="+ id + "删除缓存");
		this.userMapper.delete(id);
	}
	
	//清空缓存名称为userCache（看类名上的注解)下的所有缓存
	//如果数据失败了，缓存时不会清除的
	@CacheEvict(allEntries = true)
	public void deleteAll(){
		userMapper.deleteAll();
	}
	
	public PageInfo<User> queryPage(String userName,int pageNum,int pageSize){
		Page<User> page = PageHelper.startPage(pageNum, pageSize);
		//PageHelper会自动拦截到下面这查询sql
		userMapper.query(userName);
		return page.toPageInfo();
	}

}

```

#### 使用自定义注解
```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Cacheable(value="users")
public @interface MyCacheable {
}
```
 那么在我们需要缓存的方法上使用@MyCacheable进行标注也可以达到同样的效果。
 ```java
    @MyCacheable
   public User findById(Integer id) {
      System.out.println("find user by id: " + id);
      User user = new User();
      user.setId(id);
      user.setName("Name" + id);
      return user;
   }
 ```
