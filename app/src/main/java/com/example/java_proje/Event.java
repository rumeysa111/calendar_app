package com.example.java_proje;

import java.util.Date;

public class Event {
    private String eventId; // Firestore'dan alınacak benzersiz kimlik
    private String title;
    private String description;
    private Date date;
    private String teamName;

    public Event(String eventId, String title, String description, Date date, String teamName) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.teamName = teamName;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public String getTeamName() {
        return teamName;
    }
}
