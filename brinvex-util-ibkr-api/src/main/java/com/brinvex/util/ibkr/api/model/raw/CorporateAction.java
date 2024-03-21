package com.brinvex.util.ibkr.api.model.raw;

import com.brinvex.util.ibkr.api.model.AssetCategory;
import com.brinvex.util.ibkr.api.model.AssetSubCategory;
import com.brinvex.util.ibkr.api.model.Currency;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.StringJoiner;

public class CorporateAction implements Activity, Serializable {

    private Currency currency;

    private AssetCategory assetCategory;

    private AssetSubCategory assetSubCategory;

    private String symbol;

    private String description;

    private String securityID;

    private SecurityIDType securityIDType;

    private String figi;

    private String isin;

    private String listingExchange;

    private String issuerCountryCode;

    private LocalDate reportDate;

    private ZonedDateTime dateTime;

    private CorporateActionType type;

    private BigDecimal quantity;

    private BigDecimal amount;

    private BigDecimal proceeds;

    private BigDecimal value;

    private String transactionId;

    private String actionID;

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

    public AssetSubCategory getAssetSubCategory() {
        return assetSubCategory;
    }

    public CorporateAction setAssetSubCategory(AssetSubCategory assetSubCategory) {
        this.assetSubCategory = assetSubCategory;
        return this;
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

    public String getIssuerCountryCode() {
        return issuerCountryCode;
    }

    public void setIssuerCountryCode(String issuerCountryCode) {
        this.issuerCountryCode = issuerCountryCode;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public CorporateActionType getType() {
        return type;
    }

    public void setType(CorporateActionType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
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

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getActionID() {
        return actionID;
    }

    public void setActionID(String actionID) {
        this.actionID = actionID;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CorporateAction.class.getSimpleName() + "[", "]")
                .add("currency=" + currency)
                .add("assetCategory=" + assetCategory)
                .add("assetSubCategory=" + assetSubCategory)
                .add("symbol='" + symbol + "'")
                .add("description='" + description + "'")
                .add("securityID='" + securityID + "'")
                .add("securityIDType=" + securityIDType)
                .add("figi='" + figi + "'")
                .add("isin='" + isin + "'")
                .add("listingExchange='" + listingExchange + "'")
                .add("issuerCountryCode='" + issuerCountryCode + "'")
                .add("reportDate=" + reportDate)
                .add("dateTime=" + dateTime)
                .add("type=" + type)
                .add("quantity=" + quantity)
                .add("amount=" + amount)
                .add("proceeds=" + proceeds)
                .add("value=" + value)
                .add("transactionId='" + transactionId + "'")
                .add("actionId='" + actionID + "'")
                .toString();
    }
}
