package com.brinvex.util.ibkr.impl.parser;

import com.brinvex.util.ibkr.api.model.AssetSubCategory;
import com.brinvex.util.ibkr.api.model.Currency;
import com.brinvex.util.ibkr.api.model.AssetCategory;
import com.brinvex.util.ibkr.api.model.raw.BuySell;
import com.brinvex.util.ibkr.api.model.raw.CashTransaction;
import com.brinvex.util.ibkr.api.model.raw.CashTransactionType;
import com.brinvex.util.ibkr.api.model.raw.CorporateAction;
import com.brinvex.util.ibkr.api.model.raw.CorporateActionType;
import com.brinvex.util.ibkr.api.model.raw.EquitySummary;
import com.brinvex.util.ibkr.api.model.raw.FlexStatement;
import com.brinvex.util.ibkr.api.model.raw.FlexStatementType;
import com.brinvex.util.ibkr.api.model.raw.SecurityIDType;
import com.brinvex.util.ibkr.api.model.raw.Trade;
import com.brinvex.util.ibkr.api.model.raw.TradeConfirm;
import com.brinvex.util.ibkr.api.model.raw.TradeType;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("DuplicatedCode")
public class FlexStatementXmlParser {

    private static class LazyHolder {
        private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        private static final DateTimeFormatter ibkrDf = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 20230727;052240 EDT ---> 2023-07-27T05:22:40-04:00[America/New_York]
        private static final DateTimeFormatter ibkrDtf = DateTimeFormatter.ofPattern("yyyyMMdd;HHmmss z");

    }

    private static class FlexQueryResponseQN {
        static final QName type = new QName("type");
    }

    private static class FlexStatementQN {
        static final QName accountId = new QName("accountId");
        static final QName fromDate = new QName("fromDate");
        static final QName toDate = new QName("toDate");
        static final QName whenGenerated = new QName("whenGenerated");
    }

    private static class CashTransactionQN {
        static final QName currency = new QName("currency");
        static final QName description = new QName("description");
        static final QName symbol = new QName("symbol");
        static final QName listingExchange = new QName("listingExchange");
        static final QName assetCategory = new QName("assetCategory");
        static final QName subCategory = new QName("subCategory");
        static final QName figi = new QName("figi");
        static final QName isin = new QName("isin");
        static final QName dateTime = new QName("dateTime");
        static final QName settleDate = new QName("settleDate");
        static final QName amount = new QName("amount");
        static final QName type = new QName("type");
        static final QName transactionID = new QName("transactionID");
        static final QName reportDate = new QName("reportDate");
        static final QName actionID = new QName("actionID");
    }

    private static class TradeQN {
        static final QName currency = new QName("currency");
        static final QName assetCategory = new QName("assetCategory");
        static final QName subCategory = new QName("subCategory");
        static final QName symbol = new QName("symbol");
        static final QName description = new QName("description");
        static final QName securityID = new QName("securityID");
        static final QName securityIDType = new QName("securityIDType");
        static final QName figi = new QName("figi");
        static final QName isin = new QName("isin");
        static final QName listingExchange = new QName("listingExchange");
        static final QName tradeID = new QName("tradeID");
        static final QName reportDate = new QName("reportDate");
        static final QName dateTime = new QName("dateTime");
        static final QName tradeDate = new QName("tradeDate");
        static final QName settleDateTarget = new QName("settleDateTarget");
        static final QName transactionType = new QName("transactionType");
        static final QName exchange = new QName("exchange");
        static final QName quantity = new QName("quantity");
        static final QName tradePrice = new QName("tradePrice");
        static final QName tradeMoney = new QName("tradeMoney");
        static final QName proceeds = new QName("proceeds");
        static final QName taxes = new QName("taxes");
        static final QName ibCommission = new QName("ibCommission");
        static final QName ibCommissionCurrency = new QName("ibCommissionCurrency");
        static final QName netCash = new QName("netCash");
        static final QName cost = new QName("cost");
        static final QName buySell = new QName("buySell");
        static final QName transactionID = new QName("transactionID");
        static final QName ibOrderID = new QName("ibOrderID");
        static final QName orderTime = new QName("orderTime");
    }

