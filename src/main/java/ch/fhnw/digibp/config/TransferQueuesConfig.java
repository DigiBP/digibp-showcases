/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TransferQueue;

@Configuration
public class TransferQueuesConfig {

    @Bean(name = "transferQueues")
    @Scope("singleton")
    public Map<Object, TransferQueue<Object>> createTransferQueues(){
        return new HashMap<>();
    }
}
