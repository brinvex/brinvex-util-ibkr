package com.brinvex.util.ibkr.impl.parser;

import com.brinvex.util.ibkr.api.model.raw.AssetCategory;
import com.brinvex.util.ibkr.api.model.raw.BuySell;
import com.brinvex.util.ibkr.api.model.raw.RawTransactionType;
import com.brinvex.util.ibkr.api.model.Currency;
import com.brinvex.util.ibkr.api.model.raw.FlexStatement;
import com.brinvex.util.ibkr.api.model.raw.SecurityIDType;
import com.brinvex.util.ibkr.api.model.raw.RawTransaction;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNull;

public class ActivityFlexStatementXmlParser {

    private static class LazyHolder {
        private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        private static final DateTimeFormatter ibkrDf = DateTimeFormatter.ofPattern("yyyyMMdd");
        private static final DateTimeFormatter ibkrDtf = DateTimeFormatter.ofPattern("yyyyMMdd;HHmmss");

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

    public FlexStatement parseStatement(String statementXmlContent) {
        FlexStatement flexStatement = null;
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
                            flexStatement = new FlexStatement();
                            flexStatement.setAccountId(e.getAttributeByName(FlexStatementQN.accountId).getValue());
                            flexStatement.setFromDate(parseDate(e.getAttributeByName(FlexStatementQN.fromDate).getValue()));
                            flexStatement.setToDate(parseDate(e.getAttributeByName(FlexStatementQN.toDate).getValue()));
                            flexStatement.setWhenGenerated(parseDateTime(e.getAttributeByName(FlexStatementQN.whenGenerated).getValue()));
                        }
                        case "Trade" -> {
                            RawTransaction cashTran = new RawTransaction();
                            cashTran.setAccountId(e.getAttributeByName(TradeQN.accountId).getValue());
                            cashTran.setCurrency(Currency.valueOf(e.getAttributeByName(TradeQN.currency).getValue()));
                            cashTran.setAssetCategory(AssetCategory.valueOf(e.getAttributeByName(TradeQN.assetCategory).getValue()));
                            cashTran.setSymbol(e.getAttributeByName(TradeQN.symbol).getValue());
                            cashTran.setDescription(e.getAttributeByName(TradeQN.description).getValue());
                            cashTran.setSecurityID(e.getAttributeByName(TradeQN.securityID).getValue());
                            cashTran.setSecurityIDType(parseSecurityIDType(e.getAttributeByName(TradeQN.securityIDType).getValue()));
                            cashTran.setIsin(e.getAttributeByName(TradeQN.isin).getValue());
                            cashTran.setListingExchange(e.getAttributeByName(TradeQN.listingExchange).getValue());
                            cashTran.setTradeID(e.getAttributeByName(TradeQN.tradeID).getValue());
                            cashTran.setReportDate(parseDate(e.getAttributeByName(TradeQN.reportDate).getValue()));
                            cashTran.setDateTime(parseDateTime(e.getAttributeByName(TradeQN.dateTime).getValue()));
                            cashTran.setTradeDate(parseDate(e.getAttributeByName(TradeQN.tradeDate).getValue()));
                            cashTran.setSettleDateTarget(parseDate(e.getAttributeByName(TradeQN.settleDateTarget).getValue()));
                            cashTran.setType(parseCashTransactionType(e.getAttributeByName(TradeQN.transactionType).getValue()));
                            cashTran.setExchange(e.getAttributeByName(TradeQN.exchange).getValue());
                            cashTran.setQuantity(new BigDecimal(e.getAttributeByName(TradeQN.quantity).getValue()));
                            cashTran.setTradePrice(new BigDecimal(e.getAttributeByName(TradeQN.tradePrice).getValue()));
                            cashTran.setTradeMoney(new BigDecimal(e.getAttributeByName(TradeQN.tradeMoney).getValue()));
                            cashTran.setProceeds(new BigDecimal(e.getAttributeByName(TradeQN.proceeds).getValue()));
                            cashTran.setTaxes(new BigDecimal(e.getAttributeByName(TradeQN.taxes).getValue()));
                            cashTran.setIbCommission(new BigDecimal(e.getAttributeByName(TradeQN.ibCommission).getValue()));
                            cashTran.setIbCommissionCurrency(Currency.valueOf(e.getAttributeByName(TradeQN.ibCommissionCurrency).getValue()));
                            cashTran.setNetCash(new BigDecimal(e.getAttributeByName(TradeQN.netCash).getValue()));
                            cashTran.setCost(new BigDecimal(e.getAttributeByName(TradeQN.cost).getValue()));
                            cashTran.setBuySell(BuySell.valueOf(e.getAttributeByName(TradeQN.buySell).getValue()));
                            cashTran.setTransactionID(e.getAttributeByName(TradeQN.transactionID).getValue());
                            cashTran.setIbOrderID(e.getAttributeByName(TradeQN.ibOrderID).getValue());
                            cashTran.setOrderTime(parseDateTime(e.getAttributeByName(TradeQN.orderTime).getValue()));

                            requireNonNull(flexStatement);
                            flexStatement.getTransactions().add(cashTran);
                        }
                        case "CashTransaction" -> {
                            RawTransaction cashTran = new RawTransaction();
                            cashTran.setAccountId(e.getAttributeByName(CashTransactionQN.accountId).getValue());
                            cashTran.setCurrency(Currency.valueOf(e.getAttributeByName(CashTransactionQN.currency).getValue()));
                            cashTran.setSymbol(e.getAttributeByName(CashTransactionQN.symbol).getValue());
                            cashTran.setListingExchange(e.getAttributeByName(CashTransactionQN.listingExchange).getValue());
                            cashTran.setIsin(e.getAttributeByName(CashTransactionQN.isin).getValue());
                            cashTran.setDescription(e.getAttributeByName(CashTransactionQN.description).getValue());
                            cashTran.setDateTime(parseDateTime(e.getAttributeByName(CashTransactionQN.dateTime).getValue()));
                            cashTran.setSettleDate(parseDate(e.getAttributeByName(CashTransactionQN.settleDate).getValue()));
                            cashTran.setAmount(new BigDecimal(e.getAttributeByName(CashTransactionQN.amount).getValue()));
                            cashTran.setType(parseCashTransactionType(e.getAttributeByName(CashTransactionQN.type).getValue()));
                            cashTran.setTransactionID(e.getAttributeByName(CashTransactionQN.transactionID).getValue());
                            cashTran.setReportDate(parseDate(e.getAttributeByName(CashTransactionQN.reportDate).getValue()));
                            cashTran.setActionId(e.getAttributeByName(CashTransactionQN.actionID).getValue());

                            requireNonNull(flexStatement);
                            flexStatement.getTransactions().add(cashTran);
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

    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        if (str.indexOf(';') > -1) {
            return LocalDateTime.parse(str, LazyHolder.ibkrDtf);
        } else {
            return LocalDate.parse(str, LazyHolder.ibkrDf).atStartOfDay();
        }
    }

    private LocalDate parseDate(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return LocalDate.parse(str, LazyHolder.ibkrDf);
    }

    private RawTransactionType parseCashTransactionType(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return switch (str) {
            case "ExchTrade" -> RawTransactionType.ExchTrade;
            case "Deposits/Withdrawals" -> RawTransactionType.Deposits_Withdrawals;
            case "Withholding Tax" -> RawTransactionType.Withholding_Tax;
            case "Dividends" -> RawTransactionType.Dividends;
            case "Other Fees" -> RawTransactionType.Other_Fees;
            default -> throw new IllegalStateException("Unexpected value: " + str);
        };
    }

}
