package odata_jdbc.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ODataUrlBuilderTest {

    @Test
    public void testReplaceAllExcludeWithinLiteral() {
        assertEquals("id eq 'a'", ODataUrlBuilder.replaceAllIgnoreCaseExcludeWithinLiteral("id = 'a'", "=", "eq"));
        assertEquals("id eq 'a=b'", ODataUrlBuilder.replaceAllIgnoreCaseExcludeWithinLiteral("id = 'a=b'", "=", "eq"));
        assertEquals("id eq 'a=b' or xx eq 'a=b'", ODataUrlBuilder.replaceAllIgnoreCaseExcludeWithinLiteral("id = 'a=b' or xx = 'a=b'", "=", "eq"));

        assertEquals("id eq 'a' and xx eq '1 AND 2'", ODataUrlBuilder.replaceAllIgnoreCaseExcludeWithinLiteral("id eq 'a' AND xx eq '1 AND 2'", " AND ", " and "));
        assertEquals("id eq 'a' and xx eq '1 And 2' and x eq 1", ODataUrlBuilder.replaceAllIgnoreCaseExcludeWithinLiteral("id eq 'a' And xx eq '1 And 2' anD x eq 1", " AND ", " and "));
    }

}
