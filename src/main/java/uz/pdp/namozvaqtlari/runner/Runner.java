package uz.pdp.namozvaqtlari.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import uz.pdp.namozvaqtlari.controller.BotController;
import uz.pdp.namozvaqtlari.repo.UsersRepository;

@Configuration
public class Runner implements CommandLineRunner {
    private final UsersRepository usersRepository;
    private final BotController botController;

    public Runner(UsersRepository usersRepository, BotController botController) {
        this.usersRepository = usersRepository;
        this.botController = botController;
    }

    @Override
    public void run(String... args) throws Exception {
      botController.start();
    }
}
