/*
 * Copyright © 2023 Brinvex (dev@brinvex.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.brinvex.util.ibkr.impl;

import com.brinvex.util.ibkr.api.model.AssetSubCategory;
import com.brinvex.util.ibkr.api.model.Country;
import com.brinvex.util.ibkr.api.model.Currency;
import com.brinvex.util.ibkr.api.model.Transaction;
import com.brinvex.util.ibkr.api.model.TransactionType;
import com.brinvex.util.ibkr.api.model.AssetCategory;
import com.brinvex.util.ibkr.api.model.raw.BuySell;
import com.brinvex.util.ibkr.api.model.raw.CashTransaction;
import com.brinvex.util.ibkr.api.model.raw.CashTransactionType;
import com.brinvex.util.ibkr.api.model.raw.CorporateAction;
import com.brinvex.util.ibkr.api.model.raw.CorporateActionType;
import com.brinvex.util.ibkr.api.model.raw.Trade;
import com.brinvex.util.ibkr.api.model.raw.TradeConfirm;
import com.brinvex.util.ibkr.api.model.raw.TradeType;
import com.brinvex.util.ibkr.api.service.exception.IbkrServiceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.brinvex.util.ibkr.impl.ValidationUtil.assertIsNegative;
import static com.brinvex.util.ibkr.impl.ValidationUtil.assertTrue;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNullElse;

@SuppressWarnings({"DuplicatedCode"})
public class TransactionMapper {

    public List<Transaction> mapCashTransactions(Set<String> oldTranIds, List<CashTransaction> rawCashTrans) {
        rawCashTrans = new ArrayList<>(rawCashTrans);
        rawCashTrans.sort(comparing(TranIdGenerator::getId));

        Map<String, List<CashTransaction>> rawTransByActionId = new LinkedHashMap<>();
        for (CashTransaction rawTran : rawCashTrans) {
            rawTransByActionId.computeIfAbsent(rawTran.getActionID(), k -> new ArrayList<>()).add(rawTran);
        }

        List<Transaction> resultTrans = new ArrayList<>();
        Set<CashTransaction> rawTransToSkip = new HashSet<>();
        Set<String> newTranIds = new HashSet<>();
        for (CashTransaction rawCashTran : rawCashTrans) {
            if (rawTransToSkip.contains(rawCashTran)) {
                continue;
            }
            var tranId = TranIdGenerator.getId(rawCashTran);
            if (!newTranIds.add(tranId)) {
                throw new IbkrServiceException("ID collision: %s, %s".formatted(tranId, rawCashTran));
            }
            Temporal dateTime = rawCashTran.getDateTime();
            var rawType = rawCashTran.getType();
            var amount = rawCashTran.getAmount();
            var ccy = rawCashTran.getCurrency();
            var ibCommissionCcy = rawCashTran.getIbCommissionCurrency();
            var fees = requireNonNullElse(rawCashTran.getIbCommission(), ZERO);

            if (rawType == CashTransactionType.Deposits_Withdrawals) {
                assertTrue(ibCommissionCcy == null || ccy.equals(ibCommissionCcy));
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setType(amount.compareTo(ZERO) > 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL);
                tran.setQty(ZERO);
                tran.setCurrency(ccy);
                tran.setGrossValue(amount);
                tran.setNetValue(amount.add(fees));
                tran.setFees(fees);
                tran.setSettleDate(rawCashTran.getSettleDate());
                resultTrans.add(tran);
            } else if (rawType == CashTransactionType.Dividends || rawType == CashTransactionType.Payment_In_Lieu_Of_Dividends) {
                if (rawCashTran.getDescription().contains("CASH DIVIDEND") || rawCashTran.getDescription().contains("PAYMENT IN LIEU OF DIVIDEND (Ordinary Dividend)")) {

                    List<CashTransaction> dividendTaxTrans = rawTransByActionId.getOrDefault(rawCashTran.getActionID(), emptyList())
                            .stream()
                            .filter(t -> t != rawCashTran)
                            .filter(t -> t.getType().equals(CashTransactionType.Withholding_Tax))
                            .toList();
                    CashTransaction dividendTaxTran = switch (dividendTaxTrans.size()) {
                        case 0 -> null;
                        case 1 -> dividendTaxTrans.get(0);
                        case 3 -> {
                            /*
                            <CashTransaction assetCategory="STK" symbol="ARCC" description="ARCC (US04010L1035) CASH DIVIDEND USD 0.48 - US TAX" conid="31400554" securityID="US04010L1035" securityIDType="ISIN" cusip="04010L103" isin="US04010L1035" dateTime="20231228;202000 EST" settleDate="20231228" amount="-7.78" type="Withholding Tax" tradeID="" transactionID="644171144" reportDate="20231228"  actionID="129229958" subCategory="COMMON" figi="BBG000PD6X77" />
                        	<CashTransaction assetCategory="STK" symbol="ARCC" description="ARCC (US04010L1035) CASH DIVIDEND USD 0.48 - US TAX" conid="31400554" securityID="US04010L1035" securityIDType="ISIN" cusip="04010L103" isin="US04010L1035" dateTime="20231228;202000 EST" settleDate="20231228" amount="7.78" type="Withholding Tax" tradeID="" transactionID="667043504" reportDate="20240201"  actionID="129229958" subCategory="COMMON" figi="BBG000PD6X77" />
	                        <CashTransaction assetCategory="STK" symbol="ARCC" description="ARCC (US04010L1035) CASH DIVIDEND USD 0.48 - US TAX" conid="31400554" securityID="US04010L1035" securityIDType="ISIN" cusip="04010L103" isin="US04010L1035" dateTime="20231228;202000 EST" settleDate="20231228" amount="-7.71" type="Withholding Tax" tradeID="" transactionID="667043505" reportDate="20240201"  actionID="129229958" subCategory="COMMON" figi="BBG000PD6X77" />
	                        <CashTransaction assetCategory="STK" symbol="ARCC" description="ARCC (US04010L1035) CASH DIVIDEND USD 0.48 (Ordinary Dividend)" conid="31400554" securityID="US04010L1035" securityIDType="ISIN" cusip="04010L103" isin="US04010L1035" dateTime="20231228;202000 EST" settleDate="20231228" amount="51.84" type="Dividends" tradeID="" transactionID="644171143" reportDate="20231228"  actionID="129229958" subCategory="COMMON" figi="BBG000PD6X77" />
                             */
                            CashTransaction divTaxTran0 = dividendTaxTrans.get(0);
                            CashTransaction divTaxTran1 = dividendTaxTrans.get(1);
                            CashTransaction divTran2 = dividendTaxTrans.get(2);
                            if (divTaxTran0.getSettleDate().isEqual(divTaxTran1.getSettleDate())
                                    && divTaxTran0.getAmount().negate().compareTo(divTaxTran1.getAmount()) == 0
                            ) {
                                rawTransToSkip.add(divTaxTran0);
                                rawTransToSkip.add(divTaxTran1);
                                yield divTran2;
                            }
                            throw new IllegalStateException("Unexpected dividendTaxTrans: #%s, %s".formatted(
                                    dividendTaxTrans.size(), dividendTaxTrans));
                        }
                        case 5 -> {
                            /*
                            <CashTransaction assetCategory="STK" symbol="ARCC" description="ARCC (US04010L1035) CASH DIVIDEND USD 0.48 - US TAX" conid="31400554" securityID="US04010L1035" cusip="04010L103" isin="US04010L1035" dateTime="20231228;202000 EST" settleDate="20231228" amount="-7.78" type="Withholding Tax" tradeID="" code="" transactionID="644171144" reportDate="20231228" clientReference="" actionID="129229958"  subCategory="COMMON" figi="BBG000PD6X77" />
                            <CashTransaction assetCategory="STK" symbol="ARCC" description="ARCC (US04010L1035) CASH DIVIDEND USD 0.48 - US TAX" conid="31400554" securityID="US04010L1035" cusip="04010L103" isin="US04010L1035" dateTime="20231228;202000 EST" settleDate="20231228" amount="7.78" type="Withholding Tax" tradeID="" code="" transactionID="667043504" reportDate="20240201" clientReference="" actionID="129229958"  subCategory="COMMON" figi="BBG000PD6X77" />
                            <CashTransaction assetCategory="STK" symbol="ARCC" description="ARCC (US04010L1035) CASH DIVIDEND USD 0.48 - US TAX" conid="31400554" securityID="US04010L1035" cusip="04010L103" isin="US04010L1035" dateTime="20231228;202000 EST" settleDate="20231228" amount="-7.71" type="Withholding Tax" tradeID="" code="" transactionID="667043505" reportDate="20240201" clientReference="" actionID="129229958"  subCategory="COMMON" figi="BBG000PD6X77" />
                            <CashTransaction assetCategory="STK" symbol="ARCC" description="ARCC (US04010L1035) CASH DIVIDEND USD 0.48 - US TAX" conid="31400554" securityID="US04010L1035" cusip="04010L103" isin="US04010L1035" dateTime="20231228;202000 EST" settleDate="20231228" amount="7.71" type="Withholding Tax" tradeID="" code="" transactionID="676687895" reportDate="20240214" clientReference="" actionID="129229958"  subCategory="COMMON" figi="BBG000PD6X77" />
                            <CashTransaction assetCategory="STK" symbol="ARCC" description="ARCC (US04010L1035) CASH DIVIDEND USD 0.48 - US TAX" conid="31400554" securityID="US04010L1035" cusip="04010L103" isin="US04010L1035" dateTime="20231228;202000 EST" settleDate="20231228" amount="-0.69" type="Withholding Tax" tradeID="" code="" transactionID="676687896" reportDate="20240214" clientReference="" actionID="129229958"  subCategory="COMMON" figi="BBG000PD6X77" />
                             */
                            CashTransaction divTaxTran0 = dividendTaxTrans.get(0);
                            CashTransaction divTaxTran1 = dividendTaxTrans.get(1);
                            CashTransaction divTaxTran2 = dividendTaxTrans.get(2);
                            CashTransaction divTaxTran3 = dividendTaxTrans.get(3);
                            CashTransaction divTran4 = dividendTaxTrans.get(4);
                            if (divTaxTran0.getSettleDate().isEqual(divTaxTran1.getSettleDate())
                                    && divTaxTran0.getAmount().negate().compareTo(divTaxTran1.getAmount()) == 0

                                    && divTaxTran2.getSettleDate().isEqual(divTaxTran0.getSettleDate())
                                    && divTaxTran2.getSettleDate().isEqual(divTaxTran3.getSettleDate())
                                    && divTaxTran2.getAmount().negate().compareTo(divTaxTran3.getAmount()) == 0
                            ) {
                                rawTransToSkip.add(divTaxTran0);
                                rawTransToSkip.add(divTaxTran1);
                                rawTransToSkip.add(divTaxTran2);
                                rawTransToSkip.add(divTaxTran3);
                                yield divTran4;
                            }
                            throw new IllegalStateException("Unexpected dividendTaxTrans: #%s, %s".formatted(
                                    dividendTaxTrans.size(), dividendTaxTrans));
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + dividendTaxTrans.size());
                    };
                    assertTrue(fees.compareTo(ZERO) == 0);

                    Transaction tran = new Transaction();
                    tran.setId(tranId);
                    tran.setDate(dateTime);
                    tran.setCountry(detectCountryByExchange(rawCashTran.getListingExchange()));
                    tran.setSymbol(stripToNull(rawCashTran.getSymbol()));
                    tran.setAssetCategory(rawCashTran.getAssetCategory());
                    tran.setAssetSubCategory(rawCashTran.getAssetSubCategory());
                    tran.setFigi(stripToNull(rawCashTran.getFigi()));
                    tran.setIsin(stripToNull(rawCashTran.getIsin()));
                    tran.setCurrency(ccy);
                    tran.setType(switch (rawType) {
                        case Dividends -> TransactionType.CASH_DIVIDEND;
                        case Payment_In_Lieu_Of_Dividends -> TransactionType.PAYMENT_IN_LIEU_OF_DIVIDENDS;
                        default -> throw new IllegalStateException("Unexpected value: " + rawType);
                    });
                    tran.setGrossValue(rawCashTran.getAmount());
                    tran.setQty(ZERO);
                    tran.setFees(ZERO);
                    if (dividendTaxTran == null) {
                        tran.setNetValue(rawCashTran.getAmount());
                        tran.setTax(ZERO);
                    } else {
                        rawTransToSkip.add(dividendTaxTran);
                        BigDecimal taxAmount = dividendTaxTran.getAmount();
                        assertIsNegative(taxAmount);
                        tran.setNetValue(rawCashTran.getAmount().add(taxAmount));
                        tran.setTax(taxAmount);
                    }
                    tran.setSettleDate(rawCashTran.getSettleDate());
                    resultTrans.add(tran);
                } else {
                    throw new IbkrServiceException("Not yet implemented tran=%s".formatted(rawCashTran));
                }
            } else if (rawType == CashTransactionType.Other_Fees || rawType == CashTransactionType.Broker_Interest_Paid || rawType == CashTransactionType.Broker_Fees) {
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setCountry(detectCountryByExchange(rawCashTran.getListingExchange()));
                tran.setSymbol(stripToNull(rawCashTran.getSymbol()));
                tran.setAssetCategory(rawCashTran.getAssetCategory());
                tran.setAssetSubCategory(rawCashTran.getAssetSubCategory());
                tran.setFigi(stripToNull(rawCashTran.getFigi()));
                tran.setIsin(stripToNull(rawCashTran.getIsin()));
                tran.setCurrency(ccy);
                tran.setType(TransactionType.FEE);
                tran.setGrossValue(ZERO);
                tran.setQty(ZERO);
                tran.setFees(rawCashTran.getAmount());
                tran.setNetValue(rawCashTran.getAmount());
                tran.setTax(ZERO);
                tran.setSettleDate(rawCashTran.getSettleDate());
                resultTrans.add(tran);
            } else {
                throw new IbkrServiceException("Not yet implemented tran=%s".formatted(rawCashTran));
            }
        }
        return resultTrans.stream().filter(t -> !oldTranIds.contains(t.getId())).toList();
    }

    public List<Transaction> mapTrades(
            Set<String> oldTranIds,
            List<Trade> rawTrades
    ) {
        rawTrades = new ArrayList<>(rawTrades);
        rawTrades.sort(comparing(TranIdGenerator::getId));

        Map<String, List<Trade>> rawTradesByIbOrderId = new LinkedHashMap<>();
        for (Trade rawTran : rawTrades) {
            rawTradesByIbOrderId.computeIfAbsent(rawTran.getIbOrderID(), k -> new ArrayList<>()).add(rawTran);
        }

        List<Transaction> resultTrans = new ArrayList<>();
        Set<String> newTranIds = new HashSet<>();
        for (Trade rawTrade : rawTrades) {
            String tranId = TranIdGenerator.getId(rawTrade);
            if (!newTranIds.add(tranId)) {
                throw new IbkrServiceException("ID collision: %s, %s".formatted(tranId, rawTrade));
            }
            var rawType = rawTrade.getTransactionType();
            var ccy = rawTrade.getCurrency();
            var ibCommissionCcy = rawTrade.getIbCommissionCurrency();
            var fees = requireNonNullElse(rawTrade.getIbCommission(), ZERO);
            var dateTime = rawTrade.getDateTime();
            var ibOrderID = rawTrade.getIbOrderID();
            var bunchId = rawTradesByIbOrderId.get(ibOrderID).size() > 1 ? ibOrderID : null;

            if (rawType == TradeType.ExchTrade && rawTrade.getAssetCategory() != AssetCategory.CASH) {
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setCountry(detectCountryByExchange(rawTrade.getListingExchange()));
                tran.setSymbol(stripToNull(rawTrade.getSymbol()));
                tran.setAssetCategory(rawTrade.getAssetCategory());
                tran.setAssetSubCategory(rawTrade.getAssetSubCategory());
                tran.setFigi(stripToNull(rawTrade.getFigi()));
                tran.setIsin(stripToNull(rawTrade.getIsin()));
                tran.setCurrency(ccy);
                tran.setType(rawTrade.getBuySell() == BuySell.BUY ? TransactionType.BUY : TransactionType.SELL);
                tran.setQty(rawTrade.getQuantity());
                tran.setPrice(rawTrade.getTradePrice());
                tran.setGrossValue(rawTrade.getProceeds());
                tran.setNetValue(rawTrade.getNetCash());
                tran.setFees(fees);
                tran.setSettleDate(rawTrade.getSettleDateTarget());
                tran.setBunchId(bunchId);
                resultTrans.add(tran);
            } else if (rawType == TradeType.ExchTrade && rawTrade.getAssetCategory() == AssetCategory.CASH) {
                if (rawTrade.getBuySell() == BuySell.SELL) {
                    Currency sellCcy = Currency.valueOf(rawTrade.getSymbol().substring(0, 3));
                    Currency buyCcy = Currency.valueOf(rawTrade.getSymbol().substring(4));
                    assertTrue(buyCcy.equals(ccy));
                    assertTrue(sellCcy.equals(ibCommissionCcy));
                    if (ccy != ibCommissionCcy) {
                        Transaction tran = new Transaction();
                        tran.setId(tranId);
                        tran.setDate(dateTime);
                        tran.setSymbol(buyCcy.name());
                        tran.setAssetCategory(AssetCategory.CASH);
                        tran.setAssetSubCategory(AssetSubCategory.CASH);
                        tran.setCurrency(sellCcy);
                        tran.setType(TransactionType.FX_BUY);
                        tran.setQty(rawTrade.getProceeds());
                        tran.setPrice(rawTrade.getTradePrice());
                        tran.setGrossValue(rawTrade.getQuantity());
                        tran.setNetValue(rawTrade.getQuantity().add(rawTrade.getIbCommission()));
                        tran.setFees(rawTrade.getIbCommission());
                        tran.setSettleDate(rawTrade.getSettleDateTarget());
                        tran.setBunchId(bunchId);
                        resultTrans.add(tran);
                    } else {
                        throw new IbkrServiceException("Not yet implemented ccy=%s, ibCommissionCcy=%s, tran=%s"
                                .formatted(ccy, ibCommissionCcy, rawTrade));
                    }
                } else if (rawTrade.getBuySell() == BuySell.BUY) {
                    Currency buyCcy = Currency.valueOf(rawTrade.getSymbol().substring(0, 3));
                    Currency sellCcy = Currency.valueOf(rawTrade.getSymbol().substring(4));
                    assertTrue(sellCcy.equals(ccy));
                    assertTrue(buyCcy.equals(ibCommissionCcy));
                    if (ccy != ibCommissionCcy) {
                        Transaction tran = new Transaction();
                        tran.setId(tranId);
                        tran.setDate(dateTime);
                        tran.setSymbol(buyCcy.name());
                        tran.setAssetCategory(AssetCategory.CASH);
                        tran.setAssetSubCategory(AssetSubCategory.CASH);
                        tran.setCurrency(ccy);
                        tran.setType(TransactionType.FX_BUY);
                        tran.setQty(rawTrade.getQuantity());
                        tran.setPrice(rawTrade.getTradePrice());
                        tran.setGrossValue(rawTrade.getProceeds());
                        tran.setNetValue(rawTrade.getProceeds().add(rawTrade.getIbCommission()));
                        tran.setFees(rawTrade.getIbCommission());
                        tran.setSettleDate(rawTrade.getSettleDateTarget());
                        tran.setBunchId(bunchId);
                        resultTrans.add(tran);
                    } else {
                        throw new IbkrServiceException("Not yet implemented ccy=%s, ibCommissionCcy=%s, tran=%s"
                                .formatted(ccy, ibCommissionCcy, rawTrade));
                    }
                }
            } else if (rawType == TradeType.FracShare && rawTrade.getAssetCategory() == AssetCategory.STK && rawTrade.getBuySell() == BuySell.SELL) {
                assertTrue(rawTrade.getIbCommission().compareTo(ZERO) == 0);
                BigDecimal grossValue = rawTrade.getProceeds();
                assertTrue(grossValue.compareTo(ZERO) > 0);
                assertTrue(grossValue.compareTo(rawTrade.getTradeMoney().negate()) == 0);
                assertTrue(grossValue.compareTo(rawTrade.getNetCash()) == 0);
                assertTrue(rawTrade.getTaxes().compareTo(ZERO) == 0);
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setSymbol(rawTrade.getSymbol());
                tran.setCountry(detectCountryByExchange(rawTrade.getListingExchange()));
                tran.setIsin(rawTrade.getIsin());
                tran.setFigi(rawTrade.getFigi());
                tran.setAssetCategory(AssetCategory.STK);
                tran.setAssetSubCategory(AssetSubCategory.STK_COMMON);
                tran.setCurrency(rawTrade.getCurrency());
                tran.setType(TransactionType.SELL);
                tran.setQty(rawTrade.getQuantity());
                tran.setPrice(rawTrade.getTradePrice());
                tran.setGrossValue(grossValue);
                tran.setNetValue(grossValue);
                tran.setFees(rawTrade.getIbCommission());
                tran.setSettleDate(rawTrade.getSettleDateTarget());
                tran.setBunchId(bunchId);
                resultTrans.add(tran);
            } else {
                throw new IbkrServiceException("Not yet implemented tran=%s".formatted(rawTrade));
            }
        }
        return resultTrans.stream().filter(t -> !oldTranIds.contains(t.getId())).toList();
    }

    public List<Transaction> mapTradeConfirms(
            Set<String> oldTranIds,
            List<TradeConfirm> rawTradeConfirms
    ) {
        List<Trade> tradeConfirmTrades = new ArrayList<>();
        for (TradeConfirm rawTradeConfirm : rawTradeConfirms) {
            Trade trade = new Trade();
            trade.setCurrency(rawTradeConfirm.getCurrency());
            trade.setAssetCategory(rawTradeConfirm.getAssetCategory());
            trade.setAssetSubCategory(rawTradeConfirm.getAssetSubCategory());
            trade.setSymbol(rawTradeConfirm.getSymbol());
            trade.setDescription(rawTradeConfirm.getDescription());
            trade.setSecurityID(rawTradeConfirm.getSecurityID());
            trade.setSecurityIDType(rawTradeConfirm.getSecurityIDType());
            trade.setIsin(rawTradeConfirm.getIsin());
            trade.setListingExchange(rawTradeConfirm.getListingExchange());
            trade.setTradeID(rawTradeConfirm.getTradeID());
            trade.setReportDate(rawTradeConfirm.getReportDate());
            trade.setDateTime(rawTradeConfirm.getDateTime());
            trade.setSettleDateTarget(rawTradeConfirm.getSettleDate());
            trade.setTransactionType(rawTradeConfirm.getTransactionType());
            trade.setExchange(rawTradeConfirm.getExchange());
            trade.setQuantity(rawTradeConfirm.getQuantity());
            trade.setTradePrice(rawTradeConfirm.getPrice());
            trade.setTradeMoney(rawTradeConfirm.getAmount());
            trade.setProceeds(rawTradeConfirm.getProceeds());
            trade.setNetCash(rawTradeConfirm.getNetCash());
            trade.setIbCommission(rawTradeConfirm.getCommission());
            trade.setIbCommissionCurrency(rawTradeConfirm.getCommissionCurrency());
            trade.setTaxes(rawTradeConfirm.getTax());
            trade.setBuySell(rawTradeConfirm.getBuySell());
            trade.setIbOrderID(rawTradeConfirm.getOrderID());
            trade.setOrderTime(rawTradeConfirm.getOrderTime());
            tradeConfirmTrades.add(trade);
        }
        return mapTrades(oldTranIds, tradeConfirmTrades);
    }

    public List<Transaction> mapCorporateAction(
            Set<String> oldTranIds,
            List<CorporateAction> rawCorpActions
    ) {
        rawCorpActions = new ArrayList<>(rawCorpActions);
        rawCorpActions.sort(comparing(TranIdGenerator::getId));

        List<Transaction> resultTrans = new ArrayList<>();
        Set<String> newTranIds = new HashSet<>();
        for (CorporateAction rawCorpAction : rawCorpActions) {
            String tranId = TranIdGenerator.getId(rawCorpAction);
            if (!newTranIds.add(tranId)) {
                throw new IbkrServiceException("ID collision: %s, %s".formatted(tranId, rawCorpAction));
            }
            var ccy = rawCorpAction.getCurrency();
            var dateTime = rawCorpAction.getDateTime();

            if (rawCorpAction.getDescription().contains("MERGED(Acquisition)") && rawCorpAction.getType().equals(CorporateActionType.TC)) {
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setType(TransactionType.TRANSFORMATION);
                tran.setCountry(Country.valueOf(rawCorpAction.getIssuerCountryCode()));
                tran.setSymbol(stripToNull(rawCorpAction.getSymbol()));
                tran.setIsin(stripToNull(rawCorpAction.getIsin()));
                tran.setFigi(stripToNull(rawCorpAction.getFigi()));
                tran.setAssetCategory(rawCorpAction.getAssetCategory());
                tran.setAssetSubCategory(rawCorpAction.getAssetSubCategory());
                tran.setCurrency(ccy);
                tran.setQty(rawCorpAction.getQuantity());
                tran.setPrice(rawCorpAction.getProceeds().divide(rawCorpAction.getQuantity().abs(), 2, RoundingMode.HALF_UP));
                tran.setGrossValue(rawCorpAction.getProceeds());
                tran.setNetValue(rawCorpAction.getProceeds());
                tran.setTax(ZERO);
                tran.setFees(ZERO);
                tran.setSettleDate(rawCorpAction.getReportDate());
                tran.setBunchId(null);
                tran.setDescription(rawCorpAction.getDescription());
                resultTrans.add(tran);
            } else if (rawCorpAction.getDescription().contains("SPINOFF") && rawCorpAction.getType().equals(CorporateActionType.SO)) {
                assertTrue(rawCorpAction.getAmount().compareTo(ZERO) == 0);
                assertTrue(rawCorpAction.getProceeds().compareTo(ZERO) == 0);
                assertTrue(rawCorpAction.getValue().compareTo(ZERO) == 0);
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setType(TransactionType.TRANSFORMATION);
                tran.setCountry(Country.valueOf(rawCorpAction.getIssuerCountryCode()));
                tran.setSymbol(stripToNull(rawCorpAction.getSymbol()));
                tran.setIsin(stripToNull(rawCorpAction.getIsin()));
                tran.setFigi(stripToNull(rawCorpAction.getFigi()));
                tran.setAssetCategory(rawCorpAction.getAssetCategory());
                tran.setAssetSubCategory(rawCorpAction.getAssetSubCategory());
                tran.setCurrency(ccy);
                tran.setQty(rawCorpAction.getQuantity());
                tran.setPrice(ZERO);
                tran.setGrossValue(ZERO);
                tran.setNetValue(ZERO);
                tran.setTax(ZERO);
                tran.setFees(ZERO);
                tran.setSettleDate(rawCorpAction.getReportDate());
                tran.setBunchId(null);
                tran.setDescription(rawCorpAction.getDescription());
                resultTrans.add(tran);
            } else if (rawCorpAction.getDescription().contains("SPLIT") && rawCorpAction.getType().equals(CorporateActionType.FS)) {
                assertTrue(rawCorpAction.getAmount().compareTo(ZERO) == 0);
                assertTrue(rawCorpAction.getProceeds().compareTo(ZERO) == 0);
                assertTrue(rawCorpAction.getValue().compareTo(ZERO) == 0);
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setType(TransactionType.TRANSFORMATION);
                tran.setCountry(Country.valueOf(rawCorpAction.getIssuerCountryCode()));
                tran.setSymbol(stripToNull(rawCorpAction.getSymbol()));
                tran.setIsin(stripToNull(rawCorpAction.getIsin()));
                tran.setFigi(stripToNull(rawCorpAction.getFigi()));
                tran.setAssetCategory(rawCorpAction.getAssetCategory());
                tran.setAssetSubCategory(rawCorpAction.getAssetSubCategory());
                tran.setCurrency(ccy);
                tran.setQty(rawCorpAction.getQuantity());
                tran.setPrice(ZERO);
                tran.setGrossValue(ZERO);
                tran.setNetValue(ZERO);
                tran.setTax(ZERO);
                tran.setFees(ZERO);
                tran.setSettleDate(rawCorpAction.getReportDate());
                tran.setBunchId(null);
                tran.setDescription(rawCorpAction.getDescription());
                resultTrans.add(tran);
            } else {
                throw new IbkrServiceException("Not yet implemented rawCorpAction=%s".formatted(rawCorpAction));
            }
        }
        return resultTrans.stream().filter(t -> !oldTranIds.contains(t.getId())).toList();
    }

    private Country detectCountryByExchange(String listingExchange) {
        return listingExchange == null || listingExchange.isBlank() ? null : switch (listingExchange) {
            case "NYSE", "NASDAQ" -> Country.US;
            case "IBIS", "IBIS2" -> Country.DE;
            default -> throw new IllegalStateException("Unexpected value: " + listingExchange);
        };
    }

    private String stripToNull(String str) {
        return str == null || str.isBlank() ? null : str.trim();
    }
}