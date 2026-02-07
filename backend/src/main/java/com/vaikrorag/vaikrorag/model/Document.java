package com.vaikrorag.vaikrorag.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "documents")
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String title;
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DocumentChunk> chunks;
    private LocalDateTime createdAt;
    private boolean Indexed;
    private String sessionId;
    public Document() {
        this.title = "Default";
        this.createdAt = LocalDateTime.now();
    }

    public Document(String title, List<DocumentChunk> chunks, String sessionId) {
        this.title = title;
        this.chunks = chunks;
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
    }

    public Document(String title, String sessionId){
        this.title = title;
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
    }
    public Document(List<DocumentChunk> chunks) {
        this.title = "Default";
        this.chunks = chunks;
        this.createdAt = LocalDateTime.now();
    }
}
