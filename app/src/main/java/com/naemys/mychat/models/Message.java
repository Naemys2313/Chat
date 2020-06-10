package com.naemys.mychat.models;

public class Message {

    private String text;
    private String imageUrl;
    private String userName;
    private String senderId;
    private String recipientId;

    public Message() {
    }

    public Message(String text, String imageUrl, String userName, String senderId, String recipientId) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.userName = userName;
        this.senderId = senderId;
        this.recipientId = recipientId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
}
