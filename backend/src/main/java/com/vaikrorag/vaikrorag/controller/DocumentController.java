package com.vaikrorag.vaikrorag.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vaikrorag.vaikrorag.DTO.DocumentDTO;
import com.vaikrorag.vaikrorag.model.Document;
import com.vaikrorag.vaikrorag.service.DocumentService;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    
    private final DocumentService documentService;
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload/{sessionId}")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file, @PathVariable String sessionId) {
        try (PDDocument pdf = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdf);

            // Save document + chunks
            Document saved = documentService.ingest(file.getOriginalFilename(), text, sessionId);

            return ResponseEntity.ok(DocumentDTO.fromDocument(saved));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error parsing PDF: " + e.getMessage());
        }
    }

    @GetMapping("/retrieve/{sessionId}")
    public ResponseEntity<?> getDocuments(@PathVariable String sessionId){
        try{
            List<Document> docs = documentService.getDocuments(sessionId);
            List<DocumentDTO> docsDTO = new ArrayList<>();
            for(Document doc : docs){
                docsDTO.add(DocumentDTO.fromDocument(doc));
            }
            return ResponseEntity.ok(docsDTO);
        }catch(Exception e){
        return ResponseEntity.status(500).body("Error retrieving the documents: " + e.getMessage());
        }
    }
    @PostMapping("/delete/{sessionId}")
    public ResponseEntity<?> removeDocument(@RequestBody DocId docId, @PathVariable String sessionId){
        try{
            documentService.deleteDocument(docId.docId(), sessionId);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.status(500).body("Error deleting document: " + e.getMessage());
        }
    }
    static record DocId(String docId){};
}
