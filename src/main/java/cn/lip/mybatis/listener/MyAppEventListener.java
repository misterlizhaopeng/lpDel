package cn.lip.mybatis.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MyAppEventListener implements ApplicationListener<MyAppEvent> {
    @Override
    public void onApplicationEvent(MyAppEvent event) {
        System.out.println("接收到的事件："+event.getClass());
    }
}
