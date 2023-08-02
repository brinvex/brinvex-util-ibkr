package com.brinvex.util.ibkr.impl;

import com.brinvex.util.ibkr.api.model.Currency;
import com.brinvex.util.ibkr.api.model.Portfolio;
import com.brinvex.util.ibkr.api.model.raw.EquitySummary;
import com.brinvex.util.ibkr.api.model.raw.FlexStatement;
import com.brinvex.util.ibkr.api.service.IbkrService;
import com.brinvex.util.ibkr.api.service.IbkrServiceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IbkrServiceTest {

    private static TestHelper testHelper;

    @BeforeAll
    static void beforeAll() {
        testHelper = new TestHelper();
    }

    @AfterAll
    static void afterAll() throws Exception {
        testHelper.close();
    }

    @Test
    void parseTransactions() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s -> s.contains("Activity"));
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            FlexStatement flexStatement = ibkrService.parseActivitiesFromStatements(Stream.of(content));
            assertNotNull(flexStatement);
        }
    }


    @Test
    void processStatements1() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s -> s.contains("U029_Activity_20230101_20230726.xml"));
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(Stream.of(content));
            assertNotNull(ptf);

            assertEquals(2, ptf.getCash().size());
            assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("482.502129806")));
            assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("0.2340287")));

            assertEquals(13, ptf.getPositions().size());
        }
    }

    @Test
    void processStatements2() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(
                s -> s.contains("U029_TradeConfirm_20230726.xml") || s.contains("U029_Activity_20230101_20230726.xml"));
        Portfolio ptf = null;
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            ptf = ibkrService.fillPortfolioFromStatements(ptf, Stream.of(content));
        }
        assertNotNull(ptf);

        assertEquals(2, ptf.getCash().size());
        assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("482.502129806")));
        assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("0.2340287")));

        assertEquals(13, ptf.getPositions().size());
    }

    @Test
    void processStatements3() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = List.of(
                testHelper.getTestFilePath(s -> s.contains("U029_Activity_20230101_20230726.xml")),
                testHelper.getTestFilePath(s -> s.contains("U029_Activity_20230101_20230726.xml"))
        );
        Portfolio ptf = null;
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            ptf = ibkrService.fillPortfolioFromStatements(ptf, Stream.of(content));
        }
        assertNotNull(ptf);

        assertEquals(2, ptf.getCash().size());
        assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("482.502129806")));
        assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("0.2340287")));

        assertEquals(13, ptf.getPositions().size());
    }

    @Test
    void fetch() throws IOException {
        Path credentialsPath = testHelper.getTestFilePath(s -> s.contains("IBKR_Flex_credentials_LR"));
        if (credentialsPath != null) {
            List<String> credentials = Files.readAllLines(credentialsPath);
            String token = credentials.get(0);
            String activityFlexQueryId = credentials.get(1);
            String tradeConfirmFlexQueryId = credentials.get(2);
            IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
            String activityStatement = ibkrService.fetchStatement(token, activityFlexQueryId);
            String tradeConfirmStatement = ibkrService.fetchStatement(token, tradeConfirmFlexQueryId);
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(Stream.of(activityStatement, tradeConfirmStatement));
            assertNotNull(ptf);
        }
    }

    @Test
    void parseEquitySummaries() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = List.of(
                testHelper.getTestFilePath(s -> s.contains("U029_Activity_20220802_20230801.xml"))
        );
        FlexStatement flexStatement = ibkrService.parseEquitySummariesFromStatements(activityReportPaths);
        assertNotNull(flexStatement);


        List<EquitySummary> equitySummaries = flexStatement.getEquitySummaries();
        assertEquals(0, equitySummaries.get(0).getTotal().compareTo(BigDecimal.ZERO));

        EquitySummary newestEquitySummary = equitySummaries.get(equitySummaries.size() - 1);
        assertTrue(newestEquitySummary.getReportDate().isEqual(LocalDate.parse("2023-08-01")));

        BigDecimal total = new BigDecimal("14937.395623791");
        BigDecimal cash = new BigDecimal("260.616751991");
        BigDecimal stock = new BigDecimal("14672.7549922");
        BigDecimal dividendAccruals = new BigDecimal("4.0238796");
        assertEquals(0, newestEquitySummary.getCash().compareTo(cash));
        assertEquals(0, newestEquitySummary.getTotal().compareTo(total));
        assertEquals(0, newestEquitySummary.getStock().compareTo(stock));
        assertEquals(0, newestEquitySummary.getDividendAccruals().compareTo(dividendAccruals));
        assertEquals(0, cash.add(stock).add(dividendAccruals).compareTo(total));
    }
}