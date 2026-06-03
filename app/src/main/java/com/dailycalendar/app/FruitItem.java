package com.dailycalendar.app;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

class FruitItem {
    final String name;
    final int startMonth;
    final int startDay;
    final int endMonth;
    final int endDay;
    final boolean favorite;

    FruitItem(String name, int startMonth, int startDay, int endMonth, int endDay, boolean favorite) {
        this.name = name;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.favorite = favorite;
    }

    boolean isInSeason(Calendar date) {
        int current = (date.get(Calendar.MONTH) + 1) * 100 + date.get(Calendar.DAY_OF_MONTH);
        int start = startMonth * 100 + startDay;
        int end = endMonth * 100 + endDay;
        if (start <= end) return current >= start && current <= end;
        return current >= start || current <= end;
    }

    String seasonText() {
        return startMonth + "月" + startDay + "日 - " + endMonth + "月" + endDay + "日";
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("startMonth", startMonth);
        object.put("startDay", startDay);
        object.put("endMonth", endMonth);
        object.put("endDay", endDay);
        object.put("favorite", favorite);
        return object;
    }

    static FruitItem fromJson(JSONObject object) {
        String name = object.optString("name", "");
        if (name.isEmpty()) {
            String variety = object.optString("variety", "");
            String fruit = object.optString("fruit", "水果");
            name = variety.isEmpty() ? fruit : variety;
        }
        return new FruitItem(
                name,
                object.optInt("startMonth", 1),
                object.optInt("startDay", 1),
                object.optInt("endMonth", 12),
                object.optInt("endDay", 31),
                object.optBoolean("favorite", true)
        );
    }
}