    private static class TradeConfirmQN {
        static final QName currency = new QName("currency");
        static final QName assetCategory = new QName("assetCategory");
        static final QName subCategory = new QName("subCategory");
        static final QName symbol = new QName("symbol");
        static final QName description = new QName("description");
        static final QName securityID = new QName("securityID");
        static final QName securityIDType = new QName("securityIDType");
        static final QName figi = new QName("figi");
        static final QName isin = new QName("isin");
        static final QName listingExchange = new QName("listingExchange");
        static final QName tradeID = new QName("tradeID");
        static final QName reportDate = new QName("reportDate");
        static final QName dateTime = new QName("dateTime");
        static final QName tradeDate = new QName("tradeDate");
        static final QName settleDate = new QName("settleDate");
        static final QName transactionType = new QName("transactionType");
        static final QName exchange = new QName("exchange");
        static final QName quantity = new QName("quantity");
        static final QName price = new QName("price");
        static final QName amount = new QName("amount");
        static final QName proceeds = new QName("proceeds");
        static final QName netCash = new QName("netCash");
        static final QName tax = new QName("tax");
        static final QName commission = new QName("commission");
        static final QName commissionCurrency = new QName("commissionCurrency");
        static final QName buySell = new QName("buySell");
        static final QName orderID = new QName("orderID");
        static final QName orderTime = new QName("orderTime");
    }
    private static class CorporateActionQN {
        static final QName currency = new QName("currency");
        static final QName assetCategory = new QName("assetCategory");
        static final QName subCategory = new QName("subCategory");
        static final QName symbol = new QName("symbol");
        static final QName description = new QName("description");
        static final QName securityID = new QName("securityID");
        static final QName securityIDType = new QName("securityIDType");
        static final QName figi = new QName("figi");
        static final QName isin = new QName("isin");
        static final QName listingExchange = new QName("listingExchange");
        static final QName issuerCountryCode = new QName("issuerCountryCode");
        static final QName reportDate = new QName("reportDate");
        static final QName dateTime = new QName("dateTime");
        static final QName type = new QName("type");
        static final QName quantity = new QName("quantity");
        static final QName amount = new QName("amount");
        static final QName proceeds = new QName("proceeds");
        static final QName value = new QName("value");
        static final QName transactionID = new QName("transactionID");
        static final QName actionID = new QName("actionID");
    }

    private static class EquitySummaryQN {
        static final QName currency = new QName("currency");
        static final QName reportDate = new QName("reportDate");
        static final QName cash = new QName("cash");
        static final QName stock = new QName("stock");
        static final QName dividendAccruals = new QName("dividendAccruals");
        static final QName interestAccruals = new QName("interestAccruals");
        static final QName total = new QName("total");
    }

