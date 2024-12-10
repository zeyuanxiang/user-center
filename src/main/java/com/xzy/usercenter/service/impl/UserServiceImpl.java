package com.xzy.usercenter.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzy.usercenter.mapper.UserMapper;
import com.xzy.usercenter.model.User;
import com.xzy.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;


@Service
@Slf4j
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
        userQueryWrapper.eq("userAccount", userAccount);
        int count = this.count(userQueryWrapper);
        if(count > 0){
            return -1;
        }

        //特殊字符校验
        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        boolean matches = p.matcher(userAccount).matches();
        if(matches){
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

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        //校验
        if(StringUtils.isEmpty(userAccount) || StringUtils.isEmpty(userPassword)){
            return null;
        }
        if(userAccount.length() < 4 || userPassword.length() < 8){
            return null;
        }

        //特殊字符校验
        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        boolean matches = p.matcher(userAccount).matches();
        if(matches){
            return null;
        }

        final String SALT = "user";
        String password = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", password);
        User user = this.getOne(userQueryWrapper);
        if(user == null){
            log.info("user login failed, userAccount={}", userAccount);
            return null;
        }

        User safetyUser = new User();
        safetyUser.setUserAccount(user.getUserAccount());

        safetyUser.setUserName(user.getUserName());
        safetyUser.setUserAvatar(user.getUserAvatar());
        safetyUser.setUserProfile("");
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setUpdateTime(user.getUpdateTime());
        safetyUser.setIsDelete(0);

        request.getSession().setAttribute("user", safetyUser);


        return safetyUser;
    }


}
