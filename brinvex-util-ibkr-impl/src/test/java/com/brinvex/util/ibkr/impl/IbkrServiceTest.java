package com.brinvex.util.ibkr.impl;

import com.brinvex.util.ibkr.api.model.Currency;
import com.brinvex.util.ibkr.api.model.Portfolio;
import com.brinvex.util.ibkr.api.model.Transaction;
import com.brinvex.util.ibkr.api.model.TransactionType;
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
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s -> s.contains("Activity-LR-IBKR-20220803-20230802.xml"));
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(Stream.of(content));
            assertNotNull(ptf);

            assertEquals(2, ptf.getCash().size());
            assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("43.659223735")));
            assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("0.402378700")));

            assertEquals(15, ptf.getPositions().size());
        }
    }

    @Test
    void processStatements2() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s -> s.contains("Activity-LR-IBKR-20230203-20240202.xml"));
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(Stream.of(content));
            assertNotNull(ptf);
        }
    }

    @Test
    void processStatements3() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = List.of(
                testHelper.getTestFilePath(s -> s.contains("Activity-LR-IBKR-20220803-20230802.xml")),
                testHelper.getTestFilePath(s -> s.contains("Activity-LR-IBKR-20221118-20231117.xml"))
        );
        Portfolio ptf = null;
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            ptf = ibkrService.fillPortfolioFromStatements(ptf, Stream.of(content));
        }
        assertNotNull(ptf);

        assertEquals(2, ptf.getCash().size());
        assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("722.811854405")));
        assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("183.601774170")));

        assertEquals(23, ptf.getPositions().size());
    }

    @Test
    void processStatements4() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = List.of(
                testHelper.getTestFilePath(s -> s.contains("Activity-LR-IBKR-20220803-20230802.xml")),
                testHelper.getTestFilePath(s -> s.contains("Activity-LR-IBKR-20221130-20231129.xml"))
        );
        Portfolio ptf = null;
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            ptf = ibkrService.fillPortfolioFromStatements(ptf, Stream.of(content));
        }
        assertNotNull(ptf);

        assertEquals(2, ptf.getCash().size());
        assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("722.811854405")));
        assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("1071.101774170")));
        assertEquals(0, ptf.getPositions().stream()
                .filter(p -> p.getSymbol().equals("VMW"))
                .findFirst()
                .orElseThrow().getQty().compareTo(BigDecimal.ZERO));

        assertEquals(23, ptf.getPositions().size());
    }

    @Test
    void processStatements5() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s -> s.contains("Activity-LR-IBKR-20230215-20240214.xml"));
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(Stream.of(content));
            assertNotNull(ptf);
        }
    }

    @Test
    void processSpinoff() {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        PortfolioManager ptfManager = new PortfolioManager();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s ->
                s.equals("Activity-LR-IBKR-20220803-20230802.xml") ||
                        s.equals("Activity-LR-IBKR-20230404-20240402.xml")
        );
        if (!activityReportPaths.isEmpty()) {
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(activityReportPaths);

            assertNotNull(ptf);
            assertEquals(0, ptfManager.findPosition(ptf, "GE").getQty().compareTo(new BigDecimal(6)));
            assertEquals(0, ptfManager.findPosition(ptf, "GEV").getQty().compareTo(new BigDecimal(1)));
            assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("234.561374405")));
            assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("153.48807417")));

            Transaction transformationTran = ptf.getTransactions().get(215);
            assertEquals(transformationTran.getType(), TransactionType.TRANSFORMATION);
            assertEquals(transformationTran.getSymbol(), "GEV");
            Transaction sellTran = ptf.getTransactions().get(216);
            assertEquals(sellTran.getType(), TransactionType.SELL);
            assertEquals(sellTran.getSymbol(), "GEV");
        }
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
            FlexStatement activities = ibkrService.parseActivitiesFromStatements(Stream.of(activityStatement, tradeConfirmStatement));
            assertNotNull(activities);
            FlexStatement summaries = ibkrService.parseEquitySummariesFromStatements(Stream.of(activityStatement, tradeConfirmStatement));
            assertNotNull(summaries);
        }
    }

    @Test
    void parseEquitySummaries() {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = List.of(
                testHelper.getTestFilePath(s -> s.contains("Activity-LR-IBKR-20220803-20230802.xml"))
        );
        FlexStatement flexStatement = ibkrService.parseEquitySummariesFromStatements(activityReportPaths);
        assertNotNull(flexStatement);


        List<EquitySummary> equitySummaries = flexStatement.getEquitySummaries();
        assertEquals(0, equitySummaries.get(0).getTotal().compareTo(BigDecimal.ZERO));

        EquitySummary newestEquitySummary = equitySummaries.get(equitySummaries.size() - 1);
        assertTrue(newestEquitySummary.getReportDate().isEqual(LocalDate.parse("2023-08-02")));

        BigDecimal total = new BigDecimal("15899.966794514");
        BigDecimal cash = new BigDecimal("44.027090414");
        BigDecimal stock = new BigDecimal("15843.9724334");
        BigDecimal dividendAccruals = new BigDecimal("11.9672707");
        assertEquals(0, newestEquitySummary.getCash().compareTo(cash));
        assertEquals(0, newestEquitySummary.getTotal().compareTo(total));
        assertEquals(0, newestEquitySummary.getStock().compareTo(stock));
        assertEquals(0, newestEquitySummary.getDividendAccruals().compareTo(dividendAccruals));
        assertEquals(0, cash.add(stock).add(dividendAccruals).compareTo(total));
    }
}