/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.service;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class FileSenderService {
    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    public String sendBinary(String url, MultipartFile file, String basicAuthUser, String basicAuthPassword){
        RestTemplate restTemplate = restTemplateBuilder.basicAuthentication(basicAuthUser, basicAuthPassword).build();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(new ByteArrayInputStream(file.getBytes()), outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return restTemplate.postForObject(url, outputStream.toByteArray(), String.class);
    }
}
