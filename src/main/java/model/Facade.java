package model;

import java.util.*;

public class Facade {
    public static String merchantNotFound = "Merchant not found";
    public static String giftCardNotRedeemed = "Giftcard not redeemed yet";
    public static String invalidUsernameOrPassword = "Invalid username or password";
    public static String giftCardNotFound = "Gift card not found";

    private List<GiftCard> giftCards;
    private Map<String, String> validUsers;
    private List<GiftCard> userGiftCards;
    private List<String> merchants;

    private Session session;
    private String token;
    private Clock clock;

    public Facade(List<String> merchants, List<GiftCard> giftCards, Map<String, String> validUsers, Clock clock) {
        this.userGiftCards = new ArrayList<>();
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

        userGiftCards.add(findGiftCard(gcId).redeem());
        return this;
    }


    //Pongo esto en vez de buy porque para la compra no hace falta el token y eso. Solo el id de merchant y giftcard
    public Facade merchantCharge(String merchantId, String gcId, int amount){
        assertMerchantExists(merchantId);
        session.ensureValid(token);
        findUserGiftCard(gcId).decreaseBalance(amount).logMovement(amount,clock.now(),merchantId);
        return this;
    }


    public int balanceOf(String token, String gcId) {
        return validatedUserGiftCard(token, gcId).getBalance();
    }

    public List<GiftCardMovements> movementsOf(String token, String gcId){
        return validatedUserGiftCard(token, gcId).getLogGiftCardMovements();
    }

    private void assertMerchantExists(String merchantId) {
        if(!merchants.contains(merchantId)){
            throw new RuntimeException(merchantNotFound);
        }
    }


    private GiftCard findGiftCard(String gcId) {
        return obtainFrom(giftCards, gcId, giftCardNotFound);
    }

    private GiftCard findUserGiftCard(String gcId) {
        return obtainFrom(userGiftCards, gcId, giftCardNotRedeemed);
    }

    private GiftCard obtainFrom(List<GiftCard> source, String gcId, String errorMessage) {
        return source.stream()
                .filter(giftCard -> giftCard.getId().equals(gcId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }


    private void checkValidUser(String user, String pass ) {
        if ( !pass.equals( validUsers.get( user ) ) ) {
            throw new RuntimeException(invalidUsernameOrPassword);
        }
    }

    private GiftCard validatedUserGiftCard(String token, String gcId) {
        session.ensureValid(token);
        return findUserGiftCard(gcId);
    }

    public List<GiftCard> getUserGiftCards() {
        return userGiftCards;
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
