package odata_jdbc;

import odata_jdbc.jdbc.ODataDataSource;
import org.junit.jupiter.api.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class Example {

    @Test
    public void executeSqlUsingDriverManagerV4() throws Exception {
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
    public void executePrepareStatementWithSingleQuote() throws Exception {
        String sql = "SELECT *"
                + " FROM Customers"
                + " WHERE CompanyName = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:odata-jdbc:https://services.odata.org/V3/Northwind/Northwind.svc/", "", "")) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(0, "B's Beverages");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("B's Beverages", rs.getString("CompanyName"));
                    assertFalse(rs.next());
                }
            }
        }
    }

//    // TODOが未実装でテストが通らないため今はコメントアウト
//    @Test
//    public void executePrepareStatementWithGuid() throws Exception {
//        String sql = "SELECT *"
//                + " FROM Advertisements"
//                + " WHERE ID = ?";
//        try (Connection conn = DriverManager.getConnection("jdbc:odata-jdbc:https://services.odata.org/V3/OData/OData.svc/", "", "")) {
//            try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                // TODO: このIDフィールドはEdm.Guid型なので、$filter=ID eq guid'f89dee73-af9f-4cd4-b330-db93c25ff3c7' とする必要がある
//                //       Edm.Guid型かどうかの判定は$metadataを取得するしかなさそう？参考にするために、普通のJDBCドライバはResultSetMetaDataをいつ取得するのか調査する
//                //       -> PostgreSQLの場合、フィールド定義情報はResultSetインスタンス生成時にコンストラクタ引数として渡されており、SQL実行時に取得されているっぽい。
//                //          テーブル情報等一部の情報はResultSetMetaDataのメソッドを呼び出した時にSQL実行して取得している。
//                //       -> 毎回metadata取得よりはSQL独自文法・関数を導入してでも1リクエストにした方がユーザにとってはよくないか？
//                //          ps.setGuid(0, new Guid("f89dee73-af9f-4cd4-b330-db93c25ff3c7")); を用意するとか WHERE ID = guid'f89dee73-af9f-4cd4-b330-db93c25ff3c7' で書いてもらうとか。
//                //          ただしps.setGuidは実装してもO/Rマッパーからは呼ばれないだろうからあまり使われなさそう。
//                //       -> 他にもmetadataがほしいケースが出てくるかもしれないので、ある程度出揃ってから決める。
//                ps.setString(0, "f89dee73-af9f-4cd4-b330-db93c25ff3c7");
//                try (ResultSet rs = ps.executeQuery()) {
//                    assertTrue(rs.next());
//                    assertEquals("f89dee73-af9f-4cd4-b330-db93c25ff3c7", rs.getString("ID"));
//                    assertFalse(rs.next());
//                }
//            }
//        }
//    }

    @Test
    public void executeSqlUsingDriverManagerV2() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:odata-jdbc:https://services.odata.org/V2/(S(readwrite))/OData/OData.svc/", "", "")) {
            try (Statement statement = conn.createStatement()) {
                String sql = "SELECT *"
                        + " FROM Products"
                        + " WHERE Name = 'Bread'";
                try (ResultSet rs = statement.executeQuery(sql)) {
                    assertTrue(rs.next());
                    assertEquals("Bread", rs.getString("Name"));
                    assertEquals(LocalDate.of(1992, 1, 1), rs.getDate("ReleaseDate").toLocalDate());
                    assertEquals(new Timestamp(694224000000L), rs.getTimestamp("ReleaseDate"));
                    assertEquals(4, rs.getInt("Rating"));
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
