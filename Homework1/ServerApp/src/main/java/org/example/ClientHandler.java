package org.example;

public interface ClientHandler {
    void printServerStatistics(String protocol, int totalMessagesReceived, long totalBytesReceived);
}
