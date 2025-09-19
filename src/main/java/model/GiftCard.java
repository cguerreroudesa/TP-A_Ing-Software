package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GiftCard {
    private String id;
    private int initialBalance;
    private int currentBalance;
    private boolean redeemed;
    private List<GiftCardMovements> logGiftCardMovements;


    public static String giftCardNotRedeemed = "Giftcard not redeemed";
    public static String giftCardRedeemed = "Giftcard already redeemed";
    public static String negativeAmountNotAllowed = "Amount must be positive";
    public static String insufficientBalance = "Insufficient balance";

    public GiftCard(String id, int balance){
        this.id = id;
        this.initialBalance = balance;
        this.currentBalance = balance;
        this.redeemed = false;
        this.logGiftCardMovements = new ArrayList<>();
    }

    public String getId(){ return id; }
    public int getBalance(){ return currentBalance; }
    public boolean isRedeemed(){ return redeemed; }

    public GiftCard redeem(){
        if (redeemed){
            throw new RuntimeException(giftCardRedeemed);
        }
        redeemed = true;
        return this;
    }

    public GiftCard decreaseBalance(int amount){
        if (!redeemed){throw new RuntimeException(giftCardNotRedeemed);}
        if (amount < 0){ throw new RuntimeException(negativeAmountNotAllowed);}
        if (amount > currentBalance){ throw new RuntimeException(insufficientBalance);}

        this.currentBalance -= amount;
        return this;
    }

    public void logMovement(int amount, LocalDateTime now, String merchantId) {
        logGiftCardMovements.add(new GiftCardMovements(amount, now, merchantId));
    }

    public List<GiftCardMovements> getLogGiftCardMovements() {
        return logGiftCardMovements;
    }
}
