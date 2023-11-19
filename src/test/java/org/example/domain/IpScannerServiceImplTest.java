package org.example.domain;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IpScannerServiceImplTest {

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


//    @Test
//    void testScanWithDefaultOutputFile() {
//        IpScannerServiceImpl ipScannerServiceImpl = new IpScannerServiceImpl();
//        ipScannerServiceImpl.startScan("151.101.193.164" + "/30", 1); // Замените IP-адресом и количеством потоков на нужные значения
//        // Здесь вы можете добавить проверки, чтобы убедиться, что сканирование выполнено корректно.
//        // Например, вы можете проверить, что файл "output.txt" был создан и содержит ожидаемые данные.
//    }

//    @Test
//    void testGetResult() throws IOException {
//        final IpScannerServiceImpl ipScannerService = new IpScannerServiceImpl();
//        final ExecutorService executorService = mock(ExecutorService.class);
//        final HttpClient httpClient = mock(HttpClient.class);
//
//        // Задаем возвращаемые значения для методов мок-объектов
//        when(executorService.submit(any(Runnable.class))).thenReturn(null); // Задаем, что при отправке задачи на сканирование IP-адреса, возвращается строка "OK"
//        when(httpClient.execute(any(HttpHost.class), any(ClassicHttpRequest.class), any(HttpContext.class))).thenReturn(null); // Задаем, что при отправке HTTP-запроса к IP-адресу, возвращается код ответа "200"
//
//        // Вызываем тестируемый метод с тестовыми данными
//        String result = ipScannerService.getResult("192.168.0.1");
//
//        // Проверяем, что результат соответствует ожидаемому
//        assertEquals("{\"ip\":\"192.168.0.1\",\"status\":\"200\"}", result); // Ожидаем, что результат содержит JSON-строку с IP-адресом и статусом ответа
//    }

//    @Test
//    void testStartScan() {
//        final IpScannerServiceImpl ipScannerService = new IpScannerServiceImpl();
//        final ExecutorService executorService = Mockito.mock(ExecutorService.class);
//        final CloseableHttpClient httpClient = HttpClients.createMinimal(httpClientConnectionManager());
//
//        when(executorService.submit(any(Runnable.class))).thenReturn(null); // Задаем, что при отправке задачи на сканирование IP-адреса, возвращается null
//
//        ipScannerService.startScan("192.168.0.1/24", 2);
//
//        verify(executorService, times(2)).submit(any(Runnable.class)); // Ожидаем, что метод submit был вызван два раза с любым Runnable в качестве аргумента
//    }

    // Копируем метод httpClientConnectionManager из вашего кода
    private HttpClientConnectionManager httpClientConnectionManager() {
        final SSLContext sslContext = SSLContexts.createDefault();
        final HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        final Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("https", new SSLConnectionSocketFactory(sslContext, hostnameVerifier))
                .build();
        return new PoolingHttpClientConnectionManager(registry);
    }

}