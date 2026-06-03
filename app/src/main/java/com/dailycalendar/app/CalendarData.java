package com.dailycalendar.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class CalendarData {
    static final String TYPE_EVENT = "生日";
    static final String TYPE_PRODUCE = "应季";
    static final String TYPE_SOLAR_TERM = "节气";
    static final String TYPE_HOLIDAY = "节假日";
    static final String TYPE_WORKDAY = "调休";

    private static final int BASE_YEAR = 1900;
    private static final int[] LUNAR_INFO = {
            0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
            0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
            0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
            0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
            0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,
            0x06ca0,0x0b550,0x15355,0x04da0,0x0a5d0,0x14573,0x052d0,0x0a9a8,0x0e950,0x06aa0,
            0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,
            0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b6a0,0x195a6,
            0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,
            0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,
            0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,
            0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,
            0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,
            0x05aa0,0x076a3,0x096d0,0x04bd7,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,
            0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0,
            0x14b63,0x09370,0x049f8,0x04970,0x064b0,0x168a6,0x0ea50,0x06aa0,0x1a6c4,0x0aae0,
            0x092e0,0x0d2e3,0x0c960,0x0d557,0x0d4a0,0x0da50,0x05d55,0x056a0,0x0a6d0,0x055d4,
            0x052d0,0x0a9b8,0x0a950,0x0b4a0,0x0b6a6,0x0ad50,0x055a0,0x0aba4,0x0a5b0,0x052b0,
            0x0b273,0x06930,0x07337,0x06aa0,0x0ad50,0x14b55,0x04b60,0x0a570,0x054e4,0x0d160,
            0x0e968,0x0d520,0x0daa0,0x16aa6,0x056d0,0x04ae0,0x0a9d4,0x0a2d0,0x0d150,0x0f252,
            0x0d520
    };

    private static final SimpleDateFormat KEY_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    static LunarDate toLunar(Calendar solar) {
        Calendar base = Calendar.getInstance();
        base.clear();
        base.set(1900, Calendar.JANUARY, 31);
        Calendar date = (Calendar) solar.clone();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        int offset = (int) ((date.getTimeInMillis() - base.getTimeInMillis()) / 86400000L);

        int lunarYear;
        int daysOfYear = 0;
        for (lunarYear = BASE_YEAR; lunarYear < BASE_YEAR + LUNAR_INFO.length && offset > 0; lunarYear++) {
            daysOfYear = yearDays(lunarYear);
            offset -= daysOfYear;
        }
        if (offset < 0) {
            offset += daysOfYear;
            lunarYear--;
        }

        int leapMonth = leapMonth(lunarYear);
        boolean leap = false;
        int lunarMonth;
        int daysOfMonth = 0;
        for (lunarMonth = 1; lunarMonth <= 12 && offset > 0; lunarMonth++) {
            if (leapMonth > 0 && lunarMonth == leapMonth + 1 && !leap) {
                lunarMonth--;
                leap = true;
                daysOfMonth = leapDays(lunarYear);
            } else {
                daysOfMonth = monthDays(lunarYear, lunarMonth);
            }
            offset -= daysOfMonth;
            if (leap && lunarMonth == leapMonth + 1) leap = false;
        }
        if (offset == 0 && leapMonth > 0 && lunarMonth == leapMonth + 1) {
            if (leap) {
                leap = false;
            } else {
                leap = true;
                lunarMonth--;
            }
        }
        if (offset < 0) {
            offset += daysOfMonth;
            lunarMonth--;
        }
        return new LunarDate(lunarYear, lunarMonth, offset + 1, leap);
    }

    static List<DayNote> notesFor(Calendar date, List<UserEvent> events) {
        ArrayList<DayNote> notes = new ArrayList<>();
        LunarDate lunar = toLunar(date);
        int solarMonth = date.get(Calendar.MONTH) + 1;
        int solarDay = date.get(Calendar.DAY_OF_MONTH);

        for (UserEvent event : events) {
            if (event.matches(solarMonth, solarDay, lunar)) {
                notes.add(new DayNote(TYPE_EVENT, event.display()));
            }
        }
        String lunarFestival = lunarFestival(lunar);
        if (!lunarFestival.isEmpty()) notes.add(new DayNote(TYPE_HOLIDAY, lunarFestival));

        String produce = produceFor(lunar.month);
        if (!produce.isEmpty()) notes.add(new DayNote(TYPE_PRODUCE, produce));

        String solarTerm = solarTermFor(key(date));
        if (!solarTerm.isEmpty()) notes.add(new DayNote(TYPE_SOLAR_TERM, solarTerm));

        String holiday = holidayFor(key(date));
        if (!holiday.isEmpty()) notes.add(new DayNote(TYPE_HOLIDAY, holiday));

        String workday = workdayFor(key(date));
        if (!workday.isEmpty()) notes.add(new DayNote(TYPE_WORKDAY, workday));
        return notes;
    }

    static String key(Calendar date) {
        return KEY_FORMAT.format(date.getTime());
    }

    static String produceFor(int lunarMonth) {
        switch (lunarMonth) {
            case 1: return "应季：菠菜、韭菜、草莓、柑橘";
            case 2: return "应季：春笋、荠菜、豌豆苗、菠萝";
            case 3: return "应季：香椿、芦笋、青团菜、枇杷";
            case 4: return "应季：黄瓜、番茄、樱桃、桑葚";
            case 5: return "应季：茄子、豆角、西瓜、杨梅";
            case 6: return "应季：丝瓜、苦瓜、桃子、葡萄";
            case 7: return "应季：莲藕、冬瓜、梨、无花果";
            case 8: return "应季：南瓜、毛豆、石榴、柿子";
            case 9: return "应季：山药、萝卜、苹果、猕猴桃";
            case 10: return "应季：白菜、芥蓝、橙子、柚子";
            case 11: return "应季：菜心、花菜、甘蔗、冬枣";
            case 12: return "应季：白萝卜、菠菜、砂糖橘、苹果";
            default: return "";
        }
    }

    private static String lunarFestival(LunarDate lunar) {
        if (lunar.leap) return "";
        if (lunar.month == 1 && lunar.day == 1) return "春节";
        if (lunar.month == 1 && lunar.day == 15) return "元宵节";
        if (lunar.month == 5 && lunar.day == 5) return "端午节";
        if (lunar.month == 7 && lunar.day == 7) return "七夕";
        if (lunar.month == 8 && lunar.day == 15) return "中秋节";
        if (lunar.month == 9 && lunar.day == 9) return "重阳节";
        if (lunar.month == 12 && lunar.day == 8) return "腊八节";
        return "";
    }

    private static String solarTermFor(String key) {
        Map<String, String> map = new HashMap<>();
        map.put("2026-01-05", "小寒"); map.put("2026-01-20", "大寒");
        map.put("2026-02-04", "立春"); map.put("2026-02-18", "雨水");
        map.put("2026-03-05", "惊蛰"); map.put("2026-03-20", "春分");
        map.put("2026-04-05", "清明"); map.put("2026-04-20", "谷雨");
        map.put("2026-05-05", "立夏"); map.put("2026-05-21", "小满");
        map.put("2026-06-05", "芒种"); map.put("2026-06-21", "夏至");
        map.put("2026-07-07", "小暑"); map.put("2026-07-23", "大暑");
        map.put("2026-08-07", "立秋"); map.put("2026-08-23", "处暑");
        map.put("2026-09-07", "白露"); map.put("2026-09-23", "秋分");
        map.put("2026-10-08", "寒露"); map.put("2026-10-23", "霜降");
        map.put("2026-11-07", "立冬"); map.put("2026-11-22", "小雪");
        map.put("2026-12-07", "大雪"); map.put("2026-12-21", "冬至");
        return map.containsKey(key) ? map.get(key) : "";
    }

    private static String holidayFor(String key) {
        Map<String, String> map = new HashMap<>();
        putRange(map, "2026-01-01", "2026-01-03", "元旦休假");
        putRange(map, "2026-02-15", "2026-02-23", "春节休假");
        putRange(map, "2026-04-04", "2026-04-06", "清明休假");
        putRange(map, "2026-05-01", "2026-05-05", "劳动节休假");
        putRange(map, "2026-06-19", "2026-06-21", "端午休假");
        putRange(map, "2026-09-25", "2026-09-27", "中秋休假");
        putRange(map, "2026-10-01", "2026-10-07", "国庆休假");
        return map.containsKey(key) ? map.get(key) : "";
    }

    private static String workdayFor(String key) {
        Map<String, String> map = new HashMap<>();
        map.put("2026-02-14", "春节调休上班");
        map.put("2026-02-28", "春节调休上班");
        map.put("2026-05-09", "劳动节调休上班");
        map.put("2026-09-20", "中秋调休上班");
        map.put("2026-10-10", "国庆调休上班");
        return map.containsKey(key) ? map.get(key) : "";
    }

    private static void putRange(Map<String, String> map, String start, String end, String value) {
        try {
            Calendar current = Calendar.getInstance();
            current.setTime(KEY_FORMAT.parse(start));
            Calendar last = Calendar.getInstance();
            last.setTime(KEY_FORMAT.parse(end));
            while (!current.after(last)) {
                map.put(key(current), value);
                current.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception ignored) {
        }
    }

    private static int yearDays(int year) {
        int sum = 348;
        int info = LUNAR_INFO[year - BASE_YEAR];
        for (int mask = 0x8000; mask > 0x8; mask >>= 1) if ((info & mask) != 0) sum++;
        return sum + leapDays(year);
    }

    private static int leapDays(int year) {
        return leapMonth(year) == 0 ? 0 : ((LUNAR_INFO[year - BASE_YEAR] & 0x10000) != 0 ? 30 : 29);
    }

    private static int leapMonth(int year) {
        return LUNAR_INFO[year - BASE_YEAR] & 0xf;
    }

    private static int monthDays(int year, int month) {
        return (LUNAR_INFO[year - BASE_YEAR] & (0x10000 >> month)) == 0 ? 29 : 30;
    }
}

class LunarDate {
    final int year;
    final int month;
    final int day;
    final boolean leap;

    LunarDate(int year, int month, int day, boolean leap) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.leap = leap;
    }

    String text() {
        if (day == 1) return (leap ? "闰" : "") + monthName(month);
        return dayName(day);
    }

    String fullText() {
        return year + "年" + (leap ? "闰" : "") + monthName(month) + dayName(day);
    }

    static String monthName(int month) {
        String[] names = {"", "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月"};
        return month >= 1 && month < names.length ? names[month] : month + "月";
    }

    static String dayName(int day) {
        String[] tens = {"初", "十", "廿", "三"};
        String[] ones = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        if (day == 10) return "初十";
        if (day == 20) return "二十";
        if (day == 30) return "三十";
        return tens[day / 10] + ones[day % 10];
    }
}

class DayNote {
    final String type;
    final String text;

    DayNote(String type, String text) {
        this.type = type;
        this.text = text;
    }
}
