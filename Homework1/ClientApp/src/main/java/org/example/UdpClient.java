package org.example;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class UdpClient implements Client {
    private static final String HANDSHAKE_MESSAGE = "HELLO_SERVER";

    @Override
    public void startClient(String protocol, String host, int port, int numMessages) {

        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(host);

            System.out.println("Connecting to UDP server at " + host + ":" + port + "...");

            // Perform the handshake
            performHandshake(clientSocket, serverAddress);

            long startTime = System.currentTimeMillis();

            long totalMessageBytes = 0;
            for (int i = 0; i < numMessages; i++) {
                // byte[] message = this.buildMessageFile(ClientApp.LARGE_FILE_PATH);
                byte[] message = this.buildMessageString();
                totalMessageBytes += sendSizeAndMessage(clientSocket, serverAddress, message);
            }

            // Send the end signal
            sendMessageEnd(clientSocket, serverAddress);

            long endTime = System.currentTimeMillis();
            long transmissionTime = endTime - startTime;

            printClientStatistics("UDP", numMessages, totalMessageBytes, transmissionTime);

            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performHandshake(DatagramSocket socket, InetAddress serverAddress) throws Exception {
        byte[] handshakeBytes = HANDSHAKE_MESSAGE.getBytes();
        sendMessage(socket, serverAddress, handshakeBytes);
    }

    @Override
    public void printClientStatistics(String protocol, int totalMessagesSent, long totalBytesSent, long transmissionTime) {
        System.out.println("\nClient statistics for " + protocol.toUpperCase() + ":");
        System.out.println("Transmission time: " + transmissionTime + " milliseconds (" + ((double) transmissionTime / 1000) + " seconds)");
        System.out.println("Number of sent messages: " + totalMessagesSent);
        System.out.println("Number of bytes sent: " + totalBytesSent + " bytes (" + ((double) totalBytesSent / (1024 * 1024 * 1024)) + " GB)");
    }

    private int sendSizeAndMessage(DatagramSocket socket, InetAddress serverAddress, byte[] messageBytes) throws Exception {
        byte[] sizeBytes = intToBytes(messageBytes.length);

        // Send the size first
        sendMessage(socket, serverAddress, sizeBytes);

        // Split the message into chunks and send each chunk
        int totalBytesSent = 0;
        int chunkSize = 1024;

        for (int offset = 0; offset < messageBytes.length; offset += chunkSize) {
            int remainingBytes = Math.min(chunkSize, messageBytes.length - offset);
            byte[] chunk = Arrays.copyOfRange(messageBytes, offset, offset + remainingBytes);

            // Send the actual message chunk
            sendMessage(socket, serverAddress, chunk);
            totalBytesSent += remainingBytes;
            readStatus(socket);
        }

        return totalBytesSent;
    }

    private byte[] intToBytes(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    private void sendMessage(DatagramSocket socket, InetAddress serverAddress, byte[] data) throws Exception {
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress, ClientApp.PORT);
        socket.send(sendPacket);
    }

    private void sendMessageEnd(DatagramSocket socket, InetAddress serverAddress) throws Exception {
        String endMessage = "END";
        byte[] endMessageBytes = endMessage.getBytes();
        sendSizeAndMessage(socket, serverAddress, endMessageBytes);
        readStatus(socket);
    }

    private static boolean readStatus(DatagramSocket socket) throws IOException {
        byte[] receiveData = new byte[ClientApp.CHUNK_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);

        String status = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println("Server Status: " + status);

        return status.startsWith("[OK]");
    }

    private byte[] buildMessageString() {
        String message = "This is a sample message from the client.";
        return message.getBytes();
    }

    private byte[] buildMessageFile(String filePath) {
        // for example, use largeFile.txt from the resources folder
        File file = new File(filePath);
        // read the file bytes
        byte[] fileBytes = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileBytes;
    }

    private byte[] buildMessageLargeFile() {
        return buildMessageFile("/home/paul/tempData/client/largeFile.txt");
    }
}
