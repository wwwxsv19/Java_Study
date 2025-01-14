package mine.testcode.domain.notify;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mine.testcode.domain.notify.types.NotifyType;

@Builder
@Entity
@Table(name = "notify_tbl")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notify {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int notifyId;

    private String notifyTitle;

    private String notifyContent;

    @Enumerated(EnumType.STRING)
    private NotifyType notifyType;

    private String notifyUrl;

    private String receiveUserEmail;

    private boolean isRead;
}
