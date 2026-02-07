package com.vaikrorag.vaikrorag.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String text;
    
    @JsonProperty("isUser")
    private boolean isUser;

    private String sessionId;

    public Messages() {};

    public Messages(String text, boolean isUser, String sessionId){
        this.text = text;
        this.isUser = isUser;
        this.sessionId = sessionId;
    }
}
