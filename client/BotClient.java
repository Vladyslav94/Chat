package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BotClient extends Client{

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (!message.contains(": ")) return;

            String[] nameAndarg = message.split(": ");

            SimpleDateFormat sdf = null;

            if ("дата".equals(nameAndarg[1])) {
                sdf = new SimpleDateFormat("d.MM.YYYY");
            } else if ("день".equals(nameAndarg[1])) {
                sdf = new SimpleDateFormat("d");
            } else if ("месяц".equals(nameAndarg[1])) {
                sdf = new SimpleDateFormat("MMMM");
            } else if ("год".equals(nameAndarg[1])) {
                sdf = new SimpleDateFormat("YYYY");
            } else if ("время".equals(nameAndarg[1])) {
                sdf = new SimpleDateFormat("H:mm:ss");
            } else if ("час".equals(nameAndarg[1])) {
                sdf = new SimpleDateFormat("H");
            } else if ("минуты".equals(nameAndarg[1])) {
                sdf = new SimpleDateFormat("m");
            } else if ("секунды".equals(nameAndarg[1])) {
                sdf = new SimpleDateFormat("s");
            }

            if (sdf != null) {
                Calendar calendar = Calendar.getInstance();
                String result = "Информация для " + nameAndarg[0] + ": ";
                result += sdf.format(calendar.getTime());
                sendTextMessage(result);
            }
        }
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
