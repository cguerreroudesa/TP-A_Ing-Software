package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Iterator;
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


    private Facade facadeWithUserLogged;
    private String funnyToken;

    @BeforeEach
    public void setUp() {
        facadeWithUserLogged = new Facade(merchants,validGiftCards(),users, new Clock());
        funnyToken = loginAndGetToken(facadeWithUserLogged, userFunny, passFunny);
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
        assertThrowsLike(() -> facadeWithUserLogged.redeemGiftCard(userFunny,"invalidToken", cardD2Id),
                Session.incorrectToken);
    }

    @Test
    public void test03sessionIsActiveAfterLoginIn() {
        assertFalse(facadeWithUserLogged.getUserSession(userFunny).isExpired());
    }

    @Test
    public void test04userRedeemedCardCorrectly() {
        assertTrue(userRedeemedCard2().isCardRedeemed(cardD2Id));
    }

    @Test
    public void test05SameUserCanRedeemMultipleGiftCards() {
        assertTrue(userRedeemedCard2()
                .redeemGiftCard(userFunny, funnyToken, cardD1Id)
                .isCardRedeemed(cardD1Id));
    }

    @Test
    public void test06userCannotRedeemInvalidCard() {
        assertThrowsLike(
                () -> facadeWithUserLogged.redeemGiftCard(userFunny, funnyToken,"invalidCardId"),
                Facade.giftCardNotFound
        );
    }

    @Test
    public void test07DifferentUserCannotRedeemAnAlreadyRedeemedCard(){
        String johnnyToken = loginAndGetToken(userRedeemedCard2(), userJohnny, passJohnny);

        assertThrowsLike(
                () -> facadeWithUserLogged.redeemGiftCard(userJohnny, johnnyToken , cardD2Id),
                Facade.giftCardRedeemed);
    }

    @Test
    public void test08buyUsingRedeemedGiftCard() {
        userRedeemedCard2().checkOut(merchantIdR1, cardD2Id, 80);
        assertEquals(20, facadeWithUserLogged.balanceOf(userFunny, funnyToken, cardD2Id));
    }

    @Test
    public void test09cannotBuyUsingAnUnknownMerchant() {
        assertThrowsLike(
                () -> facadeWithUserLogged.checkOut("invalidMerchantId", cardD2Id, 80),
                Facade.merchantNotFound
        );
    }

    @Test
    public void test10notEnoughFunds() {
        assertThrowsLike(
                () -> userRedeemedCard2().checkOut(merchantIdR1, cardD2Id, 120),
                GiftCard.insufficientBalance
        );
    }

    @Test
    public void test11checkoutIsRecordedAsMovement() {
        userRedeemedCard2().checkOut(merchantIdR1, cardD2Id, 80);
        List<GiftCardMovements>  movements = facadeWithUserLogged.movementsOf(userFunny, funnyToken, cardD2Id);

        assertEquals(1, movements.size());
        assertEquals(80, movements.getFirst().getExpense());
        assertEquals(merchantIdR1, movements.getFirst().getCommerce());
    }


    @Test
    public void test12checkoutIncorrectlyIsNotRecordedAsMovementAndBalanceDoesNotChange() {
        Facade facade = userRedeemedCard2();
        assertThrowsLike(() ->facade.checkOut(merchantIdR1, cardD2Id, 120)
                ,GiftCard.insufficientBalance);

        assertEquals(0, facade.movementsOf(userFunny, funnyToken, cardD2Id).size());
        assertEquals(100, facade.balanceOf(userFunny, funnyToken, cardD2Id));
    }


    @Test
    public void test13loginInMultipleTimesChangesTheTokenAndUpdatesTheSession() {
        Clock myClock = customClock();
        Facade facade = facadeWithClock(myClock);

        String firstToken = loginAndGetToken(facade, userFunny, passFunny);
        LocalDateTime firstCreationTime = facade.getUserSession(userFunny).getTokenCreationTime();

        String secondToken = loginAndGetToken(facade, userFunny, passFunny);
        LocalDateTime secondCreationTime = facade.getUserSession(userFunny).getTokenCreationTime();

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

        String secondFunnyToken = loginAndGetToken(expiredSessionFacade, userFunny, passFunny);
        assertEquals(20, expiredSessionFacade.balanceOf(userFunny,secondFunnyToken, cardD2Id));
    }

    @Test
    public void test16multipleUsersHaveDifferentSessions() {
        facadeWithUserLogged.login(userFunny, passFunny).login(userJohnny, passJohnny);

        assertNotEquals(facadeWithUserLogged.getUserTokenSession(userFunny), facadeWithUserLogged.getUserTokenSession(userJohnny));
    }

    @Test
    public void test17UserCanSpendInPersonalGiftCard() {
        String johnnyToken = loginAndGetToken(userRedeemedCard2(), userJohnny, passJohnny);
        facadeWithUserLogged.redeemGiftCard(userJohnny, johnnyToken, cardD1Id);

        facadeWithUserLogged.checkOut(merchantIdR1, cardD2Id,80);
        facadeWithUserLogged.checkOut(merchantIdR2, cardD1Id,70);

        assertNotEquals(facadeWithUserLogged.balanceOf(userFunny, funnyToken, cardD2Id),
                facadeWithUserLogged.balanceOf(userJohnny, johnnyToken, cardD1Id));
    }

    @Test
    public void test18CannotRedeemSameGiftCard() {
        String johnnyToken = loginAndGetToken(userRedeemedCard2(), userJohnny, passJohnny);

        assertThrowsLike(() -> facadeWithUserLogged.redeemGiftCard(userJohnny, johnnyToken, cardD2Id), GiftCard.giftCardRedeemed);
    }

    @Test
    public void test19cannotCheckGiftCardOfAnotherUser() {
        String johnnyToken = loginAndGetToken(userRedeemedCard2(), userJohnny, passJohnny);

        assertThrowsLike(()-> facadeWithUserLogged.balanceOf(userJohnny, johnnyToken, cardD2Id), Facade.userDoesNotOwnGiftCard);
        assertThrowsLike(()-> facadeWithUserLogged.movementsOf(userJohnny, johnnyToken, cardD2Id),  Facade.userDoesNotOwnGiftCard);
    }

    private Clock customClock() {
        return new Clock() {
            private final List<LocalDateTime> timeline = List.of(
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(1),
                    LocalDateTime.now().plusMinutes(5));
            private final Iterator<LocalDateTime> it = timeline.iterator();
            private LocalDateTime last = timeline.get(timeline.size() - 1);

            @Override
            public LocalDateTime now() {
                if (it.hasNext()) last = it.next();
                return last;
            }
        };
    }


    private Facade facadeWithClock(Clock clock) {
        return new Facade(merchants,validGiftCards(),users, clock);
    }

    private List<GiftCard> validGiftCards() {
        return List.of(new GiftCard(cardD2Id, 100),new GiftCard(cardD1Id, 100));
    }

    private Facade userRedeemedCard2() {
        return facadeWithUserLogged.redeemGiftCard(userFunny, funnyToken, cardD2Id);
    }

    private Facade expiredFunnySessionRedeemedCard1() {
        Clock myClock = customClock();
        Facade facadeWithClock = facadeWithClock(myClock);

        String funnyToken = facadeWithClock.loginAndGetToken(userFunny, passFunny);
        facadeWithClock.redeemGiftCard(userFunny,funnyToken, cardD2Id);

        myClock.now();
        return facadeWithClock;
    }

    private String loginAndGetToken(Facade f, String user, String pass){
        return f.login(user, pass).getUserSession(user).getToken();
    }
}

