package uz.pdp.namozvaqtlari.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.namozvaqtlari.entity.Users;
import uz.pdp.namozvaqtlari.repo.UsersRepository;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class SettingsController {
    private final UsersRepository usersRepository;

    public SettingsController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @GetMapping("/chatId")
    public HttpEntity<Users> getChatId(@RequestParam Long chatId) {
        System.out.println("chatId = " + chatId);
        Users users=usersRepository.findByChatId(chatId).orElseThrow();
        System.out.println("users = " + users);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/updateSettings")
    public HttpEntity<?> updateSettings(@RequestBody Map<String, Object> request) {
        Long chatId = Long.parseLong(request.get("chatId").toString());
        String fullName = (String) request.get("fullName");
        String city = (String) request.get("city");
        Boolean dailyReminder = (Boolean) request.get("dailyReminder");

        Users user = usersRepository.findByChatId(chatId).orElseThrow();
        user.setFullName(fullName);
        user.setCity(city);
        user.setDailyReminder(dailyReminder);
        usersRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/updateReminder")
    public HttpEntity<?> updateReminder(@RequestBody Map<String, Object> request) {
        Long chatId = Long.parseLong(request.get("chatId").toString());
        Boolean dailyReminder = (Boolean) request.get("dailyReminder");

        Users user = usersRepository.findByChatId(chatId).orElseThrow();
        user.setDailyReminder(dailyReminder);
        usersRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true));
    }
}
