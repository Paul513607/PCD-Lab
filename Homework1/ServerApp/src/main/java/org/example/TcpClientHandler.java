package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class TcpClientHandler implements Runnable, ClientHandler {
    private final Socket clientSocket;
    private final String protocol;
    private final int clientNumber;

    private static final int MAX_MESSAGE_SIZE = 65535;

    public TcpClientHandler(Socket clientSocket, String protocol, int clientNumber) {
        this.clientSocket = clientSocket;
        this.protocol = protocol;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            // Acknowledge the connection
            sendStatus(out, "[OK] Connection Established");

            int totalMessageCount = 0;
            long totalMessageBytes = 0;

            while (true) {
                // Read the message size
                int messageSize = readMessageSize(in, out);
                System.out.println("Received message size: " + messageSize + " bytes");
                if (messageSize > MAX_MESSAGE_SIZE) {
                    sendStatus(out, "[ERROR] Message size exceeds maximum allowed size");
                    continue;
                }
                sendStatus(out, "[OK] Message Size Received");

                if (messageSize == 3 && Arrays.equals(readMessage(in, messageSize), "END".getBytes())) {
                    sendStatus(out, "[OK] End Signal Received");
                    break;
                }

                // Read the message in chunks
                byte[] message = readMessage(in, messageSize);
                sendStatus(out, "[OK] Message Received");
                handleMessageFile(message, clientNumber);

                totalMessageCount++;
                totalMessageBytes += messageSize;
            }

            in.close();
            out.close();
            clientSocket.close();

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

    private void sendStatus(DataOutputStream out, String status) throws IOException {
        out.writeUTF(status);
        out.flush();
    }

    private int readMessageSize(DataInputStream in, DataOutputStream out) throws IOException {
        int messageSize = in.readInt();
        return messageSize;
    }


    private byte[] readMessage(DataInputStream in, long messageSize) throws IOException {
        byte[] buffer = new byte[ServerApp.CHUNK_SIZE];
        long totalBytesRead = 0;
        int bytesRead;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while (totalBytesRead < messageSize) {
            bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, messageSize - totalBytesRead));
            if (bytesRead == -1) {
                break;
            }

            byteArrayOutputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
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