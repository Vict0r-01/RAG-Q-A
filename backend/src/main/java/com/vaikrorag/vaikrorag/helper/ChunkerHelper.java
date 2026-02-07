package com.vaikrorag.vaikrorag.helper;

import java.util.ArrayList;
import java.util.List;

public class ChunkerHelper {
    
    /**
     * Chunks text with overlap for better context preservation
     * @param text The text to chunk
     * @param maxChunkSize Maximum size of each chunk in characters
     * @param overlap Number of characters to overlap between chunks
     * @return List of text chunks
     */
    public static List<String> chunkText(String text, int maxChunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        
        // Clean up the text first
        text = text.replaceAll("\\s+", " ").trim();
        
        // Split on sentence boundaries, keeping the delimiter
        String[] sentences = text.split("(?<=[.!?])\\s+");
        
        StringBuilder currentChunk = new StringBuilder();
        List<String> currentSentences = new ArrayList<>();
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;
            
            // Handle oversized single sentences
            if (sentence.length() > maxChunkSize) {
                // Save current chunk if it has content
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentSentences.clear();
                }
                
                // Split the long sentence by words or characters
                chunks.addAll(splitLongSentence(sentence, maxChunkSize, overlap));
                continue;
            }
            
            int potentialLength = currentChunk.length() + sentence.length() + 1;
            
            if (potentialLength > maxChunkSize && currentChunk.length() > 0) {
                // Save current chunk
                chunks.add(currentChunk.toString().trim());
                
                // Create overlap by keeping last few sentences
                currentChunk = new StringBuilder();
                currentSentences = createOverlap(currentSentences, overlap);
                
                // Rebuild chunk from overlap sentences
                for (String s : currentSentences) {
                    if (currentChunk.length() > 0) {
                        currentChunk.append(" ");
                    }
                    currentChunk.append(s);
                }
            }
            
            // Add sentence to current chunk
            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(sentence);
            currentSentences.add(sentence);
        }
        
        // Add final chunk if it has content
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        // Handle edge case where no sentences were found
        if (chunks.isEmpty() && !text.isEmpty()) {
            chunks = fallbackChunking(text, maxChunkSize, overlap);
        }
        
        return chunks;
    }
    
    /**
     * Creates overlap by keeping sentences that fit within overlap size
     */
    private static List<String> createOverlap(List<String> sentences, int overlapSize) {
        List<String> overlap = new ArrayList<>();
        int currentSize = 0;
        
        // Work backwards from the end
        for (int i = sentences.size() - 1; i >= 0; i--) {
            String sentence = sentences.get(i);
            if (currentSize + sentence.length() <= overlapSize) {
                overlap.add(0, sentence); // Add to front
                currentSize += sentence.length() + 1; // +1 for space
            } else {
                break;
            }
        }
        
        return overlap;
    }
    
    /**
     * Splits a single long sentence that exceeds maxChunkSize
     */
    private static List<String> splitLongSentence(String sentence, int maxChunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        String[] words = sentence.split("\\s+");
        StringBuilder currentChunk = new StringBuilder();
        
        for (String word : words) {
            if (currentChunk.length() + word.length() + 1 > maxChunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                
                // Create overlap by keeping last few words
                String[] chunkWords = currentChunk.toString().split("\\s+");
                currentChunk = new StringBuilder();
                int overlapWords = 0;
                int overlapLength = 0;
                
                for (int i = chunkWords.length - 1; i >= 0 && overlapLength < overlap; i--) {
                    overlapLength += chunkWords[i].length() + 1;
                    overlapWords++;
                }
                
                for (int i = Math.max(0, chunkWords.length - overlapWords); i < chunkWords.length; i++) {
                    if (currentChunk.length() > 0) currentChunk.append(" ");
                    currentChunk.append(chunkWords[i]);
                }
            }
            
            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(word);
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }
    
    /**
     * Fallback character-based chunking with overlap
     */
    private static List<String> fallbackChunking(String text, int maxChunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        
        while (start < text.length()) {
            int end = Math.min(start + maxChunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += maxChunkSize - overlap;
            
            if (start >= text.length()) break;
        }
        
        return chunks;
    }
}
