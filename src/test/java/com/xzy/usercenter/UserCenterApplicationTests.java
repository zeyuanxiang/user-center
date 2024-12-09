package com.xzy.usercenter;
import java.util.Date;

import com.xzy.usercenter.mapper.UserMapper;
import com.xzy.usercenter.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import javax.annotation.Resource;

@SpringBootTest
class UserCenterApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testAddUser() {
        User user = new User();

        user.setUserAccount("xiaoheizi");
        user.setUserPassword("12345678");
        user.setUnionId("");
        user.setMpOpenId("");
        user.setUserName("小黑子");
        user.setUserAvatar("");
        user.setUserProfile("");
        user.setUserRole("user");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);

        int insert = userMapper.insert(user);
        Assert.isTrue(insert == 1);


    }

}
