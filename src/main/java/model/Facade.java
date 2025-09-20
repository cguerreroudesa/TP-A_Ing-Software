package model;

import java.util.*;

public class Facade {
    public static String userDoesNotOwnGiftCard = "Gift Card is not owned by user";
    public static String merchantNotFound = "Merchant not found";
    public static String giftCardNotRedeemed = "Giftcard not redeemed yet";
    public static String invalidUsernameOrPassword = "Invalid username or password";
    public static String giftCardNotFound = "Gift card not found";

    private List<GiftCard> giftCards;
    private Map<String, String> validUsers;
    private List<String> merchants;
    private Map<String,Session> sessions;
    private Clock clock;

    public Facade(List<String> merchants, List<GiftCard> giftCards, Map<String, String> validUsers, Clock clock) {
        this.merchants = merchants;
        this.giftCards = giftCards;
        this.validUsers = validUsers;
        this.clock = clock;
        this.sessions = new HashMap<>();
    }

    public Session generateSession(){
        return new Session(clock);
    }

    public Facade login(String username, String password){
        checkValidUser(username, password);
        this.sessions.put(username,generateSession());
        return this;
    }

    public Facade redeemGiftCard(String username,String token, String gcId) {
        validateSession(username, token);
        findGiftCard(gcId).setOwner(username).redeem();

        return this;
    }


    public Facade checkOut(String merchantId, String gcId, int amount){
        assertMerchantExists(merchantId);
        findGiftCard(gcId).decreaseBalance(amount).logMovement(amount,clock.now(),merchantId);
        return this;
    }


    public int balanceOf(String username, String token, String gcId) {
        validatedUserGiftCard(username,token, gcId);
        return findGiftCard(gcId).getBalance();
    }

    public List<GiftCardMovements> movementsOf(String username, String token, String gcId){
        validatedUserGiftCard(username,token, gcId);
        return findGiftCard(gcId).getLogGiftCardMovements();
    }

    private void assertMerchantExists(String merchantId) {
        if(!merchants.contains(merchantId)){
            throw new RuntimeException(merchantNotFound);
        }
    }

    public GiftCard findGiftCard(String gcId) {
        return giftCards.stream()
                .filter(giftCard -> giftCard.getId().equals(gcId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(giftCardNotFound));
    }

    private void checkValidUser(String user, String pass ) {
        if ( !pass.equals( validUsers.get( user ) ) ) {
            throw new RuntimeException(invalidUsernameOrPassword);
        }
    }

    private void assertUserOwnsTheGiftCard(String username, String gcId) {
        if (!Objects.equals(findGiftCard(gcId).getOwner(), username)){
            throw new RuntimeException(userDoesNotOwnGiftCard);
        }
    }

    private void validatedUserGiftCard(String username,String token, String gcId) {
        assertUserOwnsTheGiftCard(username, gcId);
        validateSession(username, token);
    }

    private void validateSession(String username, String token) {
        sessions.get(username).validateExpiration();
        sessions.get(username).validateToken(token);
    }

    public Session getUserSession(String username) {
        return this.sessions.get(username);
    }
//    usuario y contraseña
//    Consultar saldo
//    Detalles de gastos de la gift card (log)
//    Gastar
}
