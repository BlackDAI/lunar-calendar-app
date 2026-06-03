package com.dailycalendar.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

class EventStore {
    private static final String PREF = "daily_calendar_events";
    private static final String KEY = "events";

    private final SharedPreferences preferences;

    EventStore(Context context) {
        preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    ArrayList<UserEvent> load() {
        ArrayList<UserEvent> events = new ArrayList<>();
        String raw = preferences.getString(KEY, "");
        if (raw == null || raw.isEmpty()) return events;

        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                events.add(UserEvent.fromJson(array.getJSONObject(i)));
            }
        } catch (Exception ignored) {
        }
        return events;
    }

    void save(List<UserEvent> events) {
        JSONArray array = new JSONArray();
        for (UserEvent event : events) {
            try {
                array.put(event.toJson());
            } catch (Exception ignored) {
            }
        }
        preferences.edit().putString(KEY, array.toString()).apply();
    }
}
