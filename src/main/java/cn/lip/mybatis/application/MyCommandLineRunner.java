package cn.lip.mybatis.application;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MyCommandLineRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("MyCommandLineRunner-start");
        List<String> list = Arrays.asList(args);
        list.forEach(a->{
            System.out.println(a);
        });
        System.out.println("MyCommandLineRunner-end");
    }
}
