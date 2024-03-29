package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class TcpClientHandler implements Runnable, ClientHandler {
    private final TrafficMonitor trafficMonitor;
    private final TcpServer server;
    private final Socket clientSocket;
    private final String protocol;
    private final int clientNumber;

    private static final int MAX_MESSAGE_SIZE = 65535;

    public TcpClientHandler(TcpServer server, Socket clientSocket, String protocol, int clientNumber, TrafficMonitor trafficMonitor) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.protocol = protocol;
        this.clientNumber = clientNumber;
        this.trafficMonitor = trafficMonitor;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            sendStatus(out, "[OK] Connection Established", true);

            int totalMessageCount = 0;
            long totalMessageBytes = 0;

            while (true) {
                int messageSize = readMessageSize(in, out);
                System.out.println("Received message size: " + messageSize + " bytes");
                sendStatus(out, "[OK] Message Size Received", true);

                ReadDto dto = readMessage(in, out, messageSize);
                byte[] message = dto.getMessage();

                if (messageSize == 3 && Arrays.equals(message, "END".getBytes())) {
                    sendStatus(out, "[OK] End Signal Received", true);
                    break;
                }

                sendStatus(out, "[OK] Message Received", false);

                // handleMessageString(message, clientNumber);
                handleMessageFile(message, clientNumber);

                totalMessageCount += dto.getNumMessages();
                totalMessageBytes += dto.getBytesRead();
            }

            server.addTotalMessageBytes(totalMessageBytes);
            server.addTotalMessageCount(totalMessageCount);

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

    private void sendStatus(DataOutputStream out, String status, boolean force) throws IOException {
        if (!ServerApp.stopAndWait && !force) {
            return;
        }
        out.writeUTF(status);
        out.flush();
    }

    private int readMessageSize(DataInputStream in, DataOutputStream out) throws IOException {
        int messageSize = in.readInt();
        return messageSize;
    }


    private ReadDto readMessage(DataInputStream in, DataOutputStream out, long messageSize) throws IOException {
        trafficMonitor.addBytesSent(messageSize);
        byte[] buffer = new byte[ServerApp.CHUNK_SIZE];
        long totalBytesRead = 0;
        int numMessages = 0;
        int bytesRead;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (totalBytesRead < messageSize) {
            bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, messageSize - totalBytesRead));
            if (bytesRead == -1) {
                break;
            }
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
            numMessages++;
            sendStatus(out, "[OK] Chunk Received", false);
            System.out.println("Received chunk: " + buffer.length + " bytes");

        }
        trafficMonitor.addBytesReceived(totalBytesRead);

        return new ReadDto(byteArrayOutputStream.toByteArray(), totalBytesRead, numMessages);
    }

    private void handleMessageString(byte[] message, int clientNumber) {
        System.out.println("Received message: " + new String(message));
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
