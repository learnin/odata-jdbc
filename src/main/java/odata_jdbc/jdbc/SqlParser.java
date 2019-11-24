package odata_jdbc.jdbc;

import java.util.List;

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

    private SqlStatement selectStatement() {
        return null;
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
