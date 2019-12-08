package odata_jdbc.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectStatement implements SqlStatement {

    // TODO: 型を検討する
    private String quantifier;
    // TODO: First Class Collectionにする
    private List<String> selectList = new ArrayList<>();

    public void setQuantifier(String distinct) {
        this.quantifier = distinct;
    }

    public void addSelectList(String column) {
        selectList.add(column);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectStatement statement = (SelectStatement) o;
        return Objects.equals(quantifier, statement.quantifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantifier);
    }
}
