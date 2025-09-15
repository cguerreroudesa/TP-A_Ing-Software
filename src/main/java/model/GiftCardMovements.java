package model;

import java.time.LocalDate;
import java.util.ArrayList;

public class GiftCardMovements {

    private ArrayList<String> commerce;
    private ArrayList<Integer> expense;
    private ArrayList<LocalDate> expenseDate;

    public GiftCardMovements(Integer expense, LocalDate expenseDate, String commerce) {
        this.commerce = new ArrayList<>();
        this.expense = new ArrayList<>();
        this.expenseDate = new ArrayList<>();

        this.commerce.add(commerce);
        this.expense.add(expense);
        this.expenseDate.add(expenseDate);
    }
}
