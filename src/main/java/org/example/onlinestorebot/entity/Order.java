package org.example.onlinestorebot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.onlinestorebot.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne
    private TelegramUser telegramUser;

    private LocalDateTime date;

//    @ManyToMany
//    private List<Product> products;

    @Enumerated(EnumType.STRING)
    private Status status;

}