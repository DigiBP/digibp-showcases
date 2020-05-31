/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
@Scope("prototype")
public class QueuesService {
    @Autowired
    private Map<String, BlockingQueue<Object>> queues;

    protected String getKey(String traceId, String topic){
        return traceId + "-" + topic;
    }

    protected void createQueueIfNotExists(String traceId, String topic){
        if(!queues.containsKey(getKey(traceId, topic))) {
            BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(1);
            queues.put(getKey(traceId, topic), blockingQueue);
        }
    }

    public Object poll(String traceId, String topic, long seconds) throws InterruptedException {
        createQueueIfNotExists(traceId, topic);
        Object object = queues.get(getKey(traceId, topic)).poll(seconds, TimeUnit.SECONDS);
        queues.remove(getKey(traceId, topic));
        return object;
    }

    public Object take(String traceId, String topic) throws InterruptedException {
        createQueueIfNotExists(traceId, topic);
        Object object = queues.get(getKey(traceId, topic)).take();
        queues.remove(getKey(traceId, topic));
        return object;
    }

    public void put(String traceId, String topic, Object payload) throws InterruptedException {
        createQueueIfNotExists(traceId, topic);
        queues.get(getKey(traceId, topic)).put(payload);
    }
}

