package cn.lip.mybatis.service.impl;

import cn.lip.mybatis.bean.RedPacket;
import cn.lip.mybatis.bean.UserRedPacket;
import cn.lip.mybatis.dao.RedPacketMapper;
import cn.lip.mybatis.dao.UserRedPacketMapper;
import cn.lip.mybatis.service.RedisRedPacketService;
import cn.lip.mybatis.service.UserRedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class UserRedPacketServiceImpl implements UserRedPacketService {

    @Autowired
    private UserRedPacketMapper userRedPacketDao;

    @Autowired
    private RedPacketMapper redPacketDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisRedPacketService redisRedPacketService;

    // 失败
    private static final int FAILED = 0;

    // 隔离级别 为SERIALIZABLE 数据库中的数据完全正确，其他的都会出现错误；
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)//READ_COMMITTED
    public int grabRedPacket(Long redPacketId, Long userId) {
        //获取红包信息
        // 	当前方法的数据隔离级别为读提交，避免了数据库线程读脏数据的情况，但是在高并发的场景下，会出现超发的情况，也就是数据不一致的情况，下面解决办法：
        //		解决办法：①悲观锁机制；②乐观锁机制(会有丢失更新的情况，重入机制减少丢失更新);
        //RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);

        // ①悲观锁机制
        //		表引擎为innodb，根据主键查询的 for update sql，查询的行存在行级别的锁，并发下多个线程操作之，是顺序性的，但有锁则性能会下降，少量并发（本机测试千级别）可以使用！！
        //		讨论下当前悲观锁导致性能下降的原因：因为悲观锁的情况下，如果存在大量线程共享数据，当一个线程锁定了资源，其他线程都要挂起等待该线程提交释放资源锁，这样会导致cpu多线程来回挂起恢复的切换，
        //		这就是悲观锁在高并发情况下性能慢的主要原因；为了解决cpu来回切换，乐观锁机制在企业中被大量应用；
        RedPacket redPacket = redPacketDao.getRedPacketForUpdate(redPacketId);

        // 当前小红包库存大于0
        if (redPacket.getStock() > 0) {
            redPacketDao.decreaseRedPacket(redPacketId);
            // 生成抢红包信息
            UserRedPacket userRedPacket = new UserRedPacket();
            userRedPacket.setRedPacketId(redPacketId);
            userRedPacket.setUserId(userId);
            userRedPacket.setAmount(redPacket.getUnitAmount());
            userRedPacket.setNote("抢红包 " + redPacketId);
            // 插入抢红包信息
            int result = userRedPacketDao.grabRedPacket(userRedPacket);
            return result;
        }
        // 失败返回
        return FAILED;
    }

    // ②乐观锁机制（无重入）
    //		无重入的情况下，会导致部分线程更新数据会失败，但是这样就没有了cpu来回切换的性能损耗，这是乐观锁的好处；
    //		经过当前2000个并发测试，乐观锁机制下抢红包的时间和悲观锁机制下抢红包的时间差不多，甚至还没有悲观锁快，但是从原理上来说，乐观锁的确降低了cpu的挂起恢复来回切换的性能；
    //
    // 重入：因为乐观锁会导致大量的丢失更新，所以要重入，意思就是重新尝试操作数据，此例为重新抢红包，
    //      重入类型有：时间戳，比如100s内再去重试执行；按规定的次数，比如规定执行三次
    //

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int grabRedPacketForVersion(Long redPacketId, Long userId) {
        // 获取红包信息,注意version值
        RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);
        // 当前小红包库存大于0
        if (redPacket.getStock() > 0) {
            // 再次传入线程保存的version旧值给SQL判断，是否有其他线程修改过数据
            int update = redPacketDao.decreaseRedPacketForVersion(redPacketId, redPacket.getVersion());
            // 如果没有数据更新，则说明其他线程已经修改过数据，本次抢红包失败
            if (update == 0) {
                return FAILED;
            }
            // 生成抢红包信息
            UserRedPacket userRedPacket = new UserRedPacket();
            userRedPacket.setRedPacketId(redPacketId);
            userRedPacket.setUserId(userId);
            userRedPacket.setAmount(redPacket.getUnitAmount());
            userRedPacket.setNote("抢红包 " + redPacketId);
            // 插入抢红包信息
            int result = userRedPacketDao.grabRedPacket(userRedPacket);
            return result;
        }
        // 失败返回
        return FAILED;
    }

    // ②乐观锁机制（重入-时间戳：100ms）
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int grabRedPacketForVersionContinueByTimestamp(Long redPacketId, Long userId) {
        long start = System.currentTimeMillis();
        while (true) {
            long end = System.currentTimeMillis();
            if (end - start >= 10)
                return FAILED;

            // 获取红包信息,注意version值
            RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);
            // 当前小红包库存大于0
            if (redPacket.getStock() > 0) {
                // 再次传入线程保存的version旧值给SQL判断，是否有其他线程修改过数据
                int update = redPacketDao.decreaseRedPacketForVersion(redPacketId, redPacket.getVersion());
                // 如果没有数据更新，则说明其他线程已经修改过数据，再次重入
                if (update == 0) {
                    //return FAILED;
                    continue;
                }
                // 生成抢红包信息
                UserRedPacket userRedPacket = new UserRedPacket();
                userRedPacket.setRedPacketId(redPacketId);
                userRedPacket.setUserId(userId);
                userRedPacket.setAmount(redPacket.getUnitAmount());
                userRedPacket.setNote("抢红包 " + redPacketId);
                // 插入抢红包信息
                int result = userRedPacketDao.grabRedPacket(userRedPacket);
                return result;
            } else {
                return FAILED;
            }
        }
    }

    // ②乐观锁机制（重入-设置次数：比如三次等）
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int grabRedPacketForVersionContinueByTimes(Long redPacketId, Long userId) {
        int times = 3;
        for (int i = 0; i < times; i++) {
            // 获取红包信息,注意version值
            RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);
            // 当前小红包库存大于0
            if (redPacket.getStock() > 0) {
                // 再次传入线程保存的version旧值给SQL判断，是否有其他线程修改过数据
                int update = redPacketDao.decreaseRedPacketForVersion(redPacketId, redPacket.getVersion());
                // 如果没有数据更新，则说明其他线程已经修改过数据，再次重入
                if (update == 0) {
                    //return FAILED;
                    continue;
                }
                // 生成抢红包信息
                UserRedPacket userRedPacket = new UserRedPacket();
                userRedPacket.setRedPacketId(redPacketId);
                userRedPacket.setUserId(userId);
                userRedPacket.setAmount(redPacket.getUnitAmount());
                userRedPacket.setNote("抢红包 " + redPacketId);
                // 插入抢红包信息
                int result = userRedPacketDao.grabRedPacket(userRedPacket);
                return result;
            }
        }
        return FAILED;
    }


    // Lua脚本: redPacket 表示当前红包，
    String script = "local listKey = 'red_packet_list_'..KEYS[1] \n"
            + "local redPacket = 'red_packet_'..KEYS[1] \n"
            + "local stock = tonumber(redis.call('hget', redPacket, 'stock')) \n"
            + "if stock <= 0 then return 0 end \n"
            + "stock = stock -1 \n"
            + "redis.call('hset', redPacket, 'stock', tostring(stock)) \n"
            + "redis.call('rpush', listKey, ARGV[1]) \n"
            + "if stock == 0 then return 2 end \n"
            + "return 1 \n";


    // 在缓存LUA 脚本后，使用该变量保存Redis返回的32位的SHA1编码，使用它去执行缓存的LUA 脚本
    String sha1 = null;

    /**
     * 初始化工作:
     * @param redPacketId
     * @param userId
     * @return
     */
    @Override
    public Long grapRedPacketByRedis(Long redPacketId, Long userId) {

        // 当前抢红包用户和日期信息
        String args = userId + "-" + System.currentTimeMillis();
        Long result = null;
        // 获取底层Redis操作对象
        Jedis jedis = null;
        long start=System.currentTimeMillis();
        try {
            jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
            // 如果脚本没有加载过，那么进行加载，这样就会返回一个sha1编码
            if (sha1 == null) {
                sha1 = jedis.scriptLoad(script);
            }
            // 执行脚本，返回结果
            Object res = jedis.evalsha(sha1, 1, redPacketId + "", args);
            result = (Long) res;

            // 返回2时为最后一个红包，此时将抢红包信息通过异步保存到数据库中
            if (result == 2) {
                // 获取单个小红包金额
                String unitAmountStr = jedis.hget("red_packet_" + redPacketId, "unit_amount");
                // 触发保存数据库操作
                Double unitAmount = Double.parseDouble(unitAmountStr);
                System.err.println("thread_name = " + Thread.currentThread().getName());
                long end=System.currentTimeMillis();
                System.err.println("redis执行数据结束，耗时" + (end - start) + "毫秒");

                redisRedPacketService.saveUserRedPacketByRedis(redPacketId, unitAmount);
            }
        } catch (Exception ex) {
            System.out.println("----------------------------------------------------------->" + ex.toString());
        } finally {
            // 确保jedis顺利关闭
            if (jedis != null && jedis.isConnected()) {
                jedis.close();
            }
        }
        return result;
    }

}
