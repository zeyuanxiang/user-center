package com.xzy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzy.usercenter.common.ErrorCode;
import com.xzy.usercenter.exception.BusinessException;
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4 || userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度过短");
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入密码不一致");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        int count = this.count(userQueryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"账号已存在");
        }

        //特殊字符校验
        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        boolean matches = p.matcher(userAccount).matches();
        if(matches){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
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
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"注册失败，数据库错误");
        }
        return user.getId();
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        if(userAccount.length() < 4 || userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度需大于4，账号密码");
        }

        //特殊字符校验
        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        boolean matches = p.matcher(userAccount).matches();
        if(matches){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }

        final String SALT = "user";
        String password = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", password);
        User user = this.getOne(userQueryWrapper);
        if(user == null){
            log.info("user login failed, userAccount={}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码错误");
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
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"账号未登录");
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名为空");
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"页码或页数小于1");
        }
        Page<User> page = new Page<>(pageNum, pageSize);
       // List<User> userList = userMapper.searchUserListWithPage();
        Page<User> userPage = userMapper.selectPage(page, null);
        if (userPage == null) {
            log.info("userPage is null");
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"未查询到该数据");
        }
        return userPage;
    }
}
