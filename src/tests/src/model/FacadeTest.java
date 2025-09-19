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
        userToken = facade.getToken();
    }


    @Test
    public void test01userIsInvalid() {
        assertThrowsLike(() -> facade.login("Funny","d4c"),
                Facade.invalidUsernameOrPassword);
    }

    @Test
    public void test02tokenIsInvalid() {
        assertThrowsLike(() -> facade.redeemGiftCard("tokenIncorrecto", correctGcId),
                Session.incorrectToken);
    }

    @Test
    public void test03userRedeemedCardCorrectly() {
        assertTrue(facadeWithRedeemedCard()
                .getUserGiftCards()
                .stream()
                .filter(gc -> gc.getId().equals(correctGcId))
                .findFirst()
                .get().isRedeemed());
    }

    @Test
    public void test04userCannotRedeemInvalidCard() {
        assertThrowsLike(
                () -> facade.redeemGiftCard(userToken,"invalidId"),
                Facade.giftCardNotFound
        );
    }

    @Test
    public void test05buyUsingRedeemedGiftCard() {
        facade.redeemGiftCard(userToken,correctGcId).merchantCharge(merchantId,correctGcId, 80);
        assertEquals(20, facade.balanceOf(userToken, correctGcId));
    }

    @Test
    public void test06cannotBuyUsingAnUnknownMerchant() {
        assertThrowsLike(
                () -> chargedFacade(80, "invalidMerchantId"),
                Facade.merchantNotFound
        );
    }

    @Test
    public void test07notEnoughFunds() {
        assertThrowsLike(
                () -> chargedFacade(120, merchantId),
                GiftCard.insufficientBalance
        );
    }


    @Test
    public void test08merchantChargeIsRecordedAsMovement() {
        List<GiftCardMovements> movements = chargedFacade(80, merchantId)
                .movementsOf(userToken, correctGcId);

        assertEquals(1, movements.size());
        assertEquals(merchantId, movements.getFirst().getCommerce());
        assertEquals(80, movements.getFirst().getExpense());
    }

    @Test
    public void test09logginInMultipleTimesJustChangesTheTokenAndUpdatesTheSession() {
        Clock myClock = new Clock() {
            private LocalDateTime current = LocalDateTime.now();

            @Override
            public LocalDateTime now() {
                LocalDateTime toReturn = current;
                current = current.plusMinutes(1); // siempre avanza 1 minuto
                return toReturn;
            }
        };

        Facade facade = loggedInFacade(myClock);

        facade.login("Funny", "Valentine");
        String firstToken = facade.getToken();
        LocalDateTime firstCreationTime = facade.getSession().getTokenCreationTime();

        facade.login("Funny", "Valentine");
        String secondToken = facade.getToken();
        LocalDateTime secondCreationTime = facade.getSession().getTokenCreationTime();

        assertNotEquals(secondToken, firstToken);
        assertNotEquals(secondCreationTime, firstCreationTime);
    }

    @Test
    public void test10cannotReedemIfSessionExpired() {
        Clock myClock = new Clock() {
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

        Facade newFacade = loggedInFacade(myClock);
        myClock.advanceMinutes(6);
        assertThrowsLike(()->newFacade.redeemGiftCard(userToken,correctGcId), Session.sessionExpired);

    }


    private Facade chargedFacade(Integer amount, String merchantId) {
        return facadeWithRedeemedCard().merchantCharge(merchantId, correctGcId, amount);
    }

    private Facade facadeWithRedeemedCard() {
        return facade.redeemGiftCard(userToken, correctGcId);
    }

    private Facade loggedInFacade() {
        return new Facade(merchants,validGiftCards(),users, new Clock()).login("Funny","Valentine");
    }
    private Facade loggedInFacade(Clock clock) {
        return new Facade(merchants,validGiftCards(),users, clock).login("Funny","Valentine");
    }

    private List<GiftCard> validGiftCards() {
        return List.of(new GiftCard(correctGcId, 100));
    }
}

