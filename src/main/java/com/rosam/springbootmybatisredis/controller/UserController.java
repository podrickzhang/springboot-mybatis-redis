package com.rosam.springbootmybatisredis.controller;

import com.github.pagehelper.PageInfo;
import com.rosam.springbootmybatisredis.domain.User;
import com.rosam.springbootmybatisredis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/user")
public class UserController {

	
	@Autowired
	private UserService userService;
	
	@RequestMapping("/hello")
	@ResponseBody
	public String hello(){
		return "hello";
	}
	/**
	 * 测试插入
	 * @return
	 */
	@RequestMapping("/add")
	@ResponseBody
	public String add(String id,String userName){
		User u = new User();
		u.setId(id);
		u.setUserName(userName);
		userService.insertUser(u);
		return u.getId()+"    " + u.getUserName();
	}
	
	/**
	 * 测试根据id查询
	 * @return
	 */
	@RequestMapping("/get/{id}")
	@ResponseBody
	public String findById(@PathVariable("id")String id){
		User u = userService.findById(id);
		return u== null ? "找不到对象" :( u.getId()+"    " + u.getUserName());
	}
	
	
	/**
	 * 测试修改
	 * @return
	 */
	@RequestMapping("/update")
	@ResponseBody
	public String update(String id,String userName){
		User u = new User();
		u.setId(id);
		u.setUserName(userName);
		this.userService.updateUser(u);
		return u.getId()+"    " + u.getUserName();
	}
	
	/**
	 * 测试删除
	 * @return
	 */
	@RequestMapping("/delete/{id}")
	@ResponseBody
	public String delete(@PathVariable("id")String id){
		userService.deleteById(id);
		return "success";
	}
	
	/**
	 * 测试全部
	 * @return
	 */
	@RequestMapping("/deleteAll")
	@ResponseBody
	public String deleteAll(){
		userService.deleteAll();
		return "success";
	}
	
	
	
	/**
	 * 测试分页插件
	 * @return
	 */
	@RequestMapping("/queryPage")
	@ResponseBody
	public String queryPage(@RequestParam("userName") String userName){
		PageInfo<User> page = userService.queryPage(userName, 1, 2);
		System.out.println("总页数=" + page.getPages());
		System.out.println("总记录数=" + page.getTotal()) ;
		for(User u : page.getList()){
			System.out.println(u.getId() + " \t " + u.getUserName());
		}
		return page.toString();
	}
}