    @SuppressWarnings("RedundantLabeledSwitchRuleCodeBlock")
    public FlexStatement parseActivities(String statementXmlContent) {
        FlexStatement flexStatement = null;
        try {
            XMLEventReader reader = LazyHolder.xmlInputFactory.createXMLEventReader(new StringReader(statementXmlContent));
            FlexStatementType flexStatementType = null;
            while (reader.hasNext()) {
                XMLEvent xmlEvent = reader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement e = xmlEvent.asStartElement();
                    String elementName = e.getName().getLocalPart();

                    switch (elementName) {
                        case "FlexQueryResponse" -> {
                            flexStatementType = FlexStatementType.valueOf(e.getAttributeByName(FlexQueryResponseQN.type).getValue());
                        }
                        case "FlexStatement" -> {
                            if (flexStatement != null) {
                                throw new IllegalArgumentException("Unexpected xml node FlexStatement");
                            }
                            flexStatement = new FlexStatement();
                            flexStatement.setAccountId(e.getAttributeByName(FlexStatementQN.accountId).getValue());
                            flexStatement.setFromDate(parseDate(e.getAttributeByName(FlexStatementQN.fromDate).getValue()));
                            flexStatement.setToDate(parseDate(e.getAttributeByName(FlexStatementQN.toDate).getValue()));
                            flexStatement.setWhenGenerated(parseZonedDateTime(e.getAttributeByName(FlexStatementQN.whenGenerated).getValue()));
                            flexStatement.setType(requireNonNull(flexStatementType));
                        }
                        case "Trade" -> {
                            Trade trade = new Trade();
                            trade.setCurrency(Currency.valueOf(e.getAttributeByName(TradeQN.currency).getValue()));
                            trade.setAssetCategory(parseAssetCategory(e.getAttributeByName(TradeQN.assetCategory).getValue()));
                            trade.setAssetSubCategory(parseAssetSubCategory(trade.getAssetCategory(), e.getAttributeByName(TradeQN.subCategory).getValue()));
                            trade.setSymbol(e.getAttributeByName(TradeQN.symbol).getValue());
                            trade.setDescription(e.getAttributeByName(TradeQN.description).getValue());
                            trade.setSecurityID(e.getAttributeByName(TradeQN.securityID).getValue());
                            trade.setSecurityIDType(parseSecurityIDType(e.getAttributeByName(TradeQN.securityIDType).getValue()));
                            trade.setFigi(e.getAttributeByName(TradeQN.figi).getValue());
                            trade.setIsin(e.getAttributeByName(TradeQN.isin).getValue());
                            trade.setListingExchange(e.getAttributeByName(TradeQN.listingExchange).getValue());
                            trade.setTradeID(e.getAttributeByName(TradeQN.tradeID).getValue());
                            trade.setReportDate(parseDate(e.getAttributeByName(TradeQN.reportDate).getValue()));
                            trade.setDateTime(parseZonedDateTime(e.getAttributeByName(TradeQN.dateTime).getValue()));
                            trade.setTradeDate(parseDate(e.getAttributeByName(TradeQN.tradeDate).getValue()));
                            trade.setSettleDateTarget(parseDate(e.getAttributeByName(TradeQN.settleDateTarget).getValue()));
                            trade.setTransactionType(parseTradeType(e.getAttributeByName(TradeQN.transactionType).getValue()));
                            trade.setExchange(e.getAttributeByName(TradeQN.exchange).getValue());
                            trade.setQuantity(new BigDecimal(e.getAttributeByName(TradeQN.quantity).getValue()));
                            trade.setTradePrice(new BigDecimal(e.getAttributeByName(TradeQN.tradePrice).getValue()));
                            trade.setTradeMoney(new BigDecimal(e.getAttributeByName(TradeQN.tradeMoney).getValue()));
                            trade.setProceeds(new BigDecimal(e.getAttributeByName(TradeQN.proceeds).getValue()));
                            trade.setTaxes(new BigDecimal(e.getAttributeByName(TradeQN.taxes).getValue()));
                            trade.setIbCommission(new BigDecimal(e.getAttributeByName(TradeQN.ibCommission).getValue()));
                            trade.setIbCommissionCurrency(Currency.valueOf(e.getAttributeByName(TradeQN.ibCommissionCurrency).getValue()));
                            trade.setNetCash(new BigDecimal(e.getAttributeByName(TradeQN.netCash).getValue()));
                            trade.setCost(new BigDecimal(e.getAttributeByName(TradeQN.cost).getValue()));
                            trade.setBuySell(BuySell.valueOf(e.getAttributeByName(TradeQN.buySell).getValue()));
                            trade.setTransactionID(e.getAttributeByName(TradeQN.transactionID).getValue());
                            trade.setIbOrderID(e.getAttributeByName(TradeQN.ibOrderID).getValue());
                            trade.setOrderTime(parseZonedDateTime(e.getAttributeByName(TradeQN.orderTime).getValue()));

                            requireNonNull(flexStatement);
                            flexStatement.getTrades().add(trade);
                        }
                        case "TradeConfirm" -> {
                            TradeConfirm tradeConfirm = new TradeConfirm();
                            tradeConfirm.setCurrency(Currency.valueOf(e.getAttributeByName(TradeConfirmQN.currency).getValue()));
                            tradeConfirm.setAssetCategory(parseAssetCategory(e.getAttributeByName(TradeConfirmQN.assetCategory).getValue()));
                            tradeConfirm.setAssetSubCategory(parseAssetSubCategory(tradeConfirm.getAssetCategory(), e.getAttributeByName(TradeConfirmQN.subCategory).getValue()));
                            tradeConfirm.setSymbol(e.getAttributeByName(TradeConfirmQN.symbol).getValue());
                            tradeConfirm.setDescription(e.getAttributeByName(TradeConfirmQN.description).getValue());
                            tradeConfirm.setSecurityID(e.getAttributeByName(TradeConfirmQN.securityID).getValue());
                            tradeConfirm.setSecurityIDType(parseSecurityIDType(e.getAttributeByName(TradeConfirmQN.securityIDType).getValue()));
                            tradeConfirm.setFigi(e.getAttributeByName(TradeConfirmQN.figi).getValue());
                            tradeConfirm.setIsin(e.getAttributeByName(TradeConfirmQN.isin).getValue());
                            tradeConfirm.setListingExchange(e.getAttributeByName(TradeConfirmQN.listingExchange).getValue());
                            tradeConfirm.setTradeID(e.getAttributeByName(TradeConfirmQN.tradeID).getValue());
                            tradeConfirm.setReportDate(parseDate(e.getAttributeByName(TradeConfirmQN.reportDate).getValue()));
                            tradeConfirm.setDateTime(parseZonedDateTime(e.getAttributeByName(TradeConfirmQN.dateTime).getValue()));
                            tradeConfirm.setTradeDate(parseDate(e.getAttributeByName(TradeConfirmQN.tradeDate).getValue()));
                            tradeConfirm.setSettleDate(parseDate(e.getAttributeByName(TradeConfirmQN.settleDate).getValue()));
                            tradeConfirm.setTransactionType(parseTradeType(e.getAttributeByName(TradeConfirmQN.transactionType).getValue()));
                            tradeConfirm.setExchange(e.getAttributeByName(TradeConfirmQN.exchange).getValue());
                            tradeConfirm.setQuantity(new BigDecimal(e.getAttributeByName(TradeConfirmQN.quantity).getValue()));
                            tradeConfirm.setPrice(new BigDecimal(e.getAttributeByName(TradeConfirmQN.price).getValue()));
                            tradeConfirm.setAmount(new BigDecimal(e.getAttributeByName(TradeConfirmQN.amount).getValue()));
                            tradeConfirm.setProceeds(new BigDecimal(e.getAttributeByName(TradeConfirmQN.proceeds).getValue()));
                            tradeConfirm.setNetCash(new BigDecimal(e.getAttributeByName(TradeConfirmQN.netCash).getValue()));
                            tradeConfirm.setCommission(new BigDecimal(e.getAttributeByName(TradeConfirmQN.commission).getValue()));
                            tradeConfirm.setCommissionCurrency(Currency.valueOf(e.getAttributeByName(TradeConfirmQN.commissionCurrency).getValue()));
                            tradeConfirm.setTax(new BigDecimal(e.getAttributeByName(TradeConfirmQN.tax).getValue()));
                            tradeConfirm.setBuySell(BuySell.valueOf(e.getAttributeByName(TradeConfirmQN.buySell).getValue()));
                            tradeConfirm.setOrderID(e.getAttributeByName(TradeConfirmQN.orderID).getValue());
                            tradeConfirm.setOrderTime(parseZonedDateTime(e.getAttributeByName(TradeConfirmQN.orderTime).getValue()));

                            requireNonNull(flexStatement);
                            flexStatement.getTradeConfirms().add(tradeConfirm);
                        }
                        case "CashTransaction" -> {
                            CashTransaction cashTran = new CashTransaction();
                            cashTran.setCurrency(Currency.valueOf(e.getAttributeByName(CashTransactionQN.currency).getValue()));
                            cashTran.setSymbol(e.getAttributeByName(CashTransactionQN.symbol).getValue());
                            cashTran.setListingExchange(e.getAttributeByName(CashTransactionQN.listingExchange).getValue());
                            cashTran.setAssetCategory(parseAssetCategory(e.getAttributeByName(CashTransactionQN.assetCategory).getValue()));
                            cashTran.setAssetSubCategory(parseAssetSubCategory(cashTran.getAssetCategory(), e.getAttributeByName(CashTransactionQN.subCategory).getValue()));
                            cashTran.setFigi(e.getAttributeByName(CashTransactionQN.figi).getValue());
                            cashTran.setIsin(e.getAttributeByName(CashTransactionQN.isin).getValue());
                            cashTran.setDescription(e.getAttributeByName(CashTransactionQN.description).getValue());
                            cashTran.setDateTime(parseTemporal(e.getAttributeByName(CashTransactionQN.dateTime).getValue()));
                            cashTran.setSettleDate(parseDate(e.getAttributeByName(CashTransactionQN.settleDate).getValue()));
                            cashTran.setAmount(new BigDecimal(e.getAttributeByName(CashTransactionQN.amount).getValue()));
                            cashTran.setType(parseCashTransactionType(e.getAttributeByName(CashTransactionQN.type).getValue()));
                            cashTran.setTransactionID(e.getAttributeByName(CashTransactionQN.transactionID).getValue());
                            cashTran.setReportDate(parseDate(e.getAttributeByName(CashTransactionQN.reportDate).getValue()));
                            cashTran.setActionID(e.getAttributeByName(CashTransactionQN.actionID).getValue());

                            requireNonNull(flexStatement);
                            flexStatement.getCashTransactions().add(cashTran);
                        }
                        case "CorporateAction" -> {
                            CorporateAction corpAction = new CorporateAction();
                            corpAction.setCurrency(Currency.valueOf(e.getAttributeByName(CorporateActionQN.currency).getValue()));
                            corpAction.setAssetCategory(parseAssetCategory(e.getAttributeByName(CorporateActionQN.assetCategory).getValue()));
                            corpAction.setAssetSubCategory(parseAssetSubCategory(corpAction.getAssetCategory(), e.getAttributeByName(CorporateActionQN.subCategory).getValue()));
                            corpAction.setSymbol(e.getAttributeByName(CorporateActionQN.symbol).getValue());
                            corpAction.setDescription(e.getAttributeByName(CorporateActionQN.description).getValue());
                            corpAction.setSecurityID(e.getAttributeByName(CorporateActionQN.securityID).getValue());
                            corpAction.setSecurityIDType(parseSecurityIDType(e.getAttributeByName(CorporateActionQN.securityIDType).getValue()));
                            corpAction.setFigi(e.getAttributeByName(CorporateActionQN.figi).getValue());
                            corpAction.setIsin(e.getAttributeByName(CorporateActionQN.isin).getValue());
                            corpAction.setListingExchange(e.getAttributeByName(CorporateActionQN.listingExchange).getValue());
                            corpAction.setIssuerCountryCode(e.getAttributeByName(CorporateActionQN.issuerCountryCode).getValue());
                            corpAction.setReportDate(parseDate(e.getAttributeByName(CorporateActionQN.reportDate).getValue()));
                            corpAction.setDateTime(parseZonedDateTime(e.getAttributeByName(CorporateActionQN.dateTime).getValue()));
                            corpAction.setType(parseCorporateActionType(e.getAttributeByName(CorporateActionQN.type).getValue()));
                            corpAction.setQuantity(new BigDecimal(e.getAttributeByName(CorporateActionQN.quantity).getValue()));
                            corpAction.setAmount(new BigDecimal(e.getAttributeByName(CorporateActionQN.amount).getValue()));
                            corpAction.setProceeds(new BigDecimal(e.getAttributeByName(CorporateActionQN.proceeds).getValue()));
                            corpAction.setValue(new BigDecimal(e.getAttributeByName(CorporateActionQN.value).getValue()));
                            corpAction.setTransactionId(e.getAttributeByName(CorporateActionQN.transactionID).getValue());
                            corpAction.setActionID(e.getAttributeByName(CorporateActionQN.actionID).getValue());

                            requireNonNull(flexStatement);
                            flexStatement.getCorporateActions().add(corpAction);
                        }
                    }
                }
            }

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }

