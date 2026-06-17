package com.coursework.system.auth.dto;

import java.util.List;

public class CurrentUserResponse {
    private Long id;
    private String username;
    private String realName;
    private List<String> roles;

    public CurrentUserResponse() {
    }

    public CurrentUserResponse(Long id, String username, String realName, List<String> roles) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
