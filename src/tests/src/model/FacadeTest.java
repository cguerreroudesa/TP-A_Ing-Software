package model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest implements AssertThrowsLike{
    public static final String correctGcId = "mcTastyTripleBacon";
    private static final String merchantId = "McDonalds";

    private static Map<String, String> users = Map.of( "Funny", "Valentine","Johnny","Joestar");
    private static List<String> merchants = Arrays.asList("McDonalds", "Starbucks");


    private Facade facade;
    private String userToken;

    @BeforeEach
    public void setUp() {
        facade = loggedInFacade();
        userToken = facade.getUserSession("Funny").getToken();
    }


    @Test
    public void test01userOrPasswordAreInvalid() {
        assertThrowsLike(() -> facade.login("Funny","InvalidPassword"),
                Facade.invalidUsernameOrPassword);
        assertThrowsLike(() -> facade.login("invalidUser","Valentine"),
                Facade.invalidUsernameOrPassword);
    }

    @Test
    public void test02tokenIsInvalid() {
        assertThrowsLike(() -> facade.redeemGiftCard("Funny","tokenIncorrecto", correctGcId),
                Session.incorrectToken);
    }

    @Test
    public void test03sessionIsActiveAfterLoginIn() {
        assertFalse(facade.getUserSession("Funny").isExpired());
    }

    @Test
    public void test04userRedeemedCardCorrectly() {
        assertTrue(facadeWithRedeemedCard().findGiftCard(correctGcId).isRedeemed());
    }

    @Test
    public void test05userCannotRedeemInvalidCard() {
        assertThrowsLike(
                () -> facade.redeemGiftCard("Funny",userToken,"invalidId"),
                Facade.giftCardNotFound
        );
    }

    @Test
    public void test06buyUsingRedeemedGiftCard() {
        facade.redeemGiftCard("Funny",userToken,correctGcId).checkOut(merchantId,correctGcId, 80);
        assertEquals(20, facade.balanceOf("Funny",userToken, correctGcId));
    }

    @Test
    public void test07cannotBuyUsingAnUnknownMerchant() {
        assertThrowsLike(
                () -> checkedOutFacade( 80, "invalidMerchantId"),
                Facade.merchantNotFound
        );
    }

    @Test
    public void test08notEnoughFunds() {
        assertThrowsLike(
                () -> checkedOutFacade(120, merchantId),
                GiftCard.insufficientBalance
        );
    }


    @Test
    public void test09checkoutIsRecordedAsMovement() {
        List<GiftCardMovements> movements = checkedOutFacade(80, merchantId)
                .movementsOf("Funny",userToken, correctGcId);

        assertEquals(1, movements.size());
        assertEquals(merchantId, movements.getFirst().getCommerce());
        assertEquals(80, movements.getFirst().getExpense());
    }

    @Test
    public void test10checkoutIncorrectlyIsNotRecordedAsMovementAndBalanceDoesNotChange() {
        assertThrowsLike(() -> checkedOutFacade(120, merchantId),GiftCard.insufficientBalance);
        assertEquals(0, facade.movementsOf("Funny",userToken, correctGcId).size());
        assertEquals(100, facade.balanceOf("Funny",userToken, correctGcId));
    }

    @Test
    public void test11loginInMultipleTimesChangesTheTokenAndUpdatesTheSession() {
        Clock myClock = customClock();
        Facade facade = loggedInFacade(myClock);

        Session session = facade.getUserSession("Funny");
        String firstToken = session.getToken();
        LocalDateTime firstCreationTime = session.getTokenCreationTime();

        facade.login("Funny", "Valentine");
        Session secondSession = facade.getUserSession("Funny");
        String secondToken = secondSession.getToken();
        LocalDateTime secondCreationTime = secondSession.getTokenCreationTime();

        assertNotEquals(secondToken, firstToken);
        assertNotEquals(secondCreationTime, firstCreationTime);
    }

    @Test
    public void test12cannotRedeemGiftCardOrCheckGiftCardStatusIfSessionExpired() {
        Clock myClock = customClock();
        Facade expiredSessionFacade = loggedInFacade(myClock);
        expiredSessionFacade.redeemGiftCard("Funny",expiredSessionFacade.getUserSession("Funny").getToken(), correctGcId);
        myClock.advanceMinutes(6);
        assertThrowsLike(()-> expiredSessionFacade.redeemGiftCard("Funny",expiredSessionFacade.getUserSession("Funny").getToken(),"caramelMacchiato"), Session.sessionExpired);
        assertThrowsLike(()-> expiredSessionFacade.balanceOf("Funny",expiredSessionFacade.getUserSession("Funny").getToken(),correctGcId), Session.sessionExpired);
        assertThrowsLike(()-> expiredSessionFacade.movementsOf("Funny",expiredSessionFacade.getUserSession("Funny").getToken(),correctGcId), Session.sessionExpired);
    }

    @Test
    public void test13canBuyEvenIfSessionExpired() {
        Clock myClock = customClock();
        Facade expiredSessionFacade = loggedInFacade(myClock);
        expiredSessionFacade.redeemGiftCard("Funny",expiredSessionFacade.getUserSession("Funny").getToken(), correctGcId);
        myClock.advanceMinutes(6);

        expiredSessionFacade.checkOut( merchantId ,correctGcId,80);
        expiredSessionFacade.login("Funny","Valentine");
        assertEquals(20, expiredSessionFacade.balanceOf("Funny",expiredSessionFacade.getUserSession("Funny").getToken(),correctGcId));
    }

    @Test
    public void test14multipleUsersHaveDifferentSessions() {
        facade.login("Funny","Valentine");
        facade.login("Johnny", "Joestar");

        assertNotEquals(facade.getUserSession("Funny").getToken(), facade.getUserSession("Johnny").getToken());
    }

    @Test
    public void test15UserCanSpendInPersonalGiftCard() {
        facade.login("Funny","Valentine");
        facade.login("Johnny", "Joestar");
        facade.redeemGiftCard("Funny",facade.getUserSession("Funny").getToken(),correctGcId);
        facade.redeemGiftCard("Johnny",facade.getUserSession("Johnny").getToken(),"caramelMacchiato");
        facade.checkOut(merchantId ,correctGcId,80);
        facade.checkOut("Starbucks" ,"caramelMacchiato",70);

        assertNotEquals(facade.balanceOf("Funny",facade.getUserSession("Funny").getToken(),correctGcId),
                facade.balanceOf("Johnny",facade.getUserSession("Johnny").getToken(),"caramelMacchiato"));
    }

    @Test
    public void test16CannotRedeemSameGiftCard() {
        facade.login("Funny","Valentine");
        facade.login("Johnny", "Joestar");
        facade.redeemGiftCard("Funny",facade.getUserSession("Funny").getToken(),correctGcId);
        assertThrowsLike(() ->facade.redeemGiftCard("Johnny",facade.getUserSession("Johnny").getToken(),correctGcId), GiftCard.giftCardRedeemed);
    }

    @Test
    public void test17cannotCheckGiftCardOfAnotherUser() {
        Facade reedemedByFunny = facadeWithRedeemedCard();
        reedemedByFunny.login("Johnny", "Joestar");

        assertThrowsLike(()-> reedemedByFunny.balanceOf("Johnny",userToken,correctGcId), Facade.userDoesNotOwnGiftCard);
        assertThrowsLike(()-> reedemedByFunny.movementsOf("Johnny",userToken,correctGcId),  Facade.userDoesNotOwnGiftCard);
    }




    private Clock customClock(){
        return new Clock() {
            private LocalDateTime current = LocalDateTime.now();

            @Override
            public LocalDateTime now() {
                LocalDateTime toReturn = current;
                current = current.plusMinutes(1);
                return toReturn;
            }

            public void advanceMinutes(Integer minutes) {
                current = current.plusMinutes(minutes);
            }
        };
    }


    private Facade checkedOutFacade(Integer amount, String merchantId) {
        return facadeWithRedeemedCard().checkOut(merchantId, correctGcId, amount);
    }

    private Facade facadeWithRedeemedCard() {
        return facade.redeemGiftCard("Funny",userToken, correctGcId);
    }

    private Facade loggedInFacade() {
        return new Facade(merchants,validGiftCards(),users, new Clock()).login("Funny","Valentine");
    }
    private Facade loggedInFacade(Clock clock) {
        return new Facade(merchants,validGiftCards(),users, clock).login("Funny","Valentine");
    }

    private List<GiftCard> validGiftCards() {
        return List.of(new GiftCard(correctGcId, 100),new GiftCard("caramelMacchiato", 100));
    }
}

