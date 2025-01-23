package com.example.threadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Spring Boot 管理外にあるスレッドから monitor を呼び出してしまう、間違った実装例。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Test1Worker {
    private static final String WORKER_NAME = "worker1";
    private static final long INITIAL_DELAY = 0;
    private static final long INTERVAL_MILLIS = 1000;

    private final RedisTemplate<String, Object> redisTemplate;

    // アプリケーション初期化後に実行される。
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        log.info(WORKER_NAME + ": start");

        // 新規スレッドで定期実行
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(WORKER_NAME);
            return thread;
        });

        scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    try {
                        monitor();
                    } catch (Exception e) {
                        log.error(WORKER_NAME + ": monitoring error", e);
                    }
                }
                , INITIAL_DELAY, INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    public void monitor() {
        log.info(WORKER_NAME + ": monitor start");

        Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(WORKER_NAME, "locked", 1, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isLocked)) {
            try {
                log.info(WORKER_NAME + ": ロックを確保しました");
                // ロックを確保した後の処理をここに追加
            } finally {
                redisTemplate.delete(WORKER_NAME);
                log.info(WORKER_NAME + ": ロックを解放しました");
            }
        } else {
            log.info(WORKER_NAME + ": ロックを確保できませんでした");
        }
    }
}
