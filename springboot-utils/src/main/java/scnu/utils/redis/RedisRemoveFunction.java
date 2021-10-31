package scnu.utils.redis;

/**
 * @author M1Q84
 * @create 2021年 08月 10日 21:12
 * Redis 延时双删政策
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
@Slf4j
@Component
public class RedisRemoveFunction {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ExecutorService executorService;

    // 延迟删除 1s
    private final static long REMOVE_DELAY = 1000;

    // 失败重试次数
    private final static int FAIL_RETRY = 5;

    /**
     * 删除 Redis 缓存
     * 如果未获取到数据，从数据库中获取
     * 如果数据库中也获取不到，则设置空值标识
     * @param key Redis Key
     * @return
     */
    public void remove(String key) {
        remove(key, null);
    }

    /**
     * 删除 Redis 缓存，并进行数据库操作
     * 如果未获取到数据，从数据库中获取
     * 如果数据库中也获取不到，则设置空值标识
     * @param key Redis Key
     * @param operatorInterface 接口函数（数据库更新）
     * @param <T> 返回数据类型
     * @return
     */
    public <T> T remove(String key, OperatorInterface<T> operatorInterface) {

        // 第一次删除，清除缓存
        redisTemplate.delete(key);
        T data = null;
        // 不为空说明进行数据库操作
        if (operatorInterface != null) {
            data = operatorInterface.apply();
        }

        // 创建线程进行二次删除，清除 1S 内的脏数据，注意是另开一个线程，避免让每个调用此方法的线程都等待一秒
        executorService.execute(() -> {
            // 失败重试
            int failRetry= FAIL_RETRY;
            while (failRetry-- > 0) {
                System.out.println("重试");
                try {
                    Thread.sleep(REMOVE_DELAY);
                    redisTemplate.delete(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return data;
    }
}

