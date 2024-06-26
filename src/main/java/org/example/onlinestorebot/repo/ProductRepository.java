package org.example.onlinestorebot.repo;

import org.example.onlinestorebot.entity.Category;
import org.example.onlinestorebot.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findProductsByCategory(Category category);
}