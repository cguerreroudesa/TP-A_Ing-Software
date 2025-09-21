package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTest implements AssertThrowsLike{
    public static String BalanceChangedAfterFail = "Balance should remain unchanged after failure";

    private static String MERCHANT_ID_R1 = "Restaurante1";
    public static final String MERCHANT_ID_R2 = "Restaurante2";

    public static String USER_FUNNY = "Funny";


    @Test
    public void test01RedeemGiftCardOnceSucceeds() {
        GiftCard giftCard = newGiftCard1();
        assertFalse(giftCard.isRedeemed());
        giftCard.redeemFor(USER_FUNNY);
        assertTrue(giftCard.isRedeemed());
    }

    @Test
    public void test02RedeemGiftCardTwiceFails() {
        GiftCard giftCard = newGiftCardRedeemed();
        assertThrowsLike(() -> giftCard.redeemFor(USER_FUNNY), GiftCard.giftCardRedeemed);
    }

    @Test
    public void test03DecreaseBalanceCorrectly() {
        assertEquals(70, newGiftCard1()
                .redeemFor(USER_FUNNY)
                .decreaseBalance(30)
                .getBalance());
    }

    @Test
    public void test04DecreaseBalanceFailsWhenNegativeAmount() {
        GiftCard giftCard = newGiftCardRedeemed();
        assertFailsWithoutChangingBalance(giftCard,
                -10,
                GiftCard.negativeAmountNotAllowed);
    }

    @Test
    public void test05DecreaseBalanceFailsWhenInsufficient() {
        GiftCard giftCard = newGiftCardRedeemed();
        assertFailsWithoutChangingBalance(giftCard,
                130,
                GiftCard.insufficientBalance);
    }

    @Test
    public void test06CantUseBeforeRedeem() {
        assertThrowsLike(() -> newGiftCard1()
                        .spend(130, LocalDateTime.now(), MERCHANT_ID_R1),
                GiftCard.giftCardNotRedeemed);
    }

    @Test
    public void test07RedeemedGiftCardStartsWithCorrectBalance() {
        assertEquals(100, newGiftCard1()
                    .redeemFor(USER_FUNNY)
                    .getBalance());
    }

    @Test
    public void test08MovementsAccumulateInOrder() {
        GiftCard card = newGiftCardRedeemed();
        Clock clock = new Clock();

        LocalDateTime t1 = clock.now();
        LocalDateTime t2 = clock.advanceMinutes(5).now();

        card.logMovement(20, t1, MERCHANT_ID_R1 );
        card.logMovement(60, t2, MERCHANT_ID_R2 );

        assertEquals(2, card.getLogGiftCardMovements().size());

        checkMovement(card, 0, 20, MERCHANT_ID_R1, t1);
        checkMovement(card, 1, 60, MERCHANT_ID_R2, t2);
    }

    @Test
    public void test09SpendFailDoesNotChangeBalanceNorMovements() {
        GiftCard gc = newGiftCardRedeemed();
        int beforeBalance = gc.getBalance();
        int beforeMovs = gc.getLogGiftCardMovements().size();

        assertThrowsLike(
                () -> gc.spend(130, LocalDateTime.now(), "InvalidMerchant"),
                GiftCard.insufficientBalance
        );

        assertEquals(beforeBalance, gc.getBalance(), BalanceChangedAfterFail);
        assertEquals(beforeMovs, gc.getLogGiftCardMovements().size());
    }


    private GiftCard newGiftCard1() {
        return new GiftCard("FelizPrimavera", 100);
    }

    private GiftCard newGiftCardRedeemed() {
        return newGiftCard1().redeemFor(USER_FUNNY);
    }

    private void assertFailsWithoutChangingBalance(GiftCard gc, Integer amount, String expectedMessage) {
        int balanceBefore = gc.getBalance();
        assertThrowsLike(() -> gc.decreaseBalance(amount), expectedMessage);
        assertEquals(balanceBefore, gc.getBalance(), BalanceChangedAfterFail);
    }

    private static void checkMovement(GiftCard card, Integer movementNumber, Integer expense, String merchantId, LocalDateTime date) {
        GiftCardMovements m = card.getLogGiftCardMovements().get(movementNumber);

        assertEquals(expense, m.getExpense());
        assertEquals(merchantId, m.getCommerce());
        assertEquals(date, m.getExpenseDate());
    }


}
