package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer implements Server {
    @Override
    public void startServer(String protocol, int port, int maxClients) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on " + protocol.toUpperCase() + " port " + port + "...");

            ExecutorService executorService = Executors.newFixedThreadPool(maxClients);

            int clientCounter = 0;

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                // Handle each client in a separate thread
                Runnable clientHandler = new TcpClientHandler(clientSocket, protocol, clientCounter + 1);
                clientCounter++;
                executorService.submit(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
