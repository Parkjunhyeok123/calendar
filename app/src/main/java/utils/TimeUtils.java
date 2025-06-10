package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getTimeDifference(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;

        if (timeDifference < TimeUnit.HOURS.toMillis(24)) {
            long hours = TimeUnit.MILLISECONDS.toHours(timeDifference);
            return hours + "시간 전";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}

