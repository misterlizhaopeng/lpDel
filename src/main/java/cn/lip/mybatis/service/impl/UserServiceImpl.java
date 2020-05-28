package cn.lip.mybatis.service.impl;

import cn.lip.mybatis.bean.Student;
import cn.lip.mybatis.bean.TbUser;
import cn.lip.mybatis.conf.RedisBloomFilter;
import cn.lip.mybatis.dao.UserDao;
import cn.lip.mybatis.service.RedisLockService;
import cn.lip.mybatis.service.UserService;
import cn.lip.mybatis.util.SerializeUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao a;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisLockService redisLockService;
    @Autowired
    private Redisson redisson;
    @Autowired
    private RedisBloomFilter redisBloomFilter;

    @Override
    public List<TbUser> getAll() {
        System.err.println("getAll-----------------------");
        return a.getUsers();
    }

    //高并发的情况下，如果redis缓存没有数据，那么大部分的请求都会去请求mysql，造成缓存被【击穿】的现象，此方法用分布式锁来解决
    // 设置的值存在hash数据中，并原子性的设置过期时间，有两种方法：1，通过lua脚本；2，通过redistemplate的事务设置；
    @Override
    public Student getStudentByIdAndName(Integer id, String name) throws Exception {
        long timeout = 6 * 1000;
        long val = System.currentTimeMillis() + timeout;
        String lockK = "getStudentByIdAndName:" + id;//表示给一个id加一个锁
        Student studentTmp = null;


        //redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
        String key = "student:id:" + id;

        //Object o = redisTemplate.opsForHash().get(key, String.valueOf(id));
        String hget = jedis.hget(key, String.valueOf(id));
        if (hget != null) {
            //Object ostu = SerializeUtil.unSerialize(hget.getBytes());
            Object o = SerializeUtils.serializeToObject(hget);

            System.out.println("data from------------------------------->redis");
            studentTmp = (Student) o;
        } else {

            //加锁
            if (!redisLockService.lock(lockK, String.valueOf(val))) {
                System.out.println("------------" + Thread.currentThread().getName() + "-------------------------->并发太多了，遇到分布式锁，返回null了");
                //表示没有获得分布式锁，此处直接返回了，意思是当前线程无效，也就是不会获取到数据：
                return null;
            }


            // 加完锁之后，再次查询一次缓存，原因：就是如果当大量线程堆积到第一次查询缓存之后，lock之外的情况下，
            // 如果lock之内不加查询缓存查询，被堆积的每个线程都会查询一下数据库
            //Object o1 = redisTemplate.opsForHash().get(key, String.valueOf(id));
            String hget1 = jedis.hget(key, String.valueOf(id));
            if (hget1 != null) {
                //Object ostu1 = SerializeUtil.unSerialize(hget1.getBytes());
                Object o1 = SerializeUtils.serializeToObject(hget1);
                System.out.println("data from------------------------------->redis");
                studentTmp = (Student) o1;
            } else {
                studentTmp = a.getStudentByIdAndName(id, name);
//                redisTemplate.opsForHash().put(key, String.valueOf(id), studentTmp); //.set(key, studentTmp);
//                redisTemplate.expire(key, 10, TimeUnit.SECONDS);
                int _timeout = 100;
                String serialize = SerializeUtils.serialize(studentTmp);


                String luaStr = "redis.call('hset','student:id:'..KEYS[1],KEYS[1],ARGV[1])  redis.call('expire' ,'student:id:'..KEYS[1]," + _timeout + ")";
                jedis.eval(luaStr, 1, String.valueOf(id), serialize);

                System.out.println("data from------------------------------------->mysql");
            }
            //解锁
            redisLockService.unlock(lockK, String.valueOf(val));
        }


        return studentTmp;

    }


    @Override
    public Student getStudentByIdAndName_lockByRedisson(Integer id, String name) {
        String lockK = "getStudentByIdAndName_lockByRedisson:" + id;//表示给一个id加一个锁
        RLock rlock = redisson.getLock(lockK);

        Student studentTmp = null;

        String key = "student:id:" + id;
        //布隆过滤器先对当前的key判断一下，看看当前的key是否在过滤器里面
        //注意：使用布隆过滤器之前，要把要找的所有的key的hash位值，添加到bitmap中；
        //      在添加修改数据的时候，也要把要通过redis查询的key添加到bitmap中；
        boolean exist = redisBloomFilter.isExist(key);
        if (!exist)
        {
            Student student = new Student();
            student.setId(-1);
            student.setAge(-1);
            student.setName("你找的数据不存在");
            return student;
        }

        Object o = redisTemplate.opsForHash().get(key, String.valueOf(id));
        if (o != null) {
            System.out.println("data from------------------------------->redis");
            studentTmp = (Student) o;
        } else {
            //加锁
            rlock.lock();


            Object o1 = redisTemplate.opsForHash().get(key, String.valueOf(id));
            if (o1 != null) {
                System.out.println("data from------------------------------->redis second done");
                studentTmp = (Student) o1;
            } else {
                studentTmp = a.getStudentByIdAndName(id, name);
                redisTemplate.opsForHash().put(key, String.valueOf(id), studentTmp); //.set(key, studentTmp);
                redisBloomFilter.put(key);
                System.out.println("data from------------------------------------->mysql");
            }


            //解锁
            rlock.unlock();
        }

        return studentTmp;

    }

}
