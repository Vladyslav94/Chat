package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                MessageType messageType = connection.receive().getType();
                String userName = getUserName();
                if (MessageType.NAME_REQUEST.equals(messageType)) {
                    connection.send(new Message(MessageType.USER_NAME, userName));
                } else if (MessageType.NAME_ACCEPTED.equals(messageType)) {
                    notifyConnectionStatusChanged(true);
                    break;
                } else
                    throw new IOException("Unexpected MessageType");

            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (MessageType.TEXT.equals(message.getType())) {
                    processIncomingMessage(message.getData());
                } else if (MessageType.USER_ADDED.equals(message.getType())) {
                    informAboutAddingNewUser(message.getData());
                } else if (MessageType.USER_REMOVED.equals(message.getType())) {
                    informAboutDeletingNewUser(message.getData());
                } else
                    throw new IOException("Unexpected MessageType");
            }
        }

        public void run(){
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();
            Socket socket = null;
            try {
                socket = new Socket(serverAddress, serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }

        }
    }

    protected String getServerAddress() {
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (shouldSendTextFromConsole()) {
            if (clientConnected) {
                sendTextMessage("Соединение установлено.\nДля выхода наберите команду 'exit'.");
            } else {
                ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            }
        }

        while (clientConnected) {
            String string = ConsoleHelper.readString();
            if (string.equalsIgnoreCase("exit")) {
                break;
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(string);
            }
        }
    }
}
