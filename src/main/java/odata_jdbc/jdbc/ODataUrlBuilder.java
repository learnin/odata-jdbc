package odata_jdbc.jdbc;

import odata_jdbc.uitl.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        String wherePhrase = sqlParseResult.wherePhrase();
        if (wherePhrase != null && !wherePhrase.isEmpty()) {
            // Logical Operators
            wherePhrase = wherePhrase.replaceAll("!=", "ne");
            wherePhrase = wherePhrase.replaceAll("<>", "ne");
            wherePhrase = wherePhrase.replaceAll("<=", "le");
            wherePhrase = wherePhrase.replaceAll(">=", "ge");
            wherePhrase = wherePhrase.replaceAll("=", "eq");
            wherePhrase = wherePhrase.replaceAll("<", "lt");
            wherePhrase = wherePhrase.replaceAll(">", "gt");
            wherePhrase = replaceAllIgnoreCase(wherePhrase, " AND ", " and ");
            wherePhrase = replaceAllIgnoreCase(wherePhrase, " OR ", " or ");
            wherePhrase = replaceAllIgnoreCase(wherePhrase, " NOT ", " not ");
            wherePhrase = replaceAllIgnoreCase(wherePhrase, " IN ", " in ");

            // String Functions
            wherePhrase = transformLikeToODataOperator(wherePhrase);
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append("$filter=" + urlEncode(wherePhrase));
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

    private String replaceAllIgnoreCase(String target, String regex, String replacement) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(target).replaceAll(replacement);
    }

    private String transformLikeToODataOperator(String wherePhrase) {
        int likeIndex = StringUtil.indexOfIgnoreCase(wherePhrase, " LIKE ");
        if (likeIndex == -1) {
            return wherePhrase;
        }
        int spaceIndexBeforeTargetColumn = wherePhrase.lastIndexOf(" ", likeIndex - 1);
        if (spaceIndexBeforeTargetColumn == -1) {
            spaceIndexBeforeTargetColumn = 0;
        }
        String targetColumn = wherePhrase.substring(spaceIndexBeforeTargetColumn, likeIndex).trim();


        String afterLike = wherePhrase.substring(likeIndex + " LIKE ".length()).trim();
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

        String result = wherePhrase.substring(0, spaceIndexBeforeTargetColumn) + " " + oDataOperator;
        if (targetValueIndex != -1) {
            result += afterLike.substring(targetValueIndex);
        }
        return transformLikeToODataOperator(result);
    }

}
