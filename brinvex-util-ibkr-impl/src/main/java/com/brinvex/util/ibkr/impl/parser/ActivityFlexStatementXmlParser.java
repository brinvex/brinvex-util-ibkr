package com.brinvex.util.ibkr.impl.parser;

import com.brinvex.util.ibkr.api.model.Currency;
import com.brinvex.util.ibkr.api.model.raw.AssetCategory;
import com.brinvex.util.ibkr.api.model.raw.BuySell;
import com.brinvex.util.ibkr.api.model.raw.CashTransaction;
import com.brinvex.util.ibkr.api.model.raw.CashTransactionType;
import com.brinvex.util.ibkr.api.model.raw.FlexStatement;
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

public class ActivityFlexStatementXmlParser {

    private static class LazyHolder {
        private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        private static final DateTimeFormatter ibkrDf = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 20230727;052240 EDT ---> 2023-07-27T05:22:40-04:00[America/New_York]
        private static final DateTimeFormatter ibkrDtf = DateTimeFormatter.ofPattern("yyyyMMdd;HHmmss z");

    }

    private static class FlexStatementQN {
        private static final QName accountId = new QName("accountId");
        private static final QName fromDate = new QName("fromDate");
        private static final QName toDate = new QName("toDate");
        private static final QName whenGenerated = new QName("whenGenerated");
    }

    private static class CashTransactionQN {
        private static final QName accountId = new QName("accountId");
        private static final QName currency = new QName("currency");
        private static final QName description = new QName("description");
        private static final QName symbol = new QName("symbol");
        private static final QName listingExchange = new QName("listingExchange");
        private static final QName isin = new QName("isin");
        private static final QName dateTime = new QName("dateTime");
        private static final QName settleDate = new QName("settleDate");
        private static final QName amount = new QName("amount");
        private static final QName type = new QName("type");
        private static final QName transactionID = new QName("transactionID");
        private static final QName reportDate = new QName("reportDate");
        private static final QName actionID = new QName("actionID");
    }

    private static class TradeQN {
        private static final QName accountId = new QName("accountId");
        private static final QName currency = new QName("currency");
        private static final QName assetCategory = new QName("assetCategory");
        private static final QName symbol = new QName("symbol");
        private static final QName description = new QName("description");
        private static final QName securityID = new QName("securityID");
        private static final QName securityIDType = new QName("securityIDType");
        private static final QName isin = new QName("isin");
        private static final QName listingExchange = new QName("listingExchange");
        private static final QName tradeID = new QName("tradeID");
        private static final QName reportDate = new QName("reportDate");
        private static final QName dateTime = new QName("dateTime");
        private static final QName tradeDate = new QName("tradeDate");
        private static final QName settleDateTarget = new QName("settleDateTarget");
        private static final QName transactionType = new QName("transactionType");
        private static final QName exchange = new QName("exchange");
        private static final QName quantity = new QName("quantity");
        private static final QName tradePrice = new QName("tradePrice");
        private static final QName tradeMoney = new QName("tradeMoney");
        private static final QName proceeds = new QName("proceeds");
        private static final QName taxes = new QName("taxes");
        private static final QName ibCommission = new QName("ibCommission");
        private static final QName ibCommissionCurrency = new QName("ibCommissionCurrency");
        private static final QName netCash = new QName("netCash");
        private static final QName cost = new QName("cost");
        private static final QName buySell = new QName("buySell");
        private static final QName transactionID = new QName("transactionID");
        private static final QName ibOrderID = new QName("ibOrderID");
        private static final QName orderTime = new QName("orderTime");
    }

    private static class TradeConfirmQN {
        private static final QName accountId = new QName("accountId");
        private static final QName currency = new QName("currency");
        private static final QName assetCategory = new QName("assetCategory");
        private static final QName symbol = new QName("symbol");
        private static final QName description = new QName("description");
        private static final QName securityID = new QName("securityID");
        private static final QName securityIDType = new QName("securityIDType");
        private static final QName isin = new QName("isin");
        private static final QName listingExchange = new QName("listingExchange");
        private static final QName tradeID = new QName("tradeID");
        private static final QName reportDate = new QName("reportDate");
        private static final QName dateTime = new QName("dateTime");
        private static final QName tradeDate = new QName("tradeDate");
        private static final QName settleDate = new QName("settleDate");
        private static final QName transactionType = new QName("transactionType");
        private static final QName exchange = new QName("exchange");
        private static final QName quantity = new QName("quantity");
        private static final QName price = new QName("price");
        private static final QName amount = new QName("amount");
        private static final QName proceeds = new QName("proceeds");
        private static final QName netCash = new QName("netCash");
        private static final QName tax = new QName("tax");
        private static final QName commission = new QName("commission");
        private static final QName commissionCurrency = new QName("commissionCurrency");
        private static final QName buySell = new QName("buySell");
        private static final QName orderID = new QName("orderID");
        private static final QName orderTime = new QName("orderTime");
    }

