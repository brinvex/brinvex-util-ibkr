package com.brinvex.util.ibkr.impl;

import com.brinvex.util.ibkr.api.model.Currency;
import com.brinvex.util.ibkr.api.model.Portfolio;
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
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            FlexStatement flexStatement = ibkrService.parseStatements(Stream.of(content));
            assertNotNull(flexStatement);
        }
    }

    @Test
    void processStatements1() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s -> s.contains("U029_Activity_20230123_20230214.xml"));
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            Portfolio ptf = ibkrService.processStatements(Stream.of(content));
            assertNotNull(ptf);

            assertEquals(2, ptf.getCash().size());
            assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("507.960201999")));
            assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("0.3349")));

            assertEquals(1, ptf.getPositions().size());
        }
    }

    @Test
    void processStatements2() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s -> s.contains("U029_Activity_20230123_20230228.xml"));
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            Portfolio ptf = ibkrService.processStatements(Stream.of(content));
            assertNotNull(ptf);


            assertEquals(2, ptf.getCash().size());
            assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("2039.393661999")));
            assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("2934.203728565")));

            assertEquals(4, ptf.getPositions().size());
        }
    }

    @Test
    void processStatements3() throws IOException {
        IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
        List<Path> activityReportPaths = testHelper.getTestFilePaths(s -> s.contains("U029_Activity_20230101_20230724.xml"));
        for (Path activityReportPath : activityReportPaths) {
            String content = Files.readString(activityReportPath);
            Portfolio ptf = ibkrService.processStatements(Stream.of(content));
            assertNotNull(ptf);


            assertEquals(2, ptf.getCash().size());
            assertEquals(0, ptf.getCash().get(Currency.EUR).compareTo(new BigDecimal("1080.641169806")));
            assertEquals(0, ptf.getCash().get(Currency.USD).compareTo(new BigDecimal("0.7408142")));

            assertEquals(12, ptf.getPositions().size());
        }
    }

    @Test
    void fetch() throws IOException {
        Path credentialsPath = testHelper.getTestFilePath(s -> s.contains("IBKR_Flex_credentials_LR"));
        if (credentialsPath != null) {
            List<String> credentials = Files.readAllLines(credentialsPath);
            String token = credentials.get(0);
            String flexQueryId = credentials.get(1);
            IbkrService ibkrService = IbkrServiceFactory.INSTANCE.getIbkrService();
            String statement = ibkrService.fetchStatement(token, flexQueryId);
            Portfolio ptf = ibkrService.processStatements(Stream.of(statement));
            assertNotNull(ptf);
        }
    }
}