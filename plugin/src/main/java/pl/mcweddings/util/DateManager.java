package pl.mcweddings.util;

import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateManager {

    public static String getDate(String splitter) {
        String month = getMonth() + "";
        if(getMonth() < 10) {
            month = "0" + month;
        }
        return getDay() + splitter + month + splitter + getYear();
    }

    public static int getDay() {
        return getCalendar().get(Calendar.DAY_OF_MONTH);
    }

    public static int getMonth() {
        return getCalendar().get(Calendar.MONTH);
    }

    public static int getYear() {
        return getCalendar().get(Calendar.YEAR);
    }

    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    @SneakyThrows
    public static long calculateDays(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
        Date dStart = sdf.parse(startDate);
        Date dEnd = sdf.parse(endDate);
        long diffInMillies = Math.abs(dEnd.getTime() - dStart.getTime());
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

}
