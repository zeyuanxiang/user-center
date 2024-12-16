package com.xzy.usercenter.service.impl;

import com.xzy.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.*;


@SpringBootTest
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

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