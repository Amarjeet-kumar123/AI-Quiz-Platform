package com.quizai.backend.dto.admin;

public class AdminUserResponse {

    private Long id;
    private String username;
    private String email;

    public AdminUserResponse(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
