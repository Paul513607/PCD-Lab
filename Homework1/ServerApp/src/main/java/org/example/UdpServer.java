package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UdpServer implements Server {
    private final TrafficMonitor trafficMonitor = new TrafficMonitor();
    private boolean isRunning = true;

    private int totalMessageCount = 0;
    private long totalMessageBytes = 0;

    public UdpServer() {
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

        System.out.println("Server stopped gracefully.");
        System.exit(0);
    }

    @Override
    public void startServer(String protocol, int port, int maxClients) {
        try {
            System.out.println("UDP Server listening on port " + port + "...");
            DatagramSocket serverSocket = new DatagramSocket(port);

            while (isRunning) {
                UdpClientHandler udpClientHandler = acceptClient(serverSocket);

                if (udpClientHandler != null) {
                    Thread clientThread = new Thread(udpClientHandler);
                    clientThread.start();
                    clientThread.join();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private UdpClientHandler acceptClient(DatagramSocket serverSocket) {
        try {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());


            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            System.out.println("Accepted connection at " + clientAddress + ":" + clientPort);

            if (receivedData.equals("HANDSHAKE")) {
                return new UdpClientHandler(this, serverSocket, clientAddress, clientPort, "UDP", 0, trafficMonitor);
            } else {
                System.out.println("Invalid handshake message received");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void printServerStatistics(String protocol, int totalMessagesReceived, long totalBytesReceived) {
        System.out.println("\nServer statistics for " + protocol.toUpperCase() + ":");
        System.out.println("Number of messages read: " + totalMessagesReceived);
        System.out.println("Number of bytes read: " + totalBytesReceived + " bytes (" + ((double) totalBytesReceived / (1024 * 1024 * 1024)) + " GB)");

        System.out.println("Traffic Monitor stats:");
        System.out.println("Bytes sent: " + trafficMonitor.getBytesSent());
        System.out.println("Bytes received: " + trafficMonitor.getBytesReceived());
    }

    public synchronized void addTotalMessageCount(int threadMessageCount) {
        totalMessageCount += threadMessageCount;
    }

    public synchronized void addTotalMessageBytes(long threadMessageBytes) {
        totalMessageBytes += threadMessageBytes;
    }
}
