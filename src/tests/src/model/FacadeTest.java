package model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest {
    public static final String CORRECT_GC_ID = "mcTastyTripleBacon";
    private static final String MERCHANT_MCD = "McDonalds";
    private static final String MERCHANT_UNKNOWN = "BurgerKing";

    private static Map<String, String> users = Map.of( "Funny", "Valentine","Johnny","Joestar");
    private static List<String> merchants = Arrays.asList("McDonalds", "Starbucks");

    public static String tokenShouldChange = "Each login must generate a new token";
    public static String sessionTimeShouldChange = "Each login must refresh the session";

    private Facade facade;
    private String userToken;

    @BeforeEach
    public void setUp() {
        facade = newFacade();
        userToken = facade.getToken();
    }


    @Test
    public void test01userIsInvalid() {
        assertThrowsLike(() -> facade.login("Funny","d4c"),
                Facade.invalidUsernameOrPassword);
    }

    @Test
    public void test02tokenIsInvalid() {
        assertThrowsLike(() -> facade.redeemGiftCard("tokenIncorrecto", CORRECT_GC_ID),
                Session.incorrectToken);
    }

    @Test
    public void test03userRedeemedCardCorrectly() {
        assertTrue(facadeWithRedeemedCard().getUserGiftCards().get(CORRECT_GC_ID).isRedeemed());
    }

    @Test
    public void test04userCannotRedeemInvalidCard() {
        assertThrowsLike(
                () -> facade.redeemGiftCard(userToken,"codigoEquivocado"),
                Facade.giftCardNotFound
        );
    }

    @Test
    public void test05buyUsingRedeemedGiftCard() {
        facade.redeemGiftCard(userToken,CORRECT_GC_ID).merchantCharge("McDonalds",CORRECT_GC_ID, 80);
        assertEquals(20, facade.balanceOf(userToken, CORRECT_GC_ID));
    }

    @Test
    public void test06cannotBuyUsingAnUnknownMerchant() {
        assertThrowsLike(
                () -> chargedFacade(80, MERCHANT_UNKNOWN),
                Facade.merchantNotFound
        );
    }

    @Test
    public void test07notEnoughFunds() {
        assertThrowsLike(
                () -> chargedFacade(120, MERCHANT_MCD),
                GiftCard.insufficientBalance
        );
    }


    @Test
    public void test08merchantChargeIsRecordedAsMovement() {
        List<GiftCardMovements> movements = chargedFacade(80, MERCHANT_MCD)
                .movementsOf(userToken, CORRECT_GC_ID);

        assertEquals(1, movements.size());
        assertEquals(MERCHANT_MCD, movements.getFirst().getCommerce());
        assertEquals(80, movements.getFirst().getExpense());
    }

    @Test
    public void test09loggingInTwiceGeneratesNewTokenAndSession() {
        Clock clock = new Clock(LocalDateTime.now());
        Facade localFacade = newFacade(clock);

        String firstToken = loginAndGetToken(localFacade, "Funny", "Valentine");
        LocalDateTime firstCreationTime = localFacade.getSession().getTokenCreationTime();

        clock.advanceMinutes(1);
        String secondToken = loginAndGetToken(localFacade, "Funny", "Valentine");
        LocalDateTime secondCreationTime = localFacade.getSession().getTokenCreationTime();

        assertNotEquals(firstToken, secondToken, tokenShouldChange);
        assertNotEquals(firstCreationTime, secondCreationTime, sessionTimeShouldChange);
    }

    private String loginAndGetToken(Facade facade, String user, String pass) {
        facade.login(user, pass);
        return facade.getToken();
    }

    private Facade chargedFacade(Integer amount, String merchantId) {
        return facadeWithRedeemedCard().merchantCharge(merchantId, CORRECT_GC_ID, amount);
    }

    private Facade facadeWithRedeemedCard() {
        return facade.redeemGiftCard(userToken, CORRECT_GC_ID);
    }


    private void assertThrowsLike(Executable runnable, String expectedMessage) {
        assertEquals(
                expectedMessage,
                assertThrows(RuntimeException.class, runnable).getMessage()
        );
    }

    private Facade newFacade() {
        return new Facade(merchants,validGiftCards(),users, new Clock()).login("Funny","Valentine");
    }
    private Facade newFacade(Clock clock) {
        return new Facade(merchants,validGiftCards(),users, clock).login("Funny","Valentine");
    }

    private Map<String, GiftCard> validGiftCards() {
        return Map.of(CORRECT_GC_ID, new GiftCard(CORRECT_GC_ID, 100));
    }
}

