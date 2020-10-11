/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.client;

import ch.fhnw.digibp.config.HeartConfig;
import ch.fhnw.digibp.service.TransferService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class HeartClient {

    @Autowired
    ExternalTaskClient client;

    @Autowired
    TransferService transferService;

    @Value("${camunda-rest.tenantid}")
    String camundaTenantId;

    @Autowired
    HeartRESTClient heartRESTClient;

    @Autowired
    HeartConfig heartConfig;

    @PostConstruct
    private void subscribeTopics() {
            client.subscribe("BloodPressureVerified")
                .tenantIdIn(camundaTenantId)
                .handler((ExternalTask externalTask, ExternalTaskService externalTaskService) -> {
                    try {
                        String category = externalTask.getVariable("category");
                        String businessKey = externalTask.getBusinessKey();
                        String streamIdUserId = (String) transferService.take(businessKey, "streamIdUserId");
                        String pryvTokenEndpointUser = heartRESTClient.getPryvTokenEndpointUser(streamIdUserId);
                        heartRESTClient.createAnalysis(pryvTokenEndpointUser, category);
                        externalTaskService.complete(externalTask);
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "failed");
                    }
                })
                .open();

            client.subscribe("NotNormalBloodPressureVerified")
                .tenantIdIn(camundaTenantId)
                .handler((ExternalTask externalTask, ExternalTaskService externalTaskService) -> {
                    try {
                        String category = externalTask.getVariable("category");
                        String businessKey = externalTask.getBusinessKey();
                        String streamIdUserId = (String) transferService.peek(businessKey, "streamIdUserId");
                        String pryvTokenEndpointUser = heartRESTClient.getPryvTokenEndpointUser(streamIdUserId);
                        heartRESTClient.createAnalysis(pryvTokenEndpointUser, category);
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("eventId", businessKey);
                        variables.put("sharingViewURL", heartConfig.getBaseURL()+"heart/sharing/"+businessKey+"/");
                        externalTaskService.complete(externalTask, variables);
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "failed");
                    }
                })
                .open();

                client.subscribe("EmergencyTreated")
                .tenantIdIn(camundaTenantId)
                .handler((ExternalTask externalTask, ExternalTaskService externalTaskService) -> {
                    try {
                        String diagnosis = externalTask.getVariable("diagnosis");
                        String businessKey = externalTask.getBusinessKey();
                        String streamIdUserId = (String) transferService.take(businessKey, "streamIdUserId");
                        String pryvTokenEndpointUser = heartRESTClient.getPryvTokenEndpointUser(streamIdUserId);
                        heartRESTClient.createDiagnosis(pryvTokenEndpointUser, diagnosis);
                        externalTaskService.complete(externalTask);
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "failed");
                    }
                })
                .open();

                client.subscribe("DiagnosisMade")
                .tenantIdIn(camundaTenantId)
                .handler((ExternalTask externalTask, ExternalTaskService externalTaskService) -> {
                    try {
                        String diagnosis = externalTask.getVariable("diagnosis");
                        String businessKey = externalTask.getBusinessKey();
                        String streamIdUserId = (String) transferService.take(businessKey, "streamIdUserId");
                        String pryvTokenEndpointUser = heartRESTClient.getPryvTokenEndpointUser(streamIdUserId);
                        heartRESTClient.createDiagnosis(pryvTokenEndpointUser, diagnosis);
                        externalTaskService.complete(externalTask);
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "failed");
                    }
                })
                .open();

                client.subscribe("RetrievePatientPersonalData")
                .tenantIdIn(camundaTenantId)
                .handler((ExternalTask externalTask, ExternalTaskService externalTaskService) -> {
                    try {
                        String businessKey = externalTask.getBusinessKey();
                        String streamIdUserId = (String) transferService.peek(businessKey, "streamIdUserId");
                        String pryvTokenEndpointUser = heartRESTClient.getPryvTokenEndpointUser(streamIdUserId);
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("patientEmail", Variables.stringValue(heartRESTClient.getPatientEmail(pryvTokenEndpointUser), true));
                        variables.put("baseURL", Variables.stringValue(heartConfig.getBaseURL(), false));
                        externalTaskService.complete(externalTask, variables);
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "failed");
                    }
                })
                .open();

    }
}