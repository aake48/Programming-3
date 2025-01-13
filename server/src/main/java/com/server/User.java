package com.server;

public class User {

    private String username;
    private String password;
    private String email;
    private String userNickName;

    public User(String username, String password, String email, String userNickName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.userNickName = userNickName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getUserNickName() {
        return userNickName;
    }


}
