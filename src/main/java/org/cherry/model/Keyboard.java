package org.cherry.model;

import org.cherry.config.DbConfig;
import org.cherry.controller.DatabaseManager;
import org.cherry.properties.ChatCommands;
import org.cherry.properties.DatabaseValues;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.text.SimpleDateFormat;
import java.util.*;

public class Keyboard {

    public static  ReplyKeyboardMarkup getMainMenu(String role){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(ChatCommands.RATING);
        row.add(ChatCommands.SCHEDULE);
        keyboardList.add(row);

        row = new KeyboardRow();
        row.add(ChatCommands.HELP_BUTTON);
        keyboardList.add(row);

        if (role == DatabaseValues.ADMIN){
            KeyboardRow secondKeyboardRow = new KeyboardRow();
            secondKeyboardRow.add(ChatCommands.FULL_SCHEDULE);
            secondKeyboardRow.add(ChatCommands.MAILING);
            keyboardList.add(secondKeyboardRow);
        }

        replyKeyboardMarkup.setKeyboard(keyboardList);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getRatingMenu(String role){
        DatabaseManager db = new DatabaseManager();
        List<Team> teams = db.getTeams();
        teams.sort(Comparator.comparing(Team::getIntRating, Comparator.reverseOrder()));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List <List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        /* Добавляем кнопки с названиеями отрядов, в data формат "show_team team Id" */
        for (int i = 0; i < teams.size(); i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(teams.get(i).getName());
            button.setCallbackData(ChatCommands.showTeamButtonData + " " + teams.get(i).getId());
            row.add(button);
            keyboard.add(row);
        }

        if (role.equals(DatabaseValues.ADMIN)){
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(ChatCommands.addTeamButtonText);
            button.setCallbackData(ChatCommands.addTeamButtonData);
            row.add(button);
            keyboard.add(row);
        }
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /* Профиль отряда */
    public static InlineKeyboardMarkup getTeamSectionMenu(String role){
        List <List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(ChatCommands.historyTeamRatingButtonText);
        button.setCallbackData(ChatCommands.historyTeamRatingButtonData);
        row.add(button);
        keyboard.add(row);

        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText(ChatCommands.showTeamsListButtonText);
        button.setCallbackData(ChatCommands.showTeamsListButtonData);
        row.add(button);
        keyboard.add(row);


        if (role.equals(DatabaseValues.ADMIN)){
            row = new ArrayList<>();
            InlineKeyboardButton addRating = new InlineKeyboardButton();
            addRating.setText(ChatCommands.addRatingButtonText);
            addRating.setCallbackData(ChatCommands.addRatingButtonData);
            row.add(addRating);

            InlineKeyboardButton deleteTeam = new InlineKeyboardButton();
            deleteTeam.setText(ChatCommands.deleteTeamButtonText);
            deleteTeam.setCallbackData(ChatCommands.deleteTeamButtonData);
            row.add(deleteTeam);
            keyboard.add(row);

            row = new ArrayList<>();
            InlineKeyboardButton editName = new InlineKeyboardButton();
            editName.setText(ChatCommands.editTeamPhotoButtonText);
            editName.setCallbackData(ChatCommands.editTeamPhotoButtonData);
            row.add(editName);

            InlineKeyboardButton editPhoto = new InlineKeyboardButton();
            editPhoto.setText(ChatCommands.editTeamNameButtonText);
            editPhoto.setCallbackData(ChatCommands.editTeamNameButtonData);
            row.add(editPhoto);
            keyboard.add(row);

        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup editSchedule(){
        List <List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(ChatCommands.editScheduleButtonText);
        button.setCallbackData(ChatCommands.editScheduleButtonData);
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(ChatCommands.deleteScheduleDayButtonText);
        button.setCallbackData(ChatCommands.deleteScheduleDayButtonData);
        row.add(button);
        keyboard.add(row);

        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText(ChatCommands.backToFullScheduleText);
        button.setCallbackData(ChatCommands.backToFullScheduleData);
        row.add(button);
        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup fullSchedule(){
        DatabaseManager db = new DatabaseManager();
        List<Schedule> fullSchedule = db.getFullSchedule();
        fullSchedule.sort(Comparator.comparing(Schedule::getDate));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List <List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        /* Добавляем кнопки с названиеями отрядов, в data формат "show_team team Id" */
        for (int i = 0; i < fullSchedule.size(); i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(new SimpleDateFormat(DbConfig.DATE_FORMAT).format(fullSchedule.get(i).getDate()));
            button.setCallbackData(ChatCommands.showSchedule + " " + new SimpleDateFormat(DbConfig.DATE_FORMAT).format(fullSchedule.get(i).getDate()));
            row.add(button);
            keyboard.add(row);
        }

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(ChatCommands.addScheduleDayButtonText);
        button.setCallbackData(ChatCommands.addScheduleDayButtonData);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        keyboard.add(row);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup confirmMailingText(){
        List <List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(ChatCommands.confirmMailingText);
        button.setCallbackData(ChatCommands.confirmMailingData);
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(ChatCommands.cancelMailingText);
        button.setCallbackData(ChatCommands.cancelMailingData);
        row.add(button);
        keyboard.add(row);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getCancelButton(){
        List <List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(ChatCommands.cancelButtonText);
        button.setCallbackData(ChatCommands.cancelButtonData);
        row.add(button);
        keyboard.add(row);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

}
