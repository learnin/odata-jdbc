package odata_jdbc.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;

public class QueryExecutor {

    private final String serviceUrl;
    private HttpURLConnection conn;

    public QueryExecutor(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String executeQuery(String sql) throws SQLException {
        try {
            // TODO: SQLのパース
            String endpoint = serviceUrl + "People";
            
            conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("GET");
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
