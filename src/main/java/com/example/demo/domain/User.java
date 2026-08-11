package com.example.demo.domain;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;

    public User() {}

    public User(Long id, String username, String passwordHash, String displayName) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    public User safeCopy() {
        return new User(id, username, null, displayName);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
