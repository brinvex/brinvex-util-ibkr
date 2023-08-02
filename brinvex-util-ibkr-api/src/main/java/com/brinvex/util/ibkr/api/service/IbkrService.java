package com.brinvex.util.ibkr.api.service;

import com.brinvex.util.ibkr.api.model.Portfolio;
import com.brinvex.util.ibkr.api.model.raw.FlexStatement;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

public interface IbkrService {

    FlexStatement parseActivitiesFromStatements(Collection<Path> statementFilePaths);

    FlexStatement parseActivitiesFromStatements(Stream<String> statementContents);

    FlexStatement parseEquitySummariesFromStatements(Collection<Path> statementFilePaths);

    FlexStatement parseEquitySummariesFromStatements(Stream<String> statementContents);

    Portfolio fillPortfolioFromStatements(Collection<Path> statementPaths);

    Portfolio fillPortfolioFromStatements(Stream<String> statementContents);

    Portfolio fillPortfolioFromStatements(Portfolio ptf, Stream<String> statementContents);

    /**
     * See https://www.interactivebrokers.co.in/en/?f=asr_statements_tradeconfirmations&p=flexqueries4
     */
    String fetchStatement(String token, String flexQueryId);
}
