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
import com.brinvex.util.ibkr.api.model.raw.RawTransaction;
import com.brinvex.util.ibkr.api.model.raw.RawTransactionType;
import com.brinvex.util.ibkr.api.service.exception.IbkrServiceException;

import java.math.BigDecimal;
import java.time.ZoneId;
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

    private static class LazyHolder {
        private static final ZoneId IBKR_TIME_ZONE = ZoneId.of("America/New_York");
    }

    public List<Transaction> mapTransactions(List<RawTransaction> rawTrans) {

        rawTrans = new ArrayList<>(rawTrans);
        rawTrans.sort(comparing(RawTransaction::getDateTime).thenComparing(RawTransaction::getTransactionID));

        Map<String, List<RawTransaction>> rawTransByActionId = new LinkedHashMap<>();
        Map<String, List<RawTransaction>> rawTransByIbOrderId = new LinkedHashMap<>();
        for (RawTransaction rawTran : rawTrans) {
            rawTransByActionId.computeIfAbsent(rawTran.getActionId(), k -> new ArrayList<>()).add(rawTran);
            rawTransByIbOrderId.computeIfAbsent(rawTran.getIbOrderID(), k -> new ArrayList<>()).add(rawTran);
        }

        List<Transaction> resultTrans = new ArrayList<>();
        Set<RawTransaction> rawTransToSkip = new HashSet<>();
        for (RawTransaction rawTran : rawTrans) {
            if (rawTransToSkip.contains(rawTran)) {
                continue;
            }
            var rawType = rawTran.getType();
            var rawTranId = rawTran.getTransactionID();
            var ibOrderID = rawTran.getIbOrderID();
            var actionId = rawTran.getActionId();
            var amount = rawTran.getAmount();
            var ccy = rawTran.getCurrency();
            var ibCommissionCcy = rawTran.getIbCommissionCurrency();
            var fees = requireNonNullElse(rawTran.getIbCommission(), ZERO);
            var dateTime = rawTran.getDateTime().atZone(LazyHolder.IBKR_TIME_ZONE);
            var tranId = "%s/%s/%s/%s".formatted(dateTime, rawTranId, actionId, ibOrderID);
            var qty = rawTran.getQuantity();
            var bunchId = rawTransByIbOrderId.get(ibOrderID).size() > 1 ? ibOrderID : null;

            if (rawType == RawTransactionType.Deposits_Withdrawals) {
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
                tran.setSettleDate(rawTran.getSettleDate());
                tran.setBunchId(bunchId);
                resultTrans.add(tran);
            } else if (rawType == RawTransactionType.ExchTrade && rawTran.getAssetCategory() != AssetCategory.CASH) {
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setCountry(detectCountryByExchange(rawTran.getListingExchange()));
                tran.setIsin(stripToNull(rawTran.getIsin()));
                tran.setSymbol(stripToNull(rawTran.getSymbol()));
                tran.setCcy(ccy);
                if (rawTran.getBuySell() == BuySell.BUY) {
                    tran.setType(TransactionType.BUY);
                } else {
                    tran.setType(TransactionType.SELL);
                }
                tran.setQty(qty);
                tran.setPrice(rawTran.getTradePrice());
                tran.setGrossValue(rawTran.getProceeds());
                tran.setNetValue(rawTran.getNetCash());
                tran.setFees(fees);
                tran.setSettleDate(rawTran.getSettleDateTarget());
                tran.setBunchId(bunchId);
                resultTrans.add(tran);
            } else if (rawType == RawTransactionType.ExchTrade && rawTran.getAssetCategory() == AssetCategory.CASH) {
                if (rawTran.getBuySell() == BuySell.SELL) {
                    Currency sellCcy = Currency.valueOf(rawTran.getSymbol().substring(0, 3));
                    Currency buyCcy = Currency.valueOf(rawTran.getSymbol().substring(4));
                    assertTrue(buyCcy.equals(ccy));
                    assertTrue(sellCcy.equals(ibCommissionCcy));
                    if (ccy != ibCommissionCcy) {
                        Transaction tran = new Transaction();
                        tran.setId(tranId);
                        tran.setDate(dateTime);
                        tran.setSymbol(buyCcy.name());
                        tran.setCcy(sellCcy);
                        tran.setType(TransactionType.FX_BUY);
                        tran.setQty(rawTran.getProceeds());
                        tran.setPrice(rawTran.getTradePrice());
                        tran.setGrossValue(rawTran.getQuantity());
                        tran.setNetValue(rawTran.getQuantity().add(rawTran.getIbCommission()));
                        tran.setFees(rawTran.getIbCommission());
                        tran.setSettleDate(rawTran.getSettleDateTarget());
                        tran.setBunchId(bunchId);
                        resultTrans.add(tran);
                    } else {
                        throw new IbkrServiceException("Not yet implemented ccy=%s, ibCommissionCcy=%s, tran=%s"
                                .formatted(ccy, ibCommissionCcy, rawTran));
                    }
                } else if (rawTran.getBuySell() == BuySell.BUY) {
                    Currency buyCcy = Currency.valueOf(rawTran.getSymbol().substring(0, 3));
                    Currency sellCcy = Currency.valueOf(rawTran.getSymbol().substring(4));
                    assertTrue(sellCcy.equals(ccy));
                    assertTrue(buyCcy.equals(ibCommissionCcy));
                    if (ccy != ibCommissionCcy) {
                        Transaction tran = new Transaction();
                        tran.setId(tranId);
                        tran.setDate(dateTime);
                        tran.setSymbol(buyCcy.name());
                        tran.setCcy(ccy);
                        tran.setType(TransactionType.FX_BUY);
                        tran.setQty(rawTran.getQuantity());
                        tran.setPrice(rawTran.getTradePrice());
                        tran.setGrossValue(rawTran.getProceeds());
                        tran.setNetValue(rawTran.getProceeds().add(rawTran.getIbCommission()));
                        tran.setFees(rawTran.getIbCommission());
                        tran.setSettleDate(rawTran.getSettleDateTarget());
                        tran.setBunchId(bunchId);
                        resultTrans.add(tran);
                    } else {
                        throw new IbkrServiceException("Not yet implemented ccy=%s, ibCommissionCcy=%s, tran=%s"
                                .formatted(ccy, ibCommissionCcy, rawTran));
                    }
                }
            } else if (rawType == RawTransactionType.Dividends) {
                if (rawTran.getDescription().contains("CASH DIVIDEND")) {

                    List<RawTransaction> dividendTaxTrans = rawTransByActionId.getOrDefault(rawTran.getActionId(), emptyList())
                            .stream()
                            .filter(t -> t != rawTran)
                            .toList();
                    RawTransaction dividendTaxTran = switch (dividendTaxTrans.size()) {
                        case 0 -> null;
                        case 1 -> dividendTaxTrans.get(0);
                        default -> throw new IllegalStateException("Unexpected value: " + dividendTaxTrans.size());
                    };
                    assertTrue(fees.compareTo(ZERO) == 0);

                    Transaction tran = new Transaction();
                    tran.setId(tranId);
                    tran.setDate(dateTime);
                    tran.setCountry(detectCountryByExchange(rawTran.getListingExchange()));
                    tran.setIsin(stripToNull(rawTran.getIsin()));
                    tran.setSymbol(stripToNull(rawTran.getSymbol()));
                    tran.setCcy(ccy);
                    tran.setType(TransactionType.CASH_DIVIDEND);
                    tran.setGrossValue(rawTran.getAmount());
                    tran.setQty(ZERO);
                    tran.setFees(ZERO);
                    if (dividendTaxTran == null) {
                        tran.setNetValue(rawTran.getAmount());
                        tran.setTax(ZERO);
                    } else {
                        rawTransToSkip.add(dividendTaxTran);
                        BigDecimal taxAmount = dividendTaxTran.getAmount();
                        assertIsNegative(taxAmount);
                        tran.setNetValue(rawTran.getAmount().add(taxAmount));
                        tran.setTax(taxAmount);
                    }
                    tran.setSettleDate(rawTran.getSettleDate());
                    tran.setBunchId(bunchId);
                    resultTrans.add(tran);
                } else {
                    throw new IbkrServiceException("Not yet implemented tran=%s".formatted(rawTran));
                }
            } else if (rawType == RawTransactionType.Other_Fees) {
                Transaction tran = new Transaction();
                tran.setId(tranId);
                tran.setDate(dateTime);
                tran.setCountry(detectCountryByExchange(rawTran.getListingExchange()));
                tran.setIsin(stripToNull(rawTran.getIsin()));
                tran.setSymbol(stripToNull(rawTran.getSymbol()));
                tran.setCcy(ccy);
                tran.setType(TransactionType.FEE);
                tran.setGrossValue(ZERO);
                tran.setQty(ZERO);
                tran.setFees(rawTran.getAmount());
                tran.setNetValue(rawTran.getAmount());
                tran.setTax(ZERO);
                tran.setSettleDate(rawTran.getSettleDate());
                tran.setBunchId(bunchId);
                resultTrans.add(tran);
            } else {
                throw new IbkrServiceException("Not yet implemented tran=%s".formatted(rawTran));
            }
        }

        return resultTrans;
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