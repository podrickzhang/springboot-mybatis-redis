package com.rosam.springbootmybatisredis.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rosam.springbootmybatisredis.dao.UserMapper;
import com.rosam.springbootmybatisredis.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
