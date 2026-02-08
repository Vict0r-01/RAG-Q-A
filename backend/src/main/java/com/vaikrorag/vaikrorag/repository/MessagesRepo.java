package com.vaikrorag.vaikrorag.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vaikrorag.vaikrorag.model.Messages;

@Repository
public interface MessagesRepo extends JpaRepository<Messages, Long> {
    
    public List<Messages> findAllBySessionIdOrderByIdAsc(String sessionId);
}