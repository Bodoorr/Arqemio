package com.ga.arqemio.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class NotificationService {
    private Map<Long, Set<SseEmitter>> emitterMap=new ConcurrentHashMap<>();
    private static final Logger logger= Logger.getLogger(NotificationService.class.getName());

    public SseEmitter subscribe(Long userId){
        SseEmitter emitter=new SseEmitter(30 * 60 * 1000L);
        emitterMap.computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet()).add(emitter);
        logger.log(Level.INFO,"User {0} subscribed to notifications",userId);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(error -> removeEmitter(userId, emitter));
        return emitter;
    }

    public void sendNotification(Long userId, String message){
        Set<SseEmitter> emitters= emitterMap.get(userId);
        if (emitters !=null){
            for (SseEmitter emitter: emitters){
                try {
                    emitter.send(message);
                    logger.log(Level.INFO, "Notification sent to user {0}", userId);
                } catch (IOException e){
                    removeEmitter(userId, emitter);
                    logger.log(Level.WARNING, "Failed to send notification to user "+ userId, e);
                }
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter){
        Set<SseEmitter> emitters = emitterMap.get(userId);
        if (emitters != null){
            emitters.remove(emitter);
            if (emitters.isEmpty()){
                emitterMap.remove(userId, emitters);
            }
        }
    }

}
