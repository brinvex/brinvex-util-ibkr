package com.brinvex.util.ibkr.impl;

import com.brinvex.util.ibkr.api.model.Portfolio;
import com.brinvex.util.ibkr.api.model.Transaction;
import com.brinvex.util.ibkr.api.model.raw.CashTransaction;
import com.brinvex.util.ibkr.api.model.raw.CorporateAction;
import com.brinvex.util.ibkr.api.model.raw.EquitySummary;
import com.brinvex.util.ibkr.api.model.raw.FlexStatement;
import com.brinvex.util.ibkr.api.model.raw.FlexStatementType;
import com.brinvex.util.ibkr.api.model.raw.Trade;
import com.brinvex.util.ibkr.api.model.raw.TradeConfirm;
import com.brinvex.util.ibkr.api.service.IbkrService;
import com.brinvex.util.ibkr.api.service.exception.IbkrServiceException;
import com.brinvex.util.ibkr.impl.parser.FlexStatementXmlParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

@SuppressWarnings({"GrazieInspection", "DuplicatedCode"})
public class IbkrServiceImpl implements IbkrService {

    private final FlexStatementXmlParser flexStatementXmlParser;

    private final PortfolioManager ptfManager;

    private final TransactionMapper transactionMapper;

    private final String flexQueryUrl;

    private static class LazyHolder {
        private static final Pattern HTTP_RESP1_STATUS_PATTERN = Pattern.compile("<Status>(.*)</Status>");
        private static final Pattern HTTP_RESP1_REFERENCE_CODE_PATTERN = Pattern.compile("<ReferenceCode>(.*)</ReferenceCode>");
        private static final Pattern HTTP_RESP1_URL_PATTERN = Pattern.compile("<Url>(.*)</Url>");
    }

    public IbkrServiceImpl() {
        this(new FlexStatementXmlParser(), new PortfolioManager(), new TransactionMapper(), "https://www.interactivebrokers.com/Universal/servlet/FlexStatementService.SendRequest?t=%s&q=%s&v=3");
    }

    public IbkrServiceImpl(String flexQueryUrl) {
        this(new FlexStatementXmlParser(), new PortfolioManager(), new TransactionMapper(), flexQueryUrl);
    }

    public IbkrServiceImpl(
            FlexStatementXmlParser flexStatementXmlParser,
            PortfolioManager ptfManager,
            TransactionMapper transactionMapper,
            String flexQueryUrl
    ) {
        this.flexStatementXmlParser = flexStatementXmlParser;
        this.ptfManager = ptfManager;
        this.transactionMapper = transactionMapper;
        this.flexQueryUrl = flexQueryUrl;
    }

