package com.dailycalendar.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

class FruitStore {
    private static final String PREF = "daily_calendar_fruits";
    private static final String KEY = "fruits";

    private final SharedPreferences preferences;

    FruitStore(Context context) {
        preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    ArrayList<FruitItem> load() {
        String raw = preferences.getString(KEY, "");
        if (raw == null || raw.isEmpty()) {
            ArrayList<FruitItem> defaults = defaults();
            save(defaults);
            return defaults;
        }

        ArrayList<FruitItem> fruits = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                fruits.add(FruitItem.fromJson(array.getJSONObject(i)));
            }
        } catch (Exception ignored) {
        }
        return fruits;
    }

    void save(List<FruitItem> fruits) {
        JSONArray array = new JSONArray();
        for (FruitItem fruit : fruits) {
            try {
                array.put(fruit.toJson());
            } catch (Exception ignored) {
            }
        }
        preferences.edit().putString(KEY, array.toString()).apply();
    }

    private ArrayList<FruitItem> defaults() {
        ArrayList<FruitItem> fruits = new ArrayList<>();
        fruits.add(new FruitItem("奉化水蜜桃", 7, 1, 8, 20, true));
        fruits.add(new FruitItem("阳山水蜜桃", 6, 20, 8, 15, true));
        fruits.add(new FruitItem("猫山王榴莲", 6, 1, 9, 30, true));
        fruits.add(new FruitItem("金枕榴莲", 4, 15, 8, 31, true));
        fruits.add(new FruitItem("妃子笑荔枝", 5, 20, 6, 25, true));
        fruits.add(new FruitItem("桂味荔枝", 6, 15, 7, 10, true));
        fruits.add(new FruitItem("白沙枇杷", 4, 20, 5, 25, true));
        fruits.add(new FruitItem("大五星枇杷", 5, 1, 6, 10, true));
        return fruits;
    }
}
