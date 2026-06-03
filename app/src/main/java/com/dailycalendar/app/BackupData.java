package com.dailycalendar.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class BackupData {
    static String exportJson(List<UserEvent> events, List<FruitItem> fruits) throws Exception {
        JSONObject root = new JSONObject();
        root.put("version", 1);

        JSONArray eventArray = new JSONArray();
        for (UserEvent event : events) eventArray.put(event.toJson());
        root.put("events", eventArray);

        JSONArray fruitArray = new JSONArray();
        for (FruitItem fruit : fruits) fruitArray.put(fruit.toJson());
        root.put("fruits", fruitArray);
        return root.toString(2);
    }

    static Result importJson(String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        ArrayList<UserEvent> events = new ArrayList<>();
        ArrayList<FruitItem> fruits = new ArrayList<>();

        JSONArray eventArray = root.optJSONArray("events");
        if (eventArray != null) {
            for (int i = 0; i < eventArray.length(); i++) {
                events.add(UserEvent.fromJson(eventArray.getJSONObject(i)));
            }
        }

        JSONArray fruitArray = root.optJSONArray("fruits");
        if (fruitArray != null) {
            for (int i = 0; i < fruitArray.length(); i++) {
                fruits.add(FruitItem.fromJson(fruitArray.getJSONObject(i)));
            }
        }
        return new Result(events, fruits);
    }

    static class Result {
        final ArrayList<UserEvent> events;
        final ArrayList<FruitItem> fruits;

        Result(ArrayList<UserEvent> events, ArrayList<FruitItem> fruits) {
            this.events = events;
            this.fruits = fruits;
        }
    }
}
