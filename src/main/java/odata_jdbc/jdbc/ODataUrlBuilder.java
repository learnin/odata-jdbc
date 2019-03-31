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
            try {
                queryString.append("$select=" + URLEncoder.encode(columns, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });
        if (queryString.length() > 0) {
            serviceUrl += "?" + queryString.toString();
        }
        return new URL(serviceUrl);
    }
}
