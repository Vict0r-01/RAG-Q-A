package com.vaikrorag.vaikrorag.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vaikrorag.vaikrorag.model.Document;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long> {

    public Document findById(long id);
    public List<Document> findAllBySessionId(String sessionId);
}
