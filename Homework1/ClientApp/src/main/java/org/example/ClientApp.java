package org.example;

import java.io.*;
import java.net.Socket;

public class ClientApp {
    public static final int CHUNK_SIZE = 1024;
    public static final String ADDRESS = "localhost";
    public static final int PORT = 12345;
    public static final String LARGE_FILE_PATH = "/home/paul/tempData/client/largeFile.txt";

    public static void main(String[] args) {
        String host = ADDRESS;
        int port = PORT;

        int numMessages = 1000;

        // Choose the protocol (TCP or UDP)
        String protocol = "tcp";
        Client client;
        if (args.length == 0) {
            System.out.println("No protocol specified. Using TCP by default.");
            client = new TcpClient();
        } else if (args[0].equalsIgnoreCase("udp")) {
            protocol = "udp";
            client = new UdpClient();
        } else if (args[0].equalsIgnoreCase("tcp")) {
            client = new TcpClient();
        } else {
            System.out.println("Invalid protocol specified. Using TCP by default.");
            client = new TcpClient();
        }

        client.startClient(protocol, host, port, numMessages);
    }
}