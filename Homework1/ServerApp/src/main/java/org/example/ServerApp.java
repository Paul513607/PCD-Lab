package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    public static final int CHUNK_SIZE = 1024;
    public static final int PORT = 12345;
    public static final int MAX_CLIENTS = 5;



    public static void main(String[] args) {
        int port = PORT;
        int maxClients = MAX_CLIENTS; // Adjust this value to set the maximum number of clients

        // Choose the protocol (TCP or UDP)
        String protocol = "tcp";
        Server server;
        if (args.length == 0) {
            System.out.println("No protocol specified. Using TCP by default.");
            server = new TcpServer();
        } else if (args[0].equalsIgnoreCase("udp")) {
            protocol = "udp";
            server = new UdpServer();
        } else if (args[0].equalsIgnoreCase("tcp")) {
            server = new TcpServer();
        } else {
            System.out.println("Invalid protocol specified. Using TCP by default.");
            server = new TcpServer();
        }

        server.startServer(protocol, port, maxClients);
    }
}