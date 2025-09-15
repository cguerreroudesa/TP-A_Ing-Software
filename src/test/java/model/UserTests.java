package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


public class UserTests {
    private static  Map<String, String> users = Map.of( "Funny", "Valentine");
    private static  Map<String, Integer> giftCards = Map.of( "mctastytriplebacon", 100);

//    @Test
//    public void test01checkUserIsInvalid() {
//        assertEquals(User.invalidUsernameOrPassword, new User().setUsers(users).checkValidUser("Funny","d4c"));
//    }

    @Test
    public void test02checkUserRedeemedCard() {
        User user = new User().setUsers(users).setGiftCards(giftCards);
        user.redeemGiftCard("123","mctastytriplebacon");
        assertTrue(user.getUserGiftCards().containsKey("mctastytriplebacon"));
    }

    @Test
    public void test03checkUserCannotRedeemInvalidCard() {
        User user = new User()
                .setUsers(users)
                .setGiftCards(giftCards);

        assertThrowsLike(
                () -> user.redeemGiftCard("123","mctastytriple"),
                user.noExisteTalGiftCard
        );
    }

    //Test checkear saldo

    //Gastar y checkear que el saldo se actualizo

    //Fijarse que la tarjeta tiene el map con la lista de sus gastos

    //Movimientos se encarga de   las acciones de la gc

    //Hacer para multiples

    private void assertThrowsLike(Executable runnable, String expectedMessage) {
        assertEquals(
                expectedMessage,
                assertThrows(RuntimeException.class, runnable).getMessage()
        );
    }

}
