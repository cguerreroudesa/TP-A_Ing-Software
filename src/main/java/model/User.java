package model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private Map<String, String> validUsers;
    public static String invalidUsernameOrPassword = "Invalid username or password";
    public static String noExisteTalGiftCard = "No existe tal gift card";
    private Map<String, Integer> giftCards;
    private Map<String, Integer> userGiftCards;


    public User() {
        this.userGiftCards = new HashMap<>();
    }

    public User setUsers(Map<String, String> validUsers){
        this.validUsers = validUsers;
        return this;
    }

    public User setGiftCards(Map<String, Integer> giftCards){
        this.giftCards = giftCards;
        return this;
    }

    public String generateToken(){
        return "123";
    }

    public void redeemGiftCard(String token, String gcId) {
        assertTokenIsValid(token);
        assertGiftCardExists(gcId);
        userGiftCards.put(gcId, giftCards.get(gcId));
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

    public Map<String, Integer> getUserGiftCards() {
        return userGiftCards;
    }
//    usuario y contraseña
//    Consultar saldo
//    Detalles de gastos de la gift card (log)
//    Gastar
}
