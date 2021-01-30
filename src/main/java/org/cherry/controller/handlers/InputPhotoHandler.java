package org.cherry.controller.handlers;

import org.cherry.controller.Bot;
import org.cherry.controller.DatabaseManager;
import org.cherry.properties.ChatStage;
import org.cherry.properties.DatabaseParameters;
import org.cherry.properties.ReplyMessages;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class InputPhotoHandler {
    private Bot bot;
    private Update update;

    public InputPhotoHandler(Bot bot, Update update) {
        this.bot = bot;
        this.update = update;
    }

    public void handle(){
        DatabaseManager db = new DatabaseManager();
        SendMessage sendMessage = new SendMessage();
        String chatId = update.getMessage().getChatId().toString();
        String photoId;

        switch (db.getSessionParameter(chatId, DatabaseParameters.CHAT_STAGE)){

            case(ChatStage.WAIT_INPUT_TEAM_PHOTO):
                String teamId = db.getSessionParameter(chatId, DatabaseParameters.CURRENT_TEAM_ID);
                photoId = update.getMessage().getPhoto().stream().findFirst().orElse(null).getFileId();
                db.setTeamPhoto(teamId,photoId);
                db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
                bot.sendMessage(chatId, ReplyMessages.success, null, null);
                break;

            case (ChatStage.WAIT_INPUT_SCHEDULE):
                db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
                String date = db.getSessionParameter(chatId, DatabaseParameters.CURRENT_SCHEDULE_DATE);
                db.deleteSessionParameter(chatId, DatabaseParameters.CURRENT_SCHEDULE_DATE);
                photoId = update.getMessage().getPhoto().stream().findFirst().orElse(null).getFileId();
                db.setSchedulePhoto(date, photoId);
                bot.sendMessage(chatId, ReplyMessages.success, null, null);
                break;

            default:
                if (update.getMessage().getCaption().equals("id")){
                    SendMessage message = new SendMessage();
                    bot.sendMessage(chatId, update.getMessage().getPhoto().stream().findFirst().orElse(null).getFileId(), null, null);
                } else {
                    bot.sendMessage(chatId, ReplyMessages.error, null,null);
                }
                break;
        }
    }
}
