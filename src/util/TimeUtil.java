package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String now() {
        return SDF.format(new Date());
    }

    public static String format(long millis) {
        return SDF.format(new Date(millis));
    }

    public static long currentMillis() {
        return System.currentTimeMillis();
    }
}
