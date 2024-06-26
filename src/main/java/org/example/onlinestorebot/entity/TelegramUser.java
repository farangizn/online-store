package org.example.onlinestorebot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.onlinestorebot.enums.BotConstant;
import org.example.onlinestorebot.enums.TelegramState;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Data
@Table(name = "telegram_user")
public class TelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    private Long chatId;

    @Enumerated(EnumType.STRING)
    private TelegramState telegramState = TelegramState.START;

    private String phone;
    private String firstname;
    private String lastname;
    private Integer selectedCategoryId;

    @OneToOne
    private Basket basket;


    public boolean checkState(TelegramState telegramState) {
        if (this.telegramState != null) {
            return this.telegramState.equals(telegramState);
        }
        return false;
    }



}
