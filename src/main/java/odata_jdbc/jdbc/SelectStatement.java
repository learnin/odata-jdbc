package odata_jdbc.jdbc;

public class SelectStatement implements SqlStatement {

    // TODO: 型を検討する
    String quantifier;

    public void setQuantifier(String distinct) {
        this.quantifier = distinct;
    }
}
