package com.xzy.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xzy.usercenter.common.BaseResponse;
import com.xzy.usercenter.common.JsonUtil;
import com.xzy.usercenter.common.RedisUtil;
import com.xzy.usercenter.common.ResultUtil;
import com.xzy.usercenter.http.CookieUtil;
import com.xzy.usercenter.model.User;
import com.xzy.usercenter.model.request.UserLoginRequest;
import com.xzy.usercenter.model.request.UserRegisterRequest;
import com.xzy.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {

        if(null == userRegisterRequest) {
            return null;
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if(null == userAccount || null == userPassword || null == checkPassword) {
            return null;
        }

        long res = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtil.success(res);

    }

    @PostMapping("/login")
    public BaseResponse<User> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response, HttpSession session) {

        if(null == userLoginRequest) {
            return null;
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if(null == userAccount || null == userPassword ) {
            return null;
        }

        User user = userService.userLogin(userAccount, userPassword, request);
        if (user != null) {
            String sessionId = session.getId();
            CookieUtil.writeCookie(response,sessionId);
            redisUtil.set(sessionId, JsonUtil.obj2String(user),60*30);
        }
        return ResultUtil.success(user);

    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request,HttpServletResponse response) {

        if(null == request) {
            log.info("request is null");
        }
        boolean res = userService.userLogOut(request);
        if(res){
            CookieUtil.delCookie(response,request);
            String sessionId = CookieUtil.readUserLoginCookie(request);
            redisUtil.del(sessionId);
        }
        return ResultUtil.success(res);
    }
    /**
     * 查询用户
     * @param userName
     * @param request
     * @return
     */
    @GetMapping("/searchUserByName")
    public BaseResponse<List<User>> searchUserByName(@RequestParam("userName") String userName, HttpServletRequest request) {

        if(!isAdmin(request)){
            return null;
        }
        if(StringUtils.isBlank(userName)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("userName", userName);
        List<User> userList = userService.list(queryWrapper);
        userList = userList.stream().map(user -> userService.getSafetyUser(user)
        ).collect(Collectors.toList());
        return ResultUtil.success(userList);
    }

    /**
     * 删除用户
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> removeUser(@RequestParam("id") long id,HttpServletRequest request) {
        if(!isAdmin(request)) {
            return null;
        }
        if(id <= 0) {
            return null;
        }
        boolean res = userService.removeById(id);
        return ResultUtil.success(res);
    }

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    private BaseResponse<Boolean> updateUser(@RequestBody User user,HttpServletRequest request) {
        if(null == user) {
            return null;
        }
        if(!isAdmin(request)){
            return null;
        }
        boolean res = userService.updateById(user);
        return ResultUtil.success(res);
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
    private BaseResponse<List<User>> searchUserListByName(@RequestParam("userName") String userName) {
        if(StringUtils.isBlank(userName)) {
            return null;
        }
        List<User> userList = userService.searchUserListByUserName(userName);
        return ResultUtil.success(userList);
    }

    @GetMapping("/searchUserListWithPage")
    public BaseResponse<Page<User>> searchUserListWithPage(@RequestParam("pageNum") int pageNum,@RequestParam("pageSize") int pageSize) {
        if(pageNum <= 0 || pageSize <= 0) {
            log.info("pageNum:{},pageSize:{}", pageNum, pageSize);
            return null;
        }
        Page<User> userPage = userService.searchUserListWithPage(pageNum, pageSize);
        if (userPage == null) {
            return null;
        }
        return ResultUtil.success(userPage);
    }


}
