package org.example.onlinestorebot.repo;

import org.example.onlinestorebot.entity.Order;
import org.example.onlinestorebot.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findOrdersByTelegramUser(TelegramUser telegramUser);
}