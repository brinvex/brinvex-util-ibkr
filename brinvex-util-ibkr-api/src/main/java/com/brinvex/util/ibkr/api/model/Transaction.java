package com.brinvex.util.ibkr.api.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.StringJoiner;

public class Transaction implements Serializable {

    private String id;

    private Temporal date;

    private TransactionType type;

    private Country country;

    private String symbol;

    private String isin;

    private String figi;

    private AssetCategory assetCategory;

    private BigDecimal qty;

    private Currency currency;

    private BigDecimal price;

    private BigDecimal grossValue;

    private BigDecimal netValue;

    private BigDecimal tax;

    private BigDecimal fees;

    private LocalDate settleDate;

    private String bunchId;

    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Temporal getDate() {
        return date;
    }

    public void setDate(Temporal date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public AssetCategory getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(AssetCategory assetCategory) {
        this.assetCategory = assetCategory;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(BigDecimal grossValue) {
        this.grossValue = grossValue;
    }

    public BigDecimal getNetValue() {
        return netValue;
    }

    public void setNetValue(BigDecimal netValue) {
        this.netValue = netValue;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public LocalDate getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(LocalDate settleDate) {
        this.settleDate = settleDate;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public String getBunchId() {
        return bunchId;
    }

    public void setBunchId(String bunchId) {
        this.bunchId = bunchId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Transaction.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("date=" + date)
                .add("type=" + type)
                .add("country=" + country)
                .add("symbol='" + symbol + "'")
                .add("assetCategory='" + assetCategory + "'")
                .add("figi='" + figi + "'")
                .add("isin='" + isin + "'")
                .add("qty=" + qty)
                .add("ccy=" + currency)
                .add("price=" + price)
                .add("grossValue=" + grossValue)
                .add("netValue=" + netValue)
                .add("tax=" + tax)
                .add("fees=" + fees)
                .add("settleDate=" + settleDate)
                .add("bunchId='" + bunchId + "'")
                .add("description='" + description + "'")
                .toString();
    }
}
