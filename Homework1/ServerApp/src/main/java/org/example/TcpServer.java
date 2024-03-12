package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer implements Server {
    private final TrafficMonitor trafficMonitor = new TrafficMonitor();
    private final ExecutorService executorService;
    boolean isRunning = true;

    int totalMessageCount = 0;
    long totalMessageBytes = 0;

    public TcpServer(int maxClients) {
        executorService = Executors.newFixedThreadPool(maxClients);

        Thread userInputThread = new Thread(this::waitForUserInput);
        userInputThread.start();
    }

    private void waitForUserInput() {
        Scanner scanner = new Scanner(System.in);

        if (scanner.hasNextLine()) {
            if (scanner.nextLine().trim().equalsIgnoreCase("exit")) {
                stopServer();
            }
        }
    }

    private void stopServer() {
        printServerStatistics("tcp", totalMessageCount, totalMessageBytes);
        isRunning = false;
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        System.out.println("Server stopped gracefully.");
        System.exit(0);
    }

    @Override
    public void startServer(String protocol, int port, int maxClients) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on " + protocol.toUpperCase() + " port " + port + "...");

            int clientCounter = 0;

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                Runnable clientHandler = new TcpClientHandler(this, clientSocket, protocol, clientCounter + 1, trafficMonitor);
                clientCounter++;
                executorService.submit(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addTotalMessageCount(int threadMessageCount) {
        totalMessageCount += threadMessageCount;
    }

    public synchronized void addTotalMessageBytes(long threadMessageBytes) {
        totalMessageBytes += threadMessageBytes;
    }

    public void printServerStatistics(String protocol, int totalMessagesReceived, long totalBytesReceived) {
        System.out.println("\nServer statistics for " + protocol.toUpperCase() + ":");
        System.out.println("Number of messages read: " + totalMessagesReceived);
        System.out.println("Number of bytes read: " + totalBytesReceived + " bytes (" + ((double) totalBytesReceived / (1024 * 1024 * 1024)) + " GB)");

        System.out.println("Traffic Monitor stats:");
        System.out.println("Bytes sent: " + trafficMonitor.getBytesSent());
        System.out.println("Bytes received: " + trafficMonitor.getBytesReceived());
    }
}
