package uz.pdp.namozvaqtlari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NamozVaqtlariApplication {

    public static void main(String[] args) {
        SpringApplication.run(NamozVaqtlariApplication.class, args);
    }

}
