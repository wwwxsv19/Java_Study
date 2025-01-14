package mine.testcode.domain.notify.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class CustomEmitterRepository implements EmitterRepository {
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCacheMap = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
        emitterMap.put(emitterId, sseEmitter);
        return sseEmitter;
    }

    @Override
    public void saveEventCache(String eventCacheId, Object event) {
        eventCacheMap.put(eventCacheId, event);
    }

    @Override
    public Map<String, SseEmitter> findAllEmitterByUserEmail(String userEmail) {
        return emitterMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userEmail))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Object> findAllEventCacheByUserEmail(String userEmail) {
        return eventCacheMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userEmail))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void deleteById(String emitterId) {
        emitterMap.remove(emitterId);
    }

    @Override
    public void deleteAllEmitterByUserEmail(String userEmail) {
        emitterMap.forEach(
                (key, emitter) -> {
                    if (key.startsWith(userEmail)) emitterMap.remove(key);
                }
        );
    }

    @Override
    public void deleteAllEventCacheByUserEmail(String userEmail) {
        eventCacheMap.forEach(
                (key, emitter) -> {
                    if (key.startsWith(userEmail)) eventCacheMap.remove(key);
                }
        );
    }
}
