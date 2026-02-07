package com.vaikrorag.vaikrorag.DTO;

import com.vaikrorag.vaikrorag.model.Document;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentDTO {
    private long id;
    private String title;
    private String chunk;
    

    public static DocumentDTO fromDocument(Document document){
        return(new DocumentDTO(
            document.getId(),
            document.getTitle(),
            document.getChunks().get(0).getText())
        );
    }
}
