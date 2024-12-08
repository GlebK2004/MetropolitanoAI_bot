package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "MetropolitanoAI_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7902607949:AAFRVlJPhc32J0fSk29tkYsTVSR6MCijbvo"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "chat-gpt-token"; //TODO: добавь токен ChatGPT в кавычках

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        sendTextMessage("Vamos!");
        String message = getMessageText();
        if (message.startsWith("/")) {
            answerForCommand(message);
        }
        else{
            sendTextMessage("Пожалуйста,выберите одну из команд:");
            sendTextMessage(loadMessage("main"));
        }

    }

    private void answerForCommand (String text)
    {
        sendPhotoMessage(text);
        String answer = loadMessage(text);
        sendTextMessage(answer);
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