    public FlexStatement parseStatement(String statementXmlContent) {
        FlexStatement flexStatement = null;
        String accountId = null;
        try {
            XMLEventReader reader = LazyHolder.xmlInputFactory.createXMLEventReader(new StringReader(statementXmlContent));
            while (reader.hasNext()) {
                XMLEvent xmlEvent = reader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement e = xmlEvent.asStartElement();
                    String elementName = e.getName().getLocalPart();

                    switch (elementName) {
                        case "FlexStatement" -> {
                            if (flexStatement != null) {
                                throw new IllegalArgumentException("Unexpected xml node FlexStatement");
                            }
                            accountId = e.getAttributeByName(FlexStatementQN.accountId).getValue();
                            flexStatement = new FlexStatement();
                            flexStatement.setAccountId(accountId);
                            flexStatement.setFromDate(parseDate(e.getAttributeByName(FlexStatementQN.fromDate).getValue()));
                            flexStatement.setToDate(parseDate(e.getAttributeByName(FlexStatementQN.toDate).getValue()));
                            flexStatement.setWhenGenerated(parseZonedDateTime(e.getAttributeByName(FlexStatementQN.whenGenerated).getValue()));
                        }
                        case "Trade" -> {
                            String tradeAccountId = e.getAttributeByName(TradeQN.accountId).getValue();
                            if (accountId == null || !accountId.equals(tradeAccountId)) {
                                throw new IllegalArgumentException("accountId mismatch: %s != %s".formatted(accountId, tradeAccountId));
                            }
                            Trade trade = new Trade();
                            trade.setCurrency(Currency.valueOf(e.getAttributeByName(TradeQN.currency).getValue()));
                            trade.setAssetCategory(AssetCategory.valueOf(e.getAttributeByName(TradeQN.assetCategory).getValue()));
                            trade.setSymbol(e.getAttributeByName(TradeQN.symbol).getValue());
                            trade.setDescription(e.getAttributeByName(TradeQN.description).getValue());
                            trade.setSecurityID(e.getAttributeByName(TradeQN.securityID).getValue());
                            trade.setSecurityIDType(parseSecurityIDType(e.getAttributeByName(TradeQN.securityIDType).getValue()));
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
                            String tradeConfAccountId = e.getAttributeByName(TradeConfirmQN.accountId).getValue();
                            if (accountId == null || !accountId.equals(tradeConfAccountId)) {
                                throw new IllegalArgumentException("accountId mismatch: %s != %s".formatted(accountId, tradeConfAccountId));
                            }
                            TradeConfirm tradeConfirm = new TradeConfirm();
                            tradeConfirm.setCurrency(Currency.valueOf(e.getAttributeByName(TradeConfirmQN.currency).getValue()));
                            tradeConfirm.setAssetCategory(AssetCategory.valueOf(e.getAttributeByName(TradeConfirmQN.assetCategory).getValue()));
                            tradeConfirm.setSymbol(e.getAttributeByName(TradeConfirmQN.symbol).getValue());
                            tradeConfirm.setDescription(e.getAttributeByName(TradeConfirmQN.description).getValue());
                            tradeConfirm.setSecurityID(e.getAttributeByName(TradeConfirmQN.securityID).getValue());
                            tradeConfirm.setSecurityIDType(parseSecurityIDType(e.getAttributeByName(TradeConfirmQN.securityIDType).getValue()));
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
                            String cashTranAccountId = e.getAttributeByName(CashTransactionQN.accountId).getValue();
                            if (accountId == null || !accountId.equals(cashTranAccountId)) {
                                throw new IllegalArgumentException("accountId mismatch: %s != %s".formatted(accountId, cashTranAccountId));
                            }
                            cashTran.setCurrency(Currency.valueOf(e.getAttributeByName(CashTransactionQN.currency).getValue()));
                            cashTran.setSymbol(e.getAttributeByName(CashTransactionQN.symbol).getValue());
                            cashTran.setListingExchange(e.getAttributeByName(CashTransactionQN.listingExchange).getValue());
                            cashTran.setIsin(e.getAttributeByName(CashTransactionQN.isin).getValue());
                            cashTran.setDescription(e.getAttributeByName(CashTransactionQN.description).getValue());
                            cashTran.setDateTime(parseTemporal(e.getAttributeByName(CashTransactionQN.dateTime).getValue()));
                            cashTran.setSettleDate(parseDate(e.getAttributeByName(CashTransactionQN.settleDate).getValue()));
                            cashTran.setAmount(new BigDecimal(e.getAttributeByName(CashTransactionQN.amount).getValue()));
                            cashTran.setType(parseCashTransactionType(e.getAttributeByName(CashTransactionQN.type).getValue()));
                            cashTran.setTransactionID(e.getAttributeByName(CashTransactionQN.transactionID).getValue());
                            cashTran.setReportDate(parseDate(e.getAttributeByName(CashTransactionQN.reportDate).getValue()));
                            cashTran.setActionId(e.getAttributeByName(CashTransactionQN.actionID).getValue());

                            requireNonNull(flexStatement);
                            flexStatement.getCashTransactions().add(cashTran);
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
            case "Other Fees" -> CashTransactionType.Other_Fees;
            default -> throw new IllegalStateException("Unexpected value: " + str);
        };
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private TradeType parseTradeType(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return switch (str) {
            case "ExchTrade" -> TradeType.ExchTrade;
            default -> throw new IllegalStateException("Unexpected value: " + str);
        };
    }

}
