package com.vaikrorag.vaikrorag.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.vaikrorag.vaikrorag.model.Messages;
import com.vaikrorag.vaikrorag.repository.MessagesRepo;

import reactor.core.publisher.Flux;

@Service
public class RagService {
    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private final ObjectMapper json = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    
    private final QdrantService qdrantService;
    private final MessagesRepo messagesRepo;
    private final OpenAiChatModel openAiChatModel;
    public RagService (QdrantService qdrantService, OpenAiChatModel openAiChatModel, MessagesRepo messagesRepo){
        this.qdrantService = qdrantService;
        this.messagesRepo = messagesRepo;
        this.openAiChatModel = openAiChatModel;
    }
    public Flux<String> ragAnswer(List<Messages> messages, int topK, String sessionId) throws Exception{

        final String requestId = UUID.randomUUID().toString();
        final Instant start = Instant.now();

        if(messages.get(messages.size()-1).getText().isBlank() || messages.get(messages.size()-1).getText().isEmpty())
            return Flux.empty();
        //Logging Initial Request
        try {
            Map<String,Object> startLog = Map.of(
                "event", "rag.request.start",
                "requestId", requestId,
                "sessionId", sessionId,
                "query", messages.get(messages.size()-1).getText(),
                "messageCount", messages.size()
            );
            log.info(json.writeValueAsString(startLog));
        } catch(Exception exc){
            log.error("Error logging starting request", exc);
        }

        // 1) Create Prompt Template

        PromptTemplate customPromptTemplate = PromptTemplate.builder()
    .template("""
            {query}

            Context information is below.

			---------------------
			{question_answer_context}
			---------------------

            Given the context information, answer the query.

            When you use information from the context, append a 'Sources' section listing the source metadata for each cited chunk in the format: [Title: <title>, ChunkIndex: <index>].

            Follow these rules:

            1. Use context to help you with the answer, if the context could not help you find the answer say that you were not able to find it and to clarify the question.
            """)
    .build();

        // 2) Build ChatClient

        ChatClient.Builder chatClient = ChatClient.builder(openAiChatModel);

        // 3) Create Query
        String systemMsg = "You are a helpful assistant. Answer using ONLY the information in the provided context. " +
    "If the 'Context' is not provided, ask to add it. If the answer is not found in the context, say that you were not able to find it and to clarify the question." +
    "Be concise.";
        String userQuery = messages.get(messages.size()-1).getText();

        //Get chat history
        List<Message> messageList = new ArrayList<>();
        for(int j = 0; j < messages.size()-1; j++){
            messageList.add((messages.get(j).isUser()) ? new UserMessage(messages.get(j).getText()) : new AssistantMessage(messages.get(j).getText()));
        }
        
        // 4) Create Response

        try {
            log.info("requestId={} calling chat model", requestId);
            Flux<String> response = chatClient.build()
            .prompt()
            .advisors(qdrantService.getAdvisor(sessionId, topK, customPromptTemplate))
            .messages(messageList)
            .system(systemMsg)
            .user(userQuery)
            .stream()
            .content()
            .transform(flux -> toChunk(flux, 50));
            
            // structured log for response
            Map<String,Object> endLog = Map.of(
                "event", "rag.request.finish",
                "requestId", requestId,
                "sessionId", sessionId,
                "query", userQuery,
                "messages", messageList,
                "latencyMs", Duration.between(start, Instant.now()).toMillis()
            );
            log.info(json.writeValueAsString(endLog));

            // Return answer + sources for UI
            return response;
        } catch (Exception e) {
            Map<String,Object> errLog = Map.of(
                "event", "rag.request.error",
                "requestId", requestId,
                "sessionId", sessionId,
                "error", e.toString(),
                "latencyMs", Duration.between(start, Instant.now()).toMillis()
            );
            try { log.error(json.writeValueAsString(errLog)); } catch (Exception ex) { log.error("Error logging rag error", ex); }
            throw e;
        }
    }   

    private Flux<String> toChunk(Flux<String> tokenFlux, int chunkSize) {
        return Flux.create(sink -> {
            StringBuilder buffer = new StringBuilder();
            tokenFlux.subscribe(
              token -> {
                  buffer.append(token);
                  if (buffer.length() >= chunkSize) {
                      sink.next(buffer.toString());
                      buffer.setLength(0);
                  }
              },
              sink::error,
              () -> {
                  if (buffer.length() > 0) {
                      sink.next(buffer.toString());
                  }
                  sink.complete();
              }
            );
        });
    }

    public List<Messages> getMessages(String sessionId){
        return messagesRepo.getAllMessagesBySessionId(sessionId);
    }

    public void addMessage(Messages message){
        System.out.println("Saved message: " + message.getText());
        messagesRepo.save(message);
    }
}
