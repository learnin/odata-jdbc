package odata_jdbc.jdbc;

import odata_jdbc.uitl.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

public class ODataUrlBuilder {

    private final String serviceRootUrl;
    private final SqlParseResult sqlParseResult;

    // TODO 設定可能にする
    private int oDataVersion = 4;

    public ODataUrlBuilder(String serviceRootUrl, SqlParseResult sqlParseResult) {
        this.serviceRootUrl = serviceRootUrl;
        this.sqlParseResult = sqlParseResult;
    }

    public URL toURL() throws MalformedURLException {
        String serviceUrl = serviceRootUrl + sqlParseResult.from();
        StringBuilder queryString = new StringBuilder();

        List<SelectSqlParser.SelectColumn> selectColumns = sqlParseResult.selectColumns();
        if (!selectColumns.get(0).column().equals("*")) {
            selectColumns.stream().map(selectColumn -> selectColumn.column())
                    .reduce((accum, column) -> accum + ", " + column).ifPresent(columns -> {
                queryString.append("$select=" + urlEncode(columns));
            });
        }

        String whereClause = sqlParseResult.whereClause();
        if (whereClause != null && !whereClause.isEmpty()) {
            // Logical Operators
            // FIXME: id = 'a==' のように値に = とかが入っているとそこまで置換されてしまうので修正必要
            // -> パース時にWHERE句を1つの文字列ではなく、Ltst<WherePredicate>にすることを考え方が、そこまでしなくても
            //    以下の置換処理を対象がシングルクォーテーションの外かどうか(置換対象文字以前のシングルクォーテーションを先頭から数えて偶数個か（''は除く））を見ながら置換すればよさそう
            whereClause = replaceAllExcludeWithinLiteral(whereClause,"!=", "ne");
            whereClause = replaceAllExcludeWithinLiteral(whereClause,"<>", "ne");
            whereClause = replaceAllExcludeWithinLiteral(whereClause,"<=", "le");
            whereClause = replaceAllExcludeWithinLiteral(whereClause,">=", "ge");
//            whereClause = whereClause.replaceAll("=", "eq");
            whereClause = replaceAllExcludeWithinLiteral(whereClause,"=", "eq");
            whereClause = replaceAllExcludeWithinLiteral(whereClause,"<", "lt");
            whereClause = replaceAllExcludeWithinLiteral(whereClause,">", "gt");
            whereClause = replaceAllIgnoreCase(whereClause, " AND ", " and ");
            whereClause = replaceAllIgnoreCase(whereClause, " OR ", " or ");
            whereClause = replaceAllIgnoreCase(whereClause, " NOT ", " not ");
            whereClause = replaceAllIgnoreCase(whereClause, " IN ", " in ");

            // String Functions
            whereClause = transformLikeToODataOperator(whereClause);
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append("$filter=" + urlEncode(whereClause));
            // FIXME: テーブル名に別名をつけていて、hoge.column1 = 'xxx' みたいな場合の考慮
        }

        if (queryString.length() > 0) {
            serviceUrl += "?" + queryString.toString();
        }
        return new URL(serviceUrl);
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Tuple replaceExcludeWithinLiteral(String target, String replaced, String replacement, int index) {
        int replacedIndex = target.indexOf(replaced, index);
        if (replacedIndex == -1) {
            return new Tuple(replacedIndex, target);
        }
        int singleQuoteCount = 0;
        int searchIndex = -1;
        while (true) {
            // FIXME: シングルクォーテーションは毎回1文字目から数え直しているので、メモ化させる等して
            // どの文字indexでシングルクォーテーション何個目（または奇数か偶数か/isOpen?）という情報を保持させる
            int singleQuoteIndex = target.indexOf("'", searchIndex + 1);
            if (singleQuoteIndex == -1 || singleQuoteIndex >= replacedIndex) {
                break;
            }
            if ("'".equals(target.substring(singleQuoteIndex + 1, singleQuoteIndex + 2))) {
                // エスケープされたシングルクォーテーション
                searchIndex = singleQuoteIndex + 1;
            } else {
                singleQuoteCount++;
                searchIndex = singleQuoteIndex;
            }
        }
        if (singleQuoteCount % 2 == 0) {
            return new Tuple(replacedIndex, target.substring(0, replacedIndex) + replacement + target.substring(replacedIndex + replaced.length()));
        }
        return new Tuple(replacedIndex, target);
    }

    static String replaceAllExcludeWithinLiteral(String target, String replaced, String replacement) {
        int replacedCount = target.split(replaced).length;
        String result = target;
        int index = 0;
        for (int i = 0; i < replacedCount; i++) {
            Tuple tuple = replaceExcludeWithinLiteral(result, replaced, replacement, index);
            index = tuple.index + replacement.length();
            result = tuple.value;
        }
        return result;
    }

    private static class Tuple {
        final int index;
        final String value;

        Tuple(int index, String value) {
            this.index = index;
            this.value = value;
        }
    }

    private String replaceAllIgnoreCase(String target, String regex, String replacement) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(target).replaceAll(replacement);
    }

    private String transformLikeToODataOperator(String whereClause) {
        int likeIndex = StringUtil.indexOfIgnoreCase(whereClause, " LIKE ");
        if (likeIndex == -1) {
            return whereClause;
        }
        int spaceIndexBeforeTargetColumn = whereClause.lastIndexOf(" ", likeIndex - 1);
        if (spaceIndexBeforeTargetColumn == -1) {
            spaceIndexBeforeTargetColumn = 0;
        }
        String targetColumn = whereClause.substring(spaceIndexBeforeTargetColumn, likeIndex).trim();


        String afterLike = whereClause.substring(likeIndex + " LIKE ".length()).trim();
        int targetValueIndex = afterLike.indexOf(" ");
        String targetValue;
        if (targetValueIndex == -1) {
            targetValue = afterLike;
        } else {
            targetValue = afterLike.substring(0, targetValueIndex);
        }

        // FIXME: ODataバージョンでポリモーフィズムを使うようにする
        String oDataOperator = "";
        if (targetValue.startsWith("'%") && targetValue.endsWith("%'")) {
            targetValue = targetValue.replaceAll("%", "");

            if (oDataVersion == 2 || oDataVersion == 3) {
                oDataOperator = "substringof(" + targetValue + ", " + targetColumn + ")";
            } else {
                oDataOperator = "contains(" + targetColumn + "," + targetValue + ")";
            }
        } else if (targetValue.startsWith("'%")) {
            targetValue = targetValue.replaceAll("%", "");
            oDataOperator = "endswith(" + targetColumn + "," + targetValue + ")";
        } else if (targetValue.endsWith("%'")) {
            targetValue = targetValue.replaceAll("%", "");
            oDataOperator = "startswith(" + targetColumn + "," + targetValue + ")";
        } else {
            // FIXME: 先頭、末尾以外に%がある場合や、_がある場合、ESCAPE指定がある場合の考慮
            throw new RuntimeException();
        }

        String result = whereClause.substring(0, spaceIndexBeforeTargetColumn) + " " + oDataOperator;
        if (targetValueIndex != -1) {
            result += afterLike.substring(targetValueIndex);
        }
        return transformLikeToODataOperator(result);
    }

}
