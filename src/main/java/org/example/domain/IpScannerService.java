package org.example.domain;

public interface IpScannerService {
    void startScan(String ipAddress, int threadsCount);
    void startScan(String ipAddress, int threadsCount, String outputFile);

    String getResult(String ipAddress);
}
