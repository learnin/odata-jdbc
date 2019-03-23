package odata_jdbc;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

public class Example {

    @Test
    public void executeSql() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:odata-jdbc://localhost/odata", "","")) {

        }
    }
}
