package cn.lip.mybatis.controller;

import cn.lip.mybatis.bean.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @RequestMapping("/testIns")
    public String showAddser(User user) {

        return "ok";
    }
}
