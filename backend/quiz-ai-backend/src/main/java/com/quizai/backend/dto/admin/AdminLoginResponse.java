package com.quizai.backend.dto.admin;

public class AdminLoginResponse {

    private String token;
    private String username;

    public AdminLoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }
}
