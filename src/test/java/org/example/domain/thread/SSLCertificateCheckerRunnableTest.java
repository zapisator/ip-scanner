package org.example.domain.thread;

import org.example.domain.certificate.CertificateServiceHttpClient;
import org.example.infastructure.data.ConcurrentQueueProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

class SSLCertificateCheckerRunnableTest {

    // Объявляем поля для тестируемого класса, моков и других переменных
    private SSLCertificateCheckerRunnable checkerRunnable;
    private List<String> hosts;
    private ConcurrentQueueProcessor queueProcessor;
    private CertificateServiceHttpClient certificateService;
    private CountDownLatch countDownLatch;

    @BeforeEach
    public void setUp() {
        // Инициализируем поля перед каждым тестом
        hosts = Arrays.asList("example.com", "google.com", "bing.com"); // список хостов для проверки
        queueProcessor = Mockito.mock(ConcurrentQueueProcessor.class); // мок для очереди
        certificateService = Mockito.mock(CertificateServiceHttpClient.class); // мок для сервиса проверки
        countDownLatch = new CountDownLatch(1); // защелка с одним счетчиком
        checkerRunnable = new SSLCertificateCheckerRunnable(hosts, queueProcessor, certificateService, countDownLatch); // создаем экземпляр тестируемого класса
    }

    @AfterEach
    public void tearDown() {
        // Освобождаем ресурсы после каждого теста
        hosts = null;
        queueProcessor = null;
        certificateService = null;
        countDownLatch = null;
        checkerRunnable = null;
    }

//    @Test
//    void testRun() {
//        // Подготавливаем моки для имитации поведения сервиса проверки и очереди
//        Mockito.when(certificateService.checkCertificate("example.com")).thenReturn("OK");
//        Mockito.when(certificateService.checkCertificate("google.com")).thenReturn("OK");
//        Mockito.when(certificateService.checkCertificate("bing.com")).thenReturn("OK");
//        Mockito.doNothing().when(queueProcessor).add(Mockito.anyString());
//
//        // Вызываем метод run из тестируемого класса
//        checkerRunnable.run();
//
//        // Проверяем, что метод checkCertificate был вызван для каждого хоста из списка
//        Mockito.verify(certificateService, Mockito.times(1)).checkCertificate("example.com");
//        Mockito.verify(certificateService, Mockito.times(1)).checkCertificate("google.com");
//        Mockito.verify(certificateService, Mockito.times(1)).checkCertificate("bing.com");
//
//        // Проверяем, что метод add был вызван для каждого результата проверки
//        Mockito.verify(queueProcessor, Mockito.times(1)).add("example.com: OK");
//        Mockito.verify(queueProcessor, Mockito.times(1)).add("google.com: OK");
//        Mockito.verify(queueProcessor, Mockito.times(1)).add("bing.com: OK");
//
//        // Проверяем, что счетчик защелки был уменьшен на единицу
//        Assertions.assertEquals(0, countDownLatch.getCount());
//    }
//
//    @Test
//    void testRunWhenCheckCertificateThrowsException() {
//        // Подготавливаем моки для имитации поведения сервиса проверки и очереди
//        Mockito.when(certificateService.checkCertificate("example.com")).thenThrow(new RuntimeException("Error"));
//        Mockito.when(certificateService.checkCertificate("google.com")).thenReturn("OK");
//        Mockito.when(certificateService.checkCertificate("bing.com")).thenReturn("OK");
//        Mockito.doNothing().when(queueProcessor).add(Mockito.anyString());
//
//        // Вызываем метод run из тестируемого класса
//        checkerRunnable.run();
//
//        // Проверяем, что метод checkCertificate был вызван для каждого хоста из списка
//        Mockito.verify(certificateService, Mockito.times(1)).checkCertificate("example.com");
//        Mockito.verify(certificateService, Mockito.times(1)).checkCertificate("google.com");
//        Mockito.verify(certificateService, Mockito.times(1)).checkCertificate("bing.com");
//
//        // Проверяем, что метод add был вызван только для успешных результатов проверки
//        Mockito.verify(queueProcessor, Mockito.times(0)).add("example.com: Error");
//        Mockito.verify(queueProcessor, Mockito.times(1)).add("google.com: OK");
//        Mockito.verify(queueProcessor, Mockito.times(1)).add("bing.com: OK");
//
//        // Проверяем, что счетчик защелки был уменьшен на единицу
//        Assertions.assertEquals(0, countDownLatch.getCount());
//    }
//
//    @Test
//    void testRunWhenAddThrowsException() {
//        // Подготавливаем моки для имитации поведения сервиса проверки и очереди
//        Mockito.when(certificateService.checkCertificate("example.com")).thenReturn("OK");
//        Mockito.when(certificateService.checkCertificate("google.com")).thenReturn("OK");
//        Mockito.when(certificateService.checkCertificate("bing.com")).thenReturn("OK");
//        Mockito.doThrow(new RuntimeException("Error")).when(queueProcessor).add(Mockito.anyString());
//
//        // Вызываем метод run из тестируемого класса
//        checkerRunnable.run();
//
//        // Проверяем, что метод checkCertificate был вызван для каждого хоста из списка
//        Mockito.verify(certificateService, Mockito.times(1)).checkCertificate("example.com");
//        Mockito.verify(certificateService, Mockito.times(1)).checkCertificate("google.com");
//        Mockito.verify(certificateService, Mockito.times(1)).checkCertificate("bing.com");
//
//        // Проверяем, что метод add был вызван для каждого результата проверки, но выбросил исключение
//        Mockito.verify(queueProcessor, Mockito.times(1)).add("example.com: OK");
//        Mockito.verify(queueProcessor, Mockito.times(1)).add("google.com: OK");
//        Mockito.verify(queueProcessor, Mockito.times(1)).add("bing.com: OK");
//
//        // Проверяем, что счетчик защелки был уменьшен на единицу
//        Assertions.assertEquals(0, countDownLatch.getCount());
//    }
}
