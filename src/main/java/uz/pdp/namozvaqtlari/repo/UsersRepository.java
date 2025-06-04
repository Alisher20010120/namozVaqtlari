package uz.pdp.namozvaqtlari.repo;

import com.pengrad.telegrambot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.namozvaqtlari.entity.Users;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByChatId(Long id);

    List<Users> findAllByDailyReminder(boolean b);
}