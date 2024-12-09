package com.xzy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzy.usercenter.mapper.UserMapper;
import com.xzy.usercenter.model.User;
import com.xzy.usercenter.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    /**
     *  用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //参数校验
        if(StringUtils.isEmpty(userAccount) || StringUtils.isEmpty(userPassword) || StringUtils.isEmpty(checkPassword)){
            return -1;
        }
        if(userAccount.length() < 4 || userPassword.length() < 8){
            return -1;
        }
        if(!userPassword.equals(checkPassword)){
            return -1;
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_account", userAccount);
        int count = this.count(userQueryWrapper);
        if(count > 0){
            return -1;
        }

        //加密
        final String SALT = "user";
        String password = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(password);
        user.setIsDelete(0);
        boolean res = this.save(user);
        if(!res){
            return -1;
        }
        return 0;
    }
}
