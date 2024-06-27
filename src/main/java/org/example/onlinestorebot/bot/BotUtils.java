package org.example.onlinestorebot.bot;

import com.pengrad.telegrambot.model.request.*;
import lombok.RequiredArgsConstructor;
import org.example.onlinestorebot.entity.BasketProduct;
import org.example.onlinestorebot.entity.Product;
import org.example.onlinestorebot.enums.BotConstant;
import org.example.onlinestorebot.repo.BasketProductRepository;

import java.util.List;

@RequiredArgsConstructor
public class BotUtils {

    private final BasketProductRepository basketProductRepository;

    public static InlineKeyboardMarkup getWelcomeOptionButtons() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("shop \uD83D\uDECD").callbackData(BotConstant.SHOP),
                new InlineKeyboardButton("my cart \uD83D\uDED2").callbackData(BotConstant.CART),
                new InlineKeyboardButton("my orders \uD83D\uDDD2").callbackData(BotConstant.ORDERS)
                );
    }

    public static Keyboard generateContactBtn() {
        KeyboardButton keyboardButton = new KeyboardButton("Share contact");
        keyboardButton.requestContact(true);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                keyboardButton
        );
        replyKeyboardMarkup.resizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static Keyboard generateMainMenuBtn() {
        KeyboardButton keyboardButton = new KeyboardButton("Back to Main Menu");
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                keyboardButton
        );
        replyKeyboardMarkup.resizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static Keyboard generateProductBtns(List<Product> products) {
        InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();
        for (Product product : products) {
            ikm.addRow(new InlineKeyboardButton(product.getName()).callbackData(String.valueOf(product.getId())));
        }
        return ikm;
    }

    public InlineKeyboardMarkup generateCounterBtns(BasketProduct currentBasketProduct) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("Back").callbackData(BotConstant.BACK),
                new InlineKeyboardButton("-").callbackData(BotConstant.MINUS),
                new InlineKeyboardButton(String.valueOf(currentBasketProduct.getAmount())).callbackData(BotConstant.COUNTER),
                new InlineKeyboardButton("+").callbackData(BotConstant.PLUS),
                new InlineKeyboardButton("\uD83D\uDED2").callbackData(BotConstant.ADD_TO_CART)
        );
    }




    public static Keyboard generateCartButtons() {
        KeyboardButton keyboardButton1 = new KeyboardButton("Back");
        KeyboardButton keyboardButton2 = new KeyboardButton("Order");
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                keyboardButton1, keyboardButton2
        );
        replyKeyboardMarkup.resizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static Keyboard generateCartProductButtons(List<BasketProduct> basketProducts) {
        InlineKeyboardMarkup ikm = new InlineKeyboardMarkup();
        for (BasketProduct basketProduct : basketProducts) {
            ikm.addRow( new InlineKeyboardButton(basketProduct.getProduct().getName()).callbackData("a"), new InlineKeyboardButton("❌").callbackData(String.valueOf(basketProduct.getId())));
        }
        ikm.addRow(new InlineKeyboardButton("Back").callbackData(BotConstant.BACK_FROM_CART));
        ikm.addRow(new InlineKeyboardButton("Order").callbackData(BotConstant.MAKE_ORDER));
        return ikm;
    }
}
