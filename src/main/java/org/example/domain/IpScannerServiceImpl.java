package org.example.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.certificate.CertificateServiceHttpClient;
import org.example.domain.thread.QueueProcessorRunnable;
import org.example.domain.thread.SSLCertificateCheckerRunnable;
import org.example.infastructure.data.ConcurrentQueueProcessor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.util.InternetUtil.getAllHostsInSubnet;
import static org.example.util.ThreadUtil.getOptimalThreadCount;
import static org.example.util.ThreadUtil.splitIPList;

@Slf4j
@RequiredArgsConstructor
public class IpScannerServiceImpl implements IpScannerService {

    @Override
    public void startScan(String ipAddress, int threadsCount) {
        log.info("Запуск сканирования с IP-адресом: {}", ipAddress);
        final String filename = conformedFilename(ipAddress);
        startScan(ipAddress, threadsCount, filename);
    }

    @Override
    public void startScan(String ipAddress, int threadsCount, String outputFile) {
        final Path path = Paths.get(outputFile);
        if (Files.exists(path) && Files.isReadable(path)) {
            log.info("Обработка уже производилась, файл `{}` результата уже доступен", outputFile);
            return;
        }
        log.info("Запуск сканирования с IP-адресом: {} и {} потоками", ipAddress, threadsCount);
        final int effectiveThreadCount = effectiveThreadCount(threadsCount);
        final List<String> hosts = getAllHostsInSubnet(ipAddress);
        final List<List<String>> threadHosts = splitIPList(hosts, effectiveThreadCount);
        final ConcurrentQueueProcessor queueProcessor = new ConcurrentQueueProcessor(path);

        scanHosts(threadHosts, queueProcessor);
    }

    public String getResult(String ipAddress) {
        final Path path = Paths.get(conformedFilename(ipAddress));
        log.info("Обрабатываем запрос на чтение файла {}", path);

        if (Files.exists(path) && Files.isReadable(path)) {
            try {
                return String.join("\n", Files.readAllLines(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return "";
    }

    @NotNull
    private static String conformedFilename(String ipAddress) {
        return "output-" + ipAddress.replace('/', '_') + ".txt";
    }

    private int effectiveThreadCount(int threadsCount) {
        log.info("Необходимое количество потоков: {}", threadsCount);
        final int threadCountToWrite = 1;
        log.info("Количество потоков для записи в файл: {}", threadCountToWrite);
        final int threadCountForAppManagement = 1;
        log.info(
                "Количество потоков для управления приложением " +
                        "без потоков для управления и записи: {}",
                threadCountForAppManagement
        );
        final int threadsAvailableCount = Math.max(
                1,
                getOptimalThreadCount()
                        - threadCountToWrite
                        - threadCountForAppManagement
        );
        log.info("Доступное количество потоков: {}", threadsAvailableCount);
        final int effectiveThreadCount = Math.min(threadsCount, threadsAvailableCount);
        log.info("Приложение будет использовать это количество потоков: {}", effectiveThreadCount);
        return effectiveThreadCount;
    }

    private void scanHosts(List<List<String>> threadHosts, ConcurrentQueueProcessor queueProcessor) {
        log.info("Запуск сканирования хостов, добавление сканнеров и писателя.");
        final int numThreads = threadHosts.size();
        final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        final CountDownLatch countDownLatch = new CountDownLatch(threadHosts.size());

        addScanners(threadHosts, queueProcessor, executorService, countDownLatch);
        addWriter(queueProcessor, executorService, countDownLatch);
        executorService.shutdown();
        log.info("Задача по сканированию завершена");
    }

    private static void addWriter(
            ConcurrentQueueProcessor queueProcessor,
            ExecutorService executorService,
            CountDownLatch countDownLatch
    ) {
        log.info("Добавление записи нити в файл");
        QueueProcessorRunnable queueProcessorRunnable = new QueueProcessorRunnable(
                queueProcessor, countDownLatch
        );
        executorService.submit(queueProcessorRunnable);
    }

    private void addScanners(
            List<List<String>> threadHosts,
            ConcurrentQueueProcessor queueProcessor,
            ExecutorService executorService,
            CountDownLatch countDownLatch
    ) {
        log.info("Добавление сканнеров в сервис исполнения нитей в количестве '{}'", countDownLatch.getCount());

        threadHosts.forEach(hosts ->
                executorService.submit(
                        new SSLCertificateCheckerRunnable(
                                hosts,
                                queueProcessor,
                                new CertificateServiceHttpClient(),
                                countDownLatch
                        )
                )
        );
    }

    public static void main(String[] args) {
        final List<String> ipAddresses = Arrays.asList(
                "151.101.193.164",
                "151.101.129.164",
                "151.101.65.164",
                "151.101.1.164",
                "151.101.64.81",
                "151.101.128.81",
                "151.101.0.81",
                "151.101.192.81",
                "151.101.131.5",
                "151.101.67.5",
                "151.101.195.5",
                "151.101.3.5",
                "15.197.146.156",
                "3.33.146.110",
                "31.13.94.35",
                "20.76.201.171",
                "20.236.44.162",
                "20.231.239.246",
                "20.112.250.133",
                "20.70.246.20",
                "151.101.65.140",
                "151.101.129.140",
                "151.101.1.140",
                "151.101.193.140",
                "74.6.231.20",
                "98.137.11.164",
                "74.6.143.26",
                "74.6.231.21",
                "74.6.143.25",
                "98.137.11.163",
                "52.202.102.163",
                "52.204.218.180",
                "52.44.86.253",
                "52.45.145.105",
                "52.71.110.200",
                "44.208.137.18",
                "52.6.211.132",
                "52.72.199.188",
                "13.107.42.14",
                "31.13.94.174",
                "20.201.28.151",
                "34.213.106.51",
                "54.68.182.72",
                "17.253.144.10",
                "66.211.166.223",
                "66.211.162.136",
                "66.211.163.120",
                "209.140.139.232",
                "209.140.136.23",
                "209.140.136.254",
                "192.0.77.40",
                "23.2.108.26",


                "208.80.153.224",
                "181.30.145.8",
                "181.30.145.32",
                "184.31.3.130",
                "23.1.99.130",
                "104.109.11.129",
                "23.1.35.132",
                "184.31.10.133",
                "23.1.106.133",
                "184.25.179.132",
                "104.109.10.129",
                "52.94.228.167",
                "52.94.237.74",
                "52.94.225.248",
                "151.101.128.84",
                "151.101.192.84",
                "151.101.64.84",
                "151.101.0.84",
                "205.251.242.103",
                "52.94.236.248",
                "54.239.28.85",
                "104.244.42.129",
                "162.125.248.18"

//                "18.65.48.2",
//                "18.65.48.37",
//                "18.65.48.39",
//                "142.250.79.110",
//                "18.65.48.25",
//                "54.237.226.164",
//                "3.230.129.93",
//                "52.3.144.142",
//                "104.18.22.201",
//                "104.18.23.201",
//                "142.251.133.14",
        );
        new IpScannerServiceImpl().startScan(ipAddresses.get(0) + "/30", 5);
    }
}
