package org.example.onlinestorebot.repo;

import org.example.onlinestorebot.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Integer> {
    Optional<TelegramUser> findTelegramUserByChatId(Long chatId);
}