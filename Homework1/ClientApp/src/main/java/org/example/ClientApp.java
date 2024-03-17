package org.example;

import java.io.*;
import java.net.Socket;

public class ClientApp {
    public static final int CHUNK_SIZE = 65000;
    public static final String ADDRESS = "ec2-**-**-**-***.eu-north-1.compute.amazonaws.com";
    public static final int PORT = 12345;
    public static final String LARGE_FILE_PATH = "/home/paul/tempData/client/largeFile.txt";

    public static boolean streaming = false;
    public static boolean stopAndWait = false;

    public static void main(String[] args) {
        String host = ADDRESS;
        int port = PORT;

        // 64 KB, 1MB, 100MB, 500MB, 1GB, 5GB, 10GB
        int numMessages = 1;

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

        if (args.length > 2) {
            port = Integer.parseInt(args[2]);
        }

        client.startClient(protocol, host, port, numMessages);
    }
}