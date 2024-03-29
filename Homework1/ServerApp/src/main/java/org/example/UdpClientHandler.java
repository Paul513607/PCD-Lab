package org.example;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UdpClientHandler implements Runnable, ClientHandler {
    private final TrafficMonitor trafficMonitor;
    private final UdpServer server;
    private final DatagramSocket socket;
    private final InetAddress clientAddress;
    private final int clientPort;
    private final String protocol;
    private final int clientNumber;

    private boolean firstChunk = true;

    public UdpClientHandler(UdpServer server, DatagramSocket socket, InetAddress clientAddress, int clientPort, String protocol, int clientNumber, TrafficMonitor trafficMonitor) {
        this.server = server;
        this.socket = socket;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.protocol = protocol;
        this.clientNumber = clientNumber;
        this.trafficMonitor = trafficMonitor;
    }

    @Override
    public void run() {
        int totalMessageCount = 0;
        long totalMessageBytes = 0;
        try {
            sendStatus(socket, clientAddress, clientPort, "[OK] Connection Established", true);

            while (true) {
                int size = readMessageSize(socket, clientAddress, clientPort);

                ReadDto dto = readMessage(socket, clientAddress, clientPort, size);

                byte[] messageData = dto.getMessage();

                if (size == 3 && new String(messageData).equals("END")) {
                    sendStatus(socket, clientAddress, clientPort, "[OK] End Signal Received", true);
                    break;
                }

                handleMessageFile(messageData, clientNumber);
                // handleMessageString(messageData, clientNumber);

                if (messageData.length > 0) {
                    totalMessageCount += dto.getNumMessages();
                    totalMessageBytes += dto.getBytesRead();
                }

                sendStatus(socket, clientAddress, clientPort, "[OK] Message Received", false);
            }

            server.addTotalMessageCount(totalMessageCount);
            server.addTotalMessageBytes(totalMessageBytes);

            printServerStatistics(protocol, totalMessageCount, totalMessageBytes);
        } catch (IOException e) {
            printServerStatistics(protocol, (int) trafficMonitor.getMessagesRead(), trafficMonitor.getBytesReceived());
            e.printStackTrace();
        }
    }

    @Override
        public void printServerStatistics(String protocol, int totalMessagesReceived, long totalBytesReceived) {
        System.out.println("\nServer statistics for " + protocol.toUpperCase() + ":");
        System.out.println("Number of messages read: " + totalMessagesReceived);
        System.out.println("Number of bytes read: " + totalBytesReceived + " bytes (" + ((double) totalBytesReceived / (1024 * 1024 * 1024)) + " GB)");
    }

    private void sendStatus(DatagramSocket socket, InetAddress clientAddress, int clientPort, String status, boolean force) throws IOException {
        if (!ServerApp.stopAndWait && !force) {
            return;
        }
        byte[] statusBytes = status.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(statusBytes, statusBytes.length, clientAddress, clientPort);
        socket.send(sendPacket);
    }

    private int readMessageSize(DatagramSocket socket, InetAddress clientAddress, int clientPort) throws IOException {
        byte[] receiveData = new byte[4];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length, clientAddress, clientPort);
        socket.receive(receivePacket);

        return bytesToInt(receivePacket.getData());
    }

    private int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    private ReadDto readMessage(DatagramSocket socket, InetAddress clientAddress, int clientPort, int messageSize) throws IOException {
        trafficMonitor.addBytesSent(messageSize);
        long totalBytesRead = 0;
        int numMessages = 0;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[ServerApp.CHUNK_SIZE];

        while (totalBytesRead < messageSize) {
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.receive(receivePacket);

            byteArrayOutputStream.write(buffer, 0, receivePacket.getLength());
            totalBytesRead += receivePacket.getLength();
            numMessages++;

            sendStatus(socket, clientAddress, clientPort, "[OK] Chunk Received", false);

            trafficMonitor.addBytesReceived(receivePacket.getLength());
            trafficMonitor.addMessagesRead();

            System.out.println("Received chunk: " + receivePacket.getLength() + " bytes");
        }

        trafficMonitor.addBytesReceived(totalBytesRead);
        return new ReadDto(byteArrayOutputStream.toByteArray(), totalBytesRead, numMessages);
    }

    private void handleMessageString(byte[] message, int clientNumber) {
        System.out.println("Received message: " + new String(message));
    }

    private void rebuildFileByParts(byte[] part) {
        File file = new File("./Cristian_Frasinaru-Curs_practic_de_Java2222.pdf");
        try {
            FileOutputStream fileOutputStream;
            if (firstChunk) {
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file);
                firstChunk = false;
            } else {
                fileOutputStream = new FileOutputStream(file, true);
            }
            fileOutputStream.write(part);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessageFile(byte[] message, int clientNumber) {
        File file = new File("./Cristian_Frasinaru-Curs_practic_de_Java.pdf");
        try {
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(message);
            fileOutputStream.close();
            System.out.println("Received file: " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
