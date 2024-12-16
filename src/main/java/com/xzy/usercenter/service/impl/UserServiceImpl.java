package com.xzy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzy.usercenter.mapper.UserMapper;
import com.xzy.usercenter.model.User;
import com.xzy.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Pattern;

import static com.xzy.usercenter.constant.UserConstant.USER_LOGIN_STATE;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Resource
    private UserMapper userMapper;

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


    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
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

        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);


        return safetyUser;
    }

    @Override
    public boolean userLogOut(HttpServletRequest request) {
        if (request == null){
            log.info("request is null");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }


    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        User user = new User();

        user.setUserAccount(user.getUserAccount());
        user.setUnionId(user.getUnionId());
        user.setMpOpenId(user.getMpOpenId());
        user.setUserName(user.getUserName());
        user.setUserAvatar(user.getUserAvatar());
        user.setUserProfile(user.getUserProfile());
        user.setUserRole(user.getUserRole());
        user.setCreateTime(user.getCreateTime());
        user.setUpdateTime(user.getUpdateTime());
        user.setIsDelete(user.getIsDelete());


        return user;
    }

    @Override
    public List<User> searchUserListByUserName(String userName) {
        if(userName == null || StringUtils.isEmpty(userName)){
            log.info("searchUserListByUserName userName is null");
            return null;
        }
        List<User> userList = userMapper.searchUserListByName(userName);
        if(userList == null){
            log.info("data not found, userName={}", userName);
        }
        return userList;
    }

    @Override
    public Page<User> searchUserListWithPage(int pageNum,int pageSize) {
        if (pageNum < 1 || pageSize < 1) {
            log.info("pageNum or pageSize can not < 1");
            return null;
        }
        Page<User> page = new Page<>(pageNum, pageSize);
       // List<User> userList = userMapper.searchUserListWithPage();
        Page<User> userPage = userMapper.selectPage(page, null);
        if (userPage == null) {
            log.info("userPage is null");
            return null;
        }
        return userPage;
    }
}
