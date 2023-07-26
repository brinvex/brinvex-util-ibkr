package com.brinvex.util.ibkr.impl;

import com.brinvex.util.ibkr.api.model.Portfolio;
import com.brinvex.util.ibkr.api.model.Transaction;
import com.brinvex.util.ibkr.api.model.raw.FlexStatement;
import com.brinvex.util.ibkr.api.model.raw.RawTransaction;
import com.brinvex.util.ibkr.api.service.IbkrService;
import com.brinvex.util.ibkr.api.service.exception.IbkrServiceException;
import com.brinvex.util.ibkr.impl.parser.ActivityFlexStatementXmlParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

@SuppressWarnings({"GrazieInspection", "StructuralWrap"})
public class IbkrServiceImpl implements IbkrService {

    private final ActivityFlexStatementXmlParser activityFlexStatementXmlParser;

    private final PortfolioManager ptfManager;

    private final TransactionMapper transactionMapper;

    private static class LazyHolder {
        private static final Pattern HTTP_RESP1_STATUS_PATTERN = Pattern.compile("<Status>(.*)</Status>");
        private static final Pattern HTTP_RESP1_REFERENCE_CODE_PATTERN = Pattern.compile("<ReferenceCode>(.*)</ReferenceCode>");
        private static final Pattern HTTP_RESP1_URL_PATTERN = Pattern.compile("<Url>(.*)</Url>");
    }

    public IbkrServiceImpl() {
        this(new ActivityFlexStatementXmlParser(), new PortfolioManager(), new TransactionMapper());
    }

    public IbkrServiceImpl(
            ActivityFlexStatementXmlParser activityFlexStatementXmlParser,
            PortfolioManager ptfManager,
            TransactionMapper transactionMapper
    ) {
        this.activityFlexStatementXmlParser = activityFlexStatementXmlParser;
        this.ptfManager = ptfManager;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public FlexStatement parseStatements(Collection<Path> statementFilePaths) {
        return parseStatements(statementFilePaths
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
    public FlexStatement parseStatements(Stream<String> statementContents) {
        List<FlexStatement> rawFlexStatements = statementContents
                .map(activityFlexStatementXmlParser::parseStatement)
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

        Set<RawTransaction> rawTrans = new LinkedHashSet<>();
        Set<String> rawTranKeys = new LinkedHashSet<>();

        for (FlexStatement rawTranList : rawFlexStatements) {
            LocalDate fromDate = rawTranList.getFromDate();
            LocalDate toDate = rawTranList.getToDate();

            String accountId = rawTranList.getAccountId();
            if (!accountId0.equals(accountId)) {
                throw new IbkrServiceException(format("Unexpected multiple accounts: %s, %s",
                        accountId0,
                        accountId
                ));
            }

            LocalDate nextPeriodFrom = result.getToDate().plusDays(1);
            if (nextPeriodFrom.isBefore(fromDate)) {
                throw new IbkrServiceException(format("Missing period: '%s - %s', accountId=%s",
                        nextPeriodFrom, fromDate.minusDays(1), accountId0));
            }
            if (toDate.isAfter(result.getToDate())) {
                result.setToDate(toDate);
            }

            for (RawTransaction rawTransaction : rawTranList.getTransactions()) {
                String tradeKey = "%s/%s/%s".formatted(
                        rawTransaction.getTradeID(),
                        rawTransaction.getTransactionID(),
                        rawTransaction.getIbOrderID()
                );
                if (rawTranKeys.add(tradeKey)) {
                    rawTrans.add(rawTransaction);
                }
            }
        }

        result.getTransactions().addAll(rawTrans
                .stream()
                .sorted(comparing(RawTransaction::getDateTime).thenComparing(RawTransaction::getTransactionID))
                .collect(toCollection(ArrayList::new))
        );

        return result;
    }

    @Override
    public Portfolio processStatements(Collection<Path> statementPaths) {
        Stream<String> statementContentStream = statementPaths
                .stream()
                .map(filePath -> {
                    try {
                        return Files.readString(filePath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        return processStatements(statementContentStream);
    }

    @Override
    public Portfolio processStatements(Stream<String> statementContents) {
        return processStatements(null, statementContents);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public Portfolio processStatements(Portfolio ptf, Stream<String> statementContents) {
        FlexStatement flexStatement = parseStatements(statementContents);
        List<RawTransaction> rawTransactions = flexStatement.getTransactions();

        String accountId = flexStatement.getAccountId();
        LocalDate periodFrom = flexStatement.getFromDate();
        LocalDate periodTo = flexStatement.getToDate();
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
                throw new IbkrServiceException(format("Missing period: '%s - %s', accountId=%s",
                        nextPeriodFrom, periodFrom.minusDays(1), accountId));
            }
            if (periodTo.isAfter(ptf.getPeriodTo())) {
                ptf.setPeriodTo(periodTo);
            }
        }

        List<Transaction> ptfTrans = ptf.getTransactions();
        if (!ptfTrans.isEmpty()) {
            Transaction prevTran = ptfTrans.get(ptfTrans.size() - 1);
            LocalDateTime lastPtfTranDate = prevTran.getDate().toLocalDateTime();
            rawTransactions.removeIf(rawTran -> !rawTran.getDateTime().isAfter(lastPtfTranDate));
        }

        List<Transaction> newTrans = transactionMapper.mapTransactions(rawTransactions);
        for (Transaction newTran : newTrans) {
            ptfTrans.add(newTran);
            ptfManager.applyTransaction(ptf, newTran);
        }

        return ptf;
    }

    @Override
    public String fetchStatement(String token, String flexQueryId) {
        String referenceCode = null;
        String baseUrl2 = null;
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        try {
            String url1 = "https://www.interactivebrokers.com/Universal/servlet/FlexStatementService.SendRequest?t=%s&q=%s&v=3"
                    .formatted(token, flexQueryId);
            HttpRequest req1 = HttpRequest.newBuilder(URI.create(url1)).build();
            HttpResponse<String> resp1 = httpClient.send(req1, HttpResponse.BodyHandlers.ofString());
            String respBody1 = resp1.body();

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
                throw new IbkrServiceException("Fetch failed - flexQueryId=%s, token=%s... -> status=%s, referenceCode=%s, baseUrl2=%s"
                        .formatted(flexQueryId, token.substring(0, 4), status, referenceCode, baseUrl2));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (int i = 1; i <= 10; i++) {
            try {
                Thread.sleep(i * 1000);
                String url2 = baseUrl2 + ("?q=%s&t=%s&v=3".formatted(referenceCode, token));
                HttpRequest req2 = HttpRequest.newBuilder(URI.create(url2)).build();
                HttpResponse<String> resp2 = httpClient.send(req2, HttpResponse.BodyHandlers.ofString());
                String respBody2 = resp2.body();

                /*
                <FlexStatementResponse timestamp='26 July, 2023 05:45 AM EDT'>
                    <Status>Warn</Status>
                    <ErrorCode>1019</ErrorCode>
                    <ErrorMessage>Statement generation in progress. Please try again shortly.</ErrorMessage>
                </FlexStatementResponse>
                 */
                if (respBody2.contains("<ErrorCode>1019</ErrorCode>")) {
                    continue;
                }
                return respBody2;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        throw new IbkrServiceException("Fetch failed - flexQueryId=%s, token=%s..."
                .formatted(flexQueryId, token.substring(0, 4)));
    }

}
