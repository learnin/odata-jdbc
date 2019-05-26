package odata_jdbc.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ODataUrlBuilderTest {

    @Test
    public void testReplaceAllExcludeWithinLiteral() {
        assertEquals("id eq 'a'", ODataUrlBuilder.replaceAllExcludeWithinLiteral("id = 'a'", "=", "eq"));
        assertEquals("id eq 'a=b'", ODataUrlBuilder.replaceAllExcludeWithinLiteral("id = 'a=b'", "=", "eq"));
        assertEquals("id eq 'a=b' or xx eq 'a=b'", ODataUrlBuilder.replaceAllExcludeWithinLiteral("id = 'a=b' or xx = 'a=b'", "=", "eq"));

        assertEquals("id eq 'a' and xx eq '1 AND 2'", ODataUrlBuilder.replaceAllExcludeWithinLiteral("id eq 'a' AND xx eq '1 AND 2'", " AND ", " and "));
    }

}
