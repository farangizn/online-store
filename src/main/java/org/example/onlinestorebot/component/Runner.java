package org.example.onlinestorebot.component;

import lombok.RequiredArgsConstructor;
import org.example.onlinestorebot.entity.Category;
import org.example.onlinestorebot.entity.Product;
import org.example.onlinestorebot.repo.CategoryRepository;
import org.example.onlinestorebot.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Runner implements CommandLineRunner {

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {

        if (ddlAuto.equals("create")) {
            Category category1 = Category.builder().name("Food").build();
            Category category2 = Category.builder().name("Refreshments").build();
            categoryRepository.save(category1);
            categoryRepository.save(category2);

            Product product1 = Product.builder().photo("https://upload.wikimedia.org/wikipedia/commons/a/a4/Fuji_Apple_%284107707252%29.jpg").price(3).category(category1).description("High quality apples nurtured in the heart of Asia. No GMO").name("Apple").build();
            Product product2 = Product.builder().photo("https://images.pexels.com/photos/416528/pexels-photo-416528.jpeg?cs=srgb&dl=pexels-pixabay-416528.jpg&fm=jpg").price(4).category(category2).description("Sparkling water, deeply cleansed and filtered").name("Blanc Bleu").build();
            Product product3 = Product.builder().photo("https://blog-images-1.pharmeasy.in/2020/08/28152823/shutterstock_583745164-1.jpg").price(12).category(category1).description("Juicy and delicious watermelon of a big size grown by the professionals").name("Watermelon").build();
            Product product4 = Product.builder().photo("https://media.istockphoto.com/id/1310458655/photo/black-lumpfish-caviar-in-a-small-pot-and-spoon.jpg?s=612x612&w=0&k=20&c=ROje4Qm8dRoZ-v5i-3THT6mvcHDqV-plzHnrkmlw2qs=").price(24).category(category1).description("Black caviar collected from the fish of the Pacific").name("Caviar").build();

            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);
            productRepository.save(product4);
        }

    }
}
