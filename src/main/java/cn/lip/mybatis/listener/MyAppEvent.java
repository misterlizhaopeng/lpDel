package cn.lip.mybatis.listener;

import org.springframework.context.ApplicationEvent;

public class MyAppEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MyAppEvent(Object source) {
        super(source);
    }
}
