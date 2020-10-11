/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.controller;

import ch.fhnw.digibp.client.HeartRESTClient;
import ch.fhnw.digibp.config.HeartConfig;
import ch.fhnw.digibp.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/heart")
public class HeartController {

    @Autowired
    TransferService transferService;

    @Autowired
    HeartRESTClient heartRESTClient;

    @Autowired
    HeartConfig heartConfig;

    @GetMapping(path = {"/", "/onboarding", "/recording", "/sharing", "/viewing"})
    public String getIndexView(){
        return "index.html";
    }

    @GetMapping(path = "/sharing/{eventId}/", produces = "text/plain")
    public void sharingViewURL(@PathVariable("eventId") String eventId, HttpServletResponse response) throws IOException {
        String streamIdUserId = (String) transferService.peek(eventId, "streamIdUserId");
        String url = heartConfig.getBaseURL();
        String pryvTokenEndpoint = heartRESTClient.getPryvTokenEndpointUser(streamIdUserId);
        url = url + "heart/sharing/?pryvApiEndpoint=" + pryvTokenEndpoint;
        response.sendRedirect(url);
    }

}
