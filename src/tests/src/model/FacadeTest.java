package model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest {
    private static Map<String, String> users = Map.of( "Funny", "Valentine","Johnny","Joestar");
    private static Map<String, Integer> giftCards = Map.of( "mctastytriplebacon", 100);
    private static List<String> merchants = Arrays.asList("McDonalds", "Starbucks");
    Facade facade;
    @BeforeEach
    public void  beforeEach() {
        facade = facade();
    }

    @Test
    public void test01userIsInvalid() {
        assertThrowsLike(() -> facade.login("Funny","d4c"),
                Facade.invalidUsernameOrPassword);
    }

    @Test
    public void test02tokenIsInvalid() {
        assertThrowsLike(() -> facade.redeemGiftCard("123","mctastytriplebacon"),
                Facade.incorrectToken);
    }


    @Test
    public void test03userRedeemedCard() {
        facade.redeemGiftCard(facade.getToken(),"mctastytriplebacon");
        assertTrue(facade.getUserGiftCards().containsKey("mctastytriplebacon"));
    }

    @Test
    public void test04userCannotRedeemInvalidCard() {
        assertThrowsLike(
                () -> facade.redeemGiftCard(facade.getToken(),"mctastytriple"),
                Facade.giftCardNotFound
        );
    }

    @Test
    public void test05buyUsingAreedemedGiftCard() {
        facade.redeemGiftCard(facade.getToken(),"mctastytriplebacon").buyProduct("mctastytriplebacon",80,"McDonalds", facade.getToken());
        assertEquals(20, facade.getUserGiftCards().get("mctastytriplebacon"));
    }

    @Test
    public void test06cannotBuyUsingAnUnknownMerchant() {
        assertThrowsLike(
                () -> facade.redeemGiftCard(facade.getToken(),"mctastytriplebacon").
                        buyProduct("mctastytriplebacon",80,"Burger King", facade.getToken()),
                Facade.merchantNotFound
        );
    }

    @Test
    public void test07notEnoughFunds() {
        facade.redeemGiftCard(facade.getToken(),"mctastytriplebacon")
                .buyProduct("mctastytriplebacon",80,"McDonalds", facade.getToken());
        assertThrowsLike(
                () -> facade.buyProduct("mctastytriplebacon",80,"McDonalds", facade.getToken()),
                Facade.notEnoughMoneyToBuy
        );
    }

    @Test
    public void test08purchaseDetailsAreWritten() {
        facade.redeemGiftCard(facade.getToken(),"mctastytriplebacon")
                .buyProduct("mctastytriplebacon",80,"McDonalds", facade.getToken());
        assertTrue(!facade.getLogGiftCardMovements().get("mctastytriplebacon").isEmpty());
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

        Facade facade = facade(myClock);

        facade.login("Funny", "Valentine");
        String firstToken = facade.getToken();
        LocalDateTime firstCreationTime = facade.getSession().getCreationTime();

        facade.login("Funny", "Valentine");
        String secondToken = facade.getToken();
        LocalDateTime secondCreationTime = facade.getSession().getCreationTime();

        assertNotEquals(secondToken, firstToken);
        assertNotEquals(secondCreationTime, firstCreationTime);
    }

    //Test checkear saldo

    //Gastar y checkear que el saldo se actualizo

    //Fijarse que la tarjeta tiene el map con la lista de sus gastos

    //Movimientos se encarga de las acciones de la gc

    //Hacer para multiples

    private void assertThrowsLike(Executable runnable, String expectedMessage) {
        assertEquals(
                expectedMessage,
                assertThrows(RuntimeException.class, runnable).getMessage()
        );
    }

    private Facade facade() {
        return new Facade(merchants,giftCards,users, new Clock()).login("Funny","Valentine");
    }
    private Facade facade(Clock clock) {
        return new Facade(merchants,giftCards,users, clock).login("Funny","Valentine");
    }
}

