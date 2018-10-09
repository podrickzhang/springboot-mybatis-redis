package com.rosam.springbootmybatisredis.controller;

import com.github.pagehelper.PageInfo;
import com.rosam.springbootmybatisredis.domain.User;
import com.rosam.springbootmybatisredis.service.User3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user3")
public class User3Controller {


    @Autowired
    private User3Service user3Service;

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
        user3Service.insertUser(u);
        return u.getId()+"    " + u.getUserName();
    }

    /**
     * 测试根据id查询
     * @return
     */
    @RequestMapping("/get/{id}")
    @ResponseBody
    public String findById(@PathVariable("id")String id){
        User u = user3Service.findById(id);
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
        user3Service.updateUser(u);
        return u.getId()+"    " + u.getUserName();
    }

    /**
     * 测试删除
     * @return
     */
    @RequestMapping("/delete/{id}")
    @ResponseBody
    public String delete(@PathVariable("id")String id){
        user3Service.deleteById(id);
        return "success";
    }

    /**
     * 测试全部
     * @return
     */
    @RequestMapping("/deleteAll")
    @ResponseBody
    public String deleteAll(){
        user3Service.deleteAll();
        return "success";
    }



    /**
     * 测试分页插件
     * @return
     */
    @RequestMapping("/queryPage")
    @ResponseBody
    public String queryPage(@RequestParam("userName") String userName){
        PageInfo<User> page = user3Service.queryPage(userName, 1, 2);
        for(User u : page.getList()){
            System.out.println(u.getId() + " \t " + u.getUserName());
        }
        return page.toString();
    }

}
