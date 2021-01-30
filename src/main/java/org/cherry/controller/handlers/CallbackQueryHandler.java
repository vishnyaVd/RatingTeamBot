package org.cherry.controller.handlers;

import org.cherry.config.DbConfig;
import org.cherry.controller.Bot;
import org.cherry.controller.DatabaseManager;
import org.cherry.model.Comment;
import org.cherry.model.Keyboard;
import org.cherry.model.Schedule;
import org.cherry.model.Team;
import org.cherry.properties.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import javax.xml.crypto.Data;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CallbackQueryHandler {
    private final Bot bot;
    private final Update update;
    private final String chatId;

    public CallbackQueryHandler(Bot bot, Update update) {
        this.bot = bot;
        this.update = update;
        chatId = update.getCallbackQuery().getMessage().getChatId().toString();
    }

    public void handle(){

        /* Есди данные кнопки начинаются с show_team_profile - нужно показать профиль отряда */
        if (update.getCallbackQuery().getData().startsWith(ChatCommands.showTeamButtonData)){
            showTeam();
        }

        /*Показать список отрядов*/
        if (update.getCallbackQuery().getData().equals(ChatCommands.showTeamsListButtonData)){
            showTeamsList();
        }

        /* Отмета */
        if (update.getCallbackQuery().getData().equals(ChatCommands.cancelButtonData)){
            DatabaseManager db = new DatabaseManager();
            db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
            bot.sendMessage(chatId, ReplyMessages.success, null, null);
        }

        /* Добавить отряд*/
        if (update.getCallbackQuery().getData().equals(ChatCommands.addTeamButtonData)){
            addTeam();
        }

        /* Удаить отрятд */
        if (update.getCallbackQuery().getData().equals(ChatCommands.deleteTeamButtonData)){
            deleteTeam();
        }

        /* Добавить баллы*/
        if (update.getCallbackQuery().getData().equals(ChatCommands.addRatingButtonData)){
            addRatingToTeam();
        }

        /* Показать историю добавления баллов */
        if (update.getCallbackQuery().getData().equals(ChatCommands.historyTeamRatingButtonData)){
            showComments();
        }
        
        if(update.getCallbackQuery().getData().equals(ChatCommands.editTeamNameButtonData)){
            editTeamName();
        }

        if(update.getCallbackQuery().getData().equals(ChatCommands.editTeamPhotoButtonData)){
            editTeamPhoto();
        }

        if (update.getCallbackQuery().getData().startsWith(ChatCommands.showSchedule)){
            showSchedule();
        }

        if (update.getCallbackQuery().getData().equals(ChatCommands.editScheduleButtonData)){
            editSchedule();
        }

        if (update.getCallbackQuery().getData().equals(ChatCommands.backToFullScheduleData)){
            showFullSchedule();
        }

        if (update.getCallbackQuery().getData().equals(ChatCommands.addScheduleDayButtonData)){
            addDayToSchedule();
        }

        if (update.getCallbackQuery().getData().equals(ChatCommands.deleteScheduleDayButtonData)){
            deleteSchedule();
        }

        if (update.getCallbackQuery().getData().equals(ChatCommands.cancelMailingData)){
            cancelMailing();
        }

        if (update.getCallbackQuery().getData().equals(ChatCommands.confirmMailingData)){
            mailing();
        }
    }

    private void mailing() {
        DatabaseManager db = new DatabaseManager();
        List<String> ids = db.getAllChatIds();
        db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
        bot.sendMailing(ids, db.getSessionParameter(chatId, DatabaseParameters.MAILING_TEXT));
        db.deleteSessionParameter(chatId, DatabaseParameters.MAILING_TEXT);
        db.close();
    }

    private void cancelMailing() {
        DatabaseManager db = new DatabaseManager();
        db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
        db.deleteSessionParameter(chatId, DatabaseParameters.MAILING_TEXT);
        db.close();
        bot.sendMessage(chatId, ReplyMessages.success, null, null);
    }

    private void deleteSchedule() {
        DatabaseManager db = new DatabaseManager();
        db.deleteSchedule(db.getSessionParameter(chatId, DatabaseParameters.CURRENT_SCHEDULE_DATE));
        db.close();
        showFullSchedule();

    }

    private void addDayToSchedule() {
        DatabaseManager db = new DatabaseManager();
        db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.WAIT_INPUT_SCHEDULE_DATE);
        db.close();
        bot.sendMessage(chatId, ReplyMessages.inputScheduleDate, Keyboard.getCancelButton(), null);
    }

    private void showFullSchedule() {
        DatabaseManager db = new DatabaseManager();
        String messageId = db.getSessionParameter(chatId, DatabaseParameters.LAST_SCHEDULE_MESSAGE_ID);
        db.deleteSessionParameter(chatId, DatabaseParameters.CURRENT_SCHEDULE_DATE);
        Schedule schedule = db.getScheduleByDate(getCurrentDate());
        String photoId = (db.getScheduleByDate(getCurrentDate()) == null) ? DatabaseValues.NOT_FOUND_IMAGE : db.getScheduleByDate(getCurrentDate()).getPhotoId();
        bot.editMessage(chatId, messageId, photoId, ReplyMessages.thisIsCurrentSchedule, Keyboard.fullSchedule());
        db.close();
    }

    private void editSchedule() {
        DatabaseManager db = new DatabaseManager();
        db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.WAIT_INPUT_SCHEDULE);
        bot.sendMessage(chatId, ReplyMessages.inputNewSchedule, Keyboard.getCancelButton(), null);
    }

    private void showSchedule() {
        DatabaseManager db = new DatabaseManager();
        String scheduleDate = update.getCallbackQuery().getData().split(ChatCommands.delim)[1]; /*Айди берём из данных кнопки, они разделены пробелом - show_team *id* */
        String messageId = db.getSessionParameter(chatId, DatabaseParameters.LAST_SCHEDULE_MESSAGE_ID); // Ид сообщения берём из базы данных, из сессии
        Schedule schedule = db.getScheduleByDate(scheduleDate);
        db.setSessionParameter(chatId, DatabaseParameters.CURRENT_SCHEDULE_DATE, scheduleDate);
        bot.editMessage(chatId, messageId, schedule.getPhotoId(), null, Keyboard.editSchedule());
        db.close();
    }

    private void editTeamPhoto() {
        DatabaseManager db = new DatabaseManager();
        db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE,ChatStage.WAIT_INPUT_TEAM_PHOTO);
        db.close();
        bot.sendMessage(chatId, ReplyMessages.inputNewTeamPhoto, null, null);
    }

    private void editTeamName() {
        DatabaseManager db = new DatabaseManager();
        db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE,ChatStage.WAIT_INPUT_TEAM_NAME);
        db.close();
        bot.sendMessage(chatId, ReplyMessages.inputNewTeamName, null, null);
    }

    private void showComments() {
        List<Comment> comments = new ArrayList<>();
        DatabaseManager db = new DatabaseManager();
        comments = db.getCommentsByTeamId(db.getSessionParameter(chatId, DatabaseParameters.CURRENT_TEAM_ID));
        db.close();
        String message = makeCommentsMessage(comments);
        bot.sendMessage(chatId, message, null,null);
    }

    private String makeCommentsMessage(List<Comment> comments) {
        if (comments.size() == 0) return ReplyMessages.noComments;
        StringBuilder stringBuilder = new StringBuilder();
        for (Comment comment : comments) {
            stringBuilder.append(comment.getMark() + "\n");
            stringBuilder.append(comment.getText() + "\n");
            stringBuilder.append("__________________\n");
        }
        return stringBuilder.toString();
    }


    private void addRatingToTeam() {
        DatabaseManager db = new DatabaseManager();
        db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.WAIT_INPUT_COMMENT_MARK);
        bot.sendMessage(chatId, ReplyMessages.inputMark, null, null);
    }

    private void deleteTeam() {
        DatabaseManager db = new DatabaseManager();
        db.deleteTeam(db.getSessionParameter(chatId, DatabaseParameters.CURRENT_TEAM_ID));
        db.close();
        showTeamsList();
    }

    private void addTeam() {
        DatabaseManager db = new DatabaseManager();
        db.addTeam();
        db.close();
        showTeamsList();
    }

    private void showTeamsList() {
        DatabaseManager db = new DatabaseManager();
        Integer messageId = Integer.parseInt(db.getSessionParameter(chatId, DatabaseParameters.LAST_RATING_MESSAGE_ID)); // Ид сообщения берём из базы данных, из сессии

        EditMessageMedia messageMedia = new EditMessageMedia();
        messageMedia.setChatId(chatId);
        messageMedia.setMessageId(messageId);
        messageMedia.setMedia(new InputMediaPhoto(DatabaseParameters.MENU_PHOTO_ID));
        bot.editMessageMedia(messageMedia);

        EditMessageReplyMarkup messageReplyMarkup = new EditMessageReplyMarkup();
        messageReplyMarkup.setChatId(chatId);
        messageReplyMarkup.setMessageId(messageId);
        messageReplyMarkup.setReplyMarkup(Keyboard.getRatingMenu(db.getUserRole(chatId)));
        bot.editMessageReplyMarkup(messageReplyMarkup);

        db.close();
    }

    private void showTeam(){
        DatabaseManager db = new DatabaseManager();
        String teamId = update.getCallbackQuery().getData().split(ChatCommands.delim)[1]; /*Айди берём из данных кнопки, они разделены пробелом - show_team *id* */
        Integer messageId = Integer.parseInt(db.getSessionParameter(chatId, DatabaseParameters.LAST_RATING_MESSAGE_ID)); // Ид сообщения берём из базы данных, из сессии
        Team team = db.getTeamById(teamId);
        db.setSessionParameter(chatId, DatabaseParameters.CURRENT_TEAM_ID, teamId);

        /* Редим фото */
        EditMessageMedia messageMedia = new EditMessageMedia();
        messageMedia.setChatId(chatId);
        messageMedia.setMessageId(messageId);
        messageMedia.setMedia(new InputMediaPhoto(team.getPhotoId()));
        bot.editMessageMedia(messageMedia);

        /* редим описание*/
        EditMessageCaption messageCaption = new EditMessageCaption();
        messageCaption.setChatId(chatId);
        messageCaption.setMessageId(messageId);
        messageCaption.setCaption(team.getName() + " - " + team.getRating());
        bot.editMessageCaption(messageCaption);

        /* Редим inline клавиатуру */
        EditMessageReplyMarkup messageReplyMarkup = new EditMessageReplyMarkup();
        messageReplyMarkup.setChatId(chatId);
        messageReplyMarkup.setMessageId(messageId);
        messageReplyMarkup.setReplyMarkup(Keyboard.getTeamSectionMenu(db.getUserRole(chatId)));
        bot.editMessageReplyMarkup(messageReplyMarkup);

        db.close();
    }

    private void cancelStage(){
        DatabaseManager db = new DatabaseManager();
        db.setSessionParameter(chatId, DatabaseParameters.CHAT_STAGE, ChatStage.NONE);
        db.close();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(ReplyMessages.success);

        bot.sendMessage(chatId, ReplyMessages.success, null, null);
    }

    private static String getCurrentDate(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DbConfig.DATE_FORMAT);
        LocalDate localDate = LocalDate.now();
        return dtf.format(localDate);
    }
}
