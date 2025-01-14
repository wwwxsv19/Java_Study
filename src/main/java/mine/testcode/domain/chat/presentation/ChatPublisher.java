package mine.testcode.domain.chat.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mine.testcode.domain.chat.ChatRoom;
import mine.testcode.domain.chat.presentation.dto.ChatMessage;
import mine.testcode.domain.chat.service.ChatService;
import mine.testcode.domain.notify.service.NotifyService;
import mine.testcode.global.utils.JwtUtil;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatPublisher {
    private final ChatService chatService;
    private final NotifyService notifyService;

    private final JwtUtil jwtUtil;

    private final SimpMessageSendingOperations messageTemplate;

    @MessageMapping("/message")
    public void message(StompHeaderAccessor accessor, ChatMessage chatMessage) {
        log.info("메시지 : {}", chatMessage.toString());

        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("존재하지 않거나 잘못된 토큰입니다.");
        }

        String token = header.substring(7);
        String userEmail = jwtUtil.getUserEmail(token);

        if (ChatMessage.MessageType.ENTER.equals(chatMessage.getMessageType())) {
            log.info("채팅방 첫 개설");
            chatMessage.initChatRoomId(chatService.createRoom(userEmail));
        }

        chatMessage.initSentAt();
        chatService.saveChatMessage(chatMessage);

        String chatRoomId = chatMessage.getChatRoomId();

        log.info("메시지 전송");
        messageTemplate.convertAndSend("/sub/" + chatRoomId, chatMessage);

        try {
            ChatRoom chatRoom = chatService.findRoomById(chatRoomId);
            Long createUserId = chatRoom.getCreateUserId();

            log.info("메시지 전송 알림 호출");
            notifyService.sendChatAnswerNotify(chatRoomId, createUserId);
        } catch (Exception e) {
            log.error("존재하지 않는 채팅방입니다.");
        }

        log.info("메시지 전송 완료");
    }
}
