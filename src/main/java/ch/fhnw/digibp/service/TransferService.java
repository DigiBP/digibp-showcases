/*
 * Copyright (c) 2020. University of Applied Sciences and Arts Northwestern Switzerland FHNW.
 * All rights reserved.
 */

package ch.fhnw.digibp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

@Service
@Scope("prototype")
public class TransferService {
    @Autowired
    private Map<Object, TransferQueue<Object>> transferQueues;

    protected void createQueueIfNotExists(TransferKey transferKey){
        if(!transferQueues.containsKey(transferKey)) {
            transferQueues.put(transferKey, new LinkedTransferQueue<>());
        }
    }

    public void put(String traceId, String topic, Object payload) throws InterruptedException {
        TransferKey transferKey = new TransferKey(traceId, topic);
        createQueueIfNotExists(transferKey);
        transferQueues.get(transferKey).put(payload);
    }

    public Object take(String traceId, String topic) throws InterruptedException {
        TransferKey transferKey = new TransferKey(traceId, topic);
        createQueueIfNotExists(transferKey);
        Object object = transferQueues.get(transferKey).take();
        transferQueues.remove(transferKey);
        return object;
    }

    public Object poll(String traceId, String topic, long seconds) throws InterruptedException {
        TransferKey transferKey = new TransferKey(traceId, topic);
        createQueueIfNotExists(transferKey);
        Object object = transferQueues.get(transferKey).poll(seconds, TimeUnit.SECONDS);
        transferQueues.remove(transferKey);
        return object;
    }

    public void transfer(String traceId, String topic, Object payload) throws InterruptedException {
        TransferKey transferKey = new TransferKey(traceId, topic);
        createQueueIfNotExists(transferKey);
        transferQueues.get(transferKey).transfer(payload);
    }

    public boolean tryTransfer(String traceId, String topic, Object payload, long seconds) throws InterruptedException {
        TransferKey transferKey = new TransferKey(traceId, topic);
        createQueueIfNotExists(transferKey);
        return transferQueues.get(transferKey).tryTransfer(payload, seconds, TimeUnit.SECONDS);
    }

    protected static class TransferKey {
        String traceId;
        String topic;

        public TransferKey(String traceId, String topic) {
            this.traceId = traceId;
            this.topic = topic;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TransferKey that = (TransferKey) o;

            if (!Objects.equals(traceId, that.traceId)) return false;
            return Objects.equals(topic, that.topic);
        }

        @Override
        public int hashCode() {
            int result = traceId != null ? traceId.hashCode() : 0;
            result = 31 * result + (topic != null ? topic.hashCode() : 0);
            return result;
        }
    }

    public void publish(String traceId, String topic, Object payload) {
        new Thread(() -> {
            try {
                transfer(traceId, topic, payload);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void subscribe(String traceId, String topic, TransferHandler transferHandler) {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Object object = take(traceId, topic);
                    transferHandler.execute(object);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @FunctionalInterface
    public interface TransferHandler {
        void execute(Object payload) throws InterruptedException;
    }
}

