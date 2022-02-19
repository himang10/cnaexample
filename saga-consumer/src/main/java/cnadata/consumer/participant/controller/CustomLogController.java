/**
 * This example is a code explaining how to implement the Saga Architecture Pattern
 * using the CNA Data Platform. This example can be used by referring
 * to coaching or development using the CNA Data Platform.
 * (CNA: Cloud Native Application )
 *
 * @author Yong Woo Yi
 * @version 1.0
 * @since 2022
 */

package cnadata.consumer.participant.controller;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import cnadata.consumer.participant.repository.PurchaseOrderMongoRepository;
import lombok.extern.slf4j.Slf4j;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.function.Consumer;


@RestController
@Slf4j
@RequestMapping(path = "/api", produces = "application/json")
public class CustomLogController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PurchaseOrderMongoRepository poRepo;

    @Autowired
    private MongoClient mongoClient;

    @GetMapping("/redirect")
    public String redirect() {
        log.info("redirect");

        return "redirect:/json-log";
    }


    @GetMapping ("outbox_event_list")
    public String mongodbList() {
        log.info("mongodb called");

        try {
            MongoDatabase database = mongoClient.getDatabase("mydev-db");
            MongoCollection<Document> collection = database.getCollection("cdc-outboxevent");

            // Created with Studio 3T, the IDE for MongoDB - https://studio3t.com/

            Document query = new Document();

            Consumer<Document> processBlock = new Consumer<Document>() {
                @Override
                public void accept(Document document) {
                    System.out.println(document);
                }
            };

            //collection.find(query).forEach(processBlock);
            collection.find(query).forEach(doc -> System.out.println(doc));

        } catch (MongoException e) {
            // handle MongoDB exception
        }

        return "success";
    }

    @GetMapping ("outbox_event")
    public String mongodb(@RequestParam("id") String id) {
        log.info("mongodb called {}", id);

        Document doc = mongoTemplate.findById(id, Document.class);
        log.info("MongoDB Found OutboxEvent: " + doc);

        return doc.toString();
    }

}
