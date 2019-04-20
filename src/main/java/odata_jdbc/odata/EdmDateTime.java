package odata_jdbc.odata;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OData Edm.DateTime
 */
public class EdmDateTime {

    private final java.util.Date value;

    private EdmDateTime(java.util.Date value) {
        this.value = value;
    }

    /**
     * JSONフォーマットの値から EdmDateTime のインスタンスを取得します。
     *
     * @param jsonFormatValue Edm.DateTime JSONフォーマットの値
     * @return EdmDateTime null以外
     * @throws DateTimeException 値が不正な場合
     */
    public static EdmDateTime fromJson(String jsonFormatValue) {
        // Edm.DateTime JSON値の正規表現
        // e.g. /Date(1547164800000)/
        // FIXME: 実際にはオフセット付きはEdmDateTimeOffsetなので、整理必要 cf) https://blogs.sap.com/2017/01/05/date-and-time-in-sap-gateway-foundation/ これはV2の定義
        // e.g. /Date(1547164800000+540)/
        Pattern pattern = Pattern.compile("^/Date\\((-?\\d+)(\\+|-)?(\\d+)?\\)/$");
        Matcher m = pattern.matcher(jsonFormatValue);
        if (!m.matches()) {
            throw new DateTimeException(jsonFormatValue + " is not OData Edm.DateTime JSON format.");
        }
        // group(1): エポックミリ秒
        // group(2): タイムゾーンオフセットの+/-
        // group(3): タイムゾーンオフセットの分
        java.util.Date dateTime = new java.util.Date(Long.valueOf(m.group(1)));
        if (m.group(2) == null || m.group(3) == null) {
            return new EdmDateTime(dateTime);
        }
        String offsetSign = m.group(2);
        int offsetMinutes = Integer.valueOf(m.group(3));
        if (offsetSign.equals("-")) {
            offsetMinutes = -offsetMinutes;
        }
        OffsetDateTime offsetDateTime = dateTime.toInstant().atOffset(ZoneOffset.ofHoursMinutes(0, offsetMinutes));
        return new EdmDateTime(java.util.Date.from(offsetDateTime.toInstant()));
    }

    public static EdmDateTime fromDate(java.util.Date date) {
        return new EdmDateTime(date);
    }

    /**
     * リテラルフォーマット値に変換します。
     *
     * @return リテラルフォーマット値
     */
    public String toLiteral() {
        return "datetime'" + (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).format(value) + "'";
    }

    /**
     * ATOMフォーマット値に変換します。
     *
     * @return ATOMフォーマット値
     */
    public String toAtom() {
        return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).format(value);
    }

    public java.sql.Date toSqlDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    public java.sql.Time toSqlTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        return new java.sql.Time(cal.getTimeInMillis());
    }

    public java.sql.Timestamp toSqlTimestamp() {
        return new Timestamp(value.getTime());
    }

}
