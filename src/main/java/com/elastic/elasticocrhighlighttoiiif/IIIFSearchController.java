package com.elastic.elasticocrhighlighttoiiif;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@RestController
@RequestMapping("/iiif")
public class IIIFSearchController {

    @Autowired
    private ElasticService elasticService;

@GetMapping("/{source}/search")
public Map<String, Object> search(
        @PathVariable String source,
        @RequestParam String q
) throws Exception {
System.out.println("SEARCH ENDPOINT CALLED");
    List<Book> response = elasticService.search(q, source);

    Map<String, Object> result = new HashMap<>();

    result.put("@context", "http://iiif.io/api/search/1/context.json");
    result.put("@type", "sc:AnnotationList");
    result.put("@id", "http://localhost:8080/iiif/" + source + "/search?q=" + q);
    result.put("resources", new ArrayList<>());
    result.put("startIndex", 0);

    Map<String, Object> within = new HashMap<>();
    within.put("@type", "sc:Layer");
    within.put("total", 0);

    List<Map<String, Object>> resources = (List<Map<String, Object>>) result.get("resources");
    
    int total = 0;
    for (Book book : response) {

        Map<String, Object> annotation = new HashMap<>();
        annotation.put("@type", "oa:Annotation");
        annotation.put("on", book.getOn());

        Map<String, Object> resource = new HashMap<>();
        resource.put("@type", "dctypes:Text");
        resource.put("format", "text/html");
        resource.put("chars", book.getChars());

        annotation.put("resource", resource);

        resources.add(annotation);
        total++;
    }

    within.put("total", total);
    result.put("within", within);

    return result;
}
}