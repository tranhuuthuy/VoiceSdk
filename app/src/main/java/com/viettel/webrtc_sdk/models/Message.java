package com.viettel.webrtc_sdk.models;

import java.util.Date;

public class Message {
    private String content;
    public Date date;

    private Type type; // 0 - user, 1-bot

    public enum Type{
        USER, BOT
    }

    public Message(String content, Type type) {
        this.content = content;
        this.date = new Date();
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
