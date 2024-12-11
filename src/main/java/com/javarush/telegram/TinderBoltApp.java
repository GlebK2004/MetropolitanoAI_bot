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
    public static final String OPEN_AI_TOKEN = "gpt:GMEpATyDssZWFB4H2wdsJFkblB3TtbsOsfleLvmgKt8qL2qt"; //TODO: добавь токен ChatGPT в кавычках

    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();

    private UserInfo me;
    private UserInfo notMe;
    private int questionCount;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);


    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь

        String message = getMessageText();

        if (message.startsWith("/")) {
            answerForCommand(message);
            switch (message) {
                case "/gpt": {
                    currentMode = DialogMode.GPT;
                    break;
                }
                case "/date": {
                    currentMode = DialogMode.DATE;
                    sendTextButtonsMessage("Выберите девушку:",
                            "Ариана Гранде", "date_grande",
                            "Марго Робби", "date_robbie",
                            "Зендая", "date_zendaya",
                            "Райан Гослинг", "date_gosling",
                            "ом Харди", "date_hardy");
                    break;
                }
                case "/main": {
                    currentMode = DialogMode.MAIN;
                    break;
                }
                case "/message": {
                    currentMode = DialogMode.MESSAGE;
                    sendTextButtonsMessage("Сбросьте свою переписку",
                            "Следующее собщение: ", "message_next",
                            "Пригласить на свидание: ", "message_date");
                    break;
                }
                case "/profile":{
                    currentMode = DialogMode.PROFILE;
                    me = new UserInfo();
                    questionCount=0;
                    sendTextMessage("Сколько вам лет?");
                    questionCount++;
                    break;
                }
                case "/opener":{
                    currentMode = DialogMode.OPENER;
                    notMe = new UserInfo();
                    questionCount=0;
                    sendTextMessage("Сколько ей лет?");
                    questionCount++;
                    break;
                }
            }
            return;
        }
        else{
            if (currentMode == null) {
                sendTextMessage("Пожалуйста,выберите одну из команд:");
                sendTextMessage(loadMessage("main"));
            }
        }

        if (!isMessageCommand()) {
            switch (currentMode) {
                case GPT: {
                    String promt = loadPrompt("gpt");
                    String answer = chatGPT.sendMessage(promt, message);
                    sendTextMessage(answer);
                    break;
                }
                case DATE: {
                    String querry = getCallbackQueryButtonKey();
                    if (querry.startsWith("date_")) {
                        if (querry.equals("date_grande")) {
                            sendPhotoMessage(querry);
                            sendTextMessage("Отичный выбор! Напиши ей: ");
                            String promt = loadPrompt(querry);
                            chatGPT.setPrompt(promt);
                            return;
                        }
                    }
                    String answer = chatGPT.sendMessage("Диалог с девушкой: ", message);
                    sendTextMessage(answer);
                    break;
                }
                case MAIN: {
                    sendTextMessage("Vamos!");

                    showMainMenu("главное меню бота", "/main",
                            "генерация Tinder-профля", "/profile",
                            "сообщение для знакомства", "/opener",
                            "переписка от вашего имени", "/message",
                            "переписка со звездами", "/date",
                            "задать вопрос чату GPT", "/gpt");
                    break;
                }
                case MESSAGE: {
                    String querry = getCallbackQueryButtonKey();
                    if (querry.startsWith("message_")) {
                        String promt = loadPrompt(querry);
                        String userChatHistory = String.join("\n\n", list);

                        Message msg = sendTextMessage("Погоди минуту...");
                        String answer = chatGPT.sendMessage(promt, userChatHistory);
                        updateTextMessage(msg, answer);
                    }
                    list.add(message);
                    break;
                }
                case PROFILE: {
                    switch (questionCount) {
                        case 1: {
                            me.age = message;
                            sendTextMessage("Кем вы работаете?");
                            questionCount++;
                            break;
                        }
                        case 2: {
                            me.occupation = message;
                            sendTextMessage("Какое у вас хобби?");
                            questionCount++;
                            break;
                        }
                        case 3: {
                            me.hobby = message;
                            sendTextMessage("Что вам НЕ нравится в людях?");
                            questionCount++;
                            break;
                        }
                        case 4: {
                            me.annoys = message;
                            sendTextMessage("Цель знакомства?");
                            questionCount++;
                            break;
                        }
                        default: {
                            me.goals = message;
                            String aboutMyself = me.toString();
                            String promt = loadPrompt("profile");
                            Message msg = sendTextMessage("Погоди минуту...");
                            String answer = chatGPT.sendMessage(promt, aboutMyself);
                            sendTextMessage(answer);
                            updateTextMessage(msg, answer);
                            break;
                        }
                    }
                    break;
                }
                case OPENER: {
                    switch (questionCount) {
                        case 1: {
                            notMe.age = message;
                            sendTextMessage("Кем она работает?");
                            questionCount++;
                            break;
                        }
                        case 2: {
                            notMe.occupation = message;
                            sendTextMessage("Какое у неё хобби?");
                            questionCount++;
                            break;
                        }
                        case 3: {
                            notMe.hobby = message;
                            sendTextMessage("Как её зовут?");
                            questionCount++;
                            break;
                        }
                        case 4: {
                            notMe.name = message;
                            sendTextMessage("Цель знакомства?");
                            questionCount++;
                            break;
                        }
                        default: {
                            notMe.goals = message;
                            String aboutHerself = notMe.toString();
                            String promt = loadPrompt("opener");
                            Message msg = sendTextMessage("Погоди минуту...");
                            String answer = chatGPT.sendMessage(promt, aboutHerself);
                            updateTextMessage(msg, answer);
                            break;
                        }
                    }
                    break;
                }
            }
        }

    }

    private void answerForCommand (String text) {
        sendPhotoMessage(text);
        String answer = loadMessage(text);
        sendTextMessage(answer);
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
