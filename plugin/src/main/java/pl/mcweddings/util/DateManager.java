package pl.mcweddings.util;

import java.util.Calendar;

public class DateManager {

    public static String getDate(String splitter) {
        return getDay() + splitter + getMonth() + splitter + getYear();
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

}
