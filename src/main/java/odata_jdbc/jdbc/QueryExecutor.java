package odata_jdbc.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
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
        try {
            // TODO: SQLのパース
            String url = serviceRootUrl + "People";

            // SQL実行時でないとODataサービスURLが決まらないので、このタイミングで接続する
            conn = (HttpURLConnection) new URL(url).openConnection();

            // TODO: 設定可能にする
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoInput(true);
            conn.connect();
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
