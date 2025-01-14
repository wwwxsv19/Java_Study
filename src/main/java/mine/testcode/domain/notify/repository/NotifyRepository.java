package mine.testcode.domain.notify.repository;

import mine.testcode.domain.notify.Notify;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotifyRepository extends JpaRepository<Notify, Integer> {
}
