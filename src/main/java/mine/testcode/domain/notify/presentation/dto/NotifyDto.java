package mine.testcode.domain.notify.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import mine.testcode.domain.notify.Notify;

public class NotifyDto {
    @Builder
    @Getter
    public static class NewResponse {
        private Notify notify;
    }
}
