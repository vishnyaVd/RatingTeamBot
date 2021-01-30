package org.cherry.controller;

import org.cherry.config.BotConfig;
import org.cherry.controller.handlers.CallbackQueryHandler;
import org.cherry.controller.handlers.InputPhotoHandler;
import org.cherry.controller.handlers.InputTextHandler;
import org.cherry.model.Keyboard;
import org.cherry.model.Team;
import org.cherry.properties.ChatCommands;
import org.cherry.properties.DatabaseParameters;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {

        /* Если пришло текстовое сообщение */
        if (update.hasMessage() && update.getMessage().hasText()){
            InputTextHandler inputTextHandler = new InputTextHandler(this, update);
            inputTextHandler.handle();
        }

        /* сли пришло фото */
        if (update.hasMessage() && update.getMessage().hasPhoto()){
            InputPhotoHandler inputPhotoHandler = new InputPhotoHandler(this, update);
            inputPhotoHandler.handle();
        }

        /* Если пришла реакция на инлайн-клавиатуру - в соответсвующий обработчик */
        if (update.hasCallbackQuery()){
            CallbackQueryHandler callbackQueryHandler = new CallbackQueryHandler(this, update);
            callbackQueryHandler.handle();


        }
    }


    public Message sendMessage(String chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup, ReplyKeyboardMarkup replyKeyboardMarkup){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (inlineKeyboardMarkup != null) message.setReplyMarkup(inlineKeyboardMarkup);
        if (replyKeyboardMarkup != null) message.setReplyMarkup(replyKeyboardMarkup);
        Message result = null;
        try{
            result = execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Message sendPhoto(String chatId, String photoId, String caption, InlineKeyboardMarkup keyboard){
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(photoId));
        sendPhoto.setCaption(caption);
        sendPhoto.setReplyMarkup(keyboard);
        Message result = null;
        try{
            result = execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Message editMessageMedia (EditMessageMedia message){
        Message result = null;
        try{
            result = (Message) execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Message editMessageCaption(EditMessageCaption message){
        Message result = null;
        try{
            result = (Message) execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void editMessageReplyMarkup(EditMessageReplyMarkup message){
        try{
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void editMessage(String chatId, String messageIdStr, String photoId, String caption, InlineKeyboardMarkup keyboardMarkup){
        Integer messageId = Integer.parseInt(messageIdStr); // Ид сообщения берём из базы данных, из сессии

        /* Редим фото */
        if (photoId != null){
            EditMessageMedia messageMedia = new EditMessageMedia();
            messageMedia.setChatId(chatId);
            messageMedia.setMessageId(messageId);
            messageMedia.setMedia(new InputMediaPhoto(photoId));
            editMessageMedia(messageMedia);
        }

        /* редим описание*/
        if (caption != null){
            EditMessageCaption messageCaption = new EditMessageCaption();
            messageCaption.setChatId(chatId);
            messageCaption.setMessageId(messageId);
            messageCaption.setCaption(caption);
            editMessageCaption(messageCaption);
        }

        /* Редим inline клавиатуру */
        if (keyboardMarkup != null){
            EditMessageReplyMarkup messageReplyMarkup = new EditMessageReplyMarkup();
            messageReplyMarkup.setChatId(chatId);
            messageReplyMarkup.setMessageId(messageId);
            messageReplyMarkup.setReplyMarkup(keyboardMarkup);
            editMessageReplyMarkup(messageReplyMarkup);
        }

    }

    public void deleteMessage(String chatId, String messageId){
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(Integer.parseInt(messageId));
        try{
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void sendMailing(List<String> ids, String text){
        for (String chatId : ids) {
            sendMessage(chatId, text, null, null);
        }
    }

}
