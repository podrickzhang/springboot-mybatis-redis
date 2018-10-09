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
@Transactional(propagation=Propagation.REQUIRED,readOnly=false,rollbackFor=Exception.class)
public class User3Service {

	@Autowired
	private UserMapper userMapper;
	
	// 因为必须要有返回值，才能保存到数据库中，如果保存的对象的某些字段是需要数据库生成的，
   //那保存对象进数据库的时候，就没必要放到缓存了
	//@CachePut(key="#p0.id")  //#p0表示第一个参数
	//必须要有返回值，否则没数据放到缓存中
	@Cacheable(value = "#p0.id",keyGenerator = "myKeyGenerator")
	public User insertUser(User u){
		userMapper.insert(u);
		//u对象中可能只有只几个有效字段，其他字段值靠数据库生成，比如id
		return this.userMapper.find(u.getId());
	}
	

	@CachePut(key="#p0.id",keyGenerator = "myKeyGenerator" )
	public User updateUser(User u){
		userMapper.update(u);
		//可能只是更新某几个字段而已，所以查次数据库把数据全部拿出来全部
		return this.userMapper.find(u.getId());
	}
	
	//使用自定义的注解
	@MyCacheable
	public User findById(String id){
		System.err.println("根据id=" + id +"获取用户对象，从数据库中获取");
		return this.userMapper.find(id);
	}
	
	//@CacheEvict(key="#p0", keyGenerator = "myKeyGenerator")  //删除缓存名称为userCache,key等于指定的id对应的缓存
	@CacheEvict(value = "key", keyGenerator = "myKeyGenerator")
	public void deleteById(String id){
		System.out.println("根据id="+ id + "删除缓存");
		this.userMapper.delete(id);
	}
	
	//清空缓存名称为userCache（看类名上的注解)下的所有缓存
	//如果数据失败了，缓存时不会清除的
	@CacheEvict(allEntries = true, keyGenerator = "myKeyGenerator")
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
