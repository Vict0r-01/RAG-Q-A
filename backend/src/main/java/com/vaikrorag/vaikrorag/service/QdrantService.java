package com.vaikrorag.vaikrorag.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;

@Service
public class QdrantService {

    private final QdrantClient qdrantClient;
    private final Map<String, QdrantVectorStore> vectorStores = new ConcurrentHashMap<>();
    private final EmbeddingModel embeddingModel;

    public QdrantService(QdrantClient qdrantClient, EmbeddingModel embeddingModel){
        this.qdrantClient = qdrantClient;
        this.embeddingModel = embeddingModel;
    }
    //Always try to create collection, ignore error if it already exist
    public void createCollectionIfMissing(String collection) {
        long vectorSize = embeddingModel.dimensions();
        Distance distanceMetric = Distance.Cosine;
        try{
            qdrantClient.createCollectionAsync(
                collection,
                VectorParams.newBuilder().setDistance(distanceMetric).setSize(vectorSize).build()
            ).get();
            System.out.println("Collection created: " + collection);
        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                System.out.println("Collection already exists: " + collection);
            } else {
                System.err.println("Error creating collection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public QdrantVectorStore getVectorStore(String collectionName) {
        return vectorStores.computeIfAbsent(collectionName, name -> QdrantVectorStore.builder(qdrantClient, embeddingModel)
            .collectionName(name)
            .initializeSchema(true)
            .build());
    }
    //Add Vectors for the document
    public void upsertPoints(String collection, List<Document> docs) {
        QdrantVectorStore vectorStore = getVectorStore(collection);

        try{
            vectorStore.add(docs);
            System.out.println("Successfully added " + docs.size() + " documents to collection: " + collection);
        }catch (Exception e) {
            System.err.println("Failed to add documents to Qdrant: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean deleteDocument(String collection, long docId){
        try{
            QdrantVectorStore vectorStore = getVectorStore(collection);
            System.out.println("Deleting document with ID: " + docId);
            Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("documentId"),
                new Filter.Value(String.valueOf(docId))
            );
            vectorStore.delete(filterExpression);
            System.out.println("Qdrant delete successful: " + docId);
            return true;
        }catch (Exception e){
            System.err.println("Exception while deleting points: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    //Search nearest neightboors in the collection
    public QuestionAnswerAdvisor getAdvisor(String collection, int topK, PromptTemplate promptTemplate) {
        System.out.println("CREATING ADVISOR FOR: " + collection);
        try{
            QdrantVectorStore vectorStore = getVectorStore(collection);
            return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                .similarityThreshold(0.3d)
                .topK(topK).build())
                .promptTemplate(promptTemplate)
            .build();
        }catch (Exception e) {
            System.err.println("Error creating Advisor: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


}
