package com.example.calendar;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;

public class EventStorage
{
    private static final String FILE_NAME = "events.json";

    public static void save(Context context)
    {
        try
        {
            JSONArray array = new JSONArray();
            for (Event event : Event.eventsList)
            {
                JSONObject obj = new JSONObject();
                obj.put("id", event.getId());
                obj.put("name", event.getName());
                obj.put("date", event.getDate().toString());
                obj.put("startTime", event.getStartTime().toString());
                obj.put("endTime", event.getEndTime().toString());
                obj.put("color", event.getColor());
                obj.put("recurrenceType", event.getRecurrenceType().name());
                obj.put("recurrenceInterval", event.getRecurrenceInterval());
                obj.put("reminderMinutes", event.getReminderMinutes());
                obj.put("eventType", event.getEventType().name());
                obj.put("importance", event.getImportance());
                obj.put("difficulty", event.getDifficulty());
                array.put(obj);
            }

            File file = new File(context.getFilesDir(), FILE_NAME);
            try (FileWriter writer = new FileWriter(file))
            {
                writer.write(array.toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void load(Context context)
    {
        try
        {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) return;

            StringBuilder sb = new StringBuilder();
            try (FileReader reader = new FileReader(file))
            {
                char[] buf = new char[4096];
                int n;
                while ((n = reader.read(buf)) != -1)
                    sb.append(buf, 0, n);
            }

            JSONArray array = new JSONArray(sb.toString());
            Event.eventsList.clear();

            for (int i = 0; i < array.length(); i++)
            {
                JSONObject obj = array.getJSONObject(i);
                int id = obj.optInt("id", i + 1);
                RecurrenceType recurrenceType = RecurrenceType.valueOf(
                    obj.optString("recurrenceType", "NONE")
                );
                EventType eventType = EventType.valueOf(
                    obj.optString("eventType", "CLASS")
                );
                Event.eventsList.add(new Event(
                    id,
                    obj.getString("name"),
                    LocalDate.parse(obj.getString("date")),
                    LocalTime.parse(obj.getString("startTime")),
                    LocalTime.parse(obj.getString("endTime")),
                    obj.getInt("color"),
                    recurrenceType,
                    obj.optInt("recurrenceInterval", 1),
                    obj.optInt("reminderMinutes", 15),
                    eventType,
                    obj.optInt("importance", 5),
                    obj.optInt("difficulty", 5)
                ));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
