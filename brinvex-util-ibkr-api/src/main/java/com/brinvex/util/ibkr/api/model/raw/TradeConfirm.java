package com.brinvex.util.ibkr.api.model.raw;

import com.brinvex.util.ibkr.api.model.AssetCategory;
import com.brinvex.util.ibkr.api.model.Currency;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.StringJoiner;

public class TradeConfirm implements Activity, Serializable {

    private Currency currency;

    private AssetCategory assetCategory;

    private String symbol;

    private String description;

    private String securityID;

    private SecurityIDType securityIDType;

    private String figi;

    private String isin;

    private String listingExchange;

    private String tradeID;

    private LocalDate reportDate;

    private ZonedDateTime dateTime;

    private LocalDate tradeDate;

    private LocalDate settleDate;

    private TradeType transactionType;

    private String exchange;

    private BigDecimal quantity;

    private BigDecimal price;

    private BigDecimal amount;

    private BigDecimal proceeds;

    private BigDecimal netCash;

    private BigDecimal commission;

    private Currency commissionCurrency;

    private BigDecimal tax;

    private BuySell buySell;

    private String orderID;

    private ZonedDateTime orderTime;

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public AssetCategory getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(AssetCategory assetCategory) {
        this.assetCategory = assetCategory;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSecurityID() {
        return securityID;
    }

    public void setSecurityID(String securityID) {
        this.securityID = securityID;
    }

    public SecurityIDType getSecurityIDType() {
        return securityIDType;
    }

    public void setSecurityIDType(SecurityIDType securityIDType) {
        this.securityIDType = securityIDType;
    }

    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getListingExchange() {
        return listingExchange;
    }

    public void setListingExchange(String listingExchange) {
        this.listingExchange = listingExchange;
    }

    public String getTradeID() {
        return tradeID;
    }

    public void setTradeID(String tradeID) {
        this.tradeID = tradeID;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public LocalDate getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(LocalDate settleDate) {
        this.settleDate = settleDate;
    }

    public TradeType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TradeType transactionType) {
        this.transactionType = transactionType;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getProceeds() {
        return proceeds;
    }

    public void setProceeds(BigDecimal proceeds) {
        this.proceeds = proceeds;
    }

    public BigDecimal getNetCash() {
        return netCash;
    }

    public void setNetCash(BigDecimal netCash) {
        this.netCash = netCash;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public Currency getCommissionCurrency() {
        return commissionCurrency;
    }

    public void setCommissionCurrency(Currency commissionCurrency) {
        this.commissionCurrency = commissionCurrency;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BuySell getBuySell() {
        return buySell;
    }

    public void setBuySell(BuySell buySell) {
        this.buySell = buySell;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public ZonedDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(ZonedDateTime orderTime) {
        this.orderTime = orderTime;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TradeConfirm.class.getSimpleName() + "[", "]")
                .add("dateTime=" + dateTime)
                .add("currency=" + currency)
                .add("assetCategory=" + assetCategory)
                .add("symbol='" + symbol + "'")
                .add("description='" + description + "'")
                .add("securityID='" + securityID + "'")
                .add("securityIDType=" + securityIDType)
                .add("figi='" + figi + "'")
                .add("isin='" + isin + "'")
                .add("listingExchange='" + listingExchange + "'")
                .add("tradeID='" + tradeID + "'")
                .add("reportDate=" + reportDate)
                .add("tradeDate=" + tradeDate)
                .add("settleDate=" + settleDate)
                .add("transactionType=" + transactionType)
                .add("exchange='" + exchange + "'")
                .add("quantity=" + quantity)
                .add("price=" + price)
                .add("amount=" + amount)
                .add("proceeds=" + proceeds)
                .add("netCash=" + netCash)
                .add("commission=" + commission)
                .add("commissionCurrency=" + commissionCurrency)
                .add("tax=" + tax)
                .add("buySell=" + buySell)
                .add("orderID='" + orderID + "'")
                .add("orderTime=" + orderTime)
                .toString();
    }
}
