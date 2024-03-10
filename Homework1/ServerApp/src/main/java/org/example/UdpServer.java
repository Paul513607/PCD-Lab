package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpServer implements Server {
    @Override
    public void startServer(String protocol, int port, int maxClients) {
        try {
            System.out.println("UDP Server listening on port " + port + "...");
            DatagramSocket serverSocket = new DatagramSocket(port);

            while (true) {
                // Receive client connection
                UdpClientHandler udpClientHandler = acceptClient(serverSocket);

                // Create a new thread to handle the client
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

            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            System.out.println("Accepted connection at " + clientAddress + ":" + clientPort);

            return new UdpClientHandler(serverSocket, clientAddress, clientPort, "UDP", 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
