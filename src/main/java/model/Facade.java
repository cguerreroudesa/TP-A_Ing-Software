package model;

import java.util.*;

public class Facade {
    public static String merchantNotFound = "Merchant not found";
    public static String incorrectToken = "Token incorrecto";
    public static String notEnoughMoneyToBuy = "La compra excede el saldo de la tarjeta";
    public static String giftCardNotreedemed = "La Gift Card no fue redimida";
    public static String invalidUsernameOrPassword = "Invalid username or password";
    public static String giftCardNotFound = "No existe tal gift card";
    private Map<String, Integer> giftCards;
    private Map<String, String> validUsers;
    private List<String> merchants;
    private Map<String, List<GiftCardMovements>> logGiftCardMovements;
    private Map<String, Integer> userGiftCards;
    private Session session;
    private String token;
    private Clock clock;

    public Facade(List<String> merchants, Map<String, Integer> giftCards, Map<String, String> validUsers, Clock clock) {
        this.userGiftCards = new HashMap<>();
        this.logGiftCardMovements = new HashMap<>();
        this.merchants = merchants;
        this.giftCards = giftCards;
        this.validUsers = validUsers;
        this.clock = clock;
    }

    public Session generateSession(){
        return new Session(clock);
    }

    public Facade login(String username, String password){
        checkValidUser(username, password);
        this.session = generateSession();
        this.token = this.session.getToken();
        return this;
    }

    public Facade redeemGiftCard(String token, String gcId) {
        assertTokenIsValid(token);
        assertGiftCardExists(gcId);
        userGiftCards.put(gcId, giftCards.get(gcId));
        logGiftCardMovements.put(gcId, new ArrayList<>());
        return this;
    }

    public void buyProduct(String gcId, int price, String merchantId, String token){
        assertGiftCardRedeemed(gcId);
        assertTokenIsValid(token);
        assertMerchanExists(merchantId);
        Integer finalBalance = userGiftCards.get(gcId) - price;
        assertEnoughMoneyToBuy(finalBalance);
        userGiftCards.put(gcId, finalBalance);
        logGiftCardMovements.get(gcId).add(new GiftCardMovements(price,clock.now(),merchantId));
    }

    private void assertMerchanExists(String merchantId) {
        if(!merchants.contains(merchantId)){
            throw new RuntimeException(merchantNotFound);
        }
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
            throw new RuntimeException(giftCardNotFound);
        }
    }

    private void assertTokenIsValid(String token) {
        if (!(Objects.equals(token, this.token))) {
            throw new RuntimeException(incorrectToken);
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

    public Map<String, List<GiftCardMovements>> getLogGiftCardMovements() {
        return logGiftCardMovements;
    }


    public String getToken() {
        return this.token;
    }

    public Session getSession() {
        return this.session;
    }

    public Clock getClock() {
        return this.clock;
    }
//    usuario y contraseña
//    Consultar saldo
//    Detalles de gastos de la gift card (log)
//    Gastar
}
