package com.brinvex.util.ibkr.api.service;

import com.brinvex.util.ibkr.api.model.Portfolio;
import com.brinvex.util.ibkr.api.model.raw.FlexStatement;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

public interface IbkrService {

    FlexStatement parseStatements(Collection<Path> statementFilePaths);

    FlexStatement parseStatements(Stream<String> statementContents);

    Portfolio processStatements(Stream<String> statementContents);

    Portfolio processStatements(Portfolio ptf, Stream<String> statementContents);

    /**
     * See https://www.interactivebrokers.co.in/en/?f=asr_statements_tradeconfirmations&p=flexqueries4
     */
    String fetchStatement(String token, String flexQueryId);
}
