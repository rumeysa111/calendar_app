package com.example.java_proje;

public class User {
    private String username;
    private String role;

    // Constructor
    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    // Getter ve Setter metodları
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
