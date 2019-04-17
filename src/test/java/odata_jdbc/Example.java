package odata_jdbc;

import odata_jdbc.jdbc.ODataDataSource;
import org.junit.jupiter.api.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Example {

    @Test
    public void executeSqlUsingDriverManager() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:odata-jdbc:https://services.odata.org/TripPinRESTierService/", "", "")) {
            try (Statement statement = conn.createStatement()) {
                String sql = "SELECT UserName, FirstName"
                        + " FROM People"
                        + " WHERE FirstName = 'Russell'"
                        + " And (UserName != 'russellwhyte' Or UserName = 'russellwhyte')"
                        + " AND Not (UserName != 'russellwhyte')"
                        + " AND UserName Like '%llw%'"
                        + " AND UserName Like '%e'"
                        + " AND UserName Like 'r%'";
                try (ResultSet rs = statement.executeQuery(sql)) {
                    assertTrue(rs.next());
                    assertEquals("russellwhyte", rs.getString("UserName"));
                    assertFalse(rs.next());
                }
            }
        }
    }

    @Test
    public void executeSqlUsingDataSource() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

        ODataDataSource ods = new ODataDataSource();
        ods.setServerName("services.odata.org");
        ods.setDatabaseName("TripPinRESTierService");

        Context context = null;
        try {
            context = new InitialContext();
            context.createSubcontext("java:");
            context.createSubcontext("java:comp");
            context.createSubcontext("java:comp/env");
            context.createSubcontext("java:comp/env/jdbc");
            context.bind("java:comp/env/jdbc/database", ods);
        } finally {
            if (context != null) {
                context.close();
            }
        }

        Context ic = null;
        try {
            ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/database");
            try (Connection conn = ds.getConnection()) {
                try (Statement statement = conn.createStatement()) {
                    String sql = "SELECT UserName, FirstName FROM People WHERE FirstName = 'Russell'";
                    try (ResultSet rs = statement.executeQuery(sql)) {
                        rs.next();
                        assertEquals("russellwhyte", rs.getString("UserName"));
                        assertFalse(rs.next());
                    }
                }
            }
        } finally {
            if (ic != null) {
                ic.close();
            }
        }
    }
}
