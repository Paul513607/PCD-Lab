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
    private final DatagramSocket socket;
    private final InetAddress clientAddress;
    private final int clientPort;
    private final String protocol;
    private final int clientNumber;

    public UdpClientHandler(DatagramSocket socket, InetAddress clientAddress, int clientPort, String protocol, int clientNumber) {
        this.socket = socket;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.protocol = protocol;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run() {
        try {
            // Acknowledge the connection
            sendStatus(socket, clientAddress, clientPort, "[OK] Connection Established", true);

            int totalMessageCount = 0;
            long totalMessageBytes = 0;

            while (true) {
                // Read the size first
                int size = readMessageSize(socket, clientAddress, clientPort);

                // Receive the actual message
                byte[] messageData = readMessage(socket, clientAddress, clientPort, size);

                // Handle the end signal
                if (size == 3 && new String(messageData).equals("END")) {
                    sendStatus(socket, clientAddress, clientPort, "[OK] End Signal Received", true);
                    break;
                }

                // handleMessageFile(messageData, clientNumber);
                handleMessageString(messageData, clientNumber);

                totalMessageCount++;
                totalMessageBytes += size;

                // Perform acknowledgement
                sendStatus(socket, clientAddress, clientPort, "[OK] Message Received", false);
            }

            printServerStatistics(protocol, totalMessageCount, totalMessageBytes);
        } catch (IOException e) {
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
        byte[] receiveData = new byte[ServerApp.CHUNK_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);

        return bytesToInt(receivePacket.getData());
    }

    private int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    private byte[] readMessage(DatagramSocket socket, InetAddress clientAddress, int clientPort, int messageSize) throws IOException {
        int totalBytesRead = 0;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (totalBytesRead < messageSize) {
            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.receive(receivePacket);

            byteArrayOutputStream.write(buffer, 0, receivePacket.getLength());
            totalBytesRead += receivePacket.getLength();

            sendStatus(socket, clientAddress, clientPort, "[OK] Chunk Received", false);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private void handleMessageString(byte[] message, int clientNumber) {
        // Process the received message as needed
        System.out.println("Received message: " + new String(message));
    }

    private void handleMessageFile(byte[] message, int clientNumber) {
        File file = new File("/home/paul/tempData/server/receivedFile_client_" + clientNumber + ".txt");
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
