package uz.pdp.namozvaqtlari.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.pdp.namozvaqtlari.entity.Users;
import uz.pdp.namozvaqtlari.repo.UsersRepository;

import java.util.List;

@Service
public class PrayerTimeScheduler {

    private final UsersRepository usersRepository;
    private final TelegramBot telegramBot;

    public PrayerTimeScheduler(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
        this.telegramBot = BotService.telegramBot;
    }

    @Scheduled(cron = "0 059 14 * * *")
    public void sendDailyPrayerTimes() {
        List<Users> usersList = usersRepository.findAllByDailyReminder(true);
        for (Users user : usersList) {
            String prayerTimes = BotService.getPrayerTimes(user);
            SendMessage sendMessage = new SendMessage(user.getChatId(), """
                    🌙 Bugungi namoz vaqtlari:
                    
                    %s
                    
                    %s
                    """.formatted(user.getCity(), prayerTimes));
            telegramBot.execute(sendMessage);
        }
    }
}
