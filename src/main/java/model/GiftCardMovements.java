package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class GiftCardMovements {

    private String commerce;
    private Integer expense;
    private LocalDateTime expenseDate;

    public GiftCardMovements(Integer expense, LocalDateTime expenseDate, String commerce) {
        this.commerce = commerce;
        this.expense = expense;
        this.expenseDate = expenseDate;
    }

    public String getCommerce() {   return commerce;        }
    public Integer getExpense() {  return expense;        }
    public LocalDateTime getExpenseDate() {     return expenseDate;       }
}
