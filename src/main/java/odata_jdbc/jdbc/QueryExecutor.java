package odata_jdbc.jdbc;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

public class QueryExecutor {

    private final String serviceRootUrl;
    private final Properties jdbcProperties;

    private HttpURLConnection conn;

    public QueryExecutor(String serviceRootUrl, Properties jdbcProperties) {
        this.serviceRootUrl = serviceRootUrl;
        this.jdbcProperties = jdbcProperties;
    }

    public String executeQuery(String sql) throws SQLException {
        // FIXME: JDBC仕様ではjava.sql.Connectionはスレッドセーフであり、一般的なJDBCドライバはロックや直列化してスレッドセーフにしている模様なので対応必要

        RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
                .handle(SocketTimeoutException.class)
                .withBackoff(1, 4, ChronoUnit.SECONDS, 1.5)
                .withMaxRetries(2);
        try {
            SqlParseResult sqlParseResult = new SelectSqlParser().parse(sql);
            URL url = new ODataUrlBuilder(serviceRootUrl, sqlParseResult).toURL();

            // SQL実行時でないとODataサービスURLが決まらないので、このタイミングで接続する
            conn = (HttpURLConnection) url.openConnection();

            // TODO: 設定可能にする
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            // TODO: CSRF対策としてX-Requested-Withヘッダをチェックされる場合用に更新系の処理の場合に設定可能にする（GETには不要）
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoInput(true);
            Failsafe.with(retryPolicy).run(conn::connect);

            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    result.append(line);
                }
            }
            return result.toString();
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    public void close() {
        conn.disconnect();
    }

}
