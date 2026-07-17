package com.elastic.elasticocrhighlighttoiiif;



import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

@Service
public class ElasticService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    
    public ElasticService(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl("https://elastic.libis.be") //prod
                .build();
        this.objectMapper = objectMapper;
    }

    
    public List<Book> search(String word, String source) throws Exception {

            String requestBody = """
            {
              "size": 1000,
              "query": {
                "bool": {
                  "must": [
                    {
                      "match": {
                        "chars": {
                          "query": "%s",
                          "operator": "or"
                        }
                      }
                    },
                    { 
                      "prefix": { 
                        "on.keyword": "https://lib.is/%s" 
                      } 
                    }                                 
                  ]
                }
              }
            }
            """.formatted(word, source);
            
    Authenticate authenticate = new Authenticate();  
    String auth = Base64.getEncoder().encodeToString((authenticate.getUsername() + ":" + authenticate.getPassword()).getBytes());
      
    String response = restClient.post()
            .uri("/annotations/_search") // prod
            .header("Authorization", "Basic " + auth)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(String.class);


    JsonNode root = objectMapper.readTree(response);

    List<Book> result = new ArrayList<>();

    for (JsonNode hit : root.path("hits").path("hits")) {
        result.add(
            objectMapper.treeToValue(
                hit.path("_source"),
                Book.class
            )
        );
    }

    return result;
}
    
    
}