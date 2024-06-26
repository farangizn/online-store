package org.example.onlinestorebot.controller;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.onlinestorebot.entity.Order;
import org.example.onlinestorebot.entity.OrderProduct;
import org.example.onlinestorebot.entity.TelegramUser;
import org.example.onlinestorebot.enums.Status;
import org.example.onlinestorebot.repo.OrderProductRepository;
import org.example.onlinestorebot.repo.OrderRepository;
import org.example.onlinestorebot.repo.ProductRepository;
import org.example.onlinestorebot.service.TelegramUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final TelegramBot telegramBot;
    private final TelegramUserService telegramUserService;

    @GetMapping
    public String get(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        for (Order order : orderRepository.findAll()) {
            System.out.println(order.toString());
        }
        model.addAttribute("orderProducts", orderProductRepository.findAll());
        model.addAttribute("products", productRepository.findAll());

        return "index";
    }


    @GetMapping("/move/right/{orderId}")
    public String moveRight(@PathVariable Integer orderId) {

        System.out.println("hey");

        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            if (order.getStatus().equals(Status.NEW)) {
                order.setStatus(Status.IN_PROGRESS);
                orderRepository.save(order);
                notifyTheUserOnTelegram(order);
            } else if (order.getStatus().equals(Status.IN_PROGRESS)) {
                order.setStatus(Status.COMPLETED);
                orderRepository.save(order);
                notifyTheUserOnTelegram(order);
            }
        }
        return "redirect:/orders";
    }

    @GetMapping("/move/left/{orderId}")
    public String moveLeft(@PathVariable Integer orderId) {

        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            if (order.getStatus().equals(Status.IN_PROGRESS)) {
                order.setStatus(Status.NEW);
                orderRepository.save(order);
                notifyTheUserOnTelegram(order);
            } else if (order.getStatus().equals(Status.COMPLETED)) {
                order.setStatus(Status.IN_PROGRESS);
                orderRepository.save(order);
                notifyTheUserOnTelegram(order);
            }
        }

        return "redirect:/orders";
    }

    @GetMapping("/info/{orderId}")
    public String info(Model model, @PathVariable Integer orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            List<OrderProduct> orderProducts = orderProductRepository.findByOrder(order);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = order.getDate().format(formatter);

            model.addAttribute("order", order);
            model.addAttribute("orderProducts", orderProducts);
            model.addAttribute("formattedDate", formattedDate);
        }
        return "info";
    }

    public void notifyTheUserOnTelegram(Order order) {
        telegramBot.execute(new SendMessage(
                order.getTelegramUser().getChatId(),
                """
                    Your order's status has been updated!
                    not it's %s
                    """.formatted(order.getStatus().toString().toUpperCase())
        ));
    }
}
