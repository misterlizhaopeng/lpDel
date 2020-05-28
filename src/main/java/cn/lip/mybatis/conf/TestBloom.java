package cn.lip.mybatis.conf;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.Test;


/*
当前项目，测试bloomfilter步骤：
    1.引入pom.xml
         <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>19.0</version>
        </dependency>

    [测试第 1 个文件，参考文章-https://www.cnblogs.com/CodeBear/p/10911177.html]
    2.当前类问第一个测试文件，方法：testBloomFilter

    [测试第 2 个文件，参考文章-https://blog.csdn.net/weixin_36507118/article/details/100667442]
    3.测试类文件 RedisBloomFilter 为第二个测试文件，类 RedisBloomFilter 暴露除了两个方法：put、isExist，方法：testBF







*/
public class TestBloom {

    private static int size = 1000000;//预计要插入多少数据

    private static double fpp = 0.0001;//期望的误判率

    private static BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), size, fpp);

    @Test
    public void testBloomFilter() {

        //插入数据
        for (int i = 0; i < 1000000; i++) {
            bloomFilter.put(i);
        }
        int count = 0;
        for (int i = 3000000; i < 4000000; i++) {
            if (bloomFilter.mightContain(i)) {
                count++;
                System.out.println(i + "误判了");
            }
        }
        System.out.println("总共的误判数:" + count);
    }


    @Test
    public void testBF() {
        RedisBloomFilter redisBloomFilter = new RedisBloomFilter();
        redisBloomFilter.setExpectedInsertions(1000);
        redisBloomFilter.setFpp(0.001F);
        redisBloomFilter.put("a");
        redisBloomFilter.isExist("a");
    }
}
