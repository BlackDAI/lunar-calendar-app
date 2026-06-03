package com.dailycalendar.app;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

class FruitItem {
    final String fruit;
    final String variety;
    final int startMonth;
    final int startDay;
    final int endMonth;
    final int endDay;
    final boolean favorite;

    FruitItem(String fruit, String variety, int startMonth, int startDay, int endMonth, int endDay, boolean favorite) {
        this.fruit = fruit;
        this.variety = variety;
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

    String title() {
        return fruit + " · " + variety;
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("fruit", fruit);
        object.put("variety", variety);
        object.put("startMonth", startMonth);
        object.put("startDay", startDay);
        object.put("endMonth", endMonth);
        object.put("endDay", endDay);
        object.put("favorite", favorite);
        return object;
    }

    static FruitItem fromJson(JSONObject object) {
        return new FruitItem(
                object.optString("fruit", "水果"),
                object.optString("variety", "默认品种"),
                object.optInt("startMonth", 1),
                object.optInt("startDay", 1),
                object.optInt("endMonth", 12),
                object.optInt("endDay", 31),
                object.optBoolean("favorite", true)
        );
    }
}
