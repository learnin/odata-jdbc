package odata_jdbc.jdbc;

import java.net.MalformedURLException;
import java.net.URL;

public class ODataUrlBuilder {

    private final String serviceRootUrl;
    private final SqlParseResult sqlParseResult;

    public ODataUrlBuilder(String serviceRootUrl, SqlParseResult sqlParseResult) {
        this.serviceRootUrl = serviceRootUrl;
        this.sqlParseResult = sqlParseResult;
    }

    public URL toURL() throws MalformedURLException {
        return new URL(serviceRootUrl + sqlParseResult.from());
    }
}
