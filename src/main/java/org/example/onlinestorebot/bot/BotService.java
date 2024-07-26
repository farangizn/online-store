package org.example.onlinestorebot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.onlinestorebot.entity.*;
import org.example.onlinestorebot.enums.BotConstant;
import org.example.onlinestorebot.enums.Status;
import org.example.onlinestorebot.enums.TelegramState;
import org.example.onlinestorebot.repo.*;
import org.example.onlinestorebot.service.TelegramUserService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotService {

    private final TelegramBot telegramBot;
    private final SimpMessagingTemplate messagingTemplate;

    private final TelegramUserService telegramUserService;

    private final BasketProductRepository basketProductRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final OrderProductRepository orderProductRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final BasketRepository basketRepository;
    private final OrderRepository orderRepository;

    private BasketProduct currentBasketProduct;
    private Product currentProduct;

    public Integer counterMessageId;
    public Integer cartMessageId;


    @Async
    public void handleUpdate(Update update) {
        if (update.message() != null) {

            Message message = update.message();
            Long chatId = message.chat().id();

            TelegramUser telegramUser = telegramUserService.getUser(chatId);
            telegramUserService.currentTelegramUser = telegramUser;

            if (message.text() != null) {
                if (message.text().equals("/start")) {
                    acceptStartAskForContact(telegramUser, message);
                    checkForBasketPresence(telegramUser);
                } else if (message.text().equals(BotConstant.BACK_TO_MAIN_MENU)) {
                    telegramUser.setTelegramState(TelegramState.MAIN_MENU);
                    SendMessage sendMessage = new SendMessage(
                            telegramUser.getChatId(),
                            "Choose an option: "
                    );
                    showMainMenu(sendMessage, telegramUser);
                } else if (telegramUser.checkState(TelegramState.CHOOSE_CART_OPTIONS)) {
                    if (message.text().equals("Back")) {
                        telegramUser.setTelegramState(TelegramState.MAIN_MENU);
                        SendMessage sendMessage = new SendMessage(
                                telegramUser.getChatId(),
                                "Choose an option: "
                        );
                        showMainMenu(sendMessage, telegramUser);
                    } else if (message.text().equals("Order")) {
                        showOrders(telegramUser);
                    }
                }
            } else if (message.contact() != null) {
                if (telegramUser.checkState(TelegramState.SHARE_CONTACT)) {
                    acceptContactShowMainMenu(telegramUser, message);
                }
            }

        } else if (update.callbackQuery() != null) {
            CallbackQuery callbackQuery = update.callbackQuery();
            Long chatId = callbackQuery.from().id();
            TelegramUser telegramUser = telegramUserService.getUser(chatId);

            if (telegramUser.checkState(TelegramState.MAIN_MENU)) {
                if (callbackQuery.data().equals(BotConstant.SHOP)) {
                    showCategories(callbackQuery, telegramUser);
                } else if (callbackQuery.data().equals(BotConstant.ORDERS)) {
                    if (!orderRepository.findOrdersByTelegramUser(telegramUser).isEmpty()) {
                        showOrders(telegramUser);
                    } else {
                        SendMessage sendMessage = new SendMessage(
                                telegramUser.getChatId(),
                                "You have no orders"
                        );
                        telegramBot.execute(sendMessage);
//                        telegramUserRepository.save(telegramUser);
                        telegramUserService.updateState(TelegramState.MAIN_MENU, telegramUser);
                    }
                }  else if (callbackQuery.data().equals(BotConstant.CART)) {
                    if (!basketProductRepository.findBasketProductByBasket(telegramUser.getBasket()).isEmpty()) {
                        showCart(telegramUser);
                    } else {
                        SendMessage sendMessage = new SendMessage(
                                telegramUser.getChatId(),
                                "Your cart is empty"
                        );
                        telegramBot.execute(sendMessage);
                        telegramUserService.updateState(TelegramState.MAIN_MENU, telegramUser);
                    }
                }
            } else if (telegramUser.checkState(TelegramState.CHOOSE_CATEGORY)) {
                for (Category category : categoryRepository.findAll()) {
                    if (callbackQuery.data().equals(String.valueOf(category.getId()))) {
                        showProductsByCategory(category, telegramUser);
                    }
                }
            } else if (telegramUser.checkState(TelegramState.CHOOSE_PRODUCT)) {
                for (Product product : productRepository.findAll()) {
                    if (callbackQuery.data().equals(String.valueOf(product.getId()))) {
                        currentProduct = product;
                        showProductInfoAndChooseAmount(product, telegramUser);
                    }
                }
            } else if (telegramUser.checkState(TelegramState.PRODUCT_OPTIONS)) {
                if (callbackQuery.data().equals(BotConstant.PLUS)) {

                    currentBasketProduct.setAmount(currentBasketProduct.getAmount() + 1);
                    basketProductRepository.save(currentBasketProduct);
                    updateMessageInlineKeyboard(chatId, counterMessageId);

                } else if (callbackQuery.data().equals(BotConstant.MINUS)) {

                    if (currentBasketProduct.getAmount() - 1 >= 0) {
                        currentBasketProduct.setAmount(currentBasketProduct.getAmount() - 1);
                        basketProductRepository.save(currentBasketProduct);
                        updateMessageInlineKeyboard(chatId, counterMessageId);
                    }

                } else if (callbackQuery.data().equals(BotConstant.ADD_TO_CART)) {
                    if (currentBasketProduct.getAmount() > 0) {
                        basketProductRepository.save(currentBasketProduct);
                        SendMessage sendMessage = new SendMessage(
                                telegramUser.getChatId(),
                                "Product (%s) successfully added to your cart".formatted(currentProduct.getName())
                        );
                        sendMessage.replyMarkup(BotUtils.generateMainMenuBtn());
                        telegramUser.setTelegramState(TelegramState.CHOOSE_PRODUCT);
                        showProductsByCategory(currentBasketProduct.getProduct().getCategory(), telegramUser);
                        telegramBot.execute(sendMessage);
                    } else {
                        SendMessage sendMessage = new SendMessage(
                                telegramUser.getChatId(),
                                "Product amount must be at least 1 in order to be added to the cart"
                        );
                        telegramBot.execute(sendMessage);
                    }

                } else if (callbackQuery.data().equals(BotConstant.BACK)) {
                    telegramUser.setTelegramState(TelegramState.CHOOSE_CATEGORY);
                    showCategories(callbackQuery, telegramUser);
                }
            } else if (telegramUser.checkState(TelegramState.CHOOSE_CART_OPTIONS)) {
                String data = callbackQuery.data();
                List<BasketProduct> basketProducts = basketProductRepository.findBasketProductByBasket(telegramUser.getBasket());
                if (callbackQuery.data().equals(BotConstant.BACK_FROM_CART)) {
                    telegramUser.setTelegramState(TelegramState.MAIN_MENU);
                    SendMessage sendMessage = new SendMessage(
                            telegramUser.getChatId(),
                            "Choose an option: "
                    );
                    showMainMenu(sendMessage, telegramUser);
                } else if (callbackQuery.data().equals(BotConstant.MAKE_ORDER)) {
                    createAnOrder(telegramUser);
                } else {
                    for (BasketProduct basketProduct : basketProducts) {
                        if (basketProduct.getId().toString().equals(data)) {
                            basketProductRepository.delete(basketProduct);
                            basketProducts = basketProductRepository.findBasketProductByBasket(telegramUser.getBasket());
                            generateEditCartProductButtons(telegramUser.getChatId(), cartMessageId, basketProducts);

                        }
                    }
//                    telegramUserService.updateState(TelegramState.MAIN_MENU, telegramUser);
//                    showCart(telegramUser);
                }
            }

        }
    }

    private void createAnOrder(TelegramUser telegramUser) {
        List<BasketProduct> basketProducts = basketProductRepository.findBasketProductByBasket(telegramUser.getBasket());
        if (!basketProducts.isEmpty()) {
            Order order = new Order();
            order.setDate(LocalDateTime.now());
            order.setTelegramUser(telegramUser);
            order.setStatus(Status.NEW);
            orderRepository.save(order);
            messagingTemplate.convertAndSend("/topic/farangiz", order);
            for (BasketProduct basketProduct : basketProducts) {
                OrderProduct orderProduct = OrderProduct.builder().order(order).product(basketProduct.getProduct()).amount(basketProduct.getAmount()).build();
                orderProductRepository.save(orderProduct);
            }
            StringBuilder str = new StringBuilder("Your order proceeded successfully ✅\nHere's the info:\n");
            Integer count = 0;
            for (BasketProduct basketProduct : basketProducts) {
                str.append("\n").append(basketProduct.getProduct().getName()).append(": ").append(basketProduct.getProduct().getPrice()).append("$").append(" x ").append(basketProduct.getAmount()).append("\n");
                str.append("------------------------");
                count += basketProduct.getProduct().getPrice() * basketProduct.getAmount();
            }
            str.append("\n\n Overall price: ").append(count).append("$");

            basketProductRepository.deleteAll(basketProducts);

            SendMessage sendMessage = new SendMessage(
                    telegramUser.getChatId(),
                    String.valueOf(str)
            );
            telegramBot.execute(sendMessage);
        } else {
            SendMessage sendMessage = new SendMessage(
                    telegramUser.getChatId(),
                    "You should have at least one product in your cart to order"
            );
            telegramBot.execute(sendMessage);
        }
    }

    private void showCart(TelegramUser telegramUser) {
        StringBuilder str = new StringBuilder("Your cart:\n");
        Basket basket = telegramUser.getBasket();
        List<BasketProduct> basketProducts = basketProductRepository.findBasketProductByBasket(basket);

        double totalPrice = 0;

        for (BasketProduct basketProduct : basketProducts) {
            double productTotalPrice = basketProduct.getAmount() * basketProduct.getProduct().getPrice();
            totalPrice += productTotalPrice;
            str.append(basketProduct.getProduct().getName())
                    .append(": ")
                    .append(basketProduct.getProduct().getPrice())
                    .append("$ x ")
                    .append(basketProduct.getAmount())
                    .append(" = ")
                    .append(productTotalPrice)
                    .append("\n");
        }

        str.append("\nTotal Price: ").append(totalPrice).append("$");

        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                str.toString()
        );

        sendMessage.replyMarkup(BotUtils.generateCartProductButtons(basketProducts));
        telegramUserService.updateState(TelegramState.CHOOSE_CART_OPTIONS, telegramUser);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        cartMessageId = sendResponse.message().messageId();

    }

    private void checkForBasketPresence(TelegramUser telegramUser) {
        if (telegramUser.getBasket() == null) {
            Basket basket = new Basket();
            basketRepository.save(basket);
            telegramUser.setBasket(basket);
            telegramUserRepository.save(telegramUser);
        }
    }

    private void showProductInfoAndChooseAmount(Product product, TelegramUser telegramUser) {
        SendPhoto sendPhoto = new SendPhoto(
                telegramUser.getChatId(),
                product.getPhoto()  // Direct image URL
        );
//                .caption(String.format(
//                "Product: %s\n-----------------------\n%s\nPrice: %s$",
//                product.getName(), product.getDescription(), product.getPrice()
//        ));
        telegramBot.execute(sendPhoto);

        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setBasket(telegramUser.getBasket());
        basketProduct.setProduct(currentProduct);
        currentBasketProduct = basketProduct;
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                """
                        Product: %s
                        -----------------------
                        %s
                        Price: %s$
                        """.formatted(product.getName(), product.getDescription(), product.getPrice())
        );

        BotUtils botUtils = new BotUtils(basketProductRepository);
        sendMessage.replyMarkup(botUtils.generateCounterBtns(currentBasketProduct));

        telegramUserService.updateState(TelegramState.PRODUCT_OPTIONS, telegramUser);
        SendResponse response = telegramBot.execute(sendMessage);
        counterMessageId = response.message().messageId();


    }


    private void showProductsByCategory(Category category, TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                "Choose product: "
        );

        List<Product> products = productRepository.findProductsByCategory(category);
        sendMessage.replyMarkup(BotUtils.generateProductBtns(products));

        telegramUserService.updateState(TelegramState.CHOOSE_PRODUCT, telegramUser);
        telegramBot.execute(sendMessage);
    }

    private void showOrders(TelegramUser telegramUser) {
        String str = "Your orders:\n";
        List<Order> orders = orderRepository.findOrdersByTelegramUser(telegramUser);
        Integer count = 0;
        for (Order order : orders) {
            List<OrderProduct> orderProducts = orderProductRepository.findOrderProductsByOrder(order);
            str += order.getId() + ". " + LocalDateTime.parse(String.valueOf(order.getDate()), DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n\n";
            for (OrderProduct orderProduct : orderProducts) {
                count += orderProduct.getAmount() * orderProduct.getProduct().getPrice();
                str += orderProduct.getProduct().getName() + ": " + orderProduct.getProduct().getPrice() + "$ x " + orderProduct.getAmount() + orderProduct.getAmount() * orderProduct.getProduct().getPrice() + "\n";
            }
            str += "------------------------\n";
            str += "\nOverall price: " + count + "$";
            count = 0;
        }

        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                str
        );
        sendMessage.replyMarkup(BotUtils.generateMainMenuBtn());
        telegramBot.execute(sendMessage);
    }

    private void acceptContactShowMainMenu(TelegramUser telegramUser, Message message) {
        Contact contact = message.contact();
        telegramUser.setPhone(contact.phoneNumber());

        SendMessage sendMessage = new SendMessage(
                message.chat().id(),
                """
                    Credentials were received successfully!
                    What can we do for you today %s?
                    """.formatted(telegramUser.getFirstname())
        );

       showMainMenu(sendMessage, telegramUser);
    }

    public void updateMessageInlineKeyboard(long chatId, int messageId) {
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup(chatId, messageId);
        editMessage.replyMarkup( new InlineKeyboardMarkup(
                new InlineKeyboardButton("Back").callbackData(BotConstant.BACK),
                new InlineKeyboardButton("-").callbackData(BotConstant.MINUS),
                new InlineKeyboardButton(String.valueOf(currentBasketProduct.getAmount())).callbackData(BotConstant.COUNTER),
                new InlineKeyboardButton("+").callbackData(BotConstant.PLUS),
                new InlineKeyboardButton("Add to Cart").callbackData(BotConstant.ADD_TO_CART)
        ));
        try {
            telegramBot.execute(editMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateEditCartProductButtons(long chatId, int messageId, List<BasketProduct> basketProducts) {
        InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();
        for (BasketProduct basketProduct : basketProducts) {
            ikm.addRow(
                    new InlineKeyboardButton(basketProduct.getProduct().getName()).callbackData("a"),
                    new InlineKeyboardButton("❌").callbackData(String.valueOf(basketProduct.getId()))
            );
        }
        ikm.addRow(new InlineKeyboardButton("Back").callbackData(BotConstant.BACK_FROM_CART));
        ikm.addRow(new InlineKeyboardButton("Order").callbackData(BotConstant.MAKE_ORDER));

        telegramBot.execute(new EditMessageReplyMarkup(chatId, messageId).replyMarkup(ikm));
    }

    private void showMainMenu(SendMessage message, TelegramUser telegramUser) {
        message.replyMarkup(BotUtils.getWelcomeOptionButtons());
        telegramUserService.updateState(TelegramState.MAIN_MENU, telegramUser);
        telegramBot.execute(message);

    }

    private void showCategories(CallbackQuery callbackQuery, TelegramUser telegramUser) {
        SendMessage sendMessage = new SendMessage(
                telegramUser.getChatId(),
                "Choose a category: "
        );

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        sendMessage.replyMarkup(inlineKeyboardMarkup);
        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {
            inlineKeyboardMarkup.addRow(
                    new InlineKeyboardButton(category.getName()).callbackData(String.valueOf(category.getId()))
            );
        }
        telegramUserService.updateState(TelegramState.CHOOSE_CATEGORY, telegramUser);
        telegramBot.execute(sendMessage);
    }

    private void acceptStartAskForContact(TelegramUser user, Message message) {
        String firstName = message.from().firstName();
        String lastName = message.from().lastName();
        user.setFirstname(firstName);
        user.setLastname(lastName);
        SendMessage sendMessage = new SendMessage(
                user.getChatId(),
                """
                Assalamu alaykum %s. Welcome to the bot.
                In order to use the bot to the fullest, please press the share contact button
                """.formatted(user.getFirstname())
        );
        sendMessage.replyMarkup(BotUtils.generateContactBtn());
        telegramUserService.updateState(TelegramState.SHARE_CONTACT, user);
        telegramBot.execute(sendMessage);
    }
}
