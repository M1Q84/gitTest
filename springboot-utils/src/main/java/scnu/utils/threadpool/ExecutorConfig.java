package scnu.utils.threadpool;

import cn.hutool.core.thread.ExecutorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author M1Q84
 * @create 2021年 08月 10日 21:04
 */
@Slf4j
@Component
public class ExecutorConfig {

    // (核心)初始线程数量
    private final static int DEFAULT_NUM = 5;

    // 最大线程数
    private final static int MAX_NUM = 10;

    // 最大等待线程数(即工作队列最大容量)
    private final static int MAX_WAITING = 100;

    @Bean
    public ExecutorService createExecutor() {
        ExecutorService executor = ExecutorBuilder.create()
                // 默认初始化 5 个线程
                .setCorePoolSize(DEFAULT_NUM)
                // 最大线程数 10
                .setMaxPoolSize(MAX_NUM)
                // 最大等待线程数 100
                .setWorkQueue(new LinkedBlockingQueue<>(MAX_WAITING))
                .build();
        log.info("\n初始化线程池\n默认初始线程数：{}\n最大线程数：{}\n最大等待线程数：{}", DEFAULT_NUM, MAX_NUM, MAX_WAITING);
        return executor;
    }
}
