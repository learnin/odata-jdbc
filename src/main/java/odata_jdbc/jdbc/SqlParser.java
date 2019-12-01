package odata_jdbc.jdbc;

import java.util.List;
import java.util.Optional;

public class SqlParser {

    private SqlTokens tokens;

    SqlParser(SqlTokens tokens) {
        this.tokens = tokens;
    }

    SqlStatement parse() throws SqlParseException {
        SqlStatementType statementType = tokens.statementType();
        switch (statementType) {
            case SELECT:
                return selectStatement();
            case INSERT:
                return insertStatement();
            case UPDATE:
                return updateStatement();
            case DELETE:
                return deleteStatement();
            default:
                // TODO 引数
                throw new SqlParseException();
        }
    }

    private SelectStatement selectStatement() {
        SelectStatement statement = new SelectStatement();
        tokens.next();
        quantifier(statement);
        selectList(statement);
        tableExpression(statement);
        return statement;
    }

    private SelectStatement tableExpression(SelectStatement statement) {
        return null;
    }

    private SelectStatement selectList(SelectStatement statement) {
        return null;
    }

    private SelectStatement quantifier(SelectStatement statement) {
        tokens.peek().ifPresent(sqlToken -> {
            if (sqlToken.value().equalsIgnoreCase("DISTINCT")) {
                statement.setQuantifier("DISTINCT");
                tokens.nextPos();
            }
        });
        return statement;
    }

    private SqlStatement insertStatement() {
        return null;
    }

    private SqlStatement updateStatement() {
        return null;
    }

    private SqlStatement deleteStatement() {
        return null;
    }
}
