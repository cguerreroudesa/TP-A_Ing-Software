package model;

import java.util.HashMap;
import java.util.Map;

public class User {
    public static String notEnoughMoneyToBuy = "La compra excede el saldo de la tarjeta";
    public static String giftCardNotreedemed = "La Gift Card no fue redimida";
    public static String invalidUsernameOrPassword = "Invalid username or password";
    public static String noExisteTalGiftCard = "No existe tal gift card";
    private Map<String, Integer> giftCards;
    private Map<String, Integer> userGiftCards;
    private Map<String, GiftCardMovements> logGiftCardMovements;
    private Map<String, String> validUsers;

    public User() {
        this.userGiftCards = new HashMap<>();
    }

    public String generateToken(){
        return "123";
    }

    public void redeemGiftCard(String token, String gcId) {
        assertTokenIsValid(token);
        assertGiftCardExists(gcId);
        userGiftCards.put(gcId, giftCards.get(gcId));
    }

    public void buyshi(String gcId, int price){
        assertGiftCardRedeemed(gcId);
        Integer balance = userGiftCards.get(gcId);
        Integer total = balance - price;
        assertEnoughMoneyToBuy(total);
        userGiftCards.put(gcId, total);
    }

    private static void assertEnoughMoneyToBuy(Integer total) {
        if (total < 0){
            throw new RuntimeException(notEnoughMoneyToBuy);
        }
    }

    private void assertGiftCardRedeemed(String gcId) {
        if ( !userGiftCards.containsKey(gcId) ) {
            throw new RuntimeException(giftCardNotreedemed);
        }
    }
    
    private void assertGiftCardExists(String gcId) {
        if ( !giftCards.containsKey(gcId) ) {
            throw new RuntimeException(noExisteTalGiftCard);
        }
    }

    private void assertTokenIsValid(String token) {
        if ( 1 == 0) {
            throw new RuntimeException(invalidUsernameOrPassword);
        }
    }


    private void checkValidUser(String user, String pass ) {
        if ( !pass.equals( validUsers.get( user ) ) ) {
            throw new RuntimeException(invalidUsernameOrPassword);
        }
    }

    public User setUsers(Map<String, String> validUsers){
        this.validUsers = validUsers;
        return this;
    }

    public User setGiftCards(Map<String, Integer> giftCards){
        this.giftCards = giftCards;
        return this;
    }

    public Map<String, Integer> getUserGiftCards() {
        return userGiftCards;
    }
//    usuario y contraseña
//    Consultar saldo
//    Detalles de gastos de la gift card (log)
//    Gastar
}
