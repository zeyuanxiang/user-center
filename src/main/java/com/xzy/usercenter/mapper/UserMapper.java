package com.xzy.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xzy.usercenter.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据库操作
 *
 */
public interface UserMapper extends BaseMapper<User> {


    List<User> searchUserByUserName(String username);

    List<User> searchUserListByName(String userName);

    List<User> searchUserListWithPage();
}




