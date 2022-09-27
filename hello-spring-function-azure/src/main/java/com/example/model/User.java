package com.example.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class User {
    private UserProperties properties;
    private String id;
    private String name;
    private String type;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }
    public UserProperties getProperties() {
        return properties;
    }

    public void setProperties(UserProperties properties) {
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static String toJson(User user) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static User fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
