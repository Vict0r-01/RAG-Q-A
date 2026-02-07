package com.vaikrorag.vaikrorag.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.vaikrorag.vaikrorag.model.Messages;
import com.vaikrorag.vaikrorag.service.RagService;

import reactor.core.publisher.Flux;

@RestController
public class RagController {
    
    private final RagService ragService;

    public RagController(RagService ragService){
        this.ragService = ragService;
    }

    @PostMapping(
    value = "/raganswer/{sessionId}",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.TEXT_EVENT_STREAM_VALUE
)
    public ResponseEntity<Flux<String>> getRagAnswer(@RequestBody List<Messages> messages, @PathVariable String sessionId){
        try{
        Flux<String> response = ragService.ragAnswer(messages, 7, sessionId);
        response.defaultIfEmpty("Could not process message, please insert a message.");
        return ResponseEntity.ok(response);
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("addMessage")
    public ResponseEntity<Void> addMessage(@RequestBody Messages message){
        try{
            ragService.addMessage(message);
            return ResponseEntity.ok().build();
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/getMessages/{sessionId}")
    public ResponseEntity<?> getMessages(@PathVariable String sessionId){
        try{
            List<Messages> messages = ragService.getMessages(sessionId);
            return ResponseEntity.ok(messages);
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body("Unable to retrieve messages: " + e.getMessage());
        }
        
    }
}
