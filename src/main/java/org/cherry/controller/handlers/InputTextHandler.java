package org.cherry.controller.handlers;

import org.cherry.config.BotConfig;
import org.cherry.config.DbConfig;
import org.cherry.controller.Bot;
import org.cherry.controller.DatabaseManager;
import org.cherry.model.Keyboard;
import org.cherry.model.Schedule;
import org.cherry.properties.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InputTextHandler {
    private final Bot bot;
    private final Update update;
    private final String chatId;

    public InputTextHandler(Bot bot, Update update) {
        this.bot = bot;
        this.update = update;
        chatId = update.getMessage().getChatId().toString();
    }

    public void handle(){
        DatabaseManager db = new DatabaseManager();

        if (!db.containSession(chatId) && update.getMessage().getText().equals(ChatCommands.START)){
            db.makeSessionDoc(chatId);
            bot.sendMessage(chatId,ReplyMessages.welcomeMessage, null, Keyboard.getMainMenu(db.getUserRole(chatId)));
        } else if (db.getSessionParameter(chatId, DatabaseParameters.CHAT_STAGE).equals(ChatStage.NONE)){
            commandHandler(db);
        } else if (!db.getSessionParameter(chatId, DatabaseParameters.CHAT_STAGE).equals(ChatStage.NONE)){
            replyTextHandle(db);
        }

        db.close();
    }

    private void commandHandler(DatabaseManager db){
        SendMessage sendMessage = new SendMessage();

        switch (update.getMessage().getText()){

            case (ChatCommands.START):
                bot.sendMessage(chatId,ReplyMessages.welcomeMessage, null, Keyboard.getMainMenu(db.getUserRole(chatId)));
                break;

            case (ChatCommands.HELP):
            case (ChatCommands.HELP_BUTTON):
                bot.sendMessage(chatId, ReplyMessages.helpMessage, null, null);
                break;

            case (ChatCommands.RATING):
                if (!db.getSessionParameter(chatId, DatabaseParameters.LAST_RATING_MESSAGE_ID).equals("0")){
                    bot.deleteMessage(chatId, db.getSessionParameter(chatId, DatabaseParameters.LAST_RATING_MESSAGE_ID));
                }
                Message lastRatingMessage = bot.sendPhoto(chatId, DatabaseParameters.MENU_PHOTO_ID, null, Keyboard.getRatingMenu(db.getUserRole(chatId)));
                db.setSessionParameter(chatId,DatabaseParameters.LAST_RATING_MESSAGE_ID, lastRatingMessage.getMessageId().toString());
                break;

            case(ChatCommands.LOGIN):
                db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.WAIT_INPUT_ADM_PASS);
                bot.sendMessage(chatId, ReplyMessages.waitInputPassword, null, null);
                break;

            case(ChatCommands.LOGOUT):
                if (db.getUserRole(chatId).equals(DatabaseValues.ADMIN)){
                    db.setSessionParameter(chatId, DatabaseParameters.ROLE, DatabaseValues.USER);
                    sendMessage.setText(ReplyMessages.success);
                    sendMessage.setChatId(chatId);
                    bot.sendMessage(chatId, ReplyMessages.success, null, Keyboard.getMainMenu(DatabaseValues.USER));
                }
                break;

            case (ChatCommands.SCHEDULE):
                if (!db.getSessionParameter(chatId, DatabaseParameters.LAST_SCHEDULE_MESSAGE_ID).equals("0")){
                    bot.deleteMessage(chatId, db.getSessionParameter(chatId, DatabaseParameters.LAST_SCHEDULE_MESSAGE_ID));
                }
                Schedule schedule = db.getScheduleByDate(getCurrentDate());
                if (schedule == null){
                    bot.sendPhoto(chatId, DatabaseValues.NOT_FOUND_IMAGE, null, null);
                    break;
                }
                if (db.getUserRole(chatId).equals(DatabaseValues.ADMIN)){
                    db.setSessionParameter(chatId,DatabaseParameters.CURRENT_SCHEDULE_DATE, new SimpleDateFormat(DbConfig.DATE_FORMAT).format(schedule.getDate().toString()));
                    Message message = bot.sendPhoto(chatId, schedule.getPhotoId(), null, Keyboard.editSchedule());
                    db.setSessionParameter(chatId,DatabaseParameters.LAST_SCHEDULE_MESSAGE_ID, message.getMessageId().toString());
                } else {
                    bot.sendPhoto(chatId, schedule.getPhotoId(), null, null);
                }
                break;

            case (ChatCommands.FULL_SCHEDULE):
                if (!db.getSessionParameter(chatId, DatabaseParameters.LAST_SCHEDULE_MESSAGE_ID).equals("0")){
                    bot.deleteMessage(chatId, db.getSessionParameter(chatId, DatabaseParameters.LAST_SCHEDULE_MESSAGE_ID));
                }
                if (db.getUserRole(chatId).equals(DatabaseValues.ADMIN)){
                    String photoId = (db.getScheduleByDate(getCurrentDate()) == null) ? DatabaseValues.NOT_FOUND_IMAGE : db.getScheduleByDate(getCurrentDate()).getPhotoId();
                    Message message = bot.sendPhoto(chatId, photoId, ReplyMessages.thisIsCurrentSchedule,  Keyboard.fullSchedule());
                    db.setSessionParameter(chatId, DatabaseParameters.LAST_SCHEDULE_MESSAGE_ID, message.getMessageId().toString());
                } else {
                  bot.sendMessage(chatId, ReplyMessages.error, null, null);
                }
                break;

            case (ChatCommands.MAILING):
                if (db.getUserRole(chatId).equals(DatabaseValues.ADMIN)){
                    bot.sendMessage(chatId, ReplyMessages.inputMailingText, Keyboard.getCancelButton(), null);
                    db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.WAIT_INPUT_MAILING_TEXT);
                } else {
                    bot.sendMessage(chatId, ReplyMessages.error, null, null);
                }
                break;

            case (ChatCommands.SCHEDULE_IMAGE):
                String path = createScheduleImage();
                break;

            default:
                bot.sendMessage(chatId, ReplyMessages.error, null, null);
                break;
        }
    }


    private void replyTextHandle(DatabaseManager db){
        String messageText = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        switch (db.getSessionParameter(chatId, DatabaseParameters.CHAT_STAGE)){

            /* Ожидание пароля */
            case(ChatStage.WAIT_INPUT_ADM_PASS):
                if (messageText.equals(BotConfig.ADMIN_PASS)){
                    db.setSessionParameter(chatId, DatabaseParameters.ROLE, DatabaseValues.ADMIN);
                    db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
                    bot.sendMessage(chatId, ReplyMessages.loginSuccess, null, Keyboard.getMainMenu(DatabaseValues.ADMIN));
                } else {
                    sendMessage.setText(ReplyMessages.loginError);
                    sendMessage.setReplyMarkup(Keyboard.getCancelButton());
                    bot.sendMessage(chatId, ReplyMessages.loginError, Keyboard.getCancelButton(), null);
                }
                break;

                /* Ожидание введения оценки */
            case (ChatStage.WAIT_INPUT_COMMENT_MARK):
                if (isNumeric(messageText)){
                    String teamId = db.getSessionParameter(chatId, DatabaseParameters.CURRENT_TEAM_ID);
                    String commentId = db.addCommentMark(teamId, messageText);
                    db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.WAIT_INPUT_COMMENT_TEXT);
                    db.setSessionParameter(chatId, DatabaseParameters.CURRENT_COMMENT_ID, commentId);
                    bot.sendMessage(chatId,ReplyMessages.inputComment,null,null);
                } else {
                    bot.sendMessage(chatId, ReplyMessages.error, null, null);
                }
                break;

                /* Ожиданме введения комментария */
            case (ChatStage.WAIT_INPUT_COMMENT_TEXT):
                String commentId = db.getSessionParameter(chatId, DatabaseParameters.CURRENT_COMMENT_ID);
                db.addCommentText(commentId, messageText);
                db.sumRating(db.getSessionParameter(chatId, DatabaseParameters.CURRENT_TEAM_ID));
                db.deleteSessionParameter(chatId, DatabaseParameters.CURRENT_TEAM_ID);
                db.deleteSessionParameter(chatId, DatabaseParameters.CURRENT_COMMENT_ID);
                db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
                bot.sendMessage(chatId, ReplyMessages.success, null, null);
                break;

                /*водим название отряда*/
            case (ChatStage.WAIT_INPUT_TEAM_NAME):
                String teamId = db.getSessionParameter(chatId, DatabaseParameters.CURRENT_TEAM_ID);
                db.setTeamName(teamId, messageText);
                db.deleteSessionParameter(chatId, DatabaseParameters.CURRENT_TEAM_ID);
                db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
                bot.sendMessage(chatId, ReplyMessages.success, null, null);
                break;

            case (ChatStage.WAIT_INPUT_SCHEDULE_DATE):
                if (checkDateFormat(messageText)){
                    if (db.getScheduleByDate(messageText) != null){
                        bot.sendMessage(chatId, ReplyMessages.errorScheduleDate, Keyboard.getCancelButton(), null);
                    } else {
                        db.addScheduleDay(messageText);
                        bot.sendMessage(chatId, ReplyMessages.success, null, null);
                    }
                    db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
                } else {
                    bot.sendMessage(chatId, ReplyMessages.error, Keyboard.getCancelButton(), null);
                }
                break;

            case (ChatStage.WAIT_INPUT_MAILING_TEXT):
                db.setSessionParameter(chatId, DatabaseParameters.MAILING_TEXT, messageText);
                bot.sendMessage(chatId, messageText, Keyboard.confirmMailingText(), null);
                break;


            default:
                if (!db.getSessionParameter(chatId, DatabaseParameters.CHAT_STAGE).equals(ChatStage.NONE)){
                    bot.sendMessage(chatId, ReplyMessages.error, Keyboard.getCancelButton(), null);
                } else {
                    bot.sendMessage(chatId, ReplyMessages.error, null, null);
                }
                break;
        }
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private static String getCurrentDate(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DbConfig.DATE_FORMAT);
        LocalDate localDate = LocalDate.now();
        return dtf.format(localDate);
    }

    private Boolean checkDateFormat(String date){
        Character ch = '/';
        if (date.length() != 10 || date.charAt(2) != ch || date.charAt(5) != ch) return false;
        SimpleDateFormat sdf = new SimpleDateFormat(DbConfig.DATE_FORMAT);
        try {
            sdf.parse(date);
        } catch(java.text.ParseException e) {
            return false;
        }
        return true;
    }

    private String createScheduleImage() {
        try{
            URL res = getClass().getClassLoader().getResource("background.jpg");
            File file = Paths.get(res.toURI()).toFile();
            BufferedImage background = ImageIO.read(file);
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
