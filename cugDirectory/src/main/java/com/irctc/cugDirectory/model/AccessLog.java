package com.irctc.cugDirectory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_log")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action; // login, login_failed, logout, search, view_record, password_change, admin_create_user, admin_toggle_user

    @Column(length = 500)
    private String detail;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public AccessLog() {}

    public AccessLog(String username, String action, String detail) {
        this.username = username;
        this.action = action;
        this.detail = detail;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}