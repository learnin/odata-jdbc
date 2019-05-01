package odata_jdbc.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractODataStatement {

    protected final ODataConnection conn;

    public AbstractODataStatement(ODataConnection conn) {
        this.conn = conn;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        QueryExecutor queryExecutor = conn.getQueryExecutor();
        String oDataPayloadString = queryExecutor.executeQuery(sql);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> oDataPayload = null;
        try {
            oDataPayload = objectMapper.readValue(oDataPayloadString, new TypeReference<LinkedHashMap<String, Object>>(){});
        } catch (IOException e) {
            throw new SQLException(e);
        }

        // TODO: OData versionはJDBCプロパティから指定。指定されていなければOData $metadataをたたいて判定
        if (oDataPayload.containsKey("value")) {
            // OData V4 の場合
            return new ODataResultSet((List<Map<String, Object>>) oDataPayload.get("value"));
        } else if (oDataPayload.containsKey("d")) {
            // OData V2 の場合
            return new ODataResultSet((List<Map<String, Object>>) oDataPayload.get("d"));
        }
        return new ODataResultSet(new ArrayList<>());
    }

}
