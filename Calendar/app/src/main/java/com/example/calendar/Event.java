package com.example.calendar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Event
{
    public static ArrayList<Event> eventsList = new ArrayList<>();

    public static ArrayList<Event> eventsForDate(LocalDate date)
    {
        ArrayList<Event> events = new ArrayList<>();
        for (Event event : eventsList)
        {
            if (event.occursOn(date))
                events.add(event);
        }
        return events;
    }

    public static ArrayList<Event> eventsForDateAndTime(LocalDate date, LocalTime time)
    {
        ArrayList<Event> events = new ArrayList<>();
        for (Event event : eventsList)
        {
            if (event.occursOn(date) && event.startTime.getHour() == time.getHour())
                events.add(event);
        }
        return events;
    }

    public static Event findById(int id)
    {
        for (Event event : eventsList)
        {
            if (event.id == id) return event;
        }
        return null;
    }


    private int id;
    private String name;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int color;
    private RecurrenceType recurrenceType;
    private int recurrenceInterval;
    private int reminderMinutes; // -1 = no reminder, 0 = at start, else minutes before

    // Constructor for new events — auto-generates ID
    public Event(String name, LocalDate date, LocalTime startTime, LocalTime endTime,
                 int color, RecurrenceType recurrenceType, int recurrenceInterval,
                 int reminderMinutes)
    {
        this.id = (int)(System.currentTimeMillis() / 1000);
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.recurrenceType = recurrenceType;
        this.recurrenceInterval = recurrenceInterval;
        this.reminderMinutes = reminderMinutes;
    }

    // Constructor for loading from storage — explicit ID
    public Event(int id, String name, LocalDate date, LocalTime startTime, LocalTime endTime,
                 int color, RecurrenceType recurrenceType, int recurrenceInterval,
                 int reminderMinutes)
    {
        this.id = id;
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.recurrenceType = recurrenceType;
        this.recurrenceInterval = recurrenceInterval;
        this.reminderMinutes = reminderMinutes;
    }

    public boolean occursOn(LocalDate queryDate)
    {
        if (queryDate.isBefore(date)) return false;

        switch (recurrenceType)
        {
            case NONE:
                return date.equals(queryDate);
            case DAILY: {
                long days = ChronoUnit.DAYS.between(date, queryDate);
                return days % recurrenceInterval == 0;
            }
            case WEEKLY: {
                long days = ChronoUnit.DAYS.between(date, queryDate);
                return days % (7L * recurrenceInterval) == 0;
            }
            case MONTHLY: {
                if (queryDate.getDayOfMonth() != date.getDayOfMonth()) return false;
                long months = ChronoUnit.MONTHS.between(date, queryDate);
                return months % recurrenceInterval == 0;
            }
            case YEARLY: {
                if (queryDate.getMonth() != date.getMonth() ||
                    queryDate.getDayOfMonth() != date.getDayOfMonth()) return false;
                long years = ChronoUnit.YEARS.between(date, queryDate);
                return years % recurrenceInterval == 0;
            }
        }
        return false;
    }

    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }

    public int getRecurrenceInterval() { return recurrenceInterval; }
    public void setRecurrenceInterval(int recurrenceInterval) { this.recurrenceInterval = recurrenceInterval; }

    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }
}
