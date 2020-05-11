package cn.lip.mybatis.plugin;

import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

@Intercepts(@Signature(type = StatementHandler.class, method = "parameterize", args = java.sql.Statement.class))
public class TestPlugin implements Interceptor {

    /**
     * 拦截器：拦截目标对象的方法的执行
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("cn.lip.mybatis.plugin.TestPlugin.intercept is invoked !......");
        Object proceed = invocation.proceed();
        return proceed;
    }

    /**
     * 包装目标对象，为目标对象创建一个代理对象
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        System.out.println("mybatis 将要包装的对象" + target);
        Object wrap = Plugin.wrap(target, this);
        return wrap;
    }

    /**
     * 将插件注册时候的属性设置进来
     *
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        System.out.println(properties);
    }

}
