package org.example.domain.thread;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.infastructure.data.ConcurrentQueueProcessor;

import java.util.concurrent.CountDownLatch;

@RequiredArgsConstructor
@Slf4j
public class QueueProcessorRunnable implements Runnable {

    private final ConcurrentQueueProcessor queueProcessor;
    private final CountDownLatch countDownLatch;

    @Override
    @SneakyThrows
    public void run() {
        while (!queueProcessor.isQueueEmpty() || countDownLatch.getCount() > 0) {
            log.info("Нить '{}'. Запуск обработки очереди. Очередь пуста: {}. Чилсо работающих сканеров: `{}`", Thread.currentThread().getId(), queueProcessor.isQueueEmpty(), countDownLatch.getCount());
            queueProcessor.processQueue();
        }
    }

}
