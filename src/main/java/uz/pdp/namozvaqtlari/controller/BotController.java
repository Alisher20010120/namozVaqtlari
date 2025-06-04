package uz.pdp.namozvaqtlari.controller;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uz.pdp.namozvaqtlari.entity.State;
import uz.pdp.namozvaqtlari.entity.Users;
import uz.pdp.namozvaqtlari.repo.QazoNamozRepository;
import uz.pdp.namozvaqtlari.repo.UsersRepository;
import uz.pdp.namozvaqtlari.service.BotService;
import com.pengrad.telegrambot.request.DeleteMessage;

import java.util.Optional;

import static uz.pdp.namozvaqtlari.service.BotService.telegramBot;

@Controller
public class BotController {
    @Autowired
    private BotService botService;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private QazoNamozRepository qazoNamozRepository;

    public void start() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                handler(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handler(Update update) {
        // Kanaldan kelgan xabarlarni qayta ishlash
        if (update.channelPost() != null) {
            botService.processChannelPost(update);
            return;
        }

        if (update.message() != null) {
            handleMessage(update.message());
        } else if (update.callbackQuery() != null) {
            handleCallbackQuery(update.callbackQuery());
        }
    }

    private void handleMessage(Message message) {
        Long chatId = message.chat().id();
        Optional<Users> existingUser = usersRepository.findByChatId(chatId);

        Users users = existingUser.orElseGet(() -> botService.genCreateUser(chatId, message.chat().firstName(), message.chat().username()));

        if (message.text() != null) {
            if (message.text().equals("/start")) {
                BotService.acceptStart(users, message, usersRepository, botService);
            } else if (message.text().equals("Qazolar hisobi") ||
                    message.text().equals("⚙️ Sozlamalar") ||
                    message.text().equals("🕌 Bugungi namoz vaqtlari")) {
                // These commands should work regardless of state
                BotService.acceptAmount(users, message, qazoNamozRepository, usersRepository);
            } else if (existingUser.isPresent() && existingUser.get().getState().equals(State.ASK_AMOUNT)) {
                BotService.acceptAmount(users, message, qazoNamozRepository, usersRepository);
            } else if (existingUser.isPresent() && existingUser.get().getState().equals(State.SETTINGS_NAME)) {
                // Ism o'zgartirish
                BotService.handleNameChange(users, message.text(), usersRepository);
            }
        }
    }

    // CallbackQuery handler metodini to'g'rilash - orqaga tugmasi uchun
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.from().id();
        Optional<Users> existingUser = usersRepository.findByChatId(chatId);

        Users users = existingUser.orElseGet(() -> botService.genCreateUser(chatId, callbackQuery.from().firstName(), callbackQuery.from().username()));

        String data = callbackQuery.data();
        System.out.println("Callback data: " + data + ", User state: " + users.getState());

        if (existingUser.isPresent() && existingUser.get().getState().equals(State.SHARING_REGION)) {
            BotService.acceptRegionAskDays(users, data, usersRepository, callbackQuery);
        } else if (existingUser.isPresent() && existingUser.get().getState().equals(State.DAYSORWEEK)) {
            BotService.acceptDaysAsk(users, data, usersRepository, callbackQuery);
        } else if (existingUser.isPresent() && existingUser.get().getState().equals(State.COUNT)) {
            BotService.acceptCount(users, data, usersRepository, qazoNamozRepository, callbackQuery);
        } else if (existingUser.isPresent() && existingUser.get().getState().equals(State.PLUS)) {
            BotService.acceptAmountAskPlus(users, data, usersRepository, qazoNamozRepository, callbackQuery);
        } else if (existingUser.isPresent() && existingUser.get().getState().equals(State.SETTINGS)) {
            // Sozlamalar menyusi
            BotService.handleSettings(users, data, usersRepository, callbackQuery);
        } else if (existingUser.isPresent() && existingUser.get().getState().equals(State.SETTINGS_CITY)) {
            // Shahar o'zgartirish
            BotService.handleCityChange(users, data, usersRepository);
        } else if (data.equals("orqaga") || data.startsWith("change_") || data.equals("toggle_reminder") || data.equals("back_to_main") || data.equals("back_to_settings")) {
            // Orqaga tugmasi va sozlamalar menyusi tugmalari
            if (data.equals("orqaga") && users.getCountMessageId() != null) {
                // Qazo namozlar o'zgartirish menyusidan orqaga qaytish
                DeleteMessage deleteCountMessage = new DeleteMessage(users.getChatId(), users.getCountMessageId());
                telegramBot.execute(deleteCountMessage);
                users.setCountMessageId(null);
                usersRepository.save(users);
            }

            users.setState(data.equals("orqaga") ? State.ASK_AMOUNT : State.SETTINGS);
            usersRepository.save(users);
            BotService.handleSettings(users, data, usersRepository, callbackQuery);
        }
    }
}
