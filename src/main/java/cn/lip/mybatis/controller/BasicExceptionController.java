package cn.lip.mybatis.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class BasicExceptionController {
    @RequestMapping("/show")
    public String showInfo(){
        String str = null;
        str.length();
        return "index";
    }


    @RequestMapping("/show2")
    public String showInfo2(){
        int a = 10/0;
        return "index";
    }



    @ExceptionHandler(value={java.lang.Exception.class})
    public ModelAndView arithmeticExceptionHandler(Exception e) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("exception", e.toString());
        mv.addObject("aaa", "aaaccc");
        mv.setViewName("error");//可以指定不同的视图页面进行展示
        return mv;
    }



}
