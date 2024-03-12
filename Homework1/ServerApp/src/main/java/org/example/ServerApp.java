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

    public static boolean streaming = false;
    public static boolean stopAndWait = false;

    public static void main(String[] args) {
        int port = PORT;
        int maxClients = MAX_CLIENTS;

        String protocol = "tcp";
        Server server;
        if (args.length == 0) {
            System.out.println("No protocol specified. Using TCP by default.");
            server = new TcpServer(maxClients);
        } else if (args[0].equalsIgnoreCase("udp")) {
            protocol = "udp";
            server = new UdpServer();
        } else if (args[0].equalsIgnoreCase("tcp")) {
            server = new TcpServer(maxClients);
        } else {
            System.out.println("Invalid protocol specified. Using TCP by default.");
            server = new TcpServer(maxClients);
        }

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("streaming")) {
                streaming = true;
            } else if (args[1].equalsIgnoreCase("stopAndWait")) {
                stopAndWait = true;
            } else {
                System.out.println("Invalid mode specified. Using default mode.");
                stopAndWait = true;
            }
        } else {
            System.out.println("No mode specified. Using default mode.");
            stopAndWait = true;
        }

        server.startServer(protocol, port, maxClients);
    }
}