        if (flexStatement == null) {
            throw new IllegalArgumentException("Could not parse: " + statementXmlContent);
        }
        return flexStatement;
    }

    public FlexStatement parseEquitySummaries(String statementXmlContent) {
        FlexStatement flexStatement = null;
        FlexStatementType flexStatementType = null;
        try {
            XMLEventReader reader = LazyHolder.xmlInputFactory.createXMLEventReader(new StringReader(statementXmlContent));
            while (reader.hasNext()) {
                XMLEvent xmlEvent = reader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement e = xmlEvent.asStartElement();
                    String elementName = e.getName().getLocalPart();

                    switch (elementName) {
                        case "FlexQueryResponse" -> {
                            flexStatementType = FlexStatementType.valueOf(e.getAttributeByName(FlexQueryResponseQN.type).getValue());
                        }
                        case "FlexStatement" -> {
                            if (flexStatement != null) {
                                throw new IllegalArgumentException("Unexpected xml node FlexStatement");
                            }
                            flexStatement = new FlexStatement();
                            flexStatement.setType(requireNonNull(flexStatementType));
                            flexStatement.setAccountId(e.getAttributeByName(FlexStatementQN.accountId).getValue());
                            flexStatement.setFromDate(parseDate(e.getAttributeByName(FlexStatementQN.fromDate).getValue()));
                            flexStatement.setToDate(parseDate(e.getAttributeByName(FlexStatementQN.toDate).getValue()));
                            flexStatement.setWhenGenerated(parseZonedDateTime(e.getAttributeByName(FlexStatementQN.whenGenerated).getValue()));
                        }
                        case "EquitySummaryByReportDateInBase" -> {
                            EquitySummary equitySummary = new EquitySummary();
                            equitySummary.setCurrency(Currency.valueOf(e.getAttributeByName(EquitySummaryQN.currency).getValue()));
                            equitySummary.setReportDate(parseDate(e.getAttributeByName(EquitySummaryQN.reportDate).getValue()));
                            equitySummary.setCash(new BigDecimal(e.getAttributeByName(EquitySummaryQN.cash).getValue()));
                            equitySummary.setStock(new BigDecimal(e.getAttributeByName(EquitySummaryQN.stock).getValue()));
                            equitySummary.setDividendAccruals(new BigDecimal(e.getAttributeByName(EquitySummaryQN.dividendAccruals).getValue()));
                            equitySummary.setInterestAccruals(new BigDecimal(e.getAttributeByName(EquitySummaryQN.interestAccruals).getValue()));
                            equitySummary.setTotal(new BigDecimal(e.getAttributeByName(EquitySummaryQN.total).getValue()));

                            requireNonNull(flexStatement);
                            flexStatement.getEquitySummaries().add(equitySummary);
                        }
                    }
                }
            }

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }

        if (flexStatement == null) {
            throw new IllegalArgumentException("Could not parse: " + statementXmlContent);
        }
        return flexStatement;
    }

    private SecurityIDType parseSecurityIDType(String str) {
        return str == null || str.isBlank() ? null : SecurityIDType.valueOf(str);
    }

    protected Temporal parseTemporal(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        if (str.indexOf(';') > -1) {
            return ZonedDateTime.parse(str, LazyHolder.ibkrDtf);
        } else {
            return LocalDate.parse(str, LazyHolder.ibkrDf);
        }
    }

    protected ZonedDateTime parseZonedDateTime(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        if (str.indexOf(';') > -1) {
            return ZonedDateTime.parse(str, LazyHolder.ibkrDtf);
        } else {
            throw new IllegalArgumentException("Unexpected format: " + str);
        }
    }

    private LocalDate parseDate(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return LocalDate.parse(str, LazyHolder.ibkrDf);
    }

    private CashTransactionType parseCashTransactionType(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return switch (str) {
            case "Deposits/Withdrawals" -> CashTransactionType.Deposits_Withdrawals;
            case "Withholding Tax" -> CashTransactionType.Withholding_Tax;
            case "Dividends" -> CashTransactionType.Dividends;
            case "Payment In Lieu Of Dividends" -> CashTransactionType.Payment_In_Lieu_Of_Dividends;
            case "Other Fees" -> CashTransactionType.Other_Fees;
            case "Broker Interest Paid" -> CashTransactionType.Broker_Interest_Paid;
            case "Broker Fees" -> CashTransactionType.Broker_Fees;
            default -> throw new IllegalStateException("Unexpected value: " + str);
        };
    }

    private TradeType parseTradeType(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return switch (str) {
            case "ExchTrade" -> TradeType.ExchTrade;
            case "FracShare" -> TradeType.FracShare;
            default -> throw new IllegalStateException("Unexpected value: " + str);
        };
    }

    private CorporateActionType parseCorporateActionType(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return switch (str) {
            case "SO" -> CorporateActionType.SO;
            case "TC" -> CorporateActionType.TC;
            case "FS" -> CorporateActionType.FS;
            default -> throw new IllegalStateException("Unexpected value: " + str);
        };
    }

    private AssetCategory parseAssetCategory(String assetCategoryStr) {
        return assetCategoryStr == null || assetCategoryStr.isBlank() ? null : AssetCategory.valueOf(assetCategoryStr);
    }

    private AssetSubCategory parseAssetSubCategory(AssetCategory cat, String assetSubCategoryStr) {
        if (cat == null) {
            if (assetSubCategoryStr != null && !assetSubCategoryStr.isBlank()) {
                throw new IllegalArgumentException("Unexpected assetSubCategoryStr=" + assetSubCategoryStr);
            }
            return null;
        }
        if (cat == AssetCategory.CASH) {
            if (assetSubCategoryStr != null && !assetSubCategoryStr.isBlank()) {
                throw new IllegalArgumentException("Unexpected assetSubCategoryStr=" + assetSubCategoryStr);
            }
            return AssetSubCategory.CASH;
        }
        return assetSubCategoryStr == null || assetSubCategoryStr.isBlank() ?
                null : AssetSubCategory.valueOf(cat.name() + "_" + assetSubCategoryStr);
    }


}
