/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.api;

import ch.fhnw.digibp.client.HeartRESTClient;
import ch.fhnw.digibp.config.HeartConfig;
import ch.fhnw.digibp.service.HeartService;
import ch.fhnw.digibp.service.TransferService;
import ch.fhnw.digibp.util.PryvUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/heart/v1")
public class HeartEndpoint {

    @Autowired
    TransferService transferService;

    @Value("${camunda-rest.tenant-id}")
    String camundaTenantId;

    @Value("${camunda-rest.url}")
    String camundaRestUrl;

    @Value("${pryv.token-endpoint-vault}")
    String pryvTokenEndpointVault;

    @Autowired
    HeartRESTClient heartRESTClient;

    @Autowired
    HeartService heartService;

    @Autowired
    HeartConfig heartConfig;

    @PostMapping(path = "/onboarding", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Boolean postOnboarding(HttpServletRequest request, @RequestBody OnboardingRequest onboardingRequest) {
        heartRESTClient.registerUserInVault(onboardingRequest.pryvTokenEndpoint);
        String webhookUrl = heartConfig.getBaseURL(request) + "/api/heart/v1/webhook/" + PryvUtil.getUserId(onboardingRequest.pryvTokenEndpoint);
        heartRESTClient.registerWebhook(onboardingRequest.pryvTokenEndpoint, webhookUrl);
        return Boolean.TRUE;
    }

    @PostMapping(path = "/webhook/{vaultStreamIdTokenEndpointPatient}", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public PryvWebhookRequest postPryvWebhook(HttpServletRequest httpServletRequest, @RequestBody PryvWebhookRequest request, @PathVariable("vaultStreamIdTokenEndpointPatient") String vaultStreamIdTokenEndpointPatient) throws InterruptedException {
        heartConfig.setBaseURL(httpServletRequest);
        heartService.processBloodPressureEvent(vaultStreamIdTokenEndpointPatient);
        return request;
    }

    @PostMapping(path = "/email", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void postEmail(@RequestBody EmailRequest request) throws InterruptedException {
        String email = request.email;
        String name = request.name;
        String out = name + ": " +email; //todo
    }

    public static class OnboardingRequest {
        public String pryvTokenEndpoint;
    }

    public static class EmailRequest {
        public String email;
        public String name;
    }

    public static class PryvWebhookRequest {
        public List<String> messages = new ArrayList<>();
        public Meta meta = new Meta();

        public static class Meta {
            public String apiVersion;
            public String serial;
            public Float serverTime;
        }
    }

}
