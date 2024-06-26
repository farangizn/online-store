package org.example.onlinestorebot;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OnlinestorebotApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlinestorebotApplication.class, args);
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot("6713933529:AAFVJ6kWBlrH3nA_KJS9k7qLXRaXdEu8hak");
    }
}
