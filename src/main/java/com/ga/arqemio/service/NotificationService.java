package com.ga.arqemio.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class NotificationService {
    private Map<Long, SseEmitter> emitterMap=new ConcurrentHashMap<>();
    private static final Logger logger= Logger.getLogger(NotificationService.class.getName());

    public SseEmitter subscribe(Long userId){
        SseEmitter emitter=new SseEmitter(30 * 60 * 1000L);
        emitterMap.put(userId,emitter);
        logger.log(Level.INFO,"User {0} subscribed to notifications",userId);
        emitter.onCompletion(()->emitterMap.remove(userId));
        emitter.onTimeout(()->emitterMap.remove(userId));
        return emitter;
    }

    public void sendNotification(Long userId, String message){
        SseEmitter emitter= emitterMap.get(userId);
        if (emitter !=null){
            try {
                emitter.send(message);
                logger.log(Level.INFO, "Notification sent to user {0}", userId);
            } catch (IOException e){
                emitterMap.remove(userId);
                logger.log(Level.WARNING, "Failed to send notification to user "+ userId, e);
            }
        }
    }

}
