package cn.lip.mybatis.controller;

import cn.lip.mybatis.bean.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {
       @RequestMapping("/usersssss")
       public String showAddser(User user) {
            //int i = 100 / 0;
              //  String s = user.toString();
//              return "addUser";
              throw  new NullPointerException("NullPointerException");
//              String[]strarr={"aa","bb","cc"};
//              String s = strarr[3];
//              String substring = s.substring(10);

       }
}