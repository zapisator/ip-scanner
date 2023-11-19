package org.example.domain.certificate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.example.util.InternetUtil.getAllHostsInSubnet;

@Slf4j
public class CertificateServiceJavax implements CertificateService {


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

    @SneakyThrows
    private List<X509Certificate> checkSSLCertificates(List<String> ipAddresses) {
        log.info("Проверка SSL-сертификатов для IP-адресов: {}", ipAddresses.size());
        final SSLSocketFactory sslSocketFactory = SSLContext
                .getDefault()
                .getSocketFactory();
        final List<X509Certificate> certificates = ipAddresses
                .stream()
                .map(ipAddress -> checkSSLForIPAddress(ipAddress, sslSocketFactory))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        log.info("Проверка SSL-сертификатов завершена. Найдено {} сертификатов.", certificates.size());
        return certificates;
    }

    @SneakyThrows
    private List<X509Certificate> checkSSLForIPAddress(String ipAddress, SSLSocketFactory sslSocketFactory) {
        log.debug("Проверка SSL для IP-адреса: {}", ipAddress);
        final int httpsPort = 443;
        List<X509Certificate> certificates;

        try (final SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(ipAddress, httpsPort)) {
            sslSocket.startHandshake();

            final X509Certificate[] peerCertificates = (X509Certificate[]) sslSocket
                    .getSession()
                    .getPeerCertificates();
            certificates = new ArrayList<>(Arrays.asList(peerCertificates));
        }
        log.info("Найдено {} сертификатов для IP-адреса: {}", certificates.size(), ipAddress);
        return certificates;
    }

    @SneakyThrows
    private List<String> extractDomainsFromCertificates(List<X509Certificate> certificates) {
        log.info("Извлечение доменов из сертификатов в колчиестве {}", certificates.size());
        final List<String> domains = certificates.stream()
                .map(this::extractDomainsFromCertificate)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        log.info("Извлечение доменов завершено. Найдено {} доменов в сертификатах.", domains.size());
        return domains;
    }

    @SneakyThrows
    private List<String> extractDomainsFromCertificate(X509Certificate certificate) {
        log.info("Извлечение доменов из сертификата: {}", certificate.getSubjectX500Principal());
        List<String> domains = new ArrayList<>();
        Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();

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

    public static void main(String[] args) {
        CertificateServiceJavax scanner = new CertificateServiceJavax();

        String subnet = "8.8.8.8/24";

        List<String> hosts = getAllHostsInSubnet(subnet);

        for (String host : hosts) {
            System.out.println(host);
        }

        List<String> ipAddresses = Arrays.asList("ya.ru", "google.com");
        List<X509Certificate> certificates = scanner.checkSSLCertificates(Arrays.asList("8.8.8.8"));

        for (X509Certificate certificate : certificates) {
            System.out.println("Found SSL Certificate: " + certificate.getSubjectX500Principal());
            System.out.println("\nДля каждого сертификата проведем поиск:\n");
            scanner.extractDomainsFromCertificate(certificate)
                    .forEach(System.out::println);
        }
    }
}
