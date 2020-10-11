/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.service;

import ch.fhnw.digibp.client.HeartRESTClient;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HeartService {

    @Autowired
    HeartRESTClient heartRESTClient;

    @Autowired
    TransferService transferService;

    static Map<String, String> lastModifiedEvent = new HashMap<>();

    public void processBloodPressureEvent(String vaultStreamIdTokenEndpointPatient) throws InterruptedException {
        String modifiedSince = lastModifiedEvent.get(vaultStreamIdTokenEndpointPatient);
        if(modifiedSince==null)
            modifiedSince = "";

        JSONObject jsonObject = heartRESTClient.getBloodPressureEvent(heartRESTClient.getpryvTokenEndpointPatient(vaultStreamIdTokenEndpointPatient), modifiedSince);

        if(!jsonObject.getJSONArray("events").isEmpty()) {
            Integer systolic = jsonObject.getJSONArray("events").getJSONObject(0).getJSONObject("content").getInt("systolic");
            Integer diastolic = jsonObject.getJSONArray("events").getJSONObject(0).getJSONObject("content").getInt("diastolic");
            lastModifiedEvent.put(vaultStreamIdTokenEndpointPatient,jsonObject.getJSONArray("events").getJSONObject(0).getString("modified"));
            String eventId = jsonObject.getJSONArray("events").getJSONObject(0).getString("id");
            transferService.put(eventId, "vaultStreamIdTokenEndpointPatient", vaultStreamIdTokenEndpointPatient);
            heartRESTClient.initHeartService(eventId, systolic, diastolic);
        }
    }
}
