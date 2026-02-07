package com.vaikrorag.vaikrorag.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vaikrorag.vaikrorag.helper.ChunkerHelper;
import com.vaikrorag.vaikrorag.model.Document;
import com.vaikrorag.vaikrorag.model.DocumentChunk;
import com.vaikrorag.vaikrorag.repository.DocumentRepo;

@Service
public class DocumentService {
    
    private final DocumentRepo documentRepo;
    private final QdrantService qdrantService;

    public DocumentService(DocumentRepo documentRepo, QdrantService qdrantService) {
        this.documentRepo = documentRepo;
        this.qdrantService = qdrantService;
    }

    // Upload Document -> Divide in Chunks -> Create embeddings -> Add embeddings to Qdrant
    public Document ingest(String title, String content, String sessionId) {
        Document doc = new Document(title, sessionId);
        String collection = sessionId;
        int chunkSize = 1000;
        int overlap = 100;
        List<String> chunks = ChunkerHelper.chunkText(content, chunkSize, overlap);
        if (chunks.isEmpty()) {
            throw new RuntimeException("No chunks created from document content");
        }
    
        System.out.println("Created " + chunks.size() + " chunks with size=" + chunkSize);
    
        // Log first chunk for debugging
        if (!chunks.isEmpty()) {
            System.out.println("First chunk preview: " + chunks.get(0).substring(0, Math.min(100, chunks.get(0).length())));
        }
        System.out.println("Chunks Created!");
        System.out.println("Created " + chunks.size() + " chunks:");
        List<DocumentChunk> documentChunks = new ArrayList<>();
        for(String chunk : chunks){
            documentChunks.add(new DocumentChunk(doc, chunk));
        }
        doc.setChunks(documentChunks);
        Document savedDoc = documentRepo.save(doc);
        System.out.println("Document Chunks Created!");

        qdrantService.createCollectionIfMissing(collection);
        System.out.println("Collection Created!");

        List<org.springframework.ai.document.Document> docs = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++){
            String chunk = chunks.get(i);
            if (chunk == null || chunk.trim().isEmpty()) {
                System.err.println("Skipping empty chunk at index " + i);
                continue;
            }
        
            String pointId = UUID.randomUUID().toString();
            
            Map<String, Object> metadata = Map.of(
                "documentId", savedDoc.getId(),
                "chunkIndex", i,
                "title", title,
                "sessionId", sessionId
            );

            String textWithMeta = "Title: " + title + "\nDocumentId: " + savedDoc.getId() + "\nChunkIndex: " + i + "\nSessionId: " + sessionId + "\n\n" + chunk;

            docs.add(org.springframework.ai.document.Document.builder()
            .id(pointId)
            .text(textWithMeta)
            .metadata(metadata)
            .build());
        }
        System.out.println("Point List Created!");
        try {
            qdrantService.upsertPoints(collection, docs);
        } catch (RuntimeException e) {
            // Add contextual logging to help debug malformed payloads
            System.err.println("Failed to upsert points to Qdrant for document id " + savedDoc.getId() + ", points: " + docs.size());
            throw e;
        }
        System.out.println("Vectors Added!");
        savedDoc.setIndexed(true);
        return documentRepo.save(savedDoc);
    }

    public void deleteDocument(String docId, String sessionId){
        long id = Long.parseLong(docId);
        if(qdrantService.deleteDocument(sessionId, id)) {
        Document deletedDoc = documentRepo.findById(id);
        documentRepo.delete(deletedDoc);
        }
    }

    public List<Document> getDocuments(String sessionId){
        return documentRepo.findAllBySessionId(sessionId);
    }
}
