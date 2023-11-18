package com.brinvex.util.ibkr.api.model.raw;

import com.brinvex.util.ibkr.api.model.AssetCategory;
import com.brinvex.util.ibkr.api.model.Currency;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.StringJoiner;

public class CashTransaction implements Activity, Serializable {

    protected Temporal dateTime;

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

    private LocalDate tradeDate;

    private LocalDate settleDateTarget;

    private CashTransactionType type;

    private String exchange;

    private BigDecimal quantity;

    private BigDecimal tradePrice;

    private BigDecimal tradeMoney;

    private BigDecimal proceeds;

    private BigDecimal taxes;

    private BigDecimal ibCommission;

    private Currency ibCommissionCurrency;

    private BigDecimal netCash;

    private BigDecimal cost;

    private BuySell buySell;

    private String transactionID;

    private String ibOrderID;

    private LocalDateTime orderTime;

    private String actionId;

    private LocalDate settleDate;

    private BigDecimal amount;

    public Temporal getDateTime() {
        return dateTime;
    }

    public void setDateTime(Temporal dateTime) {
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

    public LocalDate getSettleDateTarget() {
        return settleDateTarget;
    }

    public void setSettleDateTarget(LocalDate settleDateTarget) {
        this.settleDateTarget = settleDateTarget;
    }

    public CashTransactionType getType() {
        return type;
    }

    public void setType(CashTransactionType type) {
        this.type = type;
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

    public BigDecimal getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(BigDecimal tradePrice) {
        this.tradePrice = tradePrice;
    }

    public BigDecimal getTradeMoney() {
        return tradeMoney;
    }

    public void setTradeMoney(BigDecimal tradeMoney) {
        this.tradeMoney = tradeMoney;
    }

    public BigDecimal getProceeds() {
        return proceeds;
    }

    public void setProceeds(BigDecimal proceeds) {
        this.proceeds = proceeds;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public void setTaxes(BigDecimal taxes) {
        this.taxes = taxes;
    }

    public BigDecimal getIbCommission() {
        return ibCommission;
    }

    public void setIbCommission(BigDecimal ibCommission) {
        this.ibCommission = ibCommission;
    }

    public Currency getIbCommissionCurrency() {
        return ibCommissionCurrency;
    }

    public void setIbCommissionCurrency(Currency ibCommissionCurrency) {
        this.ibCommissionCurrency = ibCommissionCurrency;
    }

    public BigDecimal getNetCash() {
        return netCash;
    }

    public void setNetCash(BigDecimal netCash) {
        this.netCash = netCash;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BuySell getBuySell() {
        return buySell;
    }

    public void setBuySell(BuySell buySell) {
        this.buySell = buySell;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getIbOrderID() {
        return ibOrderID;
    }

    public void setIbOrderID(String ibOrderID) {
        this.ibOrderID = ibOrderID;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }


    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public LocalDate getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(LocalDate settleDate) {
        this.settleDate = settleDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CashTransaction.class.getSimpleName() + "[", "]")
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
                .add("settleDateTarget=" + settleDateTarget)
                .add("type=" + type)
                .add("exchange='" + exchange + "'")
                .add("quantity=" + quantity)
                .add("tradePrice=" + tradePrice)
                .add("tradeMoney=" + tradeMoney)
                .add("proceeds=" + proceeds)
                .add("taxes=" + taxes)
                .add("ibCommission=" + ibCommission)
                .add("ibCommissionCurrency=" + ibCommissionCurrency)
                .add("netCash=" + netCash)
                .add("cost=" + cost)
                .add("buySell=" + buySell)
                .add("transactionID='" + transactionID + "'")
                .add("ibOrderID='" + ibOrderID + "'")
                .add("orderTime=" + orderTime)
                .add("actionId='" + actionId + "'")
                .add("settleDate=" + settleDate)
                .add("amount=" + amount)
                .toString();
    }
}
