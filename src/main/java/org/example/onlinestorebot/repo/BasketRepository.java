package org.example.onlinestorebot.repo;

import org.example.onlinestorebot.entity.Basket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BasketRepository extends JpaRepository<Basket, Integer> {

}