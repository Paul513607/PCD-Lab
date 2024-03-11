package org.example;

import java.io.*;
import java.net.Socket;

public class TcpClient implements Client {
    @Override
    public void startClient(String protocol, String host, int port, int numMessages) {
        try {
            Socket clientSocket = new Socket(host, port);
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("Connecting to TCP server at " + host + ":" + port + "...");

            readStatus(in, true);

            long startTime = System.currentTimeMillis();

            long totalMessageBytes = 0;
            for (int i = 0; i < numMessages; i++) {
                byte[] message = this.buildMessageLargeFile();
                totalMessageBytes += sendSizeAndMessage(in, out, message);
            }

            sendMessageEnd(in, out);

            long endTime = System.currentTimeMillis();
            long transmissionTime = endTime - startTime;

            printClientStatistics(protocol, numMessages, totalMessageBytes, transmissionTime);

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printClientStatistics(String protocol, int totalMessagesSent, long totalBytesSent, long transmissionTime) {
        System.out.println("\nClient statistics for " + protocol.toUpperCase() + ":");
        System.out.println("Transmission time: " + transmissionTime + " milliseconds (" + ((double) transmissionTime / 1000) + " seconds)");
        System.out.println("Number of sent messages: " + totalMessagesSent);
        System.out.println("Number of bytes sent: " + totalBytesSent + " bytes (" + ((double) totalBytesSent / (1024 * 1024 * 1024)) + " GB)");
    }

    private boolean readStatus(DataInputStream in, boolean force) throws IOException {
        if (!ClientApp.stopAndWait && !force) {
            return true;
        }
        String status = in.readUTF();
        System.out.println("Server Status: " + status);
        return status.startsWith("[OK]");
    }

    /**
     * Send the end signal to the server
     * @return The number of bytes sent
     */
    private int sendSizeAndMessage(DataInputStream in, DataOutputStream out, byte[] messageBytes) throws IOException {
        out.writeInt(messageBytes.length);
        out.flush();

        boolean ok = readStatus(in, true);

        if (!ok) {
            return 0;
        }

        int totalBytesSent = 0;
        int chunkSize = ClientApp.CHUNK_SIZE;

        while (totalBytesSent < messageBytes.length) {
            int bytesToSend = Math.min(chunkSize, messageBytes.length - totalBytesSent);
            out.write(messageBytes, totalBytesSent, bytesToSend);
            out.flush();

            totalBytesSent += bytesToSend;

            readStatus(in, false);
        }

        readStatus(in, false);

        return totalBytesSent;
    }

    private void sendMessageEnd(DataInputStream in, DataOutputStream out) throws IOException {
        out.writeInt(3);
        out.write("END".getBytes());
        out.flush();
        readStatus(in, true);
    }

    private byte[] buildMessageString() {
        String message = "Hello world!";
        return message.getBytes();
    }

    private byte[] buildMessageFile(String filePath) {
        File file = new File(filePath);
        byte[] fileBytes = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileBytes;
    }

    private byte[] buildMessageLargeFile() {
        return buildMessageFile("file");
    }
}
