package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest implements AssertThrowsLike{
    public static String CARD1_ID = "caramelMacchiato";
    public static String CARD2_ID = "mcTastyTripleBacon";

    private static String MERCHANT_ID_R1 = "Restaurant1";
    public static final String MERCHANT_ID_R2 = "Restaurant2";

    public static String USER_FUNNY = "Funny";
    public static String PASS_FUNNY = "Valentine";
    public static String USER_JOHNNY = "Johnny";
    public static String PASS_JOHNNY = "Joestar";

    private static Map<String, String> users = Map.of(USER_FUNNY, PASS_FUNNY, USER_JOHNNY, PASS_JOHNNY);
    private static List<String> merchants = List.of(MERCHANT_ID_R1, MERCHANT_ID_R2);


    private Facade facadeFunnyLogged;
    private String funnyToken;

    @BeforeEach
    public void setUp() {
        facadeFunnyLogged = new Facade(merchants,validGiftCards(),users, new Clock());
        funnyToken = facadeFunnyLogged.loginAndGetToken(USER_FUNNY, PASS_FUNNY);
    }


    @Test
    public void test01userOrPasswordAreInvalid() {
        Facade cleanFacade = new  Facade(merchants,validGiftCards(),users, new Clock());
        assertThrowsLike(() -> cleanFacade.login(USER_FUNNY,"invalidPassword"),
                Facade.invalidUsernameOrPassword);
        assertThrowsLike(() -> cleanFacade.login("invalidUser", PASS_FUNNY),
                Facade.invalidUsernameOrPassword);
    }

    @Test
    public void test02tokenIsInvalid() {
        assertThrowsLike(() -> facadeFunnyLogged.redeemGiftCard(USER_FUNNY,"invalidToken", CARD2_ID),
                Session.incorrectToken);
    }

    @Test
    public void test03sessionIsActiveAfterLoginIn() {
        assertFalse(facadeFunnyLogged.getUserSession(USER_FUNNY).isExpired());
    }

    @Test
    public void test04userRedeemedCardCorrectly() {
        assertTrue(funnyFacadeRedeemedCar1().isCardRedeemed(CARD2_ID));
    }

    @Test
    public void test05SameUserCanRedeemMultipleGiftCards() {
        assertTrue(funnyFacadeRedeemedCar1()
                .redeemGiftCard(USER_FUNNY, funnyToken , CARD1_ID)
                .isCardRedeemed(CARD1_ID));
    }

    @Test
    public void test06userCannotRedeemInvalidCard() {
        assertThrowsLike(
                () -> facadeFunnyLogged.redeemGiftCard(USER_FUNNY, funnyToken,"invalidCardId"),
                Facade.giftCardNotFound
        );
    }

    @Test
    public void test07DifferentUserCannotRedeemAnAlreadyRedeemedCard(){
        String johnnyToken = getJohnnyToken();

        assertThrowsLike(
                () -> facadeFunnyLogged.redeemGiftCard(USER_JOHNNY, johnnyToken , CARD2_ID),
                Facade.giftCardRedeemed);
    }

    @Test
    public void test08buyUsingRedeemedGiftCard() {
        funnyFacadeRedeemedCar1().checkOut(MERCHANT_ID_R1, CARD2_ID, 80);
        assertEquals(20, facadeFunnyLogged.balanceOf(USER_FUNNY, funnyToken, CARD2_ID));
    }

    @Test
    public void test09cannotBuyUsingAnUnknownMerchant() {
        assertThrowsLike(
                () -> facadeFunnyLogged.checkOut("invalidMerchantId", CARD2_ID, 80),
                Facade.merchantNotFound
        );
    }

    @Test
    public void test10notEnoughFunds() {
        assertThrowsLike(
                () -> funnyFacadeRedeemedCar1().checkOut(MERCHANT_ID_R1, CARD2_ID, 120),
                GiftCard.insufficientBalance
        );
    }

    @Test
    public void test11checkoutIsRecordedAsMovement() {
        funnyFacadeRedeemedCar1().checkOut(MERCHANT_ID_R1, CARD2_ID, 80);
        assertSingleMovement(facadeFunnyLogged.movementsOf(USER_FUNNY, funnyToken, CARD2_ID), 80, MERCHANT_ID_R1);
    }


    @Test
    public void test12checkoutIncorrectlyIsNotRecordedAsMovementAndBalanceDoesNotChange() {
        Facade facade = funnyFacadeRedeemedCar1();
        assertThrowsLike(() ->facade.checkOut(MERCHANT_ID_R1, CARD2_ID, 120)
                ,GiftCard.insufficientBalance);

        assertEquals(0, facade.movementsOf(USER_FUNNY,funnyToken, CARD2_ID).size());
        assertEquals(100, facade.balanceOf(USER_FUNNY,funnyToken, CARD2_ID));
    }


    @Test
    public void test13loginInMultipleTimesChangesTheTokenAndUpdatesTheSession() {
        Clock myClock = customClock();
        Facade facade = facadeWithClock(myClock);

        String firstToken = facade.loginAndGetToken(USER_FUNNY, PASS_FUNNY);
        LocalDateTime firstCreationTime = facade.getTokenCreationTimeOf(USER_FUNNY);

        myClock.advanceMinutes(1);

        String secondToken = facade.loginAndGetToken(USER_FUNNY, PASS_FUNNY);
        LocalDateTime secondCreationTime = facade.getTokenCreationTimeOf(USER_FUNNY);

        assertNotEquals(secondToken, firstToken);
        assertNotEquals(secondCreationTime, firstCreationTime);
    }

    @Test
    public void test14cannotRedeemGiftCardOrCheckGiftCardStatusIfSessionExpired() {
        Facade expiredSessionFacade = expiredFunnySessionRedeemedCard1();

        assertThrowsLike(()-> expiredSessionFacade.redeemGiftCard(USER_FUNNY, funnyToken, CARD1_ID), Session.sessionExpired);
        assertThrowsLike(()-> expiredSessionFacade.balanceOf(USER_FUNNY, funnyToken, CARD2_ID), Session.sessionExpired);
        assertThrowsLike(()-> expiredSessionFacade.movementsOf(USER_FUNNY, funnyToken, CARD2_ID), Session.sessionExpired);
    }

    @Test
    public void test15canBuyEvenIfSessionExpired() {
        Facade expiredSessionFacade = expiredFunnySessionRedeemedCard1();

        expiredSessionFacade.checkOut(MERCHANT_ID_R1, CARD2_ID,80);

        String secondFunnyToken = expiredSessionFacade.loginAndGetToken(USER_FUNNY, PASS_FUNNY);
        assertEquals(20, expiredSessionFacade.balanceOf(USER_FUNNY,secondFunnyToken, CARD2_ID));
    }

    @Test
    public void test16multipleUsersHaveDifferentSessions() {
        facadeFunnyLogged.login(USER_FUNNY, PASS_FUNNY).login(USER_JOHNNY, PASS_JOHNNY);

        assertNotEquals(facadeFunnyLogged.getUserTokenSession(USER_FUNNY), facadeFunnyLogged.getUserTokenSession(USER_JOHNNY));
    }

    @Test
    public void test17UserCanSpendInPersonalGiftCard() {
        String johnnyToken = getJohnnyToken();
        facadeFunnyLogged.redeemGiftCard(USER_JOHNNY, johnnyToken, CARD1_ID);

        facadeFunnyLogged.checkOut(MERCHANT_ID_R1, CARD2_ID,80);
        facadeFunnyLogged.checkOut(MERCHANT_ID_R2, CARD1_ID,70);

        assertNotEquals(facadeFunnyLogged.balanceOf(USER_FUNNY, funnyToken, CARD2_ID),
                facadeFunnyLogged.balanceOf(USER_JOHNNY, johnnyToken, CARD1_ID));
    }

    @Test
    public void test18CannotRedeemSameGiftCard() {
        String johnnyToken = getJohnnyToken();

        assertThrowsLike(() -> facadeFunnyLogged.redeemGiftCard(USER_JOHNNY, johnnyToken, CARD2_ID), GiftCard.giftCardRedeemed);
    }

    @Test
    public void test19cannotCheckGiftCardOfAnotherUser() {
        String johnnyToken = getJohnnyToken();

        assertThrowsLike(()-> facadeFunnyLogged.balanceOf(USER_JOHNNY, johnnyToken, CARD2_ID), Facade.userDoesNotOwnGiftCard);
        assertThrowsLike(()-> facadeFunnyLogged.movementsOf(USER_JOHNNY, johnnyToken, CARD2_ID),  Facade.userDoesNotOwnGiftCard);
    }

    private Clock customClock(){
        return new Clock() {
            private LocalDateTime current = LocalDateTime.now();

            @Override
            public LocalDateTime now() {
                return current;
            }

            public Clock advanceMinutes(Integer minutes) {
                current = current.plusMinutes(minutes);
                return this;
            }
        };
    }


    private Facade facadeWithClock(Clock clock) {
        return new Facade(merchants,validGiftCards(),users, clock);
    }

    private List<GiftCard> validGiftCards() {
        return List.of(new GiftCard(CARD2_ID, 100),new GiftCard(CARD1_ID, 100));
    }

    private Facade funnyFacadeRedeemedCar1() {
        return facadeFunnyLogged.redeemGiftCard(USER_FUNNY, funnyToken, CARD2_ID);
    }

    private void assertSingleMovement(List<GiftCardMovements> movements, Integer amount, String merchantId) {
        assertEquals(1, movements.size());
        assertEquals(amount, movements.getFirst().getExpense());
        assertEquals(merchantId, movements.getFirst().getCommerce());
    }

    private String getJohnnyToken() {
        return funnyFacadeRedeemedCar1().loginAndGetToken(USER_JOHNNY, PASS_JOHNNY);
    }

    private Facade expiredFunnySessionRedeemedCard1() {
        Clock myClock = customClock();
        Facade facadeWithClock = facadeWithClock(myClock);

        String funnyToken = facadeWithClock.loginAndGetToken(USER_FUNNY, PASS_FUNNY);
        facadeWithClock.redeemGiftCard(USER_FUNNY,funnyToken, CARD2_ID);

        myClock.advanceMinutes(6);

        return facadeWithClock;
    }

}

