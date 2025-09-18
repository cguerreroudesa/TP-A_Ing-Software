package model;

import java.util.*;

public class Facade {
    public static String merchantNotFound = "Merchant not found";
    public static String giftCardNotRedeemed = "Giftcard not redeemed yet";
    public static String invalidUsernameOrPassword = "Invalid username or password";
    public static String giftCardNotFound = "Gift card not found";
    public static String incorrectToken = "Incorrect token";

    private Map<String, GiftCard> giftCards;
    private Map<String, String> validUsers;
    private Map<String, List<GiftCardMovements>> logGiftCardMovements;
    private Map<String, GiftCard> userGiftCards;
    private List<String> merchants;

    private Session session;
    private String token;
    private Clock clock;

    public Facade(List<String> merchants, Map<String, GiftCard> giftCards, Map<String, String> validUsers, Clock clock) {
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
        session.ensureValid(token);

        userGiftCards.put(gcId, requireGiftCard(gcId).redeem());
        logGiftCardMovements.put(gcId, new ArrayList<>());

        return this;
    }


    //Pongo esto en vez de buy porque para la compra no hace falta el token y eso. Solo el id de merchant y giftcard
    public Facade merchantCharge(String merchantId, String gcId, int amount){
        assertMerchantExists(merchantId);
        requireUserGiftCard(gcId).decreaseBalance(amount);

        logGiftCardMovements.get(gcId).add(new GiftCardMovements(amount, clock.now(), merchantId));
        return this;
    }


    public int balanceOf(String token, String gcId) {
        return validatedUserGiftCard(token, gcId).getBalance();
    }

    public List<GiftCardMovements> movementsOf(String token, String gcId){
        validatedUserGiftCard(token, gcId);
        return logGiftCardMovements.get(gcId);
    }

    private void assertMerchantExists(String merchantId) {
        if(!merchants.contains(merchantId)){
            throw new RuntimeException(merchantNotFound);
        }
    }

    private void assertGiftCardRedeemed(String gcId) {
        if ( !userGiftCards.containsKey(gcId) ) {
            throw new RuntimeException(giftCardNotRedeemed);
        }
    }

    private void assertTokenIsValid(String token) {
        session.ensureValid(token);
    }

    private GiftCard requireGiftCard(String gcId) {
        return obtainFrom(giftCards, gcId, giftCardNotFound);
    }

    private GiftCard requireUserGiftCard(String gcId) {
        return obtainFrom(userGiftCards, gcId, giftCardNotRedeemed);
    }

    private GiftCard obtainFrom(Map<String, GiftCard> source, String gcId, String errorMessage) {
        GiftCard gc = source.get(gcId);
        if (gc == null) {
            throw new RuntimeException(errorMessage);
        }
        return gc;
    }


    private void checkValidUser(String user, String pass ) {
        if ( !pass.equals( validUsers.get( user ) ) ) {
            throw new RuntimeException(invalidUsernameOrPassword);
        }
    }

    private GiftCard validatedUserGiftCard(String token, String gcId) {
        session.ensureValid(token);
        return requireUserGiftCard(gcId);
    }

    public Map<String, GiftCard> getUserGiftCards() {
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
