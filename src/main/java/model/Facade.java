package model;

import java.time.LocalDateTime;
import java.util.*;

public class Facade {
    public static String userDoesNotOwnGiftCard = "Gift Card is not owned by user";
    public static String merchantNotFound = "Merchant not found";
    public static String giftCardNotRedeemed = "Gift card not redeemed";
    public static String giftCardRedeemed = "This gift card has already been redeemed";
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

    public String loginAndGetToken(String username, String password){
        checkValidUser(username, password);
        Session s = generateSession();
        this.sessions.put(username,s);
        return s.getToken();
    }


    public Facade redeemGiftCard(String username,String token, String gcId) {
        validateSession(username, token);
        findGiftCard(gcId).redeemFor(username);
        return this;
    }


    public void checkOut(String merchantId, String gcId, int amount){
        assertMerchantExists(merchantId);
        GiftCard card = findGiftCard(gcId);

        if(!card.isRedeemed()) throw new RuntimeException(giftCardNotRedeemed);

        card.spend(amount,clock.now(),merchantId);
    }


    public int balanceOf(String username, String token, String gcId) {
        return getOwnedCard(username,token, gcId).getBalance();
    }

    public List<GiftCardMovements> movementsOf(String username, String token, String gcId){
        return getOwnedCard(username,token, gcId).getLogGiftCardMovements();
    }

    public LocalDateTime getTokenCreationTimeOf(String username){
        return getUserSession(username).getTokenCreationTime();
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

    public boolean isCardRedeemed(String cardId) {
        return findGiftCard(cardId).isRedeemed();
    }

    private void checkValidUser(String user, String pass ) {
        if ( !pass.equals( validUsers.get( user ) ) ) {
            throw new RuntimeException(invalidUsernameOrPassword);
        }
    }

    private GiftCard assertUserOwnsTheGiftCard(String username, String gcId) {
        GiftCard card = findGiftCard(gcId);
        if (!card.isRedeemed()) throw new RuntimeException(giftCardNotRedeemed);
        if (!Objects.equals(card.getOwner(), username)) throw new RuntimeException(userDoesNotOwnGiftCard);
        return card;
    }

    private GiftCard getOwnedCard(String username, String token, String gcId) {
        validateSession(username, token);
        return assertUserOwnsTheGiftCard(username, gcId);
    }


    private void validateSession(String username, String token) {
        Session userSession = sessions.get(username);
        if (userSession == null) throw new RuntimeException(invalidUsernameOrPassword);
        userSession.validateExpiration();
        userSession.validateToken(token);
    }

    public Session getUserSession(String username) {
        return this.sessions.get(username);
    }

    public String getUserTokenSession(String username) {
        return this.sessions.get(username).getToken();
    }

}
