package com.example.calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the Event static list API introduced to replace the public
 * mutable eventsList field.  Each test gets a clean list via @Before/@After.
 */
public class EventListTest
{
    private static final LocalDate DATE = LocalDate.of(2024, 6, 1);

    @Before
    @After
    public void clearEvents()
    {
        Event.clear();
    }

    private Event makeEvent(String name)
    {
        return new Event(
            name, DATE,
            LocalTime.of(9, 0), LocalTime.of(10, 0),
            0xFF0000FF, RecurrenceType.NONE, 1, -1,
            EventType.CLASS, 0, 0
        );
    }

    @Test
    public void add_makesEventRetrievable()
    {
        Event event = makeEvent("Lecture");
        Event.add(event);
        assertSame(event, Event.get(0));
    }

    @Test
    public void add_multipleEvents_maintainsOrder()
    {
        Event first  = makeEvent("First");
        Event second = makeEvent("Second");
        Event.add(first);
        Event.add(second);
        assertSame(first,  Event.get(0));
        assertSame(second, Event.get(1));
    }

    @Test
    public void indexOf_returnsCorrectIndex()
    {
        Event a = makeEvent("A");
        Event b = makeEvent("B");
        Event.add(a);
        Event.add(b);
        assertEquals(0, Event.indexOf(a));
        assertEquals(1, Event.indexOf(b));
    }

    @Test
    public void indexOf_returnsMinusOne_whenAbsent()
    {
        Event notAdded = makeEvent("Ghost");
        assertEquals(-1, Event.indexOf(notAdded));
    }

    @Test
    public void set_replacesEventAtIndex()
    {
        Event original    = makeEvent("Original");
        Event replacement = makeEvent("Replacement");
        Event.add(original);
        Event.set(0, replacement);
        assertSame(replacement, Event.get(0));
        assertEquals(-1, Event.indexOf(original));
    }

    @Test
    public void remove_deletesEventAtIndex()
    {
        Event a = makeEvent("A");
        Event b = makeEvent("B");
        Event.add(a);
        Event.add(b);
        Event.remove(0);
        assertEquals(1, Event.getAll().size());
        assertSame(b, Event.get(0));
    }

    @Test
    public void clear_removesAllEvents()
    {
        Event.add(makeEvent("X"));
        Event.add(makeEvent("Y"));
        Event.clear();
        assertTrue(Event.getAll().isEmpty());
    }

    @Test
    public void getAll_returnsUnmodifiableView()
    {
        Event.add(makeEvent("Safe"));
        List<Event> view = Event.getAll();
        try
        {
            view.add(makeEvent("Intruder"));
            fail("Expected UnsupportedOperationException");
        }
        catch (UnsupportedOperationException expected) { }
    }

    @Test
    public void set_preservesId_whenEditingEvent()
    {
        Event original = makeEvent("Original");
        Event.add(original);
        int originalId = original.getId();

        // Simulate what EventEditActivity does on save: re-create with same ID
        Event edited = new Event(
            originalId, "Edited", DATE,
            LocalTime.of(10, 0), LocalTime.of(11, 0),
            0xFFFF0000, RecurrenceType.NONE, 1, -1,
            EventType.CLASS, 0, 0
        );
        Event.set(0, edited);

        assertEquals(originalId, Event.get(0).getId());
        assertEquals("Edited",   Event.get(0).getName());
    }
}
