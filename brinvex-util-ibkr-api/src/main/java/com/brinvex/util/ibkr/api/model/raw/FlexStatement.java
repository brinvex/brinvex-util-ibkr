package com.brinvex.util.ibkr.api.model.raw;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FlexStatement implements Serializable {

    private String accountId;

    private LocalDate fromDate;

    private LocalDate toDate;

    private LocalDateTime whenGenerated;

    private final List<RawTransaction> transactions = new ArrayList<>();

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public LocalDateTime getWhenGenerated() {
        return whenGenerated;
    }

    public void setWhenGenerated(LocalDateTime whenGenerated) {
        this.whenGenerated = whenGenerated;
    }

    public List<RawTransaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FlexStatement.class.getSimpleName() + "[", "]")
                .add("accountId='" + accountId + "'")
                .add("fromDate=" + fromDate)
                .add("toDate=" + toDate)
                .add("whenGenerated=" + whenGenerated)
                .add("transactions=" + transactions)
                .toString();
    }
}
