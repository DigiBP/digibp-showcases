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

    @Value("${camunda-rest.tenant-id}")
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
                        String vaultStreamIdTokenEndpointPatient = (String) transferService.take(businessKey, "vaultStreamIdTokenEndpointPatient");
                        String pryvTokenEndpointPatient = heartRESTClient.getpryvTokenEndpointPatient(vaultStreamIdTokenEndpointPatient);
                        heartRESTClient.createAnalysis(pryvTokenEndpointPatient, category);
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
                        String vaultStreamIdTokenEndpointPatient = (String) transferService.peek(businessKey, "vaultStreamIdTokenEndpointPatient");
                        String pryvTokenEndpointPatient = heartRESTClient.getpryvTokenEndpointPatient(vaultStreamIdTokenEndpointPatient);
                        heartRESTClient.createAnalysis(pryvTokenEndpointPatient, category);
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
                        String vaultStreamIdTokenEndpointPatient = (String) transferService.take(businessKey, "vaultStreamIdTokenEndpointPatient");
                        String pryvTokenEndpointPatient = heartRESTClient.getpryvTokenEndpointPatient(vaultStreamIdTokenEndpointPatient);
                        heartRESTClient.createDiagnosis(pryvTokenEndpointPatient, diagnosis);
                        externalTaskService.complete(externalTask);
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "failed");
                    }
                })
                .open();

                client.subscribe("ConsultationMade")
                .tenantIdIn(camundaTenantId)
                .handler((ExternalTask externalTask, ExternalTaskService externalTaskService) -> {
                    try {
                        String diagnosis = externalTask.getVariable("diagnosis");
                        String businessKey = externalTask.getBusinessKey();
                        String vaultStreamIdTokenEndpointPatient = (String) transferService.take(businessKey, "vaultStreamIdTokenEndpointPatient");
                        String pryvTokenEndpointPatient = heartRESTClient.getpryvTokenEndpointPatient(vaultStreamIdTokenEndpointPatient);
                        heartRESTClient.createDiagnosis(pryvTokenEndpointPatient, diagnosis);
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
                        String vaultStreamIdTokenEndpointPatient = (String) transferService.peek(businessKey, "vaultStreamIdTokenEndpointPatient");
                        String pryvTokenEndpointPatient = heartRESTClient.getpryvTokenEndpointPatient(vaultStreamIdTokenEndpointPatient);
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("patientEmail", Variables.stringValue(heartRESTClient.getPatientEmail(pryvTokenEndpointPatient), true));
                        variables.put("patientName", Variables.stringValue(heartRESTClient.getPatientName(pryvTokenEndpointPatient), true));
                        variables.put("baseURL", Variables.stringValue(heartConfig.getBaseURL(), false));
                        externalTaskService.complete(externalTask, variables);
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "failed");
                    }
                })
                .open();

    }
}