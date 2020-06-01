/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.api;

import ch.fhnw.digibp.service.TransferService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping(path = "/api/food/snap/v1")
public class FoodSnapEndpoint {

    @Autowired
    TransferService transferService;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    @Value("${camunda-rest.tenantid}")
    String camundaTenantId;

    @Value("${camunda-rest.url}")
    String camundaRestUrl;

    @PostMapping(path = "/order", consumes = "multipart/form-data",  produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public FoodSnapResponse postSnap(@ModelAttribute FoodSnapRequest foodSnapRequest) throws InterruptedException {
        String processInstanceId = null;
        RestTemplate restTemplate = restTemplateBuilder.build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map<String, Object> map = new HashMap<>();
        map.put("messageName", "food-shot-service-ai_order-received");
        map.put("businessKey", UUID.randomUUID().toString());
        map.put("tenantId", camundaTenantId);
        map.put("resultEnabled", true);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(camundaRestUrl + "/message", entity, JsonNode.class);
        if(response.getStatusCode() == HttpStatus.OK){
            processInstanceId = Objects.requireNonNull(response.getBody()).get(0).path("processInstance").path("id").asText();
        }
        if(processInstanceId!=null) {
            transferService.put(processInstanceId, "ClassifyFood", foodSnapRequest);
            String result = (String) transferService.poll(processInstanceId, "FoodSnapResponse", 30);
            if (result != null) {
                return new FoodSnapResponse(result);
            }
            return new FoodSnapResponse("Sorry it took longer than expected to classify your food.");
        }
        return new FoodSnapResponse("I was not able to start the process.");
    }

    public static class FoodSnapRequest {
        private String customerName;
        private String customerAddress;
        private String customerEmail;
        private MultipartFile imageFile;

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerAddress() {
            return customerAddress;
        }

        public void setCustomerAddress(String customerAddress) {
            this.customerAddress = customerAddress;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public MultipartFile getImageFile() {
            return imageFile;
        }

        public void setImageFile(MultipartFile imageFile) {
            this.imageFile = imageFile;
        }
    }

    public static class FoodSnapResponse {
        private String message;

        FoodSnapResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}

