package odata_jdbc.jdbc;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class ODataUrlBuilder {

    private final String serviceRootUrl;
    private final SqlParseResult sqlParseResult;

    public ODataUrlBuilder(String serviceRootUrl, SqlParseResult sqlParseResult) {
        this.serviceRootUrl = serviceRootUrl;
        this.sqlParseResult = sqlParseResult;
    }

    public URL toURL() throws MalformedURLException {
        String serviceUrl = serviceRootUrl + sqlParseResult.from();
        List<SelectSqlParser.SelectColumn> selectColumns = sqlParseResult.selectColumns();
        if (selectColumns.get(0).column().equals("*")) {
            return new URL(serviceUrl);
        }
        StringBuilder queryString = new StringBuilder();
        selectColumns.stream().map(selectColumn -> selectColumn.column())
            .reduce((accum, column) -> accum + ", " + column).ifPresent(columns -> {
                queryString.append("$select=" + urlEncode(columns));
        });

        String wherePhrase = sqlParseResult.wherePhrase();
        if (wherePhrase != null && !wherePhrase.isEmpty()) {
            wherePhrase = wherePhrase.replaceAll("=", "eq");
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append("$filter=" + urlEncode(wherePhrase));
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
}
