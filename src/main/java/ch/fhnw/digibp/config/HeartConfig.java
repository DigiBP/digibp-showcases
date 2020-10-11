/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.config;

import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

@Configuration
public class HeartConfig {
    String baseURL = "";

    public void setBaseURL(HttpServletRequest httpServletRequest) {
        try {
            baseURL = new URL(new URL(httpServletRequest.getRequestURL().toString()), "/").toString().replaceFirst("(http://)", "https://");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getBaseURL(HttpServletRequest httpServletRequest) {
        setBaseURL(httpServletRequest);
        return baseURL;
    }

    public String getBaseURL() {
        return baseURL;
    }
}
