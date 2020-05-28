package cn.lip.mybatis;

import cn.lip.mybatis.application.MyApplicationContextInitializer;
import cn.lip.mybatis.application.MyApplicationRunner;
import cn.lip.mybatis.application.MyCommandLineRunner;
import cn.lip.mybatis.bean.ComponentTest;
import cn.lip.mybatis.bean.Student;
import cn.lip.mybatis.bean.TbUser;
import cn.lip.mybatis.conf.RedisBloomFilter;
import cn.lip.mybatis.listener.MyAppEvent;
import cn.lip.mybatis.service.RedisLockService;
import cn.lip.mybatis.service.UserRedPacketService;
import cn.lip.mybatis.service.UserService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.*;
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
        int i = 20;//000
        if (id.equals("1")) {
            for (int j = 0; j < i; j++) {
                new Thread(() -> {
                    Student student = null;
                    try {
                        student = userService.getStudentByIdAndName(2, "lp");
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                    System.out.println(student);
                }).start();
            }
        } else if (id.equals("2")) {
            for (int j = 0; j < i; j++) {
                new Thread(() -> {
                    Student student = userService.getStudentByIdAndName_lockByRedisson(2, "lp");
                    System.out.println(student);
                }).start();
            }
        } else {
            Student student = null;
            try {
                student = userService.getStudentByIdAndName(2, "lp");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(student);
        }


        return "id=" + id;
    }


    @PostMapping("/grabRp")
    public Map<String, Object> postGrabRedPacket(Long redPacketId, Long userId) {
        //int result = userRedPacketService.grabRedPacket(redPacketId, userId);
        //int result = userRedPacketService.grabRedPacketForVersion(redPacketId, userId);
        //int result = userRedPacketService.grabRedPacketForVersionContinueByTimestamp(redPacketId, userId);
        //int result = userRedPacketService.grabRedPacketForVersionContinueByTimes(redPacketId, userId);
        Long result = userRedPacketService.grapRedPacketByRedis(redPacketId, userId);
        Map<String, Object> retMap = new HashMap<>();
        boolean flag = result > 0;
        retMap.put("success", flag);
        retMap.put("message", flag ? "抢红包成功" : "抢红包失败");
        return retMap;
    }


    @Value("${server.port}")
    private String port;

    private static Integer testData = 10;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisLockService redisLockService;

    @Autowired
    private Redisson redisson;

    @Autowired
    private RedisBloomFilter redisBloomFilter;

    @PostConstruct
    public void init() {
        System.out.println("------------------------------------>springbootapplication starter");
        Set keys = redisTemplate.keys("*");

        //        for (Object o : keys) {
        //            System.out.println(o);
        //
        //        }
        keys.forEach(a -> {
            System.out.println("key--------------------------------->" + a);
            redisBloomFilter.put(a.toString());
        });


    }


    // redisson 测试分布式锁；
    // 实现步骤：1.pom.xml 2.配置bean 3.当前代码
    @GetMapping("/testLockByRedisson")
    public String testLockByRedisson(String id) {

        String lockKey = "productKeyByRedisson";
        RLock rlock = redisson.getLock(lockKey);
        try {
            rlock.lock();
            Object stock = redisTemplate.opsForValue().get("stock");
            if (stock != null && Integer.parseInt(stock.toString()) > 0) {
                int _stock = 0;
                _stock = Integer.parseInt(stock.toString());
                _stock--;
                redisTemplate.opsForValue().set("stock", String.valueOf(_stock));
                System.out.println("当前商品库存还剩：" + _stock);
            } else {
                System.out.println("当前商品库存不存在");
            }

            rlock.unlock();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return "no";
    }

    @GetMapping("/testLock")
    public String testLock(String id) {

        //        Object obj = new Object();
        //        synchronized (obj) {
        //            if (testData <= 0) {
        //                return "消费完成";
        //            }
        //
        //            testData--;
        //            System.out.println("---------------->当前商品剩余的数量：" + testData + "，当前请求id="+id+",当前端口："+port);
        //
        //        }

        long timeout = 30 * 1000;
        long val = System.currentTimeMillis() + timeout;
        String lockK = "productKey";
        try {
            if (!redisLockService.lock(lockK, String.valueOf(val))) {
                //System.out.println("并发太多了");
                return "并发太多了";
            }

            Object stock = redisTemplate.opsForValue().get("stock");
            if (stock != null && Integer.parseInt(stock.toString()) > 0) {
                int _stock = 0;
                _stock = Integer.parseInt(stock.toString());
                _stock--;
                redisTemplate.opsForValue().set("stock", String.valueOf(_stock));
                System.out.println("当前商品库存还剩：" + _stock);
            } else {
                System.out.println("当前商品库存不存在");
            }

            redisLockService.unlock(lockK, String.valueOf(val));
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return "no";
    }


}
