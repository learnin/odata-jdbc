package odata_jdbc;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class Example {

    @Test
    public void executeSql() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:odata-jdbc:https://services.odata.org/TripPinRESTierService/", "","")) {
            try (Statement statement = conn.createStatement()) {
                String sql = "SELECT UserName, FirstName FROM People WHERE FirstName = 'Russell'";
                try (ResultSet rs = statement.executeQuery(sql)) {
                    rs.next();
                    assertEquals("russellwhyte", rs.getString("UserName"));
                    assertFalse(rs.next());
                }
            }
        }
    }
}
