package com.dailycalendar.app;

import org.json.JSONException;
import org.json.JSONObject;

class UserEvent {
    static final String SOLAR = "SOLAR";
    static final String LUNAR = "LUNAR";

    final String name;
    final String type;
    final int month;
    final int day;

    UserEvent(String name, String type, int month, int day) {
        this.name = name;
        this.type = type;
        this.month = month;
        this.day = day;
    }

    boolean matches(int solarMonth, int solarDay, LunarDate lunar) {
        if (SOLAR.equals(type)) return month == solarMonth && day == solarDay;
        return lunar != null && !lunar.leap && month == lunar.month && day == lunar.day;
    }

    String display() {
        return name + "（" + (LUNAR.equals(type) ? "农历" : "公历") + month + "月" + day + "日）";
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("type", type);
        object.put("month", month);
        object.put("day", day);
        return object;
    }

    static UserEvent fromJson(JSONObject object) {
        return new UserEvent(
                object.optString("name", "生日提醒"),
                object.optString("type", SOLAR),
                object.optInt("month", 1),
                object.optInt("day", 1)
        );
    }
}
