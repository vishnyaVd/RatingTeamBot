package org.cherry.model;

public class Comment {
    private String id;
    private String teamId;
    private String mark;
    private String text;

    public Comment(String id, String teamId, String mark, String text) {
        this.id = id;
        this.teamId = teamId;
        this.mark = mark;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
