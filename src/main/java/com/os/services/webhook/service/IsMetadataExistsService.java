
package com.os.services.webhook.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.os.services.webhook.config.WebhookException;

@Service
public class IsMetadataExistsService
{
    private RestTemplate restTemplateStatic = new RestTemplate();

    // the absolute url to the yuuvis - api gateway
    private final String YUUVIS_API_GATEWAY = "http://<<api-gateway>>";

    private final String SEARCH_ENDPOINT = "/api/dms/objects/search";

    private final String OBJECT_TYPE = "object";
    private final String UNIQUE_PROPERTY_KEY = "idn";

    @SuppressWarnings("unchecked")
    public Map<String, Object> isValueExists(final Map<String, Object> dmsApiObjectList, HttpHeaders incomingHeaders) throws Throwable
    {
        List<Map<String, Object>> objectsList = (List<Map<String, Object>>)dmsApiObjectList.get("objects");
        for (Map<String, Object> object : objectsList)
        {
            Map<String, Object> properties = (Map<String, Object>)object.get("properties");
            if (properties == null)
            {
                throw new WebhookException("Wrong Object format!");
            }

            // check, if the object-type is regarded otherwise skip
            String typeIdValue = this.getValue("system:objectTypeId", properties);
            if (!typeIdValue.equalsIgnoreCase(OBJECT_TYPE))
            {
                // go to next object in batch
                continue;
            }

            String UNIQUE_PROPERTY_KEY_VALUE = this.getValue(UNIQUE_PROPERTY_KEY, properties);

            // create search statement
            String statement = String.format("SELECT COUNT(*) FROM %s WHERE %s = '%s'", OBJECT_TYPE, UNIQUE_PROPERTY_KEY, UNIQUE_PROPERTY_KEY_VALUE);

            // in update mode --> don't search for the document itself
            String currentObjectId = this.checkIfUpdateAndGetObjectId(object);
            statement += currentObjectId != null ? String.format(" AND system:objectId <> '%s'", currentObjectId) : "";

            Map<String, Object> searchResult = this.executeSearchQuery(statement, incomingHeaders.getFirst(HttpHeaders.AUTHORIZATION));

            Integer totalNumItems = (Integer)searchResult.get("totalNumItems");

            if (totalNumItems > 0)
            {
                throw new WebhookException("An object with " + UNIQUE_PROPERTY_KEY + " = '" + UNIQUE_PROPERTY_KEY_VALUE + "' already exists.");
            }
        }

        return dmsApiObjectList;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> executeSearchQuery(String statement, String authorization) throws WebhookException
    {
        try
        {
            // @formatter:off

            /* create search-query json
             * 
             *         {
             *           "query" : 
             *             {
             *               "statement" : "SELECT COUNT(*) FROM ........"
             *             }
             *         }
             */

            // @formatter:on

            Map<String, Object> query = new HashMap<>();

            Map<String, Object> searchStatement = new HashMap<>();
            query.put("query", searchStatement);

            searchStatement.put("statement", statement);

            Map<String, Object> searchQuery = new HashMap<>();
            searchQuery.putAll(searchStatement);
            String searchUrl = YUUVIS_API_GATEWAY.concat(SEARCH_ENDPOINT);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.AUTHORIZATION, authorization);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(query, httpHeaders);
            return this.restTemplateStatic.postForObject(searchUrl, requestEntity, Map.class);
        }
        catch (Throwable e)
        {
            String message = e.getMessage();
            if (e instanceof HttpClientErrorException)
            {
                HttpClientErrorException hcee = (HttpClientErrorException)e;
                message = hcee.getStatusCode() + ": " + hcee.getStatusText() + " - " + hcee.getResponseBodyAsString();
            }

            throw new WebhookException(message);
        }
    }

    private String checkIfUpdateAndGetObjectId(Map<String, Object> object)
    {
        try
        {
            // in update mode the currentVersion of the document is under 'options'
            // options={currentVersion={properties={system:objectId={value="ddeeffaabb......"}
            return (String)getValue("system:objectId", getMap("properties", getMap("currentVersion", getMap("options", object))));
        }
        catch (Throwable t)
        {
            // ignore
        }
        // this is not an update
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMap(String key, Map<String, Object> properties)
    {
        return (T)properties.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, Map<String, Object> properties)
    {
        Map<String, Object> typeId = (Map<String, Object>)properties.get(key);
        return (T) typeId.get("value");
    }

}
