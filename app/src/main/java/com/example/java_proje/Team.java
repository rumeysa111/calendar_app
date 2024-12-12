package com.example.java_proje;

public class Team {
    private String id;

    private String teamName;

    public Team(String teamName,String teamId) {
        this.teamName = teamName;
        this.id = teamId;

    }
    public String getId() {
        return id;
    }

    public String getTeamName() {
        return teamName;
    }
}

