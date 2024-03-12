package org.example;

public class TrafficMonitor {
    private long totalBytesSent;
    private long totalBytesReceived;

    public synchronized void addBytesSent(long bytes) {
        totalBytesSent += bytes;
    }

    public synchronized void addBytesReceived(long bytes) {
        totalBytesReceived += bytes;
    }

    public synchronized long getBytesSent() {
        return totalBytesSent;
    }

    public synchronized long getBytesReceived() {
        return totalBytesReceived;
    }
}