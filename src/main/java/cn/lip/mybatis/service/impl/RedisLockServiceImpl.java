package cn.lip.mybatis.service.impl;

import cn.lip.mybatis.service.RedisLockService;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 方案1：redis 实现分布式锁
 *
 * 已经规避的问题：
 *
 *      1.假如加锁成功之后，业务逻辑抛异常，不能正常解锁，会产生什么问题，出现死锁；解决方案，给锁加一个过期时间
 *      2.命令setnx和命令expire分开执行，setnx成功，expire失败，可能不会释放锁，出现死锁；解决方案，原子操作（当前解决方案为把过期时间设置value；也可以通过lua脚本实现）
 *      3.当前线程加的锁被别的线程释放了，也会导致数据不一致的问题，解决方案：给当前锁的值加一个版本，保证指定一个线程拥有一个版本，当前解决方案为getset命令实现的；
 *
 *
 *
 * 没有规避的问题：
 *      1.锁的过期时间小于业务执行时间，也就是业务没有执行完成，锁过期了，这样一个业务线程没有执行完，另一个业务线程又启动了业务执行，这样会造成数据不一致的问题，so，如何设置锁的过期时间更合理？
 *           如果把锁的过期时间设置太长,不太好，因为业务抛出异常，下一个线程再次获取锁需要交长的等待，太短可能会产生上面所说的问题；so，锁的过期时间太长太短都不好控制；
 *
 *           redisson 解决方案有个姐姐过期时间的问题，先把当前锁的过期时间默认为设置为30s，对锁设置过期时间之后，然后生成一个后台进程，会在锁的过期时间1/3的时间之后，判断当前线程在执行
 *           业务代码中是否持有当前的锁,如果是，则再设置指定的过期时间以延长过期时间，以保证分布式锁的过期时间永远大于业务代码的执行时间，进而保证了多线程下业务被串行访问；
 *
 *      redis服务器方面的问题：
 *
 *      2.当前redis服务如果采用哨兵模式，线程A成功把锁在master redis服务上面产生，并在上面设置好锁之后，同步到slave redis服务器之前，master宕机了，
 *          这时slave被选为master，另一个线程B再加锁的时候有成功获取锁，此时线程A还没有结束业务逻辑，就会造成锁被获取多次的问题；
 *          这个问题，在redisson解决方案下面针对每一个机器添加一个相同的锁即可；
 *      3.锁和分布式锁性能的问题；
 *              如何在有锁的情况下提高分布式锁的性能？
 *
 */
@Service
@Slf4j
public class RedisLockServiceImpl implements RedisLockService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 加锁
     *      下面加锁代码分三步理解：
     *          1.setnx 加锁，加锁成功返回
     *          2.如果setnx加锁失败，获取当前key的值查看是否过期，过期执行第三步，没过期返回false，表示加锁失败
     *          3.getset 加锁，加锁成功返回
     *              利用getset命令获取旧值，设置新值，如果旧值和执行getset命令之前执行的get命令获取的值相同，表加锁成功
     *
     * @param key   对key实现分布式锁
     * @param value 当前时间 + 超时时间
     * @return 返回true 表示加锁成功，返回false 表示加锁失败
     */
    @Override
    public boolean lock(String key, String value) {
        // setIfAbsent 表示redis中的setnx命令
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(key, value);
        if (ifAbsent) {
            //返回true，表示加锁成功！因为 setnx key value 命令执行成功 返回true ;
            return true;
        }

        //当锁过期，多线程下继续加锁- start

        // （start-end 这段代码的作用很重要）,如果没有当前这段代码，当调用昂前lock成功之后，执行业务代码，
        // 在释放锁之前执行业务代码报出了一个异常，其他的线程认为当前锁一直被占用不能获取锁，俗称死锁！

        Object currentValue = redisTemplate.opsForValue().get(key);
        if (currentValue != null && Long.parseLong(currentValue.toString()) < System.currentTimeMillis()) {
            // 表示当前key已经过期，则继续加锁
            Object andSet = redisTemplate.opsForValue().getAndSet(key, value);// 返回的旧值
            if (andSet != null && String.valueOf(andSet).equals(currentValue.toString())) {
                return true;// 表示加锁成功
            }
        }
        //当锁过期，多线程下继续加锁 - end
        return false;
    }

    /**
     * 释放锁
     * @param key
     * @param value
     */
    @Override
    public void unlock(String key, String value) {
        try {
            Object o = redisTemplate.opsForValue().get(key);
            if (o!=null&&value.equals(o.toString())){
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        }
        catch (Exception ex){
            System.out.println("redis 分布式锁出现异常："+ex);
        }

    }
}
