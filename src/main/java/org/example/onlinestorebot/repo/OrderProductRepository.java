package org.example.onlinestorebot.repo;

import org.example.onlinestorebot.entity.Order;
import org.example.onlinestorebot.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Integer> {
    List<OrderProduct> findOrderProductsByOrder(Order order);

    List<OrderProduct> findByOrder(Order order);
}