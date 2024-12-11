package com.xzy.usercenter;
import java.util.Date;

import com.xzy.usercenter.mapper.UserMapper;
import com.xzy.usercenter.model.User;
import com.xzy.usercenter.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import javax.annotation.Resource;

@SpringBootTest
class UserCenterApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

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

    @Test
    public void userRegister() {

        long result = userService.userRegister("zhangfei","12345678","12345678");
        Assertions.assertEquals(1,result);

        long result1 = userService.userRegister("lisi","12345678","12345678");
        Assertions.assertEquals(-1,result1);

        long result2 = userService.userRegister("li~~si","12345678","123456789");
        Assertions.assertEquals(-1,result2);

        long result3 = userService.userRegister("liubei","12345678","123456789");
        Assertions.assertEquals(-1,result3);

    }

}
