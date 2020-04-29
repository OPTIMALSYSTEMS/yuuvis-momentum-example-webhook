
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

package com.os.services.webhook.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsertDocumentService
{
    private Map<String, String> mime2repository = new HashMap<>();

    @PostConstruct
    public void init()
    {
        mime2repository.put("message/rfc822", "largeRepository");
        mime2repository.put("application/pdf", "fastRepository");

    }

    // @formatter:off
    /*
        JSON: { "objects" : 
                  [ 
                   { "properties" : {..},
                     "contentStreams" : [{ "mimeType": "application/pdf"}]
                   }, 
                   ... 
                  ]
              }
              
        POJO: Map dmsApiObjectList
                List apiObjectList = dmsApiObjectList("objects")
                  Map object = apiObjectList[n]
                    List contentStreams = object("contentStreams")
                      Map contentStream = contentStreams[0]
                        String mimetype = contentStream("mimeType")     
    */
    // @formatter: on

    public Map<String, Object> insertDocument(final Map<String, Object> dmsApiObjectList, HttpHeaders incomingHeaders) throws Throwable
    {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> apiObjectList = (List<Map<String, Object>>) dmsApiObjectList.get("objects");

        for (Map<String, Object> object : apiObjectList)
        {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> contentStreams = (List<Map<String, Object>>)object.get("contentStreams");

            if (contentStreams == null)
            {
                // seems to be an meta-data update or insert without content - just skip this object
                continue;
            }
            
            String mimetype = (String) contentStreams.get(0).get("mimeType");

            for (String mimeTypeKey : mime2repository.keySet())
            {
                if (mimeTypeKey.equalsIgnoreCase(mimetype))
                {
                    contentStreams.get(0).put("repositoryId", mime2repository.get(mimeTypeKey));
                    break;
                }
            }
        }
        
        return dmsApiObjectList;
    }
}
