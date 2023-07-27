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

import com.brinvex.util.ibkr.api.model.Country;
import com.brinvex.util.ibkr.api.model.Currency;
import com.brinvex.util.ibkr.api.model.Transaction;
import com.brinvex.util.ibkr.api.model.TransactionType;
import com.brinvex.util.ibkr.api.model.raw.AssetCategory;
import com.brinvex.util.ibkr.api.model.raw.BuySell;
import com.brinvex.util.ibkr.api.model.raw.CashTransaction;
import com.brinvex.util.ibkr.api.model.raw.CashTransactionType;
import com.brinvex.util.ibkr.api.model.raw.Trade;
import com.brinvex.util.ibkr.api.model.raw.TradeConfirm;
import com.brinvex.util.ibkr.api.model.raw.TradeType;
import com.brinvex.util.ibkr.api.service.exception.IbkrServiceException;

import java.math.BigDecimal;
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
        rawCashTrans.sort(comparing((CashTransaction ct) -> ct.getDateTime().toString()).thenComparing(CashTransaction::getTransactionID));

        Map<String, List<CashTransaction>> rawTransByActionId = new LinkedHashMap<>();
        for (CashTransaction rawTran : rawCashTrans) {
            rawTransByActionId.computeIfAbsent(rawTran.getActionId(), k -> new ArrayList<>()).add(rawTran);
        }

        List<Transaction> resultTrans = new ArrayList<>();
        Set<CashTransaction> rawTransToSkip = new HashSet<>();
        Set<String> newTranIds = new HashSet<>();
        for (CashTransaction rawCashTran : rawCashTrans) {
            if (rawTransToSkip.contains(rawCashTran)) {
                continue;
            }
            var tranId = TranIdGenerator.getId(rawCashTran);
            if (oldTranIds.contains(tranId)) {
                continue;
            }
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
                tran.setCcy(ccy);
                tran.setGrossValue(amount);
                tran.setNetValue(amount.add(fees));
                tran.setFees(fees);
                tran.setSettleDate(rawCashTran.getSettleDate());
                resultTrans.add(tran);
            } else if (rawType == CashTransactionType.Dividends) {
                if (rawCashTran.getDescription().contains("CASH DIVIDEND")) {

                    List<CashTransaction> dividendTaxTrans = rawTransByActionId.getOrDefault(rawCashTran.getActionId(), emptyList())
                            .stream()
                            .filter(t -> t != rawCashTran)
                            .filter(t -> t.getType().equals(CashTransactionType.Withholding_Tax))
                            .toList();
                    CashTransaction dividendTaxTran = switch (dividendTaxTrans.size()) {
                        case 0 -> null;
                        case 1 -> dividendTaxTrans.get(0);
                        default -> throw new IllegalStateException("Unexpected value: " + dividendTaxTrans.size());
                    };
                    assertTrue(fees.compareTo(ZERO) == 0);

                    Transaction tran = new Transaction();
                    tran.setId(tranId);
                    tran.setDate(dateTime);
                    tran.setCountry(detectCountryByExchange(rawCashTran.getListingExchange()));
                    tran.setIsin(stripToNull(rawCashTran.getIsin()));
                    tran.setSymbol(stripToNull(rawCashTran.getSymbol()));
                    tran.setCcy(ccy);
                    tran.setType(TransactionType.CASH_DIVIDEND);
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
            } else if (rawType == CashTransactionType.Other_Fees) {
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setCountry(detectCountryByExchange(rawCashTran.getListingExchange()));
                tran.setIsin(stripToNull(rawCashTran.getIsin()));
                tran.setSymbol(stripToNull(rawCashTran.getSymbol()));
                tran.setCcy(ccy);
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
        return resultTrans;
    }

    public List<Transaction> mapTrades(
            Set<String> oldTranIds,
            List<Trade> rawTrades
    ) {
        rawTrades = new ArrayList<>(rawTrades);
        rawTrades.sort(comparing((Trade t) -> t.getDateTime().toString()).thenComparing(Trade::getTradeID));

        Map<String, List<Trade>> rawTradesByIbOrderId = new LinkedHashMap<>();
        for (Trade rawTran : rawTrades) {
            rawTradesByIbOrderId.computeIfAbsent(rawTran.getIbOrderID(), k -> new ArrayList<>()).add(rawTran);
        }

        List<Transaction> resultTrans = new ArrayList<>();
        Set<String> newTranIds = new HashSet<>();
        for (Trade rawTrade : rawTrades) {
            String tranId = TranIdGenerator.getId(rawTrade);
            if (oldTranIds.contains(tranId)) {
                continue;
            }
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
                tran.setIsin(stripToNull(rawTrade.getIsin()));
                tran.setSymbol(stripToNull(rawTrade.getSymbol()));
                tran.setCcy(ccy);
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
                        tran.setCcy(sellCcy);
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
                        tran.setCcy(ccy);
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
            } else {
                throw new IbkrServiceException("Not yet implemented tran=%s".formatted(rawTrade));
            }
        }

        return resultTrans;
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

    private Country detectCountryByExchange(String listingExchange) {
        return listingExchange == null || listingExchange.isBlank() ? null : switch (listingExchange) {
            case "NYSE", "NASDAQ" -> Country.US;
            case "IBIS" -> Country.DE;
            default -> throw new IllegalStateException("Unexpected value: " + listingExchange);
        };
    }

    private String stripToNull(String str) {
        return str == null || str.isBlank() ? null : str.trim();
    }
}