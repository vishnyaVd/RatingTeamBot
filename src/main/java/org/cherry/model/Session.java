package org.cherry.model;

public class Session {
    private String id;
    private String chatId;
    private String role;
    private String chatStatus;
    private String currentTeamId;

    public Session(String id, String chatId, String role) {
        this.id = id;
        this.chatId = chatId;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getChatId() {
        return chatId;
    }

    public String getRole() {
        return role;
    }

    public String getChatStatus() {
        return chatStatus;
    }

    public void setChatStatus(String chatStatus) {
        this.chatStatus = chatStatus;
    }

    public String getCurrentTeamId() {
        return currentTeamId;
    }

    public void setCurrentTeamId(String currentTeamId) {
        this.currentTeamId = currentTeamId;
    }
}
