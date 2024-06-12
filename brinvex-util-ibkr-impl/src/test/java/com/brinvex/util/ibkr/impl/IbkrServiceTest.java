package com.brinvex.util.ibkr.impl;

import com.brinvex.util.ibkr.api.model.AssetCategory;
import com.brinvex.util.ibkr.api.model.AssetSubCategory;
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

import static com.brinvex.util.ibkr.api.model.Currency.EUR;
import static com.brinvex.util.ibkr.api.model.Currency.USD;
import static java.math.RoundingMode.HALF_UP;
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
            assertEquals(0, ptf.getCash().get(EUR).compareTo(new BigDecimal("43.659223735")));
            assertEquals(0, ptf.getCash().get(USD).compareTo(new BigDecimal("0.402378700")));

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
        assertEquals(0, ptf.getCash().get(EUR).compareTo(new BigDecimal("722.811854405")));
        assertEquals(0, ptf.getCash().get(USD).compareTo(new BigDecimal("183.601774170")));

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
        assertEquals(0, ptf.getCash().get(EUR).compareTo(new BigDecimal("722.811854405")));
        assertEquals(0, ptf.getCash().get(USD).compareTo(new BigDecimal("1071.101774170")));
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
            assertEquals(0, ptf.getCash().get(EUR).compareTo(new BigDecimal("234.561374405")));
            assertEquals(0, ptf.getCash().get(USD).compareTo(new BigDecimal("153.48807417")));

            Transaction transformationTran = ptf.getTransactions().get(215);
            assertEquals(transformationTran.getType(), TransactionType.TRANSFORMATION);
            assertEquals(transformationTran.getSymbol(), "GEV");
            Transaction sellTran = ptf.getTransactions().get(216);
            assertEquals(sellTran.getType(), TransactionType.SELL);
            assertEquals(sellTran.getSymbol(), "GEV");
        }
    }

    @Test
    void validateCashBalance20240430() {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s ->
                s.equals("Activity-LR-IBKR-20220803-20230802.xml") ||
                        s.equals("Activity-LR-IBKR-20230501-20240430.xml")
        );
        if (!activityReportPaths.isEmpty()) {
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(activityReportPaths);

            assertNotNull(ptf);
            assertEquals(0, ptf.getCash().get(EUR).setScale(2, HALF_UP).compareTo(new BigDecimal("284.92")));
            assertEquals(0, ptf.getCash().get(USD).setScale(2, HALF_UP).compareTo(new BigDecimal("164.64")));
        }
    }

    @Test
    void validateCashBalance20240531() {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s ->
                s.equals("Activity-LR-IBKR-20220803-20230802.xml") ||
                        s.equals("Activity-LR-IBKR-20230602-20240531.xml")
        );
        if (!activityReportPaths.isEmpty()) {
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(activityReportPaths);

            assertNotNull(ptf);
            assertEquals(0, ptf.getCash().get(EUR).setScale(2, HALF_UP).compareTo(new BigDecimal("130.13")));
            assertEquals(0, ptf.getCash().get(USD).setScale(2, HALF_UP).compareTo(new BigDecimal("54.07")));
        }
    }

    @Test
    void validateCashBalance20240605() {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s ->
                s.equals("Activity-LR-IBKR-20220803-20230802.xml") ||
                        s.equals("Activity-LR-IBKR-20230607-20240605.xml")
        );
        if (!activityReportPaths.isEmpty()) {
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(activityReportPaths);

            assertNotNull(ptf);
            assertEquals(0, ptf.getCash().get(EUR).setScale(2, HALF_UP).compareTo(new BigDecimal("1090.13")));
            assertEquals(0, ptf.getCash().get(USD).setScale(2, HALF_UP).compareTo(new BigDecimal("64.89")));
        }
    }

    @Test
    void validatePositions20240610() {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s ->
                s.equals("Activity-LR-IBKR-20220803-20230802.xml") ||
                        s.equals("Activity-LR-IBKR-20230612-20240610.xml")
        );
        if (!activityReportPaths.isEmpty()) {
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(activityReportPaths);

            assertNotNull(ptf);
            BigDecimal qty = ptf.getPositions().stream().filter(p -> p.getSymbol().equals("NVDA")).findFirst().orElseThrow().getQty();
            assertEquals("60", qty.toString());
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
    void parseTradeConfirm() {
        Path testFilePath = testHelper.getTestFilePath(f -> f.equals("TradeConfirm-LR-IBKR-20240418.xml"));
        if (testFilePath != null) {
            IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
            FlexStatement activityStatement = ibkrService.parseActivitiesFromStatements(List.of(testFilePath));
            assertNotNull(activityStatement);
            activityStatement.getTradeConfirms().forEach(tc -> {
                AssetCategory cat = tc.getAssetCategory();
                AssetSubCategory subCat = tc.getAssetSubCategory();
                assertTrue(
                        AssetCategory.STK.equals(cat) && AssetSubCategory.STK_COMMON.equals(subCat) ||
                        AssetCategory.CASH.equals(cat) && AssetSubCategory.CASH.equals(subCat)
                );
            });
        }
    }

    @Test
    void processTradeConfirm() {
        Path testFilePath = testHelper.getTestFilePath(f -> f.equals("TradeConfirm-LR-IBKR-20240418.xml"));
        if (testFilePath != null) {
            IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
            Portfolio ptf = ibkrService.fillPortfolioFromStatements(List.of(testFilePath));
            assertNotNull(ptf);
            ptf.getTransactions().forEach(t -> {
                AssetCategory cat = t.getAssetCategory();
                AssetSubCategory subCat = t.getAssetSubCategory();
                boolean isCommonsStock = AssetCategory.STK.equals(cat) && AssetSubCategory.STK_COMMON.equals(subCat);
                boolean isCash = AssetCategory.CASH.equals(cat) && AssetSubCategory.CASH.equals(subCat);
                assertTrue(isCommonsStock || isCash);
            });
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