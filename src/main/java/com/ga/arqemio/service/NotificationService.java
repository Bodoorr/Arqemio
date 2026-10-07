package com.ga.arqemio.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {
    private Map<Long, SseEmitter> emitterMap=new HashMap<>();

    public SseEmitter subscribe(Long userId){
        SseEmitter emitter=new SseEmitter();
        emitterMap.put(userId,emitter);
        emitter.onCompletion(()->emitterMap.remove(userId));
        emitter.onTimeout(()->emitterMap.remove(userId));
        return emitter;
    }

    public void sendNotification(Long userId, String message){
        SseEmitter emitter= emitterMap.get(userId);
        if (emitter !=null){
            try {
                emitter.send(message);
            } catch (IOException e){
                emitterMap.remove(userId);
            }
        }
    }
}
