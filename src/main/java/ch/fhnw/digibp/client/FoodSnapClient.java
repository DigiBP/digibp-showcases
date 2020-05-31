/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.client;

import ch.fhnw.digibp.api.FoodSnapEndpoint;
import ch.fhnw.digibp.service.FileSenderService;
import ch.fhnw.digibp.service.QueuesService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class FoodSnapClient {

    @Autowired
    ExternalTaskClient client;

    @Autowired
    QueuesService queuesService;

    @Autowired
    FileSenderService fileSenderService;

    @Value("${camunda-rest.tenantid}")
    String camundaTenantId;

    @Value("${service.ibm-watson-visual-recognition.URL}")
    String ibmURL;

    @Value("${service.ibm-watson-visual-recognition.APIKEY}")
    String ibmAPIKEY;

    @PostConstruct
    private void subscribeTopics() {
            client.subscribe("ClassifyFood")
                .tenantIdIn(camundaTenantId)
                .handler((ExternalTask externalTask, ExternalTaskService externalTaskService) -> {
                    try {
                        String processInstanceId = externalTask.getProcessInstanceId();
                        FoodSnapEndpoint.FoodSnapRequest foodSnapRequest = (FoodSnapEndpoint.FoodSnapRequest) queuesService.take(processInstanceId, "ClassifyFood");
                        queuesService.put(processInstanceId, "FoodClassified", foodSnapRequest);
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("classificationResponse", fileSenderService.sendBinary(ibmURL, foodSnapRequest.getImageFile(), "apikey", ibmAPIKEY));

                        externalTaskService.complete(externalTask, variables);
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "failed");
                    }
                })
                .open();

        client.subscribe("FoodClassified")
                .tenantIdIn(camundaTenantId)
                .handler((ExternalTask externalTask, ExternalTaskService externalTaskService) -> {
                    try {
                        String processInstanceId = externalTask.getProcessInstanceId();
                        FoodSnapEndpoint.FoodSnapRequest foodSnapRequest = (FoodSnapEndpoint.FoodSnapRequest) queuesService.take(processInstanceId, "FoodClassified");

                        if(foodSnapRequest != null) {
                            Map<String, Object> variables = new HashMap<>();
                            variables.put("customerName", foodSnapRequest.getCustomerName());
                            variables.put("customerAddress", foodSnapRequest.getCustomerAddress());
                            variables.put("customerEmail", foodSnapRequest.getCustomerEmail());
                            variables.put("imageFile", foodSnapRequest.getImageFile().getBytes());

                            String foodClasses = externalTask.getVariable("foodClasses");
                            String foodType = externalTask.getVariable("foodType");

                            if (foodType == null) {
                                queuesService.put(processInstanceId, "FoodSnapResponse", "Your food will be manually classified. Watson classified it as: " + foodClasses);
                            } else {
                                queuesService.put(processInstanceId, "FoodSnapResponse", "Your food has been classified as: " + foodType + ". Watson classified it as: " + foodClasses);
                            }
                            externalTaskService.complete(externalTask, variables);
                        } else {
                            externalTaskService.complete(externalTask);
                        }
                    } catch (Exception e) {
                        externalTaskService.handleBpmnError(externalTask, "failed");
                    }
                })
                .open();
    }
}