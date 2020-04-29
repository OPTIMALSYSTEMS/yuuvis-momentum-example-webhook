
package com.os.services.webhook.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.os.services.webhook.service.InsertDocumentService;
import com.os.services.webhook.service.IsMetadataExistsService;

@RestController
@RequestMapping("/api")
public class MetadataDocumentImportWebhookRestController
{
    @Autowired
    private InsertDocumentService insertDocumentService;

    @Autowired
    private IsMetadataExistsService metadataExistsService;

    @PostMapping(value = "/insert/document", produces = "application/json;charset=UTF-8", consumes = "application/json")
    public ResponseEntity<?> documentInsert(@RequestBody final Map<String, Object> dmsApiObList, @RequestHeader HttpHeaders incomingHeaders) throws Exception
    {
        try
        {
            return new ResponseEntity<>(insertDocumentService.insertDocument(dmsApiObList, incomingHeaders), HttpStatus.OK);
        }
        catch (Throwable e)
        {

            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @PostMapping(value = "/exist/metadata", produces = "application/json;charset=UTF-8", consumes = "application/json")
    public ResponseEntity<?> isMetadataExists(@RequestBody final Map<String, Object> query, @RequestHeader HttpHeaders incomingHeaders) throws Throwable
    {
        try
        {
            return new ResponseEntity<>(metadataExistsService.isValueExists(query, incomingHeaders), HttpStatus.OK);
        }
        catch (Throwable e)
        {

            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

}
