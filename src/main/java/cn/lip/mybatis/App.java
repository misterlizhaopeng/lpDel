package cn.lip.mybatis;

import cn.lip.mybatis.application.MyApplicationContextInitializer;
import cn.lip.mybatis.application.MyApplicationRunner;
import cn.lip.mybatis.application.MyCommandLineRunner;
import cn.lip.mybatis.bean.ComponentTest;
import cn.lip.mybatis.bean.Student;
import cn.lip.mybatis.bean.TbUser;
import cn.lip.mybatis.listener.MyAppEvent;
import cn.lip.mybatis.service.UserRedPacketService;
import cn.lip.mybatis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 */
//(scanBasePackages = {"cn.lip.mybatis"}, exclude = {cn.lip.mybatis.application.MyCommandLineRunner.class})
@SpringBootApplication
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {MyCommandLineRunner.class, MyApplicationRunner.class})})
@RestController
@EnableAsync //开启异步调用
public class App {


    @Autowired
    private UserService userService;
    @Autowired
    private UserRedPacketService userRedPacketService;

    public static void main(String[] args) {


//        ConfigurableApplicationContext ctx = SpringApplication.run(App.class, args);
//        ctx.publishEvent(new MyAppEvent(new Object()));
        SpringApplication ctx = new SpringApplication(App.class);
        //ctx.setBannerMode(Banner.Mode.CONSOLE);//关闭banner


        //ctx.addInitializers(new MyApplicationContextInitializer());
        ConfigurableApplicationContext applicationContext = ctx.run(args);//new String[]{"a", "b"}

        ComponentTest componentTest = applicationContext.getBean(ComponentTest.class);
        //applicationContext.close();// 关闭 spring 容器

        //        String[] beanDefinitionNames = ctx.getBeanDefinitionNames();
//        List<String> list = Arrays.asList(beanDefinitionNames);
//        list.forEach(a -> {
//            System.out.println(a);
//        });
    }


    @GetMapping("/getStrById/{id}")
    public String getStrById(@PathVariable("id") String id) {

        int i=30;//000
        //CountDownLatch countDownLatch=new CountDownLatch(i);
//        ExecutorService executorService= Executors.newFixedThreadPool(i);
        for (int j = 0; j < i; j++) {

//            executorService.execute(()->{
//                Student student = userService.getStudentByIdAndName(2, "lp");
//                System.out.println(student);
//                countDownLatch.countDown();
//            });

            new Thread(()->{
                Student student = userService.getStudentByIdAndName(2, "lp");
                System.out.println(student);
                //countDownLatch.countDown();
            }).start();
        }

//        try {
//            countDownLatch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        return "id=" + id;
    }


    @PostMapping("/grabRp")
    public Map<String,Object> postGrabRedPacket(Long redPacketId, Long userId) {
        //int result = userRedPacketService.grabRedPacket(redPacketId, userId);
        //int result = userRedPacketService.grabRedPacketForVersion(redPacketId, userId);
        //int result = userRedPacketService.grabRedPacketForVersionContinueByTimestamp(redPacketId, userId);
        //int result = userRedPacketService.grabRedPacketForVersionContinueByTimes(redPacketId, userId);
        Long result = userRedPacketService.grapRedPacketByRedis(redPacketId, userId);
        Map<String,Object> retMap=new HashMap<>();
        boolean flag = result > 0;
        retMap.put("success", flag);
        retMap.put("message", flag ? "抢红包成功" : "抢红包失败");
        return retMap;
    }


}
