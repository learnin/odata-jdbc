package odata_jdbc.jdbc;

/**
 * SQLのWHERE句の述語(e.g. id = 1)を表す
 */
public class WherePredicate {

    // 左辺
    private String leftExperssion;

    // 演算子
    private String operator;

    // 右辺
    private String rightExperssion;
}
