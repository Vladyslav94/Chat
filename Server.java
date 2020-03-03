package com.javarush.task.task30.task3008;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int serverPort = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        Socket clientsocket = null;

        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server is run");
            while (true) {
                clientsocket = serverSocket.accept();
                new Handler(clientsocket).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            serverSocket.close();
            clientsocket.close();
        }
    }

    public static void sendBroadcastMessage(Message message) {
        try {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet())
                pair.getValue().send(message);
        } catch (IOException e) {
            System.out.println("Сообщение не отправлено");
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Connection established with " + socket.getRemoteSocketAddress());
            String userName = null;

            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                ConsoleHelper.writeMessage("Connection is cosed!");

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Error");
            }



        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {

                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                String userName = message.getData();

                if (message.getType().equals(MessageType.USER_NAME) &&
                        !userName.isEmpty() && !connectionMap.containsKey(userName)) {
                    connectionMap.put(userName, connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED, "Ваше имя принято!"));
                    return userName;
                }
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> s : connectionMap.entrySet()) {
                if (!connectionMap.containsKey(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, s.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String text = message.getData();
                    Server.sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + text));
                } else {
                    ConsoleHelper.writeMessage("Ошибка");
                }
            }
        }

    }
}
