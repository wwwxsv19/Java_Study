package mine.testcode.domain.notify.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mine.testcode.domain.notify.Notify;
import mine.testcode.domain.notify.presentation.dto.NotifyDto;
import mine.testcode.domain.notify.repository.EmitterRepository;
import mine.testcode.domain.notify.repository.NotifyRepository;
import mine.testcode.domain.notify.types.NotifyType;
import mine.testcode.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotifyService {
    private final UserService userService;

    private final EmitterRepository emitterRepository;
    private final NotifyRepository notifyRepository;

    @Value("${emitter.expiration}")
    private long exprTime;

    public SseEmitter subscribe(String userEmail, String lastEventId) {
        log.info("SSE Emitter 구독 시작");

        String emitterId = userEmail + "_" + System.currentTimeMillis();
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(exprTime));

        log.info("시간 초과 혹은 비동기 요청 실패 시 자동 삭제");
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        sendToClient(emitter, emitterId, userEmail + " First Connection Success!");

        if (!lastEventId.isEmpty()) {
            log.info("이벤트 캐시 SSE 전부 전송");
            Map<String, Object> eventMap = emitterRepository.findAllEventCacheByUserEmail(userEmail);
            eventMap.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> sendToClient(emitter, entry.getKey(), entry.getValue()));
        }

        log.info("구독 완료");
        return emitter;
    }

    public void send(String notifyTitle, String notifyContent, NotifyType notifyType, String notifyUrl, String receivedUserEmail) {
        Notify notify = notifyRepository.save(this.create(notifyTitle, notifyContent, notifyType, notifyUrl, receivedUserEmail));

        Map<String, SseEmitter> emitterMap = emitterRepository.findAllEmitterByUserEmail(receivedUserEmail);
        emitterMap.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notify);
                    sendToClient(emitter, key, NotifyDto.NewResponse.builder()
                            .notify(notify)
                            .build());
                }
        );
    }

    public Notify create(String notifyTitle, String notifyContent, NotifyType notifyType, String notifyUrl, String receivedUserEmail) {
        return Notify.builder()
                .notifyTitle(notifyTitle)
                .notifyContent(notifyContent)
                .notifyType(notifyType)
                .notifyUrl(notifyUrl)
                .receiveUserEmail(receivedUserEmail)
                .build();
    }

    public void sendToClient(SseEmitter emitter, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .data(data));
        } catch (IOException e) {
            emitterRepository.deleteById(emitterId);
            // throw new BadRequestException("알림 전송에 실패하였습니다.");
            log.error("알림 전송에 실패하였습니다.");
        }
    }

    // ChatPublisher 에 들어가는 채팅 수신 알림
    public void sendChatAnswerNotify(String chatRoomId, Long createUserId) {
        log.info("채팅 수신 알림");

        String notifyTitle = "메시지가 도착했어요!";
        String notifyContent = "친구의 답변을 확인해 볼까요?";
        String notifyUrl = "/api/chat/" + chatRoomId;

        String receiveUserEmail = userService.getUserEmailByUserId(createUserId);

        this.send(notifyTitle, notifyContent, NotifyType.CHAT_ANSWER, notifyUrl, receiveUserEmail);
    }
}

