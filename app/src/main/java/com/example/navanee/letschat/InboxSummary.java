package com.example.navanee.letschat;

/**
 * Created by navanee on 21-11-2016.
 */

public class InboxSummary {
    private String userID;
    private String lastMessage;
    private String pDate;
    private int numUnreadMessages;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getNumUnreadMessages() {
        return numUnreadMessages;
    }

    public void setNumUnreadMessages(int numUnreadMessages) {
        this.numUnreadMessages = numUnreadMessages;
    }
    public String getpDate() {
        return pDate;
    }

    public void setpDate(String pDate) {
        this.pDate = pDate;
    }
}