    @Override
    public FlexStatement parseActivitiesFromStatements(Collection<Path> statementFilePaths) {
        return parseActivitiesFromStatements(statementFilePaths
                .stream()
                .map(filePath -> {
                    try {
                        return Files.readString(filePath);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
        );
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public FlexStatement parseActivitiesFromStatements(Stream<String> statementContents) {
        List<FlexStatement> rawFlexStatements = statementContents
                .map(flexStatementXmlParser::parseActivities)
                .sorted(comparing(FlexStatement::getFromDate).thenComparing(FlexStatement::getToDate))
                .toList();

        if (rawFlexStatements.isEmpty()) {
            throw new IllegalArgumentException("Expected non-empty stream of statements");
        }

        FlexStatement result = new FlexStatement();
        String accountId0;
        {
            FlexStatement rawTranList0 = rawFlexStatements.get(0);
            accountId0 = rawTranList0.getAccountId();

            result.setAccountId(accountId0);
            result.setFromDate(rawTranList0.getFromDate());
            result.setToDate(rawTranList0.getToDate());
        }

        TreeMap<String, CashTransaction> rawCashTrans = new TreeMap<>();
        TreeMap<String, Trade> rawTrades = new TreeMap<>();
        TreeMap<String, TradeConfirm> rawTradeConfirms = new TreeMap<>();
        TreeMap<String, CorporateAction> rawCorpActions = new TreeMap<>();

        for (FlexStatement flexStatement : rawFlexStatements) {
            LocalDate fromDate = flexStatement.getFromDate();
            LocalDate toDate = flexStatement.getToDate();

            String accountId = flexStatement.getAccountId();
            if (!accountId0.equals(accountId)) {
                throw new IbkrServiceException(format("Unexpected multiple accounts: %s, %s",
                        accountId0,
                        accountId
                ));
            }

            LocalDate nextPeriodFrom = result.getToDate().plusDays(1);
            if (nextPeriodFrom.isBefore(fromDate)) {
                if (!FlexStatementType.TCF.equals(flexStatement.getType())) {
                    throw new IbkrServiceException(format("Missing period: '%s - %s', accountId=%s",
                            nextPeriodFrom, fromDate.minusDays(1), accountId0));
                }
            }
            if (toDate.isAfter(result.getToDate())) {
                result.setToDate(toDate);
            }

            for (CashTransaction rawCashTran : flexStatement.getCashTransactions()) {
                rawCashTrans.putIfAbsent(TranIdGenerator.getId(rawCashTran), rawCashTran);
            }
            for (Trade rawTrade : flexStatement.getTrades()) {
                rawTrades.putIfAbsent(TranIdGenerator.getId(rawTrade), rawTrade);
            }
            for (TradeConfirm rawTradeConfirm : flexStatement.getTradeConfirms()) {
                rawTradeConfirms.putIfAbsent(TranIdGenerator.getId(rawTradeConfirm), rawTradeConfirm);
            }
            for (CorporateAction rawCorpAction : flexStatement.getCorporateActions()) {
                rawCorpActions.putIfAbsent(TranIdGenerator.getId(rawCorpAction), rawCorpAction);
            }
        }

        result.getCashTransactions().addAll(rawCashTrans.values());
        result.getTrades().addAll(rawTrades.values());
        result.getTradeConfirms().addAll(rawTradeConfirms.values());
        result.getCorporateActions().addAll(rawCorpActions.values());

        return result;
    }

    @Override
    public FlexStatement parseEquitySummariesFromStatements(Collection<Path> statementFilePaths) {
        return parseEquitySummariesFromStatements(statementFilePaths
                .stream()
                .map(filePath -> {
                    try {
                        return Files.readString(filePath);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
        );
    }

    @Override
    public FlexStatement parseEquitySummariesFromStatements(Stream<String> statementContents) {
        List<FlexStatement> rawFlexStatements = statementContents
                .map(flexStatementXmlParser::parseEquitySummaries)
                .sorted(comparing(FlexStatement::getFromDate).thenComparing(FlexStatement::getToDate))
                .toList();

        if (rawFlexStatements.isEmpty()) {
            throw new IllegalArgumentException("Expected non-empty stream of statements");
        }

        FlexStatement result = new FlexStatement();
        String accountId0;
        {
            FlexStatement flexStatement = rawFlexStatements.get(0);
            accountId0 = flexStatement.getAccountId();

            result.setAccountId(accountId0);
            result.setFromDate(flexStatement.getFromDate());
            result.setToDate(flexStatement.getToDate());
        }

        TreeMap<LocalDate, EquitySummary> equitySummaries = new TreeMap<>();

        for (FlexStatement flexStatement : rawFlexStatements) {
            LocalDate fromDate = flexStatement.getFromDate();
            LocalDate toDate = flexStatement.getToDate();

            String accountId = flexStatement.getAccountId();
            if (!accountId0.equals(accountId)) {
                throw new IbkrServiceException(format("Unexpected multiple accounts: %s, %s",
                        accountId0,
                        accountId
                ));
            }

            LocalDate nextPeriodFrom = result.getToDate().plusDays(1);
            if (nextPeriodFrom.isBefore(fromDate)) {
                if (!FlexStatementType.TCF.equals(flexStatement.getType())) {
                    throw new IbkrServiceException(format("Missing period: '%s - %s', accountId=%s",
                            nextPeriodFrom, fromDate.minusDays(1), accountId0));
                }
            }
            if (toDate.isAfter(result.getToDate())) {
                result.setToDate(toDate);
            }

            for (EquitySummary es : flexStatement.getEquitySummaries()) {
                equitySummaries.putIfAbsent(es.getReportDate(), es);
            }
        }

        result.getEquitySummaries().addAll(equitySummaries.values());

        return result;
    }

    @Override
    public Portfolio fillPortfolioFromStatements(Collection<Path> statementPaths) {
        Stream<String> statementContentStream = statementPaths
                .stream()
                .map(filePath -> {
                    try {
                        return Files.readString(filePath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        return fillPortfolioFromStatements(statementContentStream);
    }

    @Override
    public Portfolio fillPortfolioFromStatements(Stream<String> statementContents) {
        return fillPortfolioFromStatements(null, statementContents);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public Portfolio fillPortfolioFromStatements(Portfolio ptf, Stream<String> statementContents) {
        FlexStatement flexStatement = parseActivitiesFromStatements(statementContents);
        List<CashTransaction> rawCashTrans = flexStatement.getCashTransactions();
        List<Trade> rawTrades = flexStatement.getTrades();
        List<TradeConfirm> rawTradeConfirms = flexStatement.getTradeConfirms();
        List<CorporateAction> rawCorpActions = flexStatement.getCorporateActions();

        String accountId = flexStatement.getAccountId();
        LocalDate periodFrom = flexStatement.getFromDate();
        LocalDate periodTo = flexStatement.getToDate();
        LocalDate today = LocalDate.now();
        if (ptf == null) {
            ptf = ptfManager.initPortfolio(accountId, periodFrom, periodTo);
        } else {
            if (!accountId.equals(ptf.getAccountId())) {
                throw new IbkrServiceException(format("Unexpected multiple accounts: %s, %s",
                        ptf.getAccountId(),
                        accountId
                ));
            }
            LocalDate nextPeriodFrom = ptf.getPeriodTo().plusDays(1);
            if (nextPeriodFrom.isBefore(periodFrom)) {
                boolean isTodayPeriod = periodFrom.isEqual(today) && periodTo.isEqual(today);
                FlexStatementType statementType = flexStatement.getType();
                boolean tolerateMissingPeriod = (statementType == null && isTodayPeriod) || FlexStatementType.TCF.equals(statementType);
                if (!tolerateMissingPeriod) {
                    throw new IbkrServiceException(format("Missing period: '%s - %s', accountId=%s",
                            nextPeriodFrom, periodFrom.minusDays(1), accountId));
                }
            }
            if (periodTo.isAfter(ptf.getPeriodTo())) {
                ptf.setPeriodTo(periodTo);
            }
        }

        List<Transaction> ptfTrans = ptf.getTransactions();
        Set<String> tranIds = ptfTrans.stream().map(Transaction::getId).collect(toCollection(HashSet::new));

        List<Transaction> newCashTrans = transactionMapper.mapCashTransactions(tranIds, rawCashTrans);
        tranIds.addAll(newCashTrans.stream().map(Transaction::getId).toList());

        List<Transaction> newTrades = transactionMapper.mapTrades(tranIds, rawTrades);
        tranIds.addAll(newTrades.stream().map(Transaction::getId).toList());

        List<Transaction> newTradeConfirms = transactionMapper.mapTradeConfirms(tranIds, rawTradeConfirms);
        tranIds.addAll(newTradeConfirms.stream().map(Transaction::getId).toList());

        List<Transaction> newCorpActions = transactionMapper.mapCorporateAction(tranIds, rawCorpActions);
        tranIds.addAll(newTradeConfirms.stream().map(Transaction::getId).toList());

        List<Transaction> newTrans = new ArrayList<>();
        newTrans.addAll(newCashTrans);
        newTrans.addAll(newTrades);
        newTrans.addAll(newTradeConfirms);
        newTrans.addAll(newCorpActions);
        newTrans.sort(comparing(Transaction::getId));

        for (Transaction newTran : newTrans) {
            ptfTrans.add(newTran);
            ptfManager.applyTransaction(ptf, newTran);
        }

        return ptf;
    }

    @Override
    public String fetchStatement(String token, String flexQueryId) {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        String tokenPrefix = token.substring(0, 4);
        try {
            String referenceCode = null;
            String baseUrl2 = null;
            for (int i = 0; i < 10; i++) {
                var req1 = HttpRequest.newBuilder(URI.create(flexQueryUrl.formatted(token, flexQueryId))).build();
                var resp1 = httpClient.send(req1, HttpResponse.BodyHandlers.ofString());
                var respBody1 = resp1.body();

                if (httpRespContainsRepeatableError(respBody1)) {
                    Thread.sleep(i * 2000);
                    continue;
                }

                String status = null;
                {
                    Matcher m1 = LazyHolder.HTTP_RESP1_STATUS_PATTERN.matcher(respBody1);
                    if (m1.find()) {
                        status = m1.group(1);
                    }
                    Matcher m2 = LazyHolder.HTTP_RESP1_REFERENCE_CODE_PATTERN.matcher(respBody1);
                    if (m2.find()) {
                        referenceCode = m2.group(1);
                    }
                    Matcher m3 = LazyHolder.HTTP_RESP1_URL_PATTERN.matcher(respBody1);
                    if (m3.find()) {
                        baseUrl2 = m3.group(1);
                    }
                }
                if (status == null || referenceCode == null || baseUrl2 == null || !status.equals("Success")) {
                    throw new IbkrServiceException("Fetch failed - flexQueryId=%s, token=%s, resp=%s"
                            .formatted(flexQueryId, tokenPrefix, respBody1));
                } else {
                    break;
                }
            }

            for (int i = 0; i < 10; i++) {
                Thread.sleep(i * 1000 + 1000);
                var url2 = baseUrl2 + ("?q=%s&t=%s&v=3".formatted(referenceCode, token));
                var req2 = HttpRequest.newBuilder(URI.create(url2)).build();
                var resp2 = httpClient.send(req2, HttpResponse.BodyHandlers.ofString());
                var respBody2 = resp2.body();
                if (httpRespContainsRepeatableError(respBody2)) {
                    if (i < 9) {
                        continue;
                    }
                    throw new IbkrServiceException("Fetch failed - flexQueryId=%s, token=%s, resp%s"
                            .formatted(flexQueryId, tokenPrefix, respBody2));
                }
                return respBody2;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IbkrServiceException("Interrupted - flexQueryId=%s, token=%s".formatted(flexQueryId, tokenPrefix));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        throw new IbkrServiceException("Fetch failed - flexQueryId=%s, token=%s".formatted(flexQueryId, tokenPrefix));
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean httpRespContainsRepeatableError(String respBody2) {
        /*
        <FlexStatementResponse timestamp='26 July, 2023 05:45 AM EDT'>
            <Status>Warn</Status>
            <ErrorCode>1019</ErrorCode>
            <ErrorMessage>Statement generation in progress. Please try again shortly.</ErrorMessage>
        </FlexStatementResponse>
         */
        if (respBody2.contains("<ErrorCode>1019</ErrorCode>")) {
            return true;
        }

        /*
        <FlexStatementResponse timestamp='05 August, 2023 08:01 AM EDT'>
            <Status>Fail</Status>
            <ErrorCode>1018</ErrorCode>
            <ErrorMessage>Too many requests have been made from this token. Please try again shortly.</ErrorMessage>
        </FlexStatementResponse>
         */
        if (respBody2.contains("<ErrorCode>1018</ErrorCode>")) {
            return true;
        }
        return false;
    }

}
