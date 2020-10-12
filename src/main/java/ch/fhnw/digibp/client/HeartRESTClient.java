/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.client;

import ch.fhnw.digibp.util.PryvUtil;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class HeartRESTClient {
    @Value("${pryv.token-endpoint-vault}")
    String pryvTokenEndpointVault;

    @Value("${camunda-rest.tenant-id}")
    String camundaTenantId;

    @Value("${camunda-rest.url}")
    String camundaRestUrl;

    public String getpryvTokenEndpointPatient(String vaultStreamIdTokenEndpointPatient){
        return Unirest.get(PryvUtil.getEndpoint(pryvTokenEndpointVault) + "events?streams="+vaultStreamIdTokenEndpointPatient+"&limit=1")
                .header("Authorization", PryvUtil.getToken(pryvTokenEndpointVault))
                .asJson()
                .getBody()
                .getObject().getJSONArray("events").getJSONObject(0).getString("content");
    }

    public void registerUserInVault(String pryvTokenEndpoint){
        String jsonBody = "[\n" +
                "    {\n" +
                "        \"method\": \"streams.create\",\n" +
                "        \"params\": {\n" +
                "            \"id\": \"heart-access-token\",\n" +
                "            \"name\": \"Heart Access Token\"\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"method\": \"streams.create\",\n" +
                "        \"params\": {\n" +
                "            \"id\": \""+PryvUtil.getUserId(pryvTokenEndpoint)+"\",\n" +
                "            \"name\": \""+PryvUtil.getUserName(pryvTokenEndpoint)+"\",\n" +
                "            \"parentId\": \"heart-access-token\"\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"method\": \"events.create\",\n" +
                "        \"params\": {\n" +
                "            \"streamIds\": [\n" +
                "                \""+PryvUtil.getUserId(pryvTokenEndpoint)+"\"\n" +
                "            ],\n" +
                "            \"type\": \"credentials/pryv-api-endpoint\",\n" +
                "            \"content\": \""+pryvTokenEndpoint+"\"\n" +
                "        }\n" +
                "    }\n" +
                "]";

        Unirest.post(PryvUtil.getEndpoint(pryvTokenEndpointVault))
                .header("Authorization", PryvUtil.getToken(pryvTokenEndpointVault))
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .asString();
    }

    public void registerWebhook(String pryvTokenEndpoint, String webhookUrl){
        String jsonBody = "{\n" +
                "    \"url\": \""+webhookUrl+"\"\n" +
                "}";

        Unirest.post(PryvUtil.getEndpoint(pryvTokenEndpoint) + "webhooks")
                .header("Authorization", PryvUtil.getToken(pryvTokenEndpoint))
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .asString();
    }

    public void createAnalysis(String pryvTokenEndpoint, String category){
        String jsonBody = "{\n" +
                "    \"streamIds\": [\n" +
                "        \"analysis\"\n" +
                "    ],\n" +
                "    \"type\": \"note/txt\",\n" +
                "    \"content\": \"Analysis of the blood pressure category based on the last entry: "+category+"\"\n" +
                "}";

        Unirest.post(PryvUtil.getEndpoint(pryvTokenEndpoint) + "events")
                .header("Authorization", PryvUtil.getToken(pryvTokenEndpoint))
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .asString();
    }

    public JSONObject getBloodPressureEvent(String pryvTokenEndpointPatient, String modifiedSince){
        return Unirest.get(PryvUtil.getEndpoint(pryvTokenEndpointPatient) + "events?streams=blood-pressure&limit=1&modifiedSince="+modifiedSince+"")
                .header("Authorization", PryvUtil.getToken(pryvTokenEndpointPatient))
                .asJson()
                .getBody()
                .getObject();
    }

    public void initHeartService(String eventId, Integer systolic, Integer diastolic){
        Unirest.post(camundaRestUrl+"/message")
                .header("Content-Type", "application/json")
                .body("{\n" +
                        "    \"messageName\": \"Message_VerifyBloodPressure\",\n" +
                        "    \"businessKey\": \""+eventId+"\",\n" +
                        "    \"tenantId\": \""+camundaTenantId+"\",\n" +
                        "    \"resultEnabled\": false,\n" +
                        "    \"processVariables\": {\n" +
                        "        \"systolic\": {\n" +
                        "            \"value\": "+systolic+",\n" +
                        "            \"type\": \"integer\"\n" +
                        "        },\n" +
                        "        \"diastolic\": {\n" +
                        "            \"value\": "+diastolic+",\n" +
                        "            \"type\": \"integer\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
                .asString();
    }

    public void createDiagnosis(String pryvTokenEndpoint, String diagnosis){
        String jsonBody = "{\n" +
                "    \"streamIds\": [\n" +
                "        \"diagnosis\"\n" +
                "    ],\n" +
                "    \"type\": \"note/txt\",\n" +
                "    \"content\": \""+diagnosis+"\"\n" +
                "}";

        Unirest.post(PryvUtil.getEndpoint(pryvTokenEndpoint) + "events")
                .header("Authorization", PryvUtil.getToken(pryvTokenEndpoint))
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .asString();
    }

    public String getPatientEmail(String pryvTokenEndpointPatient){
        String email = "";
        JSONObject jsonObject = Unirest.get(PryvUtil.getEndpoint(pryvTokenEndpointPatient) + "events?streams=contact-email&limit=1")
                .header("Authorization", PryvUtil.getToken(pryvTokenEndpointPatient))
                .asJson()
                .getBody()
                .getObject();

        if(!jsonObject.getJSONArray("events").isEmpty()) {
            email = jsonObject.getJSONArray("events").getJSONObject(0).getString("content");
        }
        return email;
    }

    public String getPatientName(String pryvTokenEndpointPatient){
        String email = "";
        JSONObject jsonObject = Unirest.get(PryvUtil.getEndpoint(pryvTokenEndpointPatient) + "events?streams=contact-name&limit=1")
                .header("Authorization", PryvUtil.getToken(pryvTokenEndpointPatient))
                .asJson()
                .getBody()
                .getObject();

        if(!jsonObject.getJSONArray("events").isEmpty()) {
            email = jsonObject.getJSONArray("events").getJSONObject(0).getString("content");
        }
        return email;
    }

    public String createExpertSharing(String pryvTokenEndpointPatient){
        String jsonBody = "{\n" +
                "    \"name\": \"Expert sharing "+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())+"\",\n" +
                "    \"expireAfter\": 86400,\n" +
                "    \"permissions\": [\n" +
                "        {\n" +
                "            \"streamId\": \"contact\",\n" +
                "            \"level\": \"read\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"streamId\": \"blood-pressure\",\n" +
                "            \"level\": \"read\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"streamId\": \"analysis\",\n" +
                "            \"level\": \"read\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"streamId\": \"diagnosis\",\n" +
                "            \"level\": \"read\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        JSONObject jsonObject = Unirest.post(PryvUtil.getEndpoint(pryvTokenEndpointPatient) + "accesses")
                .header("Authorization", PryvUtil.getToken(pryvTokenEndpointPatient))
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .asJson()
                .getBody()
                .getObject();

        String pryvTokenSharing = "";

        if(!jsonObject.getJSONArray("access").isEmpty()) {
            pryvTokenSharing = jsonObject.getJSONArray("access").getJSONObject(0).getString("token");
        }
        return PryvUtil.getTokenEndpoint(pryvTokenSharing, PryvUtil.getEndpoint(pryvTokenEndpointPatient));
    }
}
