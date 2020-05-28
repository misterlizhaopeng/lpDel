package cn.lip.mybatis.conf;

import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import io.reactivex.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;


/*
众所周知，google的guava框架实现了BloomFilter，guava的BloomFilter和redis的bitMap都是基于位图算法的，所以redis也可以实现BloomFilter，
并且相对于BloomFilter，redis的数据存在三方redis服务器上的，并不像guava的BloomFilter是存在本地的，
这对于内存损耗及分布式系统来说显然是不适合的，所以今天分享一个基于redis实现的BloomFilter；

大致流程为：
1：根据预计插入量及可接受的错误率计算出bit数组长度及hash函数数量。

2：将key值hash后，根据hash函数的数量，计算出这个key的不同的下标数组，用于匹配key值。

3：遍历key值的下标，将相同的值（bf:hilite）根据下标的值，转存为对应下标值长度的二进制数存入bitmap。

4：判断时，将key按相同方式转换为下标数组，通过 getBit（）方法判断是否存在。

看代码也可以知道hash函数数量numHashFunctions与预计插入量expectedInsertions无关，与可接受的错误率fpp成反比，
bit数组长度numBits与预计插入量expectedInsertions成正比，与可接受的错误率fpp成反比。

*/

@ConfigurationProperties("bloom.filter")
@Component
public class RedisBloomFilter {

    @Autowired
    private RedisTemplate redisTemplate;


    //预计插入量
    private long expectedInsertions;
    //可接受的错误率
    private double fpp;
    //bit数组长度
    private long numBits;
    //hash函数数量
    private int numHashFunctions ;

    @PostConstruct
    public void init(){
        this.numBits = optimalNumOfBits(expectedInsertions, fpp);
        this.numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
    }
 
    //计算hash函数个数
    private int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }
 
    //计算bit数组长度
    private long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }
 
    /**
     * 判断keys是否存在于集合
     */
    public boolean isExist(String key) {
        long[] indexs = getIndexs(key);
        List list = redisTemplate.executePipelined(new RedisCallback<Object>() {
 
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.openPipeline();
                for (long index : indexs) {
                    redisConnection.getBit("bf:hilite".getBytes(), index);
                }
                redisConnection.close();
                return null;
            }
        });
        return !list.contains(false);
    }
 
    /**
     * 将key存入redis bitmap
     */
    public void put(String key) {
        long[] indexs = getIndexs(key);
        redisTemplate.executePipelined(new RedisCallback<Object>() {
 
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.openPipeline();
                for (long index : indexs) {
                    redisConnection.setBit("bf:hilite".getBytes(),index,true);
                }
                redisConnection.close();
                return null;
            }
        });
    }
 
    /**
     * 根据key获取bitmap下标
     */
    private long[] getIndexs(String key) {
        long hash1 = hash(key);
        long hash2 = hash1 >>> 16;
        long[] result = new long[numHashFunctions];
        for (int i = 0; i < numHashFunctions; i++) {
            long combinedHash = hash1 + i * hash2;
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }
            result[i] = combinedHash % numBits;
        }
        return result;
    }
 
    /**
     * 获取一个hash值
     */
    private long hash(String key) {
        Charset charset = Charset.forName("UTF-8");
        return Hashing.murmur3_128().hashObject(key, Funnels.stringFunnel(charset)).asLong();
    }




    public long getExpectedInsertions() {
        return expectedInsertions;
    }

    public void setExpectedInsertions(long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public void setFpp(double fpp) {
        this.fpp = fpp;
    }

    public double getFpp() {
        return fpp;
    }
}