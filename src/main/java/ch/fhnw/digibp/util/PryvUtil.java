/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.util;

import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class PryvUtil {

    public static String getTokenEndpoint(String token, String endpoint){
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(endpoint);
            uriBuilder.setUserInfo(token);
            return uriBuilder.toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getEndpoint(String tokenEndpoint){
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(tokenEndpoint);
            uriBuilder.setUserInfo(null);
            return uriBuilder.toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getToken(String tokenEndpoint){
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(tokenEndpoint);
            return uriBuilder.getUserInfo();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getHost(String tokenEndpoint){
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(tokenEndpoint);
            return uriBuilder.getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPathFirstSegment(String tokenEndpoint){
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(tokenEndpoint);
            return uriBuilder.getPathSegments().get(0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUserId(String tokenEndpoint){
        String host = getHost(tokenEndpoint);
        host = host != null ? host.replaceAll("(\\.)", "-") : null;
        String pathFirstSegment = getPathFirstSegment(tokenEndpoint);
        if((pathFirstSegment != null ? pathFirstSegment.length() : 0) >1)
        {
            host = host +"_" + pathFirstSegment;
        }
        return host;
    }

    public static String getUserName(String tokenEndpoint){
        String host = getHost(tokenEndpoint);
        String pathFirstSegment = getPathFirstSegment(tokenEndpoint);
        if((pathFirstSegment != null ? pathFirstSegment.length() : 0) >1)
        {
            host = host +"/" + pathFirstSegment;
        }
        return host;
    }

}
