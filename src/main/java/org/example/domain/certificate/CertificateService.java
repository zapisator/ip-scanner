package org.example.domain.certificate;

import java.util.List;

public interface CertificateService {

    List<String> certificateDomains(List<String> ipAddresses);
}
