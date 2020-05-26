package cn.lip.mybatis.conf;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate redisTemplate(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 最大空闲数
        poolConfig.setMaxIdle(50);
        // 最大连接数
        poolConfig.setMaxTotal(100);
        // 最大等待毫秒数
        poolConfig.setMaxWaitMillis(20000);
        // 创建Jedis连接工厂
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(poolConfig);
        connectionFactory.setHostName("192.168.25.140");
        connectionFactory.setPort(6380);
        connectionFactory.setPassword("lp");
        // 调用后初始化方法，没有它将抛出异常，此处要注意，因为对象JedisConnectionFactory不是spring实例化，所以，此处要人为调用
        connectionFactory.afterPropertiesSet();
        // 自定Redis序列化器
        RedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        RedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // 定义RedisTemplate，并设置连接工程
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        // 设置序列化器
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        //redisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);
        return redisTemplate;
    }


    @Bean
    public Redisson redissonClient() {
        Config config = new Config();

        config.useSingleServer().setPassword("lp").setAddress("redis://192.168.25.140:6380").setDatabase(0);
//        config.useClusterServers()
//                .setScanInterval(2000)
//                .addNodeAddress("redis://192.168.25.140:7001", "redis://192.168.25.140:7001")
//                .addNodeAddress("redis://192.168.25.140:7001");

        return (Redisson)Redisson.create(config);
    }
}
