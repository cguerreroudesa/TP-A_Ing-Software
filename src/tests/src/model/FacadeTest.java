package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest implements AssertThrowsLike{
    public static String cardD1Id = "caramelMacchiato";
    public static String cardD2Id = "mcTastyTripleBacon";

    private static String merchantIdR1 = "Restaurant1";
    public static final String merchantIdR2 = "Restaurant2";

    public static String userFunny = "Funny";
    public static String passFunny = "Valentine";
    public static String userJohnny = "Johnny";
    public static String passJohnny = "Joestar";

    private static Map<String, String> users = Map.of(userFunny, passFunny, userJohnny, passJohnny);
    private static List<String> merchants = List.of(merchantIdR1, merchantIdR2);


    private Facade facadeFunnyLogged;
    private String funnyToken;

    @BeforeEach
    public void setUp() {
        facadeFunnyLogged = new Facade(merchants,validGiftCards(),users, new Clock());
        funnyToken = facadeFunnyLogged.loginAndGetToken(userFunny, passFunny);
    }


    @Test
    public void test01userOrPasswordAreInvalid() {
        Facade cleanFacade = new  Facade(merchants,validGiftCards(),users, new Clock());
        assertThrowsLike(() -> cleanFacade.login(userFunny,"invalidPassword"),
                Facade.invalidUsernameOrPassword);
        assertThrowsLike(() -> cleanFacade.login("invalidUser", passFunny),
                Facade.invalidUsernameOrPassword);
    }

    @Test
    public void test02tokenIsInvalid() {
        assertThrowsLike(() -> facadeFunnyLogged.redeemGiftCard(userFunny,"invalidToken", cardD2Id),
                Session.incorrectToken);
    }

    @Test
    public void test03sessionIsActiveAfterLoginIn() {
        assertFalse(facadeFunnyLogged.getUserSession(userFunny).isExpired());
    }

    @Test
    public void test04userRedeemedCardCorrectly() {
        assertTrue(funnyFacadeRedeemedCar1().isCardRedeemed(cardD2Id));
    }

    @Test
    public void test05SameUserCanRedeemMultipleGiftCards() {
        assertTrue(funnyFacadeRedeemedCar1()
                .redeemGiftCard(userFunny, funnyToken , cardD1Id)
                .isCardRedeemed(cardD1Id));
    }

    @Test
    public void test06userCannotRedeemInvalidCard() {
        assertThrowsLike(
                () -> facadeFunnyLogged.redeemGiftCard(userFunny, funnyToken,"invalidCardId"),
                Facade.giftCardNotFound
        );
    }

    @Test
    public void test07DifferentUserCannotRedeemAnAlreadyRedeemedCard(){
        String johnnyToken = getJohnnyToken();

        assertThrowsLike(
                () -> facadeFunnyLogged.redeemGiftCard(userJohnny, johnnyToken , cardD2Id),
                Facade.giftCardRedeemed);
    }

    @Test
    public void test08buyUsingRedeemedGiftCard() {
        funnyFacadeRedeemedCar1().checkOut(merchantIdR1, cardD2Id, 80);
        assertEquals(20, facadeFunnyLogged.balanceOf(userFunny, funnyToken, cardD2Id));
    }

    @Test
    public void test09cannotBuyUsingAnUnknownMerchant() {
        assertThrowsLike(
                () -> facadeFunnyLogged.checkOut("invalidMerchantId", cardD2Id, 80),
                Facade.merchantNotFound
        );
    }

    @Test
    public void test10notEnoughFunds() {
        assertThrowsLike(
                () -> funnyFacadeRedeemedCar1().checkOut(merchantIdR1, cardD2Id, 120),
                GiftCard.insufficientBalance
        );
    }

    @Test
    public void test11checkoutIsRecordedAsMovement() {
        funnyFacadeRedeemedCar1().checkOut(merchantIdR1, cardD2Id, 80);
        assertSingleMovement(facadeFunnyLogged.movementsOf(userFunny, funnyToken, cardD2Id), 80, merchantIdR1);
    }


    @Test
    public void test12checkoutIncorrectlyIsNotRecordedAsMovementAndBalanceDoesNotChange() {
        Facade facade = funnyFacadeRedeemedCar1();
        assertThrowsLike(() ->facade.checkOut(merchantIdR1, cardD2Id, 120)
                ,GiftCard.insufficientBalance);

        assertEquals(0, facade.movementsOf(userFunny,funnyToken, cardD2Id).size());
        assertEquals(100, facade.balanceOf(userFunny,funnyToken, cardD2Id));
    }


    @Test
    public void test13loginInMultipleTimesChangesTheTokenAndUpdatesTheSession() {
        Clock myClock = customClock();
        Facade facade = facadeWithClock(myClock);

        String firstToken = facade.loginAndGetToken(userFunny, passFunny);
        LocalDateTime firstCreationTime = facade.getTokenCreationTimeOf(userFunny);

        myClock.advanceMinutes(1);

        String secondToken = facade.loginAndGetToken(userFunny, passFunny);
        LocalDateTime secondCreationTime = facade.getTokenCreationTimeOf(userFunny);

        assertNotEquals(secondToken, firstToken);
        assertNotEquals(secondCreationTime, firstCreationTime);
    }

    @Test
    public void test14cannotRedeemGiftCardOrCheckGiftCardStatusIfSessionExpired() {
        Facade expiredSessionFacade = expiredFunnySessionRedeemedCard1();

        assertThrowsLike(()-> expiredSessionFacade.redeemGiftCard(userFunny, funnyToken, cardD1Id), Session.sessionExpired);
        assertThrowsLike(()-> expiredSessionFacade.balanceOf(userFunny, funnyToken, cardD2Id), Session.sessionExpired);
        assertThrowsLike(()-> expiredSessionFacade.movementsOf(userFunny, funnyToken, cardD2Id), Session.sessionExpired);
    }

    @Test
    public void test15canBuyEvenIfSessionExpired() {
        Facade expiredSessionFacade = expiredFunnySessionRedeemedCard1();

        expiredSessionFacade.checkOut(merchantIdR1, cardD2Id,80);

        String secondFunnyToken = expiredSessionFacade.loginAndGetToken(userFunny, passFunny);
        assertEquals(20, expiredSessionFacade.balanceOf(userFunny,secondFunnyToken, cardD2Id));
    }

    @Test
    public void test16multipleUsersHaveDifferentSessions() {
        facadeFunnyLogged.login(userFunny, passFunny).login(userJohnny, passJohnny);

        assertNotEquals(facadeFunnyLogged.getUserTokenSession(userFunny), facadeFunnyLogged.getUserTokenSession(userJohnny));
    }

    @Test
    public void test17UserCanSpendInPersonalGiftCard() {
        String johnnyToken = getJohnnyToken();
        facadeFunnyLogged.redeemGiftCard(userJohnny, johnnyToken, cardD1Id);

        facadeFunnyLogged.checkOut(merchantIdR1, cardD2Id,80);
        facadeFunnyLogged.checkOut(merchantIdR2, cardD1Id,70);

        assertNotEquals(facadeFunnyLogged.balanceOf(userFunny, funnyToken, cardD2Id),
                facadeFunnyLogged.balanceOf(userJohnny, johnnyToken, cardD1Id));
    }

    @Test
    public void test18CannotRedeemSameGiftCard() {
        String johnnyToken = getJohnnyToken();

        assertThrowsLike(() -> facadeFunnyLogged.redeemGiftCard(userJohnny, johnnyToken, cardD2Id), GiftCard.giftCardRedeemed);
    }

    @Test
    public void test19cannotCheckGiftCardOfAnotherUser() {
        String johnnyToken = getJohnnyToken();

        assertThrowsLike(()-> facadeFunnyLogged.balanceOf(userJohnny, johnnyToken, cardD2Id), Facade.userDoesNotOwnGiftCard);
        assertThrowsLike(()-> facadeFunnyLogged.movementsOf(userJohnny, johnnyToken, cardD2Id),  Facade.userDoesNotOwnGiftCard);
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
        return List.of(new GiftCard(cardD2Id, 100),new GiftCard(cardD1Id, 100));
    }

    private Facade funnyFacadeRedeemedCar1() {
        return facadeFunnyLogged.redeemGiftCard(userFunny, funnyToken, cardD2Id);
    }

    private void assertSingleMovement(List<GiftCardMovements> movements, Integer amount, String merchantId) {
        assertEquals(1, movements.size());
        assertEquals(amount, movements.getFirst().getExpense());
        assertEquals(merchantId, movements.getFirst().getCommerce());
    }

    private String getJohnnyToken() {
        return funnyFacadeRedeemedCar1().loginAndGetToken(userJohnny, passJohnny);
    }

    private Facade expiredFunnySessionRedeemedCard1() {
        Clock myClock = customClock();
        Facade facadeWithClock = facadeWithClock(myClock);

        String funnyToken = facadeWithClock.loginAndGetToken(userFunny, passFunny);
        facadeWithClock.redeemGiftCard(userFunny,funnyToken, cardD2Id);

        myClock.advanceMinutes(6);

        return facadeWithClock;
    }

}

