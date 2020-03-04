package com.javarush.task.task30.task3008.client;

public class BotClient extends Client{

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    public class BotSocketThread extends SocketThread{

    }

    @Override
    protected String getUserName() {
        int num = (int)(Math.random()*100);
        return "date_bot_" + num;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }
}
