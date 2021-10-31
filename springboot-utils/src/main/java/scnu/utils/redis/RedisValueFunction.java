package scnu.utils.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author M1Q84
 * @create 2021年 08月 10日 21:54
 * 获取数据
 */
@Slf4j
@Component
public class RedisValueFunction {

    @Resource
    private RedisTemplate redisTemplate;

    // 默认空值超时时间，5S
    private final static long DEFAULT_EMPTY_TIMEOUT = 5;

    // 默认数据超时时间，30分钟
    private final static long DEFAULT_DATA_TIMEOUT = 30;

    /**
     * 获取 Redis 参数，数据不会设置过期时间
     * 如果未获取到数据，从数据库中获取
     * 如果数据库中也获取不到，则设置空值标识
     * @param key Redis Key
     * @param operatorInterface 接口函数（数据库查询）
     * @param <T> 返回数据类型
     * @return
     */
    public <T> T getNotTimeout(String key, OperatorInterface<T> operatorInterface) {
        return get(key, 0, null, operatorInterface);
    }

    /**
     * 获取 Redis 参数
     * 如果未获取到数据，从数据库中获取
     * 如果数据库中也获取不到，则设置空值标识
     * @param key Redis Key
     * @param operatorInterface 接口函数（数据库查询）
     * @param <T> 返回数据类型
     * @return
     */
    public <T> T get(String key, OperatorInterface<T> operatorInterface) {
        return get(key, DEFAULT_DATA_TIMEOUT, operatorInterface);
    }

    /**
     * 获取 Redis 参数
     * 如果未获取到数据，从数据库中获取
     * 如果数据库中也获取不到，则设置空值标识
     * @param key Redis Key
     * @param timeout 超时时间，单位分钟，当值 <= 0 时表示永不过期
     * @param operatorInterface 接口函数（数据库查询）
     * @param <T> 返回数据类型
     * @return
     */
    public <T> T get(String key, long timeout, OperatorInterface<T> operatorInterface) {
        return get(key, timeout, TimeUnit.MINUTES, operatorInterface);
    }

    /**
     * 获取 Redis 参数
     * 如果未获取到数据，从数据库中获取
     * 如果数据库中也获取不到，则设置空值标识
     * @param key Redis Key
     * @param timeout 超时时间，当值 <= 0 时表示永不过期
     * @param timeUnit 超时时间单位
     * @param operatorInterface 接口函数（数据库查询）
     * @param <T> 返回数据类型
     * @return
     */
    public <T> T get(String key, long timeout, TimeUnit timeUnit, OperatorInterface<T> operatorInterface) {
        if (redisTemplate.hasKey(key)) {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj != null && obj instanceof String && "EmptyMark".equals(obj)) {
                log.info("{}：EmptyMark", key);
                return null;
            }
            return (T) obj;
        } else {
            // 调用数据库查询接口函数
            T data = operatorInterface.apply();
            if (data == null) {
                // 设置空标识
                redisTemplate.opsForValue().set(key, "EmptyMark", DEFAULT_EMPTY_TIMEOUT, TimeUnit.SECONDS);
                log.info("{}：EmptyMark", key);
                return null;
            }
            if (timeout <= 0) {
                redisTemplate.opsForValue().set(key, data);
            } else {
                redisTemplate.opsForValue().set(key, data, timeout, timeUnit);
            }
            return data;
        }
    }
}

