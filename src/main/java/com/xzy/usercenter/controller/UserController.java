package com.xzy.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xzy.usercenter.model.User;
import com.xzy.usercenter.model.request.UserLoginRequest;
import com.xzy.usercenter.model.request.UserRegisterRequest;
import com.xzy.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xzy.usercenter.constant.UserConstant.USER_LOGIN_STATE;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Long register(@RequestBody UserRegisterRequest userRegisterRequest) {

        if(null == userRegisterRequest) {
            return null;
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if(null == userAccount || null == userPassword || null == checkPassword) {
            return null;
        }

        return userService.userRegister(userAccount,userPassword,checkPassword);

    }

    @PostMapping("/login")
    public User register(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {

        if(null == userLoginRequest) {
            return null;
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if(null == userAccount || null == userPassword ) {
            return null;
        }

        return userService.userLogin(userAccount,userPassword,request);

    }

    @PostMapping("/logout")
    public boolean logout(HttpServletRequest request) {

        if(null == request) {
            log.info("request is null");
        }
        return userService.userLogOut(request);
    }
    /**
     * 查询用户
     * @param userName
     * @param request
     * @return
     */
    @GetMapping("/searchUserByName")
    public List<User> searchUserByName(@RequestParam("userName") String userName, HttpServletRequest request) {

        if(!isAdmin(request)){
            return new ArrayList<>();
        }
        if(StringUtils.isBlank(userName)) {
            return new ArrayList<>();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("userName", userName);
        List<User> userList = userService.list(queryWrapper);
        userList = userList.stream().map(user -> userService.getSafetyUser(user)
        ).collect(Collectors.toList());
        return userList;
    }

    /**
     * 删除用户
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public boolean removeUser(@RequestParam("id") long id,HttpServletRequest request) {
        if(!isAdmin(request)) {
            return false;
        }
        if(id <= 0) {
            return false;
        }
        return userService.removeById(id);
    }

    private boolean isAdmin(HttpServletRequest request) {

        Object obj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)obj;
        if(null == user) {
            return false;
        }
        if(user.getUserRole() == "admin") {
            return true;
        }
        return false;
    }


    @GetMapping("/searchUserListByUserName")
    private List<User> searchUserListByName(@RequestParam("userName") String userName) {
        if(StringUtils.isBlank(userName)) {

            return new ArrayList<>();
        }
        List<User> userList = userService.searchUserListByUserName(userName);
        return userList;
    }

    @GetMapping("/searchUserListWithPage")
    public Page<User> searchUserListWithPage(@RequestParam("pageNum") int pageNum,@RequestParam("pageSize") int pageSize) {
        if(pageNum <= 0 || pageSize <= 0) {
            log.info("pageNum:{},pageSize:{}", pageNum, pageSize);
            return new Page<>();
        }
        Page<User> userPage = userService.searchUserListWithPage(pageNum, pageSize);
        if (userPage == null) {
            return new Page<>();
        }
        return userPage;
    }


}
