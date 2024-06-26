package org.example.onlinestorebot.service;

import lombok.RequiredArgsConstructor;
import org.example.onlinestorebot.entity.TelegramUser;
import org.example.onlinestorebot.enums.TelegramState;
import org.example.onlinestorebot.repo.TelegramUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;
    public TelegramUser currentTelegramUser;

    public TelegramUser getUser(Long chatId) {
        Optional<TelegramUser> tgUserOpt = telegramUserRepository.findTelegramUserByChatId(chatId);

        if (tgUserOpt.isPresent()) {
            return tgUserOpt.get();
        } else {
            TelegramUser telegramUser = TelegramUser.builder().chatId(chatId).build();
            telegramUserRepository.save(telegramUser);
            return telegramUser;
        }

    }

    public void updateState(TelegramState telegramState, TelegramUser telegramUser) {
        telegramUser.setTelegramState(telegramState);
        telegramUserRepository.save(telegramUser);
    }


}
