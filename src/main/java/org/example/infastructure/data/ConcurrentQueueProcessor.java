package org.example.infastructure.data;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Slf4j
public class ConcurrentQueueProcessor {

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private final Path outputFile;

    public void enqueue(String data) {
        log.debug("Добавление данных в очередь: {}", data);
        queue.add(data);
    }

    @SneakyThrows
    public synchronized void processQueue() {
        log.info("Начало обработки очереди.");
        while (!queue.isEmpty()) {
            final String data = queue.poll();
            log.info("Данные, полученные из очереди: {}", data);

            if (data != null) {
                try {
                    Files.write(
                            outputFile,
                            (data + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND
                    );
                } catch (NoSuchFileException e) {
                    log.error(
                            "При записи в файл '{}' произошла ошибка: '{}'",
                            outputFile, e.getMessage()
                    );
                }
                log.info("Данные записаны в файл '{}'", outputFile);
            }
        }
        log.info("Завершение обработки очереди.");
    }

    public boolean isQueueEmpty() {
        log.info("Очередь пуста: {}", queue.isEmpty());
        return queue.isEmpty();
    }

    public static void main(String[] args) {
        final Path outputFile = Paths.get("output.txt");
        final ConcurrentQueueProcessor processor = new ConcurrentQueueProcessor(outputFile);

        // Simulate enqueuing data.
        processor.enqueue("Data 1");
        processor.enqueue("Data 2");

        // Process the queue and write data to the file.
        processor.processQueue();
    }
}
