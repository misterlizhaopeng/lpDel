package cn.lip.mybatis.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MyApplicationRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
//        System.out.println("MyApplicationRunner-start");
//        List<String> list = Arrays.asList(args.getSourceArgs());
//        list.forEach(a -> {
//            System.out.println(a);
//        });
//        System.out.println("MyApplicationRunner-end");
//        System.out.println("==========" + args.getOptionValues("nasssme"));
//        System.out.println("---------------->"+args.getOptionValues("nasssme"));




        System.out.println("MyApplicationRunner-start");
        System.out.println(args.getOptionNames());
        System.out.println(Arrays.asList(args.getSourceArgs()));
        System.out.println(Arrays.asList(args.getNonOptionArgs()));
        System.out.println("---------------->"+args.getOptionValues("aax"));
        System.out.println("MyApplicationRunner-end");




    }
}
