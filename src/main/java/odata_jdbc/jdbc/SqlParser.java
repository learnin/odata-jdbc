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

    private void tableExpression(SelectStatement statement) {
    }

    private void selectList(SelectStatement statement) {
        if (asterisk(statement)) {
            return;
        }
        selectSubList(statement);
        // TODO: カンマがなくなるまでselectSubListを繰り返す
    }

    private void selectSubList(SelectStatement statement) {
        if (derivedColumn(statement)) {
            return;
        }
        qualifier(statement);
        period(statement);
        // TODO: asterisk(statement);
    }

    private void qualifier(SelectStatement statement) {

    }

    private void period(SelectStatement statement) {

    }

    private boolean derivedColumn(SelectStatement statement) {
        return true;
    }
    
    private boolean asterisk(SelectStatement statement) {
        Optional<SqlToken> sqlToken = tokens.peek();
        if (sqlToken.isPresent() && sqlToken.get().value().equals("*")) {
            statement.addSelectList("*");
            tokens.nextPos();
            return true;
        }
        return false;
    }

    private void quantifier(SelectStatement statement) {
        tokens.peek().ifPresent(sqlToken -> {
            if (sqlToken.value().equalsIgnoreCase("DISTINCT")) {
                statement.setQuantifier("DISTINCT");
                tokens.nextPos();
            }
        });
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
