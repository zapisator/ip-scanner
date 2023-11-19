package org.example.domain.thread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.certificate.CertificateService;
import org.example.infastructure.data.ConcurrentQueueProcessor;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@RequiredArgsConstructor
@Slf4j
public class SSLCertificateCheckerRunnable implements Runnable {
    private final List<String> ipAddresses;
    private final ConcurrentQueueProcessor queueProcessor;
    private final CertificateService certificateService;
    private final CountDownLatch countDownLatch;

    @Override
    public void run() {
        log.info(
                "Нить '{}'. Запуск SSLCertificateCheckerRunnable для IP-адресов в размере: {}",
                Thread.currentThread().getId(), ipAddresses.size()
        );
        certificateService
                .certificateDomains(ipAddresses)
                .forEach(queueProcessor::enqueue);
        countDownLatch.countDown();
        log.info(
                "Нить '{}'. Завершение работы. Работающих сканеров осталось '{}'",
                Thread.currentThread().getId(), countDownLatch.getCount()
        );
    }
}