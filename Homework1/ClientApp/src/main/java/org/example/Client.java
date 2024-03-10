package org.example;

public interface Client {
    void startClient(String protocol, String host, int port, int numMessages);
    void printClientStatistics(String protocol, int totalMessagesSent, long totalBytesSent, long transmissionTime);
}
