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
@RequestMapping("/snap")
public class SnapController {

    @GetMapping(path = {"/"})
    public String getIndexView(){
        return "snap.html";
    }

}
