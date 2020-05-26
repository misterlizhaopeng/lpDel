package cn.lip.mybatis.service;

/**
 * redis distribute lock - add by lp
 */
public interface RedisLockService {
    /**
     * 加锁
     * @param key
     * @param value
     * @return
     */
    public boolean lock(String key,String value);

    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unlock(String key,String value);
}
