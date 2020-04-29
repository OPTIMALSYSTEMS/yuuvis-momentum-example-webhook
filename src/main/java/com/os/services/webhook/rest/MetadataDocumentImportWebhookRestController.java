
/*
 * Copyright (c) 2020 OPTIMAL SYSTEMS GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.os.services.webhook.rest;

import com.os.services.webhook.service.InsertDocumentService;
import com.os.services.webhook.service.IsMetadataExistsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
