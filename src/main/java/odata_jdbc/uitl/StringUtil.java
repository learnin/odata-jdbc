package odata_jdbc.uitl;

public class StringUtil {

    public static int indexOfIgnoreCase(String target, String value) {
        return target.toLowerCase().indexOf(value.toLowerCase());
    }

    public static int indexOfIgnoreCase(String target, String value, int fromIndex) {
        return target.toLowerCase().indexOf(value.toLowerCase(), fromIndex);
    }
}
