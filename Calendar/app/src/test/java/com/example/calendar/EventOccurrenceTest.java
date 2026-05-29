package com.example.calendar;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Event.occursOn() — the recurrence computation logic.
 *
 * Recurrence is calculated on-the-fly using modular arithmetic rather than
 * pre-expanding into individual records, so correctness here is critical.
 */
public class EventOccurrenceTest
{
    private static final LocalDate BASE = LocalDate.of(2024, 1, 1);

    private Event makeEvent(LocalDate date, RecurrenceType type, int interval)
    {
        return new Event(
            "Test", date,
            LocalTime.of(9, 0), LocalTime.of(10, 0),
            0xFF0000FF, type, interval, -1,
            EventType.CLASS, 0, 0
        );
    }

    // --- Non-recurring ---

    @Test
    public void nonRecurring_matchesExactDate()
    {
        Event event = makeEvent(BASE, RecurrenceType.NONE, 1);
        assertTrue(event.occursOn(BASE));
    }

    @Test
    public void nonRecurring_noMatchOnDifferentDate()
    {
        Event event = makeEvent(BASE, RecurrenceType.NONE, 1);
        assertFalse(event.occursOn(BASE.plusDays(1)));
    }

    @Test
    public void nonRecurring_noMatchBeforeStartDate()
    {
        Event event = makeEvent(BASE, RecurrenceType.NONE, 1);
        assertFalse(event.occursOn(BASE.minusDays(1)));
    }

    // --- Daily ---

    @Test
    public void daily_matchesConsecutiveDays()
    {
        Event event = makeEvent(BASE, RecurrenceType.DAILY, 1);
        assertTrue(event.occursOn(BASE.plusDays(1)));
        assertTrue(event.occursOn(BASE.plusDays(30)));
    }

    @Test
    public void daily_everyTwoDays_matchesOnInterval()
    {
        Event event = makeEvent(BASE, RecurrenceType.DAILY, 2);
        assertTrue(event.occursOn(BASE));
        assertTrue(event.occursOn(BASE.plusDays(2)));
        assertTrue(event.occursOn(BASE.plusDays(4)));
    }

    @Test
    public void daily_everyTwoDays_noMatchBetweenInterval()
    {
        Event event = makeEvent(BASE, RecurrenceType.DAILY, 2);
        assertFalse(event.occursOn(BASE.plusDays(1)));
        assertFalse(event.occursOn(BASE.plusDays(3)));
    }

    // --- Weekly ---

    @Test
    public void weekly_matchesSameDayNextWeek()
    {
        Event event = makeEvent(BASE, RecurrenceType.WEEKLY, 1);
        assertTrue(event.occursOn(BASE.plusWeeks(1)));
        assertTrue(event.occursOn(BASE.plusWeeks(4)));
    }

    @Test
    public void weekly_noMatchMidWeek()
    {
        Event event = makeEvent(BASE, RecurrenceType.WEEKLY, 1);
        assertFalse(event.occursOn(BASE.plusDays(3)));
    }

    @Test
    public void weekly_everyTwoWeeks_matchesOnInterval()
    {
        Event event = makeEvent(BASE, RecurrenceType.WEEKLY, 2);
        assertTrue(event.occursOn(BASE.plusWeeks(2)));
        assertFalse(event.occursOn(BASE.plusWeeks(1)));
    }

    // --- Monthly ---

    @Test
    public void monthly_matchesSameDayEachMonth()
    {
        Event event = makeEvent(BASE, RecurrenceType.MONTHLY, 1);
        assertTrue(event.occursOn(BASE.plusMonths(1)));
        assertTrue(event.occursOn(BASE.plusMonths(6)));
    }

    @Test
    public void monthly_noMatchOnDifferentDayOfMonth()
    {
        Event event = makeEvent(BASE, RecurrenceType.MONTHLY, 1);
        assertFalse(event.occursOn(BASE.plusMonths(1).plusDays(1)));
    }

    // --- Yearly ---

    @Test
    public void yearly_matchesSameDateNextYear()
    {
        Event event = makeEvent(BASE, RecurrenceType.YEARLY, 1);
        assertTrue(event.occursOn(BASE.plusYears(1)));
        assertTrue(event.occursOn(BASE.plusYears(3)));
    }

    @Test
    public void yearly_noMatchOnDifferentMonth()
    {
        Event event = makeEvent(BASE, RecurrenceType.YEARLY, 1);
        assertFalse(event.occursOn(BASE.plusYears(1).plusMonths(1)));
    }
}
