package com.brinvex.util.ibkr.api.model.raw;

import com.brinvex.util.ibkr.api.model.Currency;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.StringJoiner;

public class EquitySummary implements Serializable {

    private LocalDate reportDate;

    private Currency currency;

    private BigDecimal cash;

    private BigDecimal stock;

    private BigDecimal dividendAccruals;

    private BigDecimal interestAccruals;

    private BigDecimal total;

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public BigDecimal getDividendAccruals() {
        return dividendAccruals;
    }

    public void setDividendAccruals(BigDecimal dividendAccruals) {
        this.dividendAccruals = dividendAccruals;
    }

    public BigDecimal getInterestAccruals() {
        return interestAccruals;
    }

    public void setInterestAccruals(BigDecimal interestAccruals) {
        this.interestAccruals = interestAccruals;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", EquitySummary.class.getSimpleName() + "[", "]")
                .add("reportDate=" + reportDate)
                .add("currency=" + currency)
                .add("cash=" + cash)
                .add("stock=" + stock)
                .add("dividendAccruals=" + dividendAccruals)
                .add("interestAccruals=" + interestAccruals)
                .add("total=" + total)
                .toString();
    }
}
