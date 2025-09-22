package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GiftCard {
    private String id;
    private int currentBalance;
    private boolean redeemed;
    private List<GiftCardMovements> logGiftCardMovements;
    private String owner;


    public static String giftCardNotRedeemed = "Gift card not redeemed";
    public static String giftCardRedeemed = "This gift card has already been redeemed";
    public static String negativeAmountNotAllowed = "Amount must be positive";
    public static String insufficientBalance = "Insufficient balance";
    public static String invalidMerchant = "Invalid merchant";

    public GiftCard(String id, int balance){
        this.id = id;
        this.currentBalance = balance;
        this.redeemed = false;
        this.logGiftCardMovements = new ArrayList<>();
        this.owner = null;
    }

    public String getId(){ return id; }
    public int getBalance(){ return currentBalance; }
    public boolean isRedeemed(){ return redeemed; }

    public GiftCard redeemFor(String username){
        if (redeemed){
            throw new RuntimeException(giftCardRedeemed);
        }
        redeemed = true;
        this.owner = username;
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

    public void spend(int amount, LocalDateTime now, String merchantId){
        if (merchantId == null ) throw new RuntimeException(invalidMerchant);
        decreaseBalance(amount);
        logMovement(amount, now, merchantId);
    }

    public List<GiftCardMovements> getLogGiftCardMovements() {
        return logGiftCardMovements;
    }

    public String getOwner() {
        return owner;
    }
}
