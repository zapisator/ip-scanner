package org.example.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadUtil {

    public static int getOptimalThreadCount() {
        log.info("Вычисление оптимального числа потоков");
        final int availableProcessors = Runtime
                .getRuntime()
                .availableProcessors();
        final int parallelism = ForkJoinPool
                .getCommonPoolParallelism();
        final int optimalThreadCount = Math.min(availableProcessors, parallelism);
        log.info(
                "\nДоступно процессоров: {}" +
                        "\nЦелевой уровень параллелизма общего пула: {}" +
                        "\nОптимальное число потоков: {}",
                availableProcessors,
                parallelism,
                optimalThreadCount
        );
        return optimalThreadCount;
    }

    public static List<List<String>> splitIPList(List<String> ipAddresses, int numThreads) {
        log.info("Разбиение списка IP-адресов на {} потоков", numThreads);
        if (ipAddresses.isEmpty() || numThreads <= 0) {
            log.warn("Список IP-адресов пуст или недопустимое значение numThreads, возвращается пустой список");
            return Collections.emptyList();
        }

        final int batchSize = ipAddresses.size() / numThreads;
        final int remainder = ipAddresses.size() % numThreads;

        return IntStream.range(0, numThreads)
                .mapToObj(i -> {
                    final int additional = i < remainder ? 1 : 0;
                    final int start = i * batchSize + Math.min(i, remainder);
                    final int end = start + batchSize + additional;

                    return ipAddresses.subList(start, end);
                })
                .filter(sublist -> !sublist.isEmpty())
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        int optimalThreadCount = getOptimalThreadCount();
        System.out.println("Оптимальное число потоков: " + optimalThreadCount);

        List<String> ipAddresses = Arrays.asList(
                "192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5",
                "192.168.1.6", "192.168.1.7", "192.168.1.8"
        );
        int numThreads = 5;

        List<List<String>> dividedIPs = splitIPList(ipAddresses, numThreads);

        for (int i = 0; i < dividedIPs.size(); i++) {
            log.info("Поток " + i + ": " + dividedIPs.get(i));
        }
    }
}

