package mine.testcode.domain.notify.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {
    SseEmitter save(String emitterId, SseEmitter sseEmitter);

    void saveEventCache(String eventCacheId, Object event);

    Map<String, SseEmitter> findAllEmitterByUserEmail(String userEmail);

    Map<String, Object> findAllEventCacheByUserEmail(String userEmail);

    void deleteById(String emitterId);

    void deleteAllEmitterByUserEmail(String userEmail);

    void deleteAllEventCacheByUserEmail(String userEmail);

}
