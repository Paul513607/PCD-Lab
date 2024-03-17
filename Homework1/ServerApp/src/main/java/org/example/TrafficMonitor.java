package org.example;

public class TrafficMonitor {
    private long totalBytesSent;
    private long totalBytesReceived;

    private long numMessagesRead;

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

    public synchronized void addMessagesRead() {
        numMessagesRead++;
    }

    public synchronized long getMessagesRead() {
        return numMessagesRead;
    }
}