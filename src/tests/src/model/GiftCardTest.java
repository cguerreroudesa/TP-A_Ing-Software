package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTest {
    public static String BalanceChangedAfterFail = "Balance should remain unchanged after failure";

    @Test
    public void test01RedeemGiftCardOnceSucceeds() {
        GiftCard giftCard = newGiftCard();
        assertFalse(giftCard.isRedeemed());
        giftCard.redeem();
        assertTrue(giftCard.isRedeemed());
    }

    @Test
    public void test02RedeemGiftCardTwiceFails() {
        GiftCard giftCard = NewGiftCardRedeemed();
        assertThrowsLike(giftCard::redeem, GiftCard.giftCardRedeemed);
    }

    @Test
    public void test03DecreaseBalanceCorrectly() {
        assertEquals(70, newGiftCard()
                .redeem()
                .decreaseBalance(30)
                .getBalance());
    }

    @Test
    public void test04DecreaseBalanceFailsWhenNegativeAmount() {
        GiftCard giftCard = NewGiftCardRedeemed();
        assertFailsWithoutChangingBalance(giftCard,
                -10,
                GiftCard.negativeAmountNotAllowed);
    }

    @Test
    public void test05DecreaseBalanceFailsWhenInsufficient() {
        GiftCard giftCard = NewGiftCardRedeemed();
        assertFailsWithoutChangingBalance(giftCard,
                130,
                GiftCard.insufficientBalance);
    }

    @Test
    public void test06CantUseBeforeRedeem() {
        assertThrowsLike(() -> newGiftCard()
                        .decreaseBalance(130),
                GiftCard.giftCardNotRedeemed);
    }


    private void assertThrowsLike(Executable runnable, String expectedMessage) {
        assertEquals(
                expectedMessage,
                assertThrows(RuntimeException.class, runnable).getMessage()
        );
    }

    private GiftCard newGiftCard() {
        return new GiftCard("abc", 100);
    }

    private GiftCard NewGiftCardRedeemed() {
        return newGiftCard().redeem();
    }

    private void assertFailsWithoutChangingBalance(GiftCard gc, Integer amount, String expectedMessage) {
        int balanceBefore = gc.getBalance();
        assertThrowsLike(() -> gc.decreaseBalance(amount), expectedMessage);
        assertEquals(balanceBefore, gc.getBalance(), BalanceChangedAfterFail);
    }


}
