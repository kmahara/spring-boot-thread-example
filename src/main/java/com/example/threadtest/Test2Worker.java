package com.example.threadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Async を使う例。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Test2Worker {
    private static final String WORKER_NAME = "worker2";
    private static final long INITIAL_DELAY = 0;
    private static final long INTERVAL_MILLIS = 1000;

    private final RedisTemplate<String, Object> redisTemplate;

    // アプリケーション初期化後に実行される。
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        createMonitorThread();
    }

    @Async
    public void createMonitorThread() {
        Thread.currentThread().setName(WORKER_NAME);

        log.info(WORKER_NAME + ": start");

        try {
            Thread.sleep(INITIAL_DELAY);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    monitor();
                } catch (Exception e) {
                    log.error(WORKER_NAME + ": error", e);
                    // 続行する。
                }
                Thread.sleep(INTERVAL_MILLIS);
            }
        } catch (InterruptedException e) {
            log.info(WORKER_NAME + ": interrupted");
        }

        log.info(WORKER_NAME + ": end");
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
