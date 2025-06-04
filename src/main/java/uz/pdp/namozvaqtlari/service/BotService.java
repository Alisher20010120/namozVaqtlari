package uz.pdp.namozvaqtlari.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.namozvaqtlari.entity.QazoNamoz;
import uz.pdp.namozvaqtlari.entity.State;
import uz.pdp.namozvaqtlari.entity.Users;
import uz.pdp.namozvaqtlari.repo.QazoNamozRepository;
import uz.pdp.namozvaqtlari.repo.UsersRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BotService {
    @Autowired
    private UsersRepository usersRepository;

    // Eng oxirgi kanal xabarini saqlash uchun o'zgaruvchilar
    private static Message lastChannelMessage = null;
    private static String channelUsername = null;

    // Kanalingiz ID'si (e.g. @mychannel yoki -1001234567890)
    private static final String CHANNEL_ID = "@yuboruvchi_b"; // Kanalingiz ID'sini kiriting

    public static TelegramBot telegramBot = new TelegramBot("");

    // Asosiy tugmalarni yaratish uchun helper metod
    private static ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{
                        new KeyboardButton("Qazolar hisobi"),
                        new KeyboardButton("⚙️ Sozlamalar")
                },
                new KeyboardButton[]{
                        new KeyboardButton("🕌 Bugungi namoz vaqtlari")
                }
        );
        keyboard.resizeKeyboard(true);
        keyboard.oneTimeKeyboard(false); // Tugmalar yo'qolmasligi uchun
        return keyboard;
    }

    // Faqat kerakli xabarlarni o'chirish uchun helper metod
    private static void deleteSpecificMessages(Users user, boolean deletePrayerTimes) {
        // City message
        if (user.getCityMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getCityMessageId()));
            user.setCityMessageId(null);
        }

        // City accepted message
        if (user.getCityAcceptedMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getCityAcceptedMessageId()));
            user.setCityAcceptedMessageId(null);
        }

        // Reminder message
        if (user.getReminderMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getReminderMessageId()));
            user.setReminderMessageId(null);
        }

        // Reminder response message
        if (user.getReminderResponseMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getReminderResponseMessageId()));
            user.setReminderResponseMessageId(null);
        }

        // Prayer times message - faqat kerak bo'lganda o'chirish
        if (deletePrayerTimes && user.getPrayerTimesMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getPrayerTimesMessageId()));
            user.setPrayerTimesMessageId(null);
        }

        // Menu message
        if (user.getMenuMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getMenuMessageId()));
            user.setMenuMessageId(null);
        }

        // Qazo message
        if (user.getQazoMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getQazoMessageId()));
            user.setQazoMessageId(null);
        }

        // Count message
        if (user.getCountMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getCountMessageId()));
            user.setCountMessageId(null);
        }

        // Settings message
        if (user.getSettingsMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getSettingsMessageId()));
            user.setSettingsMessageId(null);
        }
    }

    // Barcha xabarlarni o'chirish uchun universal metod
    public static void deleteAllMessages(Users user) {
        // City message
        if (user.getCityMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getCityMessageId()));
            user.setCityMessageId(null);
        }

        // City accepted message
        if (user.getCityAcceptedMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getCityAcceptedMessageId()));
            user.setCityAcceptedMessageId(null);
        }

        // Reminder message
        if (user.getReminderMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getReminderMessageId()));
            user.setReminderMessageId(null);
        }

        // Reminder response message
        if (user.getReminderResponseMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getReminderResponseMessageId()));
            user.setReminderResponseMessageId(null);
        }

        // Prayer times message
        if (user.getPrayerTimesMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getPrayerTimesMessageId()));
            user.setPrayerTimesMessageId(null);
        }

        // Menu message
        if (user.getMenuMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getMenuMessageId()));
            user.setMenuMessageId(null);
        }

        // Qazo message
        if (user.getQazoMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getQazoMessageId()));
            user.setQazoMessageId(null);
        }

        // Count message
        if (user.getCountMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getCountMessageId()));
            user.setCountMessageId(null);
        }

        // Settings message
        if (user.getSettingsMessageId() != null) {
            telegramBot.execute(new DeleteMessage(user.getChatId(), user.getSettingsMessageId()));
            user.setSettingsMessageId(null);
        }
    }

    // Bugungi namoz vaqtlarini yuborish uchun metod
    public static void sendTodayPrayerTimes(Users users) {
        String prayerTimes = getPrayerTimes(users);
        SendMessage prayerTimesMessage = new SendMessage(users.getChatId(), """
                🌙 Bugungi namoz vaqtlari:
                
                %s
                
                %s
                """.formatted(users.getCity(), prayerTimes));
        SendResponse response = telegramBot.execute(prayerTimesMessage);
        users.setPrayerTimesMessageId(response.message().messageId());
    }

    // Kanaldan kelgan xabarlarni qayta ishlash
    public void processChannelPost(Update update) {
        if (update.channelPost() != null && update.channelPost().chat() != null) {
            Message channelPost = update.channelPost();
            String channelId = channelPost.chat().id().toString();

            // Faqat bizning kanaldan kelgan xabarlarni qayta ishlash
            if (channelId.equals(CHANNEL_ID) ||
                    (channelPost.chat().username() != null &&
                            ("@" + channelPost.chat().username()).equals(CHANNEL_ID))) {

                // Eng oxirgi xabarni saqlash
                lastChannelMessage = channelPost;
                channelUsername = channelPost.chat().username();
                System.out.println("Kanaldan yangi xabar qabul qilindi: " +
                        (channelPost.text() != null ? channelPost.text() : "Media content"));

                // Barcha foydalanuvchilarga xabarni yuborish
                sendMessageToAllUsers(channelPost);
            }
        }
    }

    // Barcha foydalanuvchilarga xabarni yuborish
    private void sendMessageToAllUsers(Message channelMessage) {
        // Barcha foydalanuvchilarni olish
        List<Users> allUsers = usersRepository.findAll();

        System.out.println("Xabar " + allUsers.size() + " ta foydalanuvchiga yuborilmoqda...");

        for (Users user : allUsers) {
            try {
                sendChannelMessageToUser(user.getChatId(), channelMessage);
            } catch (Exception e) {
                System.err.println("Foydalanuvchiga xabar yuborishda xatolik: " + user.getChatId() + ", Xato: " + e.getMessage());
            }
        }

        System.out.println("Xabar barcha foydalanuvchilarga yuborildi.");
    }

    // Bitta foydalanuvchiga kanal xabarini yuborish
    private void sendChannelMessageToUser(Long chatId, Message channelMessage) {
        try {
            if (channelMessage.text() != null) {
                // Matn xabari
                telegramBot.execute(new SendMessage(chatId, channelMessage.text()));
            } else if (channelMessage.photo() != null && channelMessage.photo().length > 0) {
                // Rasm
                SendPhoto sendPhoto = new SendPhoto(chatId, channelMessage.photo()[channelMessage.photo().length - 1].fileId());
                if (channelMessage.caption() != null) {
                    sendPhoto.caption(channelMessage.caption());
                }
                telegramBot.execute(sendPhoto);
            } else if (channelMessage.video() != null) {
                // Video
                SendVideo sendVideo = new SendVideo(chatId, channelMessage.video().fileId());
                if (channelMessage.caption() != null) {
                    sendVideo.caption(channelMessage.caption());
                }
                telegramBot.execute(sendVideo);
            } else if (channelMessage.document() != null) {
                // Hujjat/fayl
                SendDocument sendDocument = new SendDocument(chatId, channelMessage.document().fileId());
                if (channelMessage.caption() != null) {
                    sendDocument.caption(channelMessage.caption());
                }
                telegramBot.execute(sendDocument);
            } else if (channelMessage.audio() != null) {
                // Audio
                SendAudio sendAudio = new SendAudio(chatId, channelMessage.audio().fileId());
                if (channelMessage.caption() != null) {
                    sendAudio.caption(channelMessage.caption());
                }
                telegramBot.execute(sendAudio);
            } else if (channelMessage.voice() != null) {
                // Ovozli xabar
                SendVoice sendVoice = new SendVoice(chatId, channelMessage.voice().fileId());
                if (channelMessage.caption() != null) {
                    sendVoice.caption(channelMessage.caption());
                }
                telegramBot.execute(sendVoice);
            } else {
                // Boshqa turdagi xabarlar
                telegramBot.execute(new SendMessage(chatId, "Kanaldan yangi xabar mavjud, lekin uni yuborib bo'lmadi."));
            }

            System.out.println("Kanal xabari foydalanuvchiga yuborildi: " + chatId);
        } catch (Exception e) {
            System.err.println("Xabarni yuborishda xatolik: " + e.getMessage());
            telegramBot.execute(new SendMessage(chatId, "Kanaldan xabarni yuborishda xatolik yuz berdi."));
        }
    }

    // Eng oxirgi kanal xabarini foydalanuvchiga yuborish
    public void sendLatestChannelMessageToUser(Long chatId) {
        if (lastChannelMessage == null) {
            System.out.println("Kanal xabari topilmadi");
            return;
        }

        sendChannelMessageToUser(chatId, lastChannelMessage);
    }

    public static void acceptRegionAskDays(Users users, String message, UsersRepository usersRepository, CallbackQuery callbackQuery) {
        // Faqat kerakli xabarlarni o'chirish
        deleteSpecificMessages(users, false);

        boolean validCity = true;
        switch (message.toLowerCase()) {
            case "toshkent" -> users.setCity("Toshkent");
            case "buxoro" -> users.setCity("Buxoro");
            case "samarqand" -> users.setCity("Samarqand");
            case "navoiy" -> users.setCity("Navoiy");
            case "xiva" -> users.setCity("Xiva");
            case "nukus" -> users.setCity("Nukus");
            case "termiz" -> users.setCity("Termiz");
            case "shahrisabz" -> users.setCity("Shahrisabz");
            case "jizzax" -> users.setCity("Jizzax");
            case "sirdaryo" -> users.setCity("Sirdaryo");
            case "fargona" -> users.setCity("Farg'ona");
            case "andijon" -> users.setCity("Andijon");
            case "namangan" -> users.setCity("Namangan");
            default -> {
                SendMessage sendMessage = new SendMessage(users.getChatId(), "Kechirasiz nimadir xatolik bo'ldi.🥱  Iltimos qayta urinib ko'ring😊");
                telegramBot.execute(sendMessage);
                validCity = false;
            }
        }

        if (validCity) {
            // Shahar tanlash xabarini o'chirish uchun markupni o'zgartirish
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(
                    users.getChatId(),
                    callbackQuery.message().messageId()
            );
            telegramBot.execute(editMessageReplyMarkup);

            // Shahar qabul qilindi xabarini yuborish
            SendMessage sendMessage1 = new SendMessage(users.getChatId(), "🏷️ Shahringiz muvaffaqqiyatli qabul qilindi✅✅✅👌");
            SendResponse response1 = telegramBot.execute(sendMessage1);
            users.setCityAcceptedMessageId(response1.message().messageId());

            // Namoz vaqtlari haqida so'rash
            SendMessage sendMessage = new SendMessage(users.getChatId(), users.getFullName() + " 🕋 Sizga har kunlik namoz vaqtlarini yuborib turishimizni xohlaysizmi");
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.addRow(
                    new InlineKeyboardButton("Ha").callbackData("ha"),
                    new InlineKeyboardButton("Yo'q").callbackData("yo")
            );
            sendMessage.replyMarkup(inlineKeyboardMarkup);
            SendResponse response2 = telegramBot.execute(sendMessage);
            users.setReminderMessageId(response2.message().messageId());

            users.setState(State.DAYSORWEEK);
            usersRepository.save(users);
        }
    }

    public static void acceptDaysAsk(Users users, String message, UsersRepository usersRepository, CallbackQuery callbackQuery) {
        // Faqat kerakli xabarlarni o'chirish
        deleteSpecificMessages(users, false);

        switch (message) {
            case "ha" -> {
                SendMessage sendMessage = new SendMessage(users.getChatId(),
                        "📌Xizmatimizni tanlaganingizdan xursandmiz! Endi har kunlik namoz vaqtlarini yuborib turamiz 😊");
                users.setDailyReminder(true);
                SendResponse response = telegramBot.execute(sendMessage);
                users.setReminderResponseMessageId(response.message().messageId());
            }
            case "yo" -> {
                SendMessage sendMessage = new SendMessage(users.getChatId(),
                        "📌  Agarda bu qism sizga kerak bo'lib qolsa, sozlamalardan to'g'rilab olishingiz mumkin🛠️");
                users.setDailyReminder(false);
                SendResponse response = telegramBot.execute(sendMessage);
                users.setReminderResponseMessageId(response.message().messageId());
            }
            default -> {
                SendMessage sendMessage = new SendMessage(users.getChatId(), "📌Nimadir xato🫤");
                telegramBot.execute(sendMessage);
            }
        }

        // Reminder xabarini o'chirish
        if (callbackQuery.message() != null && callbackQuery.message().messageId() != null) {
            DeleteMessage deleteMessage = new DeleteMessage(users.getChatId(), callbackQuery.message().messageId());
            telegramBot.execute(deleteMessage);
            users.setReminderMessageId(null);
        }

        // Namoz vaqtlarini yuborish
        sendTodayPrayerTimes(users);

        // Tugmalarni yuborish
        SendMessage sendMessage = new SendMessage(users.getChatId(), "📑 Agar sizga biror nimani o'zgartirish kerak bo'lsa pastdagilardan birini tanlang👇");
        // Helper metod orqali asosiy tugmalarni yaratish
        ReplyKeyboardMarkup keyboard = createMainKeyboard();
        sendMessage.replyMarkup(keyboard);
        SendResponse menuResponse = telegramBot.execute(sendMessage);
        users.setMenuMessageId(menuResponse.message().messageId());

        // Foydalanuvchi holatini yangilash
        users.setState(State.ASK_AMOUNT);
        usersRepository.save(users);

        // Reminder response xabarini o'chirish (keyinroq)
        if (users.getReminderResponseMessageId() != null) {
            DeleteMessage deleteReminderResponse = new DeleteMessage(users.getChatId(), users.getReminderResponseMessageId());
            telegramBot.execute(deleteReminderResponse);
            users.setReminderResponseMessageId(null);
            usersRepository.save(users);
        }
    }

    @SneakyThrows
    public static String getPrayerTimes(Users users) {
        String prayerTimes = """
                🕔Bomdod : %s
                🌤️Quyosh : %s
                🕑Peshin : %s
                🕒Asr : %s
                🕔Shom : %s
                🕘Xufton : %s
                """;
        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        String city = users.getCity().toLowerCase().replace("'", "");
        HtmlPage page = webClient.getPage("https://namozvaqti.uz/shahar/" + city);
        List<HtmlElement> forms = page.getByXPath("//p");
        if (forms.size() < 6) {
            return "📌Saytdan namoz vaqtlari topilmadi‼️";
        }

        String[] arr = new String[6];
        int in = 0;
        for (int i = 1; i < forms.size() - 2; i++) {
            if (i == 5) {
                continue;
            }
            arr[in++] = forms.get(i).asNormalizedText();
        }

        webClient.close();
        return prayerTimes.formatted(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
    }

    public static void acceptAmount(Users users, Message data, QazoNamozRepository qazoNamozRepository, UsersRepository usersRepository) {
        if (data == null || data.text() == null) {
            System.out.println("Xatolik: kelgan ma'lumot bo'sh!");
            return;
        }

        System.out.println("Foydalanuvchi tanlagan tugma: " + data.text());
        System.out.println("Foydalanuvchi tanlagan tugma: " + data.chat().id());

        if (data.text().equals("Qazolar hisobi")) {
            // Faqat ushbu foydalanuvchiga tegishli qazo namozlarini olish
            List<QazoNamoz> qazoNamozs = qazoNamozRepository.findByUsers(users);
            if (qazoNamozs.isEmpty()) {
                QazoNamoz qazo = new QazoNamoz();
                qazo.setUsers(users);
                qazo.setBomdod(0);
                qazo.setPeshin(0);
                qazo.setAsr(0);
                qazo.setShom(0);
                qazo.setXufton(0);
                qazoNamozRepository.save(qazo);
                SendMessage sendMessage = new SendMessage(users.getChatId(), "📌 Sizda qazo qilingan namoz mavjud emas 😉");
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

                keyboard.addRow(
                        new InlineKeyboardButton("Bomdod").callbackData("bomdod")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Peshin").callbackData("peshin")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Asr").callbackData("asr")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Shom").callbackData("shom")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Xufton").callbackData("xufton")
                );
                sendMessage.replyMarkup(keyboard);
                SendResponse response = telegramBot.execute(sendMessage);
                users.setQazoMessageId(response.message().messageId());
                users.setState(State.COUNT);
                usersRepository.save(users);

                return;
            }

            boolean qazoMavjud = false;
            for (QazoNamoz qazo : qazoNamozs) {
                if (qazo.getBomdod() > 0 || qazo.getPeshin() > 0 || qazo.getAsr() > 0 || qazo.getShom() > 0 || qazo.getXufton() > 0) {
                    qazoMavjud = true;
                    break;
                }
            }

            if (!qazoMavjud) {
                SendMessage sendMessage = new SendMessage(users.getChatId(), "📌 Sizda qazo qilingan namoz mavjud emas 😉");
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

                keyboard.addRow(
                        new InlineKeyboardButton("Bomdod").callbackData("bomdod")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Peshin").callbackData("peshin")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Asr").callbackData("asr")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Shom").callbackData("shom")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Xufton").callbackData("xufton")
                );
                sendMessage.replyMarkup(keyboard);
                SendResponse response = telegramBot.execute(sendMessage);
                users.setQazoMessageId(response.message().messageId());
                users.setState(State.COUNT);
                usersRepository.save(users);

            } else {
                StringBuilder messageText = new StringBuilder("📜 Sizning qazo namozlaringiz:\n");

                for (QazoNamoz qazoNamoz : qazoNamozs) {
                    messageText.append("🕌 Bomdod: ").append(qazoNamoz.getBomdod()).append("\n")
                            .append("🕌 Peshin: ").append(qazoNamoz.getPeshin()).append("\n")
                            .append("🕌 Asr: ").append(qazoNamoz.getAsr()).append("\n")
                            .append("🕌 Shom: ").append(qazoNamoz.getShom()).append("\n")
                            .append("🕌 Xufton: ").append(qazoNamoz.getXufton()).append("\n")
                            .append("---------------------\n");
                }

                SendMessage sendMessage = new SendMessage(users.getChatId(), messageText.toString());
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

                keyboard.addRow(
                        new InlineKeyboardButton("Bomdod").callbackData("bomdod")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Peshin").callbackData("peshin")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Asr").callbackData("asr")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Shom").callbackData("shom")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Xufton").callbackData("xufton")
                );
                sendMessage.replyMarkup(keyboard);
                SendResponse response = telegramBot.execute(sendMessage);
                users.setQazoMessageId(response.message().messageId());
                users.setState(State.COUNT);
                usersRepository.save(users);
            }
        } else if (data.text().equals("⚙️ Sozlamalar")) {
            // Sozlamalar menyusini yaratish
            SendMessage sendMessage = new SendMessage(users.getChatId(), "⚙️ Sozlamalar menyusi");
            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

            keyboard.addRow(
                    new InlineKeyboardButton("Shaharni o'zgartirish").callbackData("change_city")
            );
            keyboard.addRow(
                    new InlineKeyboardButton("Eslatmalarni " + (users.getDailyReminder() ? "o'chirish" : "yoqish")).callbackData("toggle_reminder")
            );
            keyboard.addRow(
                    new InlineKeyboardButton("Orqaga").callbackData("back_to_main")
            );

            sendMessage.replyMarkup(keyboard);
            SendResponse response = telegramBot.execute(sendMessage);
            users.setSettingsMessageId(response.message().messageId());
            users.setState(State.SETTINGS);
            usersRepository.save(users);
        } else if (data.text().equals("🕌 Bugungi namoz vaqtlari")) {
            // Bugungi namoz vaqtlarini yuborish
            sendTodayPrayerTimes(users);
        } else {
            SendMessage sendMessage = new SendMessage(users.getChatId(), "❌ Noto'g'ri tanlov! Iltimos, tugmalardan birini tanlang.");
            // Asosiy tugmalarni qayta yuborish
            sendMessage.replyMarkup(createMainKeyboard());
            telegramBot.execute(sendMessage);
        }
    }

    // Sozlamalar uchun yangi metod
    public static void handleSettings(Users users, String data, UsersRepository usersRepository, CallbackQuery callbackQuery) {
        switch (data) {
            case "change_city" -> {
                SendMessage citySelectionMessage = new SendMessage(users.getChatId(), "📌 Yangi shahringizni tanlang 👇");
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Toshkent").callbackData("city_toshkent"),
                        new InlineKeyboardButton("Buxoro").callbackData("city_buxoro")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Samarqand").callbackData("city_samarqand"),
                        new InlineKeyboardButton("Navoiy").callbackData("city_navoiy")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Jizzax").callbackData("city_jizzax"),
                        new InlineKeyboardButton("Sirdaryo").callbackData("city_sirdaryo")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Qashqadaryo(Shahrisabz)").callbackData("city_shahrisabz"),
                        new InlineKeyboardButton("Surxondaryo").callbackData("city_termiz")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Xorazm(Xiva)").callbackData("city_xiva"),
                        new InlineKeyboardButton("Qoraqalpog'iston(Nukus)").callbackData("city_nukus")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Farg'ona").callbackData("city_fargona"),
                        new InlineKeyboardButton("Andijon").callbackData("city_andijon")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Namangan").callbackData("city_namangan")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Orqaga").callbackData("back_to_settings")
                );
                citySelectionMessage.replyMarkup(inlineKeyboardMarkup);

                SendResponse cityMessageResponse = telegramBot.execute(citySelectionMessage);
                users.setCityMessageId(cityMessageResponse.message().messageId());
                users.setState(State.SETTINGS_CITY);
                usersRepository.save(users);
            }
            case "toggle_reminder" -> {
                // Eslatmalarni yoqish/o'chirish
                users.setDailyReminder(!users.getDailyReminder());
                usersRepository.save(users);

                SendMessage reminderMessage = new SendMessage(users.getChatId(),
                        users.getDailyReminder()
                                ? "✅ Kunlik namoz vaqtlari eslatmalari yoqildi"
                                : "❌ Kunlik namoz vaqtlari eslatmalari o'chirildi");

                SendResponse response = telegramBot.execute(reminderMessage);

                // Sozlamalar menyusini qayta yuborish
                SendMessage settingsMessage = new SendMessage(users.getChatId(), "⚙️ Sozlamalar menyusi");
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

                keyboard.addRow(
                        new InlineKeyboardButton("Shaharni o'zgartirish").callbackData("change_city")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Eslatmalarni " + (users.getDailyReminder() ? "o'chirish" : "yoqish")).callbackData("toggle_reminder")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Orqaga").callbackData("back_to_main")
                );

                settingsMessage.replyMarkup(keyboard);
                SendResponse settingsResponse = telegramBot.execute(settingsMessage);
                users.setSettingsMessageId(settingsResponse.message().messageId());
            }
            // "back_to_main" tugmasi bosilganda barcha sozlamalar xabarlarini o'chirish
            case "back_to_main" -> {
                // Barcha xabarlarni o'chirish
                deleteAllMessages(users);

                // Asosiy menyuga qaytish
                SendMessage mainMenuMessage = new SendMessage(users.getChatId(), "📑 Asosiy menyu");
                mainMenuMessage.replyMarkup(createMainKeyboard());
                telegramBot.execute(mainMenuMessage);
                users.setState(State.ASK_AMOUNT);
                usersRepository.save(users);
            }
        }
    }

    // Shahar o'zgartirish uchun metod
    public static void handleCityChange(Users users, String data, UsersRepository usersRepository) {
        String city = data.replace("city_", "");

        switch (city) {
            case "toshkent" -> users.setCity("Toshkent");
            case "buxoro" -> users.setCity("Buxoro");
            case "samarqand" -> users.setCity("Samarqand");
            case "navoiy" -> users.setCity("Navoiy");
            case "xiva" -> users.setCity("Xiva");
            case "nukus" -> users.setCity("Nukus");
            case "termiz" -> users.setCity("Termiz");
            case "shahrisabz" -> users.setCity("Shahrisabz");
            case "jizzax" -> users.setCity("Jizzax");
            case "sirdaryo" -> users.setCity("Sirdaryo");
            case "fargona" -> users.setCity("Farg'ona");
            case "andijon" -> users.setCity("Andijon");
            case "namangan" -> users.setCity("Namangan");
            case "back_to_settings" -> {
                // Shahar tanlash xabarini o'chirish
                if (users.getCityMessageId() != null) {
                    DeleteMessage deleteCityMessage = new DeleteMessage(users.getChatId(), users.getCityMessageId());
                    telegramBot.execute(deleteCityMessage);
                    users.setCityMessageId(null);
                }

                // Sozlamalar menyusiga qaytish
                SendMessage settingsMessage = new SendMessage(users.getChatId(), "⚙️ Sozlamalar menyusi");
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

                keyboard.addRow(
                        new InlineKeyboardButton("Shaharni o'zgartirish").callbackData("change_city")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Eslatmalarni " + (users.getDailyReminder() ? "o'chirish" : "yoqish")).callbackData("toggle_reminder")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Orqaga").callbackData("back_to_main")
                );

                settingsMessage.replyMarkup(keyboard);
                SendResponse settingsResponse = telegramBot.execute(settingsMessage);
                users.setSettingsMessageId(settingsResponse.message().messageId());
                users.setState(State.SETTINGS);
                usersRepository.save(users);
                return;
            }
            default -> {
                SendMessage errorMessage = new SendMessage(users.getChatId(), "Kechirasiz nimadir xatolik bo'ldi. Iltimos qayta urinib ko'ring.");
                telegramBot.execute(errorMessage);
                return;
            }
        }

        usersRepository.save(users);

        // Shahar tanlash xabarini o'chirish
        if (users.getCityMessageId() != null) {
            DeleteMessage deleteCityMessage = new DeleteMessage(users.getChatId(), users.getCityMessageId());
            telegramBot.execute(deleteCityMessage);
            users.setCityMessageId(null);
        }

        // Shahar o'zgartirilgani haqida xabar
        SendMessage cityChangedMessage = new SendMessage(users.getChatId(),
                "🏙️ Shahringiz muvaffaqiyatli " + users.getCity() + " ga o'zgartirildi!");
        telegramBot.execute(cityChangedMessage);

        // Yangi shahar uchun namoz vaqtlarini yuborish
        sendTodayPrayerTimes(users);

        // Sozlamalar menyusiga qaytish
        SendMessage settingsMessage = new SendMessage(users.getChatId(), "⚙️ Sozlamalar menyusi");
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        keyboard.addRow(
                new InlineKeyboardButton("Shaharni o'zgartirish").callbackData("change_city")
        );
        keyboard.addRow(
                new InlineKeyboardButton("Eslatmalarni " + (users.getDailyReminder() ? "o'chirish" : "yoqish")).callbackData("toggle_reminder")
        );
        keyboard.addRow(
                new InlineKeyboardButton("Orqaga").callbackData("back_to_main")
        );

        settingsMessage.replyMarkup(keyboard);
        SendResponse settingsResponse = telegramBot.execute(settingsMessage);
        users.setSettingsMessageId(settingsResponse.message().messageId());
        users.setState(State.SETTINGS);
        usersRepository.save(users);
    }

    public static void acceptCount(Users users, String data, UsersRepository usersRepository, QazoNamozRepository qazoNamozRepository, CallbackQuery callbackQuery) {
        // Oldingi qazo xabarini o'chirish
        if (users.getQazoMessageId() != null) {
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(
                    users.getChatId(),
                    callbackQuery.message().messageId()
            );
            telegramBot.execute(editMessageReplyMarkup);
            users.setQazoMessageId(null);
        }

        List<QazoNamoz> qazoNamozs = qazoNamozRepository.findByUsers(users);

        for (QazoNamoz qazoNamoz : qazoNamozs) {
            Map<String, Integer> namozlar = new HashMap<>();
            namozlar.put("bomdod", qazoNamoz.getBomdod());
            namozlar.put("peshin", qazoNamoz.getPeshin());
            namozlar.put("asr", qazoNamoz.getAsr());
            namozlar.put("shom", qazoNamoz.getShom());
            namozlar.put("xufton", qazoNamoz.getXufton());

            if (namozlar.containsKey(data)) {
                SendMessage sendMessage = new SendMessage(users.getChatId(), "👇");
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

                keyboard.addRow(
                        new InlineKeyboardButton(data.substring(0, 1).toUpperCase() + data.substring(1)).callbackData("b")
                );
                keyboard.addRow(
                        new InlineKeyboardButton("-").callbackData("minus_" + data),
                        new InlineKeyboardButton(namozlar.get(data) + "").callbackData("q_" + data),
                        new InlineKeyboardButton("+").callbackData("plus_" + data)
                );
                keyboard.addRow(
                        new InlineKeyboardButton("Tasdiqlash").callbackData("tasdiq"),
                        new InlineKeyboardButton("Orqaga").callbackData("orqaga")
                );

                sendMessage.replyMarkup(keyboard);
                SendResponse response = telegramBot.execute(sendMessage);
                users.setCountMessageId(response.message().messageId());
                users.setState(State.PLUS);
                usersRepository.save(users);
            }
        }
    }

    //    Bu yer vaqtinchalik qazo namozlar sonini ushlab turadi
    private static final Map<Long, QazoNamoz> tempChanges = new HashMap<>();

    //     Bu yer qazo namozlar va sozlamalarni chiqarish uchun + - tasdiqlash bekor qilish asosiy logikalari shu yerda
    public static void acceptAmountAskPlus(Users users, String data, UsersRepository usersRepository,
                                           QazoNamozRepository qazoNamozRepository, CallbackQuery callbackQuery) {
        Long chatId = users.getChatId();

        QazoNamoz qazoNamoz = tempChanges.getOrDefault(chatId, qazoNamozRepository.findByUsers(users).stream().findFirst().orElse(null));
        if (qazoNamoz == null) return;

        if (data.startsWith("plus_") || data.startsWith("minus_")) {
            String namoz = data.split("_")[1];

            switch (namoz) {
                case "bomdod":
                    if (data.startsWith("plus_") || qazoNamoz.getBomdod() > 0) {
                        qazoNamoz.setBomdod(qazoNamoz.getBomdod() + (data.startsWith("plus_") ? 1 : -1));
                    }
                    break;
                case "peshin":
                    if (data.startsWith("plus_") || qazoNamoz.getPeshin() > 0) {
                        qazoNamoz.setPeshin(qazoNamoz.getPeshin() + (data.startsWith("plus_") ? 1 : -1));
                    }
                    break;
                case "asr":
                    if (data.startsWith("plus_") || qazoNamoz.getAsr() > 0) {
                        qazoNamoz.setAsr(qazoNamoz.getAsr() + (data.startsWith("plus_") ? 1 : -1));
                    }
                    break;
                case "shom":
                    if (data.startsWith("plus_") || qazoNamoz.getShom() > 0) {
                        qazoNamoz.setShom(qazoNamoz.getShom() + (data.startsWith("plus_") ? 1 : -1));
                    }
                    break;
                case "xufton":
                    if (data.startsWith("plus_") || qazoNamoz.getXufton() > 0) {
                        qazoNamoz.setXufton(qazoNamoz.getXufton() + (data.startsWith("plus_") ? 1 : -1));
                    }
                    break;
            }

            tempChanges.put(chatId, qazoNamoz);
            editUpdateMessage(chatId, callbackQuery.message().messageId(), namoz, qazoNamoz, telegramBot);
        }

        if (data.equals("tasdiq")) {
            // Ma'lumotlarni database'ga saqlash
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(
                    users.getChatId(),
                    callbackQuery.message().messageId()
            );

            qazoNamozRepository.save(qazoNamoz);
            telegramBot.execute(editMessageReplyMarkup);
            tempChanges.remove(chatId); // Xotiradan tozalash

            // Delete the count message (sticker)
            if (users.getCountMessageId() != null) {
                DeleteMessage deleteCountMessage = new DeleteMessage(users.getChatId(), users.getCountMessageId());
                telegramBot.execute(deleteCountMessage);
                users.setCountMessageId(null);
            }

            // Tasdiqlash xabarini yuborish
            SendMessage confirmMessage = new SendMessage(chatId, "✅ O'zgarishlar saqlandi!");
            SendResponse response = telegramBot.execute(confirmMessage);

            // Qazo namozlarni yangi hisob bilan qayta chiqarish
            StringBuilder messageText = new StringBuilder("📜 Sizning qazo namozlaringiz:\n");
            messageText.append("🕌 Bomdod: ").append(qazoNamoz.getBomdod()).append("\n")
                    .append("🕌 Peshin: ").append(qazoNamoz.getPeshin()).append("\n")
                    .append("🕌 Asr: ").append(qazoNamoz.getAsr()).append("\n")
                    .append("🕌 Shom: ").append(qazoNamoz.getShom()).append("\n")
                    .append("🕌 Xufton: ").append(qazoNamoz.getXufton()).append("\n")
                    .append("---------------------\n");

            SendMessage qazoMessage = new SendMessage(users.getChatId(), messageText.toString());
            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

            keyboard.addRow(
                    new InlineKeyboardButton("Bomdod").callbackData("bomdod")
            );
            keyboard.addRow(
                    new InlineKeyboardButton("Peshin").callbackData("peshin")
            );
            keyboard.addRow(
                    new InlineKeyboardButton("Asr").callbackData("asr")
            );
            keyboard.addRow(
                    new InlineKeyboardButton("Shom").callbackData("shom")
            );
            keyboard.addRow(
                    new InlineKeyboardButton("Xufton").callbackData("xufton")
            );
            qazoMessage.replyMarkup(keyboard);
            SendResponse qazoResponse = telegramBot.execute(qazoMessage);
            users.setQazoMessageId(qazoResponse.message().messageId());

            // Foydalanuvchi holatini yangilash
            users.setState(State.COUNT);
            usersRepository.save(users);
        }

        // "orqaga" tugmasi bosilganda qazo namozlar xabarini o'chirish
        if (data.equals("orqaga")) {
            // O'zgarishlarni bekor qilish va eski ma'lumotlarni qaytarish
            tempChanges.remove(chatId);

            // Count message (sticker) ni o'chirish
            if (users.getCountMessageId() != null) {
                DeleteMessage deleteCountMessage = new DeleteMessage(users.getChatId(), users.getCountMessageId());
                telegramBot.execute(deleteCountMessage);
                users.setCountMessageId(null);
            }

            // Bekor qilish xabarini yuborish
            SendMessage cancelMessage = new SendMessage(chatId, "❌ O'zgarishlar bekor qilindi!");
            telegramBot.execute(cancelMessage);

            // Asosiy menyuga qaytish
            users.setState(State.ASK_AMOUNT);
            usersRepository.save(users);
        }
    }


    //     Bu yer qazo namozlarni + - qilish saqlash bekor qilish
    private static void editUpdateMessage(Long chatId, Integer messageId, String namoz, QazoNamoz qazoNamoz, TelegramBot telegramBot) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        keyboard.addRow(
                new InlineKeyboardButton(namoz.substring(0, 1).toUpperCase() + namoz.substring(1)).callbackData("b")
        );
        keyboard.addRow(
                new InlineKeyboardButton("-").callbackData("minus_" + namoz),
                new InlineKeyboardButton(qazoNamoz.getValue(namoz) + "").callbackData("q_" + namoz),
                new InlineKeyboardButton("+").callbackData("plus_" + namoz)
        );
        keyboard.addRow(
                new InlineKeyboardButton("Tasdiqlash").callbackData("tasdiq"),
                new InlineKeyboardButton("Orqaga").callbackData("orqaga")
        );

        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup(chatId, messageId).replyMarkup(keyboard);
        telegramBot.execute(editMessage);
    }

    public static void handleNameChange(Users users, String text, UsersRepository usersRepository) {
        // Ism o'zgartirish logikasi
    }


    //     Bu yer userni saqlash (generatsiya) qilish
    public Users genCreateUser(Long id, String name, String username) {
        return usersRepository.findByChatId(id).orElseGet(() -> {
            Users user = new Users();
            user.setChatId(id);
            user.setUsername(username);
            user.setFullName(name);
            user.setState(State.START);
            return usersRepository.save(user);
        });
    }


    //      Bu yer boshlanishi start bosilishi va shahar tanlanishi
    public static void acceptStart(Users users, Message message, UsersRepository usersRepostory, BotService botService) {
        // Birinchi xabarni yuborish (greeting message - keep this one)
        SendMessage sendMessage = new SendMessage(users.getChatId(), "Assalom aleykum " + message.chat().firstName() + " 🫡 botimizda ko'rib turganimizdan xursandmiz😉");
        try {
            // Birinchi xabarni yuborish va ID sini olish
            SendResponse response = telegramBot.execute(sendMessage);
            Integer greetingMessageId = response.message().messageId();
            users.setGreetingMessageId(greetingMessageId);

            // Eng oxirgi kanal xabarini yuborish
            botService.sendLatestChannelMessageToUser(users.getChatId());

            // Foydalanuvchi topilgan bo'lsa, ikkinchi xabarni yuborish
            usersRepostory.findByChatId(users.getChatId()).ifPresent(user -> {
                // Shahar tanlash xabarini yuborish
                SendMessage citySelectionMessage = new SendMessage(users.getChatId(), "📌 Shahringizni tanlang 👇");
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Toshkent").callbackData("toshkent"),
                        new InlineKeyboardButton("Buxoro").callbackData("buxoro")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Samarqand").callbackData("samarqand"),
                        new InlineKeyboardButton("Navoiy").callbackData("navoiy")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Jizzax").callbackData("jizzax"),
                        new InlineKeyboardButton("Sirdaryo").callbackData("sirdaryo")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Qashqadaryo(Shahrisabz)").callbackData("shahrisabz"),
                        new InlineKeyboardButton("Surxondaryo").callbackData("termiz")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Xorazm(Xiva)").callbackData("xiva"),
                        new InlineKeyboardButton("Qoraqalpog'iston(Nukus)").callbackData("nukus")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Farg'ona").callbackData("fargona"),
                        new InlineKeyboardButton("Andijon").callbackData("andijon")
                );
                inlineKeyboardMarkup.addRow(
                        new InlineKeyboardButton("Namangan").callbackData("namangan")
                );
                citySelectionMessage.replyMarkup(inlineKeyboardMarkup);

                // Shahar tanlash xabarini yuborish va ID sini saqlash
                SendResponse cityMessageResponse = telegramBot.execute(citySelectionMessage);
                Integer cityMessageId = cityMessageResponse.message().messageId();
                users.setCityMessageId(cityMessageId);

                // Foydalanuvchi holatini yangilash
                users.setState(State.SHARING_REGION);
                usersRepostory.save(users);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
