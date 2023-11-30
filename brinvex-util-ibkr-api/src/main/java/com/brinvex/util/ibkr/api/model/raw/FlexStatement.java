package com.brinvex.util.ibkr.api.model.raw;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FlexStatement implements Serializable {

    private String accountId;

    private LocalDate fromDate;

    private LocalDate toDate;

    private ZonedDateTime whenGenerated;

    private FlexStatementType type;

    private final List<CashTransaction> cashTransactions = new ArrayList<>();

    private final List<Trade> trades = new ArrayList<>();

    private final List<TradeConfirm> tradeConfirms = new ArrayList<>();

    private final List<EquitySummary> equitySummaries = new ArrayList<>();

    private final List<CorporateAction> corporateActions = new ArrayList<>();

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

    public ZonedDateTime getWhenGenerated() {
        return whenGenerated;
    }

    public void setWhenGenerated(ZonedDateTime whenGenerated) {
        this.whenGenerated = whenGenerated;
    }

    public FlexStatementType getType() {
        return type;
    }

    public void setType(FlexStatementType type) {
        this.type = type;
    }

    public List<CashTransaction> getCashTransactions() {
        return cashTransactions;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public List<TradeConfirm> getTradeConfirms() {
        return tradeConfirms;
    }

    public List<EquitySummary> getEquitySummaries() {
        return equitySummaries;
    }

    public List<CorporateAction> getCorporateActions() {
        return corporateActions;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FlexStatement.class.getSimpleName() + "[", "]")
                .add("accountId='" + accountId + "'")
                .add("fromDate=" + fromDate)
                .add("toDate=" + toDate)
                .add("whenGenerated=" + whenGenerated)
                .add("type=" + type)
                .add("cashTransactions=" + cashTransactions)
                .add("trades=" + trades)
                .add("tradeConfirms=" + tradeConfirms)
                .add("equitySummaries=" + equitySummaries)
                .add("corporateActions=" + corporateActions)
                .toString();
    }
}
