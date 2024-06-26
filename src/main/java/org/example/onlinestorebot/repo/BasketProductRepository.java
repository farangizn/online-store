package org.example.onlinestorebot.repo;

import org.example.onlinestorebot.entity.Basket;
import org.example.onlinestorebot.entity.BasketProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BasketProductRepository extends JpaRepository<BasketProduct, Integer> {
    List<BasketProduct> findBasketProductByBasket(Basket basket);
}