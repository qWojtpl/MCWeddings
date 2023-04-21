package pl.mcweddings.util;

import java.util.Calendar;

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

}
