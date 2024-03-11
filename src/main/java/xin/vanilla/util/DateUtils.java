package xin.vanilla.util;

import org.sqlite.date.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class DateUtils {

    public static final String HMS_FORMAT = "HH:mm:ss";
    public static final String ISO_YEAR_FORMAT = "yyyy";
    public static final String ISO_MONTH_FORMAT = "yyyyMM";
    public static final String ISO_DATE_FORMAT = "yyyyMMdd";
    public static final String ISO_EXPANDED_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_ISO_DATE_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String CHINESE_EXPANDED_DATE_FORMAT = "yyyy年MM月dd日";
    public static final String TAIWAN_DATE_FORMAT = "yyyy/MM/ddHHmm";
    public static final String TAIWAN_DATE_FORMAT2 = "yyyy/MM/dd";
    public static final String DATE_FORMAT_DATETIME_14 = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT_POINTYYYYMMDD = "yyyy.MM.dd";

    public DateUtils() {
    }

    public static String toString(Date date) {
        return toString(date, ISO_EXPANDED_DATE_FORMAT);
    }

    public static String toDateTimeString(Date date) {
        return toString(date, DATETIME_PATTERN);
    }

    public static String toString(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static Date getCurrYearFirst() {
        Calendar currCal = Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return getYearFirst(currentYear);
    }

    public static Date getCurrYearLast() {
        Calendar currCal = Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return getYearLast(currentYear);
    }

    public static Date getYearFirst(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    public static Date getYearLast(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }

    public static Date getMinDate() {
        return fromLocalDate(LocalDate.MIN);
    }

    public static Date getMaxDate() {
        return fromLocalDate(LocalDate.MAX);
    }

    public static int daysBetween(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / 86400000L;
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static Date parse(String dateStr, String pattern) {
        if (StringUtils.isNullOrEmpty(dateStr)) {
            return null;
        } else {
            try {
                return (new SimpleDateFormat(pattern)).parse(dateStr);
            } catch (ParseException var3) {
                return null;
            }
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static Date parse(String dateStr) {
        return parse(dateStr, "yyyy-MM-dd");
    }

    public static boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(date1).equals(fmt.format(date2));
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMM");
        return fmt.format(date1).equals(fmt.format(date2));
    }

    public static boolean isSameYear(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy");
        return fmt.format(date1).equals(fmt.format(date2));
    }

    public static Date format(String strTime) {
        return format(strTime, null);
    }

    public static Date format(String strTime, String pattern) {
        if (StringUtils.isNullOrEmpty(strTime)) {
            return null;
        } else {
            Date date = null;
            List<String> formats = new ArrayList<>();
            if (!StringUtils.isNullOrEmpty(pattern)) {
                formats.add(pattern);
            } else {
                formats.add("HH:mm:ss");
                formats.add("yyyy");
                formats.add("yyyyMM");
                formats.add("yyyyMMdd");
                formats.add("yyyy-MM-dd");
                formats.add("yyyy-MM-dd HH:mm");
                formats.add("yyyy-MM-dd HH:mm:ss");
                formats.add("yyyy年MM月dd日");
                formats.add("yyyy/MM/ddHHmm");
                formats.add("yyyy/MM/dd");
                formats.add("yyyyMMddHHmmss");
                formats.add("yyyy.MM.dd");
            }

            for (String format : formats) {
                if ((strTime.indexOf("-") <= 0 || format.contains("-")) && (strTime.contains("-") || format.indexOf("-") <= 0) && strTime.length() <= format.length()) {
                    date = formatEx(strTime, format);
                    if (date != null) {
                        break;
                    }
                }
            }

            return date;
        }
    }

    public static Date addYear(Date current, int year) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        calendar.add(Calendar.YEAR, year);
        return calendar.getTime();
    }

    public static Date addYear(Date current, float year) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        double floor = Math.floor(year);
        calendar.add(Calendar.YEAR, (int) floor);
        calendar.add(Calendar.DATE, (int) (DateUtils.getDaysOfYear(current) * (year - floor)));
        return calendar.getTime();
    }

    public static Date addMonth(Date current, int month) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        calendar.add(Calendar.MONTH, month);
        return calendar.getTime();
    }

    public static Date addMonth(Date current, float month) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        double floor = Math.floor(month);
        calendar.add(Calendar.MONTH, (int) floor);
        calendar.add(Calendar.DATE, (int) (DateUtils.getDaysOfMonth(calendar.getTime()) * (month - floor)));
        return calendar.getTime();
    }

    public static Date addDay(Date current, int day) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    public static Date addDay(Date current, float day) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        double floor = Math.floor(day);
        calendar.add(Calendar.DATE, (int) floor);
        calendar.add(Calendar.MILLISECOND, (int) (24 * 60 * 60 * 1000 * (day - floor)));
        return calendar.getTime();
    }

    public static Date addHour(Date current, int hour) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        calendar.add(Calendar.HOUR, hour);
        return calendar.getTime();
    }

    public static Date addHour(Date current, float hour) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        double floor = Math.floor(hour);
        calendar.add(Calendar.HOUR, (int) floor);
        calendar.add(Calendar.MILLISECOND, (int) (60 * 60 * 1000 * (hour - floor)));
        return calendar.getTime();
    }

    public static Date addMinute(Date current, int minute) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        calendar.add(Calendar.MINUTE, minute);
        return calendar.getTime();
    }

    public static Date addMinute(Date current, float minute) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        double floor = Math.floor(minute);
        calendar.add(Calendar.MINUTE, (int) floor);
        calendar.add(Calendar.MILLISECOND, (int) (60 * 1000 * (minute - floor)));
        return calendar.getTime();
    }

    public static Date addSecond(Date current, int second) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        calendar.add(Calendar.SECOND, second);
        return calendar.getTime();
    }

    public static Date addSecond(Date current, float second) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        double floor = Math.floor(second);
        calendar.add(Calendar.SECOND, (int) floor);
        calendar.add(Calendar.MILLISECOND, (int) (1000 * (second - floor)));
        return calendar.getTime();
    }

    public static Date addMilliSecond(Date current, int ms) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        calendar.add(Calendar.MILLISECOND, ms);
        return calendar.getTime();
    }

    private static Date formatEx(String dateStr, String pattern) {
        if (StringUtils.isNullOrEmpty(dateStr)) {
            return null;
        } else {
            try {
                return (new SimpleDateFormat(pattern)).parse(dateStr);
            } catch (ParseException var3) {
                return null;
            }
        }
    }

    public static int getYearOfDate(Date current) {
        if (current == null) {
            current = new Date();
        }

        int result;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        result = calendar.get(Calendar.YEAR);
        return result;
    }

    public static int getQuarterOfYear(Date current) {
        if (current == null) {
            current = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        return calendar.get(Calendar.MONTH) / 3 + 1;
    }

    public static int getMonthOfDate(Date date) {
        if (date == null) {
            date = new Date();
        }

        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getMonthValue();
    }

    public static String getMonthOfDateWithZero(Date date) {
        if (date == null) {
            date = new Date();
        }

        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getMonthValue() < 10 ? "0" + localDate.getMonthValue() : String.valueOf(localDate.getMonthValue());
    }

    public static int getDayOfMonth(Date date) {
        if (date == null) {
            date = new Date();
        }

        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getDayOfMonth();
    }

    public static Date getLastDayOfMonth(Date date) {
        if (date == null) {
            date = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int lastDay = calendar.getActualMaximum(Calendar.DATE);
        calendar.set(Calendar.DATE, lastDay);
        return toEndTime(calendar.getTime());
    }

    public static int getQuarterOfYear() {
        return getQuarterOfYear(new Date());
    }

    public static Date getCurrentQuarterStartTime(int quarter) {
        Calendar calendar = Calendar.getInstance();
        switch (quarter) {
            case 1:
                calendar.set(Calendar.MONTH, 0);
                break;
            case 2:
                calendar.set(Calendar.MONTH, 3);
                break;
            case 3:
                calendar.set(Calendar.MONTH, 6);
                break;
            case 4:
                calendar.set(Calendar.MONTH, 9);
        }

        calendar.set(Calendar.DATE, 1);
        return toStartTime(calendar.getTime());
    }

    public static Date getCurrentQuarterEndTime(int quarter) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getCurrentQuarterStartTime(quarter));
        calendar.add(Calendar.MONTH, 2);
        return getLastDayOfMonth(calendar.getTime());
    }

    public static Date getDate(int year, int month, int day, int hour, int minute, int second, int milliSecond) {
        Calendar cal = Calendar.getInstance();
        cal.setLenient(false);
        cal.set(year, month - 1, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, milliSecond);
        return cal.getTime();
    }

    public static Date getDate(int year, int month, int day) {
        return getDate(year, month, day, 0, 0, 0, 0);
    }

    public static Date toStartTime(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    public static Date toEndTime(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.HOUR_OF_DAY, 23);
        ca.set(Calendar.MINUTE, 59);
        ca.set(Calendar.SECOND, 59);
        ca.set(Calendar.MILLISECOND, 999);
        return ca.getTime();
    }

    public static Date dateAddMilliSecond(Date date, long milliSecond) {
        long time = date.getTime() + milliSecond;
        return new Date(time);
    }

    public static Date dateAddSecond(Date date, int second) {
        long milliSecond = (long) second * 1000L;
        return dateAddMilliSecond(date, milliSecond);
    }

    public static Date dateAddMinute(Date date, int minute) {
        long milliSecond = (long) minute * 60L * 1000L;
        return dateAddMilliSecond(date, milliSecond);
    }

    public static Date dateAddHour(Date date, int hour) {
        long milliSecond = (long) hour * 60L * 60L * 1000L;
        return dateAddMilliSecond(date, milliSecond);
    }

    public static Date dateAddDay(Date date, int day) {
        long milliSecond = (long) day * 24L * 60L * 60L * 1000L;
        return dateAddMilliSecond(date, milliSecond);
    }

    public static Date dateAddMonth(Date date, int month) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.MONTH, month);
        return ca.getTime();
    }

    public static Date dateAddYear(Date date, int year) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.YEAR, year);
        return ca.getTime();
    }

    public static int daysOfTwo(Date start, Date end) {
        if (start.after(end)) {
            Date t = start;
            start = end;
            end = t;
        }

        return (int) ((end.getTime() - start.getTime()) / 86400000L);
    }

    public static int monthOfTwo(Date start, Date end) {
        if (start.after(end)) {
            Date t = start;
            start = end;
            end = t;
        }

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        Calendar temp = Calendar.getInstance();
        temp.setTime(end);
        temp.add(Calendar.DATE, 1);
        int year = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int month = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        if (startCalendar.get(Calendar.DATE) == 1 && temp.get(Calendar.DATE) == 1) {
            return year * 12 + month + 1;
        } else if (startCalendar.get(Calendar.DATE) != 1 && temp.get(Calendar.DATE) == 1) {
            return year * 12 + month;
        } else if (startCalendar.get(Calendar.DATE) == 1 && temp.get(Calendar.DATE) != 1) {
            return year * 12 + month;
        } else {
            return year * 12 + month - 1 < 0 ? 0 : year * 12 + month;
        }
    }

    public static int yearOfTwo(Date start, Date end) {
        return Math.abs(getYearPart(start) - getYearPart(end));
    }

    public static int getYearPart(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.get(Calendar.YEAR);
    }

    public static int getMonthPart(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.get(Calendar.MONTH) + 1;
    }

    public static int getDayPart(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.get(Calendar.DATE);
    }

    public static int getHourPart(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinutePart(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.get(Calendar.MINUTE);
    }

    public static int getSecondPart(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.get(Calendar.SECOND);
    }

    public static int getMilliSecondPart(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.get(Calendar.MILLISECOND);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate toLocalDate(Date date) {
        return toLocalDateTime(date).toLocalDate();
    }

    public static LocalTime toLocalTime(Date date) {
        return toLocalDateTime(date).toLocalTime();
    }

    public static Date fromLocalDateTime(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date fromLocalDate(LocalDate date) {
        return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date fromLocalTime(LocalDate date, LocalTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return fromLocalDateTime(dateTime);
    }

    public static String getStrYear(String dateStr) {
        if (dateStr.length() < 4) {
            throw new RuntimeException("日期长度错误");
        } else {
            return dateStr.substring(0, 4);
        }
    }

    public static String getStrMonth(String dateStr) {
        if (dateStr.length() < 7) {
            throw new RuntimeException("日期长度错误");
        } else {
            return dateStr.substring(0, 7);
        }
    }

    public static int getStrYearPart(String yearStr) {
        return Integer.parseInt(getStrYear(yearStr));
    }

    public static int getStrMonthPart(String monthStr) {
        return Integer.parseInt(monthStr.substring(5, 7));
    }

    public static int getStrDayPart(String datStr) {
        return Integer.parseInt(datStr.substring(8, 10));
    }

    public static ArrayList<String> getForwardExpandListByMonth(Date lastDate, int expansionMonthNumber) {
        Date startDate = dateAddMonth(lastDate, -expansionMonthNumber + 1);
        return getExpandListByMonth(startDate, expansionMonthNumber);
    }

    public static ArrayList<String> getExpandListByMonth(Date startDate, int expandBackwardNumber) {
        if (expandBackwardNumber >= 1 && expandBackwardNumber <= 12) {
            ArrayList<String> dates = new ArrayList<>();

            for (int i = 0; i < expandBackwardNumber; ++i) {
                dates.add(DateFormatUtils.format(startDate, "yyyy-MM"));
                startDate = dateAddMonth(startDate, 1);
            }

            return dates;
        } else {
            throw new RuntimeException("月展开范围超出索引");
        }
    }

    public static int getDaysOfYear(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    public static int getDaysOfYear(int year) {
        Calendar ca = Calendar.getInstance();
        ca.set(year, Calendar.JANUARY, 1);
        return ca.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    public static int getDaysOfMonth(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        return ca.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

}
