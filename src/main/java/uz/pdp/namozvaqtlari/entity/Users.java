package uz.pdp.namozvaqtlari.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static uz.pdp.namozvaqtlari.entity.State.SHARING_REGION;
import static uz.pdp.namozvaqtlari.entity.State.START;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data

public class Users {
    @Id
    private Long chatId;

    private String fullName;

    private String username;

    private String city;

    private Boolean dailyReminder;

    private Integer missedPrayers;
    @Enumerated(EnumType.STRING)
    private State state=START;

    private Integer greetingMessageId;
    private Integer cityMessageId;
    private Integer cityAcceptedMessageId;
    private Integer reminderMessageId;
    private Integer reminderResponseMessageId;
    private Integer prayerTimesMessageId;
    private Integer menuMessageId;
    private Integer qazoMessageId;
    private Integer countMessageId;
    private Integer settingsMessageId;
}
