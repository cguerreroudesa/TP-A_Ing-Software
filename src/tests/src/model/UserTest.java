package model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private static Map<String, String> users = Map.of( "Funny", "Valentine");
    private static  Map<String, Integer> giftCards = Map.of( "mctastytriplebacon", 100);
    @BeforeEach
    public void  beforeEach() {
        user = user();
    }


    User user;
    @Test
    public void test01checkUserIsInvalid() {
        assertThrowsLike(() -> user.login("Funny","d4c"),
                User.invalidUsernameOrPassword);
    }

    @Test
    public void test02checkUserRedeemedCard() {
        user.redeemGiftCard("123","mctastytriplebacon");
        assertTrue(user.getUserGiftCards().containsKey("mctastytriplebacon"));
    }

    @Test
    public void test03checkUserCannotRedeemInvalidCard() {
        assertThrowsLike(
                () -> user.redeemGiftCard("123","mctastytriple"),
                User.noExisteTalGiftCard
        );
    }

    @Test
    public void test04useAreedemedGiftCard() {
        user.redeemGiftCard("123","mctastytriplebacon").buyshi("mctastytriplebacon",80);
        assertEquals(20,user.getUserGiftCards().get("mctastytriplebacon"));

    }

    @Test
    public void test05cannotSpendMoreThanTheBalanceLeftInACard() {
        user.redeemGiftCard("123","mctastytriplebacon")
                .buyshi("mctastytriplebacon",80);
        assertThrowsLike(
                () -> user.buyshi("mctastytriplebacon",80),
                User.notEnoughMoneyToBuy
        );
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
//    private static TusLibrosSystemFacade systemFacade() {
//        return systemFacade( new Clock() );
//    }

    private static User user() {
        return new User().setUsers(users).setGiftCards(giftCards);
    }
}

