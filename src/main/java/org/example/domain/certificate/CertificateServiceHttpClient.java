package org.example.domain.certificate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
public class CertificateServiceHttpClient implements CertificateService {

    private final CloseableHttpClient httpClient = HttpClients
            .createMinimal(httpClientConnectionManager());

    @Override
    public List<String> certificateDomains(List<String> ipAddresses) {
        log.info(
                "Запущен процесс проверки SSL-сертификатов для IP-адресов от '{}' до '{}' " +
                        "общим числом: {}",
                ipAddresses.get(0),
                ipAddresses.get(ipAddresses.size() - 1),
                ipAddresses.size()
        );
        final List<X509Certificate> certificates = checkSSLCertificates(ipAddresses);
        log.info("Найдено {} сертификатов", certificates.size());
        final List<String> domains = extractDomainsFromCertificates(certificates);
        log.info(
                "Проверка SSL-сертификатов завершена. Найдено {} доменов в сертификатах.",
                domains.size()
        );
        return domains;
    }

    private List<X509Certificate> checkSSLCertificates(List<String> ipAddresses) {
        log.info("Проверка SSL-сертификатов для IP-адресов: {}", ipAddresses.size());
        final List<X509Certificate> certificates = ipAddresses
                .stream()
                .map(this::retrieveServerCertificates)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        log.info("Проверка SSL-сертификатов завершена. Найдено {} сертификатов.", certificates.size());
        return certificates;
    }

    private List<String> extractDomainsFromCertificates(List<X509Certificate> certificates) {
        log.info("Извлечение доменов из '{}' сертификатов", certificates.size());
        List<String> domains = certificates.stream()
                .map(this::extractDomainsFromCertificate)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        log.info("Извлечение доменов завершено. Найдено {} доменов в сертификатах.", domains.size());
        return domains;
    }

    public List<X509Certificate> retrieveServerCertificates(String url) {
        final HttpContext context = filledContext(url);
        final X509Certificate[] peerCertificates;

        if (context == null) {
            return Collections.emptyList();
        }
        try {
            peerCertificates = ((X509Certificate[]) HttpClientContext
                    .adapt(context)
                    .getSSLSession()
                    .getPeerCertificates());
        } catch (SSLPeerUnverifiedException e) {
            log.error("Данного {} ip нет среди альтернативных имен в его сертификате\n {}", url, e.getStackTrace());
            return Collections.emptyList();
        }
        log.info("Извлечены сертификаты с URL: {}, количество сертификатов: {}", url, peerCertificates.length);
        return Arrays.asList(peerCertificates);
    }

    private HttpContext filledContext(String url) {
        log.info("Выполнение HTTP-запроса для извлечения сертификатов с URL: {}", url);
        final HttpContext context = new BasicHttpContext();
        try {
            httpClient
                    .execute(target(url), httpRequest(), context);
            return context;
        } catch (SSLHandshakeException e) {
            log.error("Данного сертификата нет в системном хранилище сертификатов trustStore");
            return null;
        } catch (IOException e) {
            log.error("Другие исключения\n {}", e.getStackTrace());
            return null;
        }
    }

    @NotNull
    private ClassicHttpRequest httpRequest() {
        return new BasicClassicHttpRequest("GET", "/");
    }

    private HttpClientConnectionManager httpClientConnectionManager() {
        final SSLContext sslContext = SSLContexts.createDefault();
        final HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        final Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("https", new SSLConnectionSocketFactory(sslContext, hostnameVerifier))
                .build();
        return new PoolingHttpClientConnectionManager(registry);
    }

    private HttpHost target(String url) {
        final String scheme = "https";
        final int port = 443;

        return new HttpHost(scheme, url, port);
    }

    @SneakyThrows
    public List<String> extractDomainsFromCertificate(X509Certificate certificate) {
        log.info("Извлечение доменов из сертификата: {}", certificate.getSubjectX500Principal());
        final List<String> domains = new ArrayList<>();
        final Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();

        if (nonNull(subjectAlternativeNames)) {
            for (List<?> san : subjectAlternativeNames) {
                if (san.get(1) instanceof String) {
                    final String domain = (String) san.get(1);

                    domains.add(domain);
                }
            }
        }

        log.info("Найдено {} доменов в сертификате", domains.size());
        return domains;
    }

    @SneakyThrows
    public static void main(String[] args) {
        final CertificateService certificateService = new CertificateServiceHttpClient();
//        // Создайте экземпляр HttpClient
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//
//            // Укажите IP-адрес и порт в URL
//            String ipAddress = "8.8.8.8";
//            int port = 443; // Порт по умолчанию для HTTP
//
//            String url = "https://" + ipAddress;
//
//            // Создайте HTTP-запрос с указанным URL
////            HttpGet httpGet = new HttpGet(url);
////
////            // Выполните запрос
////            HttpResponse response = httpClient.execute(httpGet);
//
//            Arrays.stream(new CertificateServiceHttpClient()
//                            .retrieveServerCertificates(url))
//                    .forEach(System.out::println);
//
////            System.out.println(response);
//        }

//        CertificateServiceJavax scanner = new CertificateServiceJavax();
//
//        String subnet = "8.8.8.8/24";
//
//        List<String> hosts = getAllHostsInSubnet(subnet);
//
//        for (String host : hosts) {
//            System.out.println(host);
//        }
//
//        List<String> ipAddresses = Arrays.asList("ya.ru", "google.com");

        final List<String> ipAddresses = Arrays.asList(
//                "8.8.8.8",

//                timeout
//                "91.220.176.248",
//                "98.137.149.56",
//                "174.140.154.32",
//                "72.21.211.176",
//                "207.97.227.239",
//                "199.47.217.179"

//                нвт ip среди альтернативных имен
                "74.6.143.26",
                "72.247.244.88",
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

//                нет среди trustStore
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
//                "142.251.133.14"
        );
        certificateService
                .certificateDomains(
                        ipAddresses
                )
                .forEach(System.out::println);
    }
}