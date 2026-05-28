package com.example.calendar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalTime;

public class event_edit extends AppCompatActivity
{
    public static final String EXTRA_EVENT_INDEX = "EVENT_INDEX";

    private EditText eventNameET;
    private TextView eventDateTV, eventStartTimeTV, eventEndTimeTV;
    private Button deleteEventBtn;
    private Spinner recurrenceSpinner;
    private LinearLayout intervalRow;
    private EditText intervalET;
    private TextView intervalUnitTV;
    private Spinner reminderSpinner;
    private RadioGroup eventTypeGroup;
    private LinearLayout projectSection;
    private SeekBar importanceSeekBar, difficultySeekBar;
    private TextView importanceLabel, difficultyLabel;

    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int eventIndex = -1;

    private static final String[] REMINDER_LABELS = {
        "No reminder", "At start time", "5 minutes before",
        "15 minutes before", "30 minutes before", "1 hour before", "1 day before"
    };
    private static final int[] REMINDER_MINUTES = { -1, 0, 5, 15, 30, 60, 1440 };

    private static final String[] RECURRENCE_LABELS = {
        "Does not repeat", "Day(s)", "Week(s)", "Month(s)", "Year(s)"
    };
    private static final String[] INTERVAL_UNITS = {
        "", "day(s)", "week(s)", "month(s)", "year(s)"
    };
    private static final RecurrenceType[] RECURRENCE_TYPES = {
        RecurrenceType.NONE, RecurrenceType.DAILY,
        RecurrenceType.WEEKLY, RecurrenceType.MONTHLY, RecurrenceType.YEARLY
    };

    private final int[] swatchIds = {
        R.id.swatchBlue, R.id.swatchRed, R.id.swatchGreen,
        R.id.swatchOrange, R.id.swatchPurple
    };
    private int[] swatchColors;
    private View[] swatches;
    private GradientDrawable[] swatchDrawables;
    private int selectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        initWidgets();
        setupColorSwatches();
        setupRecurrenceSpinner();
        setupReminderSpinner();
        setupEventTypeToggle();
        setupSeekBars();

        eventIndex = getIntent().getIntExtra(EXTRA_EVENT_INDEX, -1);

        if (eventIndex >= 0)
        {
            Event existing = Event.eventsList.get(eventIndex);
            eventDate = existing.getDate();
            startTime = existing.getStartTime();
            endTime = existing.getEndTime();
            eventNameET.setText(existing.getName());
            selectSwatchForColor(existing.getColor());
            preselectRecurrence(existing.getRecurrenceType(), existing.getRecurrenceInterval());
            preselectReminder(existing.getReminderMinutes());
            deleteEventBtn.setVisibility(View.VISIBLE);
            if (existing.getEventType() == EventType.PROJECT)
            {
                eventTypeGroup.check(R.id.radioProject);
                importanceSeekBar.setProgress(existing.getImportance() - 1);
                difficultySeekBar.setProgress(existing.getDifficulty() - 1);
            }
        }
        else
        {
            eventDate = CalendarUtils.selectedDate;
            startTime = LocalTime.now().withSecond(0).withNano(0);
            endTime = startTime.plusHours(1);
            selectSwatch(0);
        }

        updateDateLabel();
        updateTimeLabels();
        eventDateTV.setOnClickListener(v -> openDatePicker());
        eventStartTimeTV.setOnClickListener(v -> openStartTimePicker());
        eventEndTimeTV.setOnClickListener(v -> openEndTimePicker());
    }

    private void initWidgets()
    {
        eventNameET = findViewById(R.id.eventNameET);
        eventDateTV = findViewById(R.id.eventDateTV);
        eventStartTimeTV = findViewById(R.id.eventStartTimeTV);
        eventEndTimeTV = findViewById(R.id.eventEndTimeTV);
        deleteEventBtn = findViewById(R.id.deleteEventBtn);
        recurrenceSpinner = findViewById(R.id.recurrenceSpinner);
        intervalRow = findViewById(R.id.intervalRow);
        intervalET = findViewById(R.id.intervalET);
        intervalUnitTV = findViewById(R.id.intervalUnitTV);
        reminderSpinner = findViewById(R.id.reminderSpinner);
        eventTypeGroup = findViewById(R.id.eventTypeGroup);
        projectSection = findViewById(R.id.projectSection);
        importanceSeekBar = findViewById(R.id.importanceSeekBar);
        difficultySeekBar = findViewById(R.id.difficultySeekBar);
        importanceLabel = findViewById(R.id.importanceLabel);
        difficultyLabel = findViewById(R.id.difficultyLabel);
    }

    private void setupEventTypeToggle()
    {
        eventTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioProject)
                projectSection.setVisibility(View.VISIBLE);
            else
                projectSection.setVisibility(View.GONE);
        });
    }

    private void setupSeekBars()
    {
        importanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                importanceLabel.setText("Importance: " + (progress + 1));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        difficultySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                difficultyLabel.setText("Difficulty: " + (progress + 1));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupReminderSpinner()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, REMINDER_LABELS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reminderSpinner.setAdapter(adapter);
        reminderSpinner.setSelection(3); // default: 15 minutes before
    }

    private void preselectReminder(int minutes)
    {
        for (int i = 0; i < REMINDER_MINUTES.length; i++)
        {
            if (REMINDER_MINUTES[i] == minutes)
            {
                reminderSpinner.setSelection(i);
                return;
            }
        }
        reminderSpinner.setSelection(0); // fallback: no reminder
    }

    private void setupRecurrenceSpinner()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, RECURRENCE_LABELS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurrenceSpinner.setAdapter(adapter);

        recurrenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (position == 0)
                {
                    intervalRow.setVisibility(View.GONE);
                }
                else
                {
                    intervalRow.setVisibility(View.VISIBLE);
                    intervalUnitTV.setText(INTERVAL_UNITS[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void preselectRecurrence(RecurrenceType type, int interval)
    {
        for (int i = 0; i < RECURRENCE_TYPES.length; i++)
        {
            if (RECURRENCE_TYPES[i] == type)
            {
                recurrenceSpinner.setSelection(i);
                break;
            }
        }
        intervalET.setText(String.valueOf(interval));
    }

    private void setupColorSwatches()
    {
        swatchColors = new int[] {
            getColor(R.color.blue),
            getColor(R.color.eventRed),
            getColor(R.color.eventGreen),
            getColor(R.color.eventOrange),
            getColor(R.color.eventPurple)
        };

        swatches = new View[swatchIds.length];
        swatchDrawables = new GradientDrawable[swatchIds.length];

        for (int i = 0; i < swatchIds.length; i++)
        {
            swatches[i] = findViewById(swatchIds[i]);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(swatchColors[i]);
            swatches[i].setBackground(bg);
            swatchDrawables[i] = bg;

            final int index = i;
            swatches[i].setOnClickListener(v -> selectSwatch(index));
        }
    }

    private void selectSwatch(int index)
    {
        selectedColor = swatchColors[index];
        float density = getResources().getDisplayMetrics().density;
        int strokeWidth = Math.max(1, (int)(3 * density));

        for (int i = 0; i < swatchDrawables.length; i++)
        {
            if (i == index)
                swatchDrawables[i].setStroke(strokeWidth, 0xFFFFFFFF);
            else
                swatchDrawables[i].setStroke(0, 0x00000000);
        }
    }

    private void selectSwatchForColor(int color)
    {
        for (int i = 0; i < swatchColors.length; i++)
        {
            if (swatchColors[i] == color)
            {
                selectSwatch(i);
                return;
            }
        }
        selectedColor = color;
    }

    private void updateDateLabel()
    {
        eventDateTV.setText("Date: " + CalendarUtils.formattedDate(eventDate));
    }

    private void updateTimeLabels()
    {
        eventStartTimeTV.setText("Start: " + CalendarUtils.formattedShortTime(startTime));
        eventEndTimeTV.setText("End:     " + CalendarUtils.formattedShortTime(endTime));
    }

    private void openDatePicker()
    {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            eventDate = LocalDate.of(year, month + 1, dayOfMonth);
            updateDateLabel();
        }, eventDate.getYear(), eventDate.getMonthValue() - 1, eventDate.getDayOfMonth()).show();
    }

    private void openStartTimePicker()
    {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            startTime = LocalTime.of(hourOfDay, minute);
            if (!endTime.isAfter(startTime))
                endTime = startTime.plusHours(1);
            updateTimeLabels();
        }, startTime.getHour(), startTime.getMinute(), true).show();
    }

    private void openEndTimePicker()
    {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            endTime = LocalTime.of(hourOfDay, minute);
            updateTimeLabels();
        }, endTime.getHour(), endTime.getMinute(), true).show();
    }

    private RecurrenceType selectedRecurrenceType()
    {
        return RECURRENCE_TYPES[recurrenceSpinner.getSelectedItemPosition()];
    }

    private int selectedInterval()
    {
        try
        {
            int val = Integer.parseInt(intervalET.getText().toString().trim());
            return Math.max(1, val);
        }
        catch (NumberFormatException e)
        {
            return 1;
        }
    }

    public void saveEventAction(View view)
    {
        int reminderMinutes = REMINDER_MINUTES[reminderSpinner.getSelectedItemPosition()];
        boolean isProject = eventTypeGroup.getCheckedRadioButtonId() == R.id.radioProject;
        EventType eventType = isProject ? EventType.PROJECT : EventType.CLASS;
        int importance = isProject ? importanceSeekBar.getProgress() + 1 : 0;
        int difficulty = isProject ? difficultySeekBar.getProgress() + 1 : 0;
        Event event = new Event(
            eventNameET.getText().toString(),
            eventDate, startTime, endTime, selectedColor,
            selectedRecurrenceType(), selectedInterval(),
            reminderMinutes, eventType, importance, difficulty
        );
        if (eventIndex >= 0)
        {
            EventAlarmScheduler.cancel(this, Event.eventsList.get(eventIndex).getId());
            Event.eventsList.set(eventIndex, event);
        }
        else
        {
            Event.eventsList.add(event);
        }
        EventStorage.save(this);
        EventAlarmScheduler.schedule(this, event);
        finish();
    }

    public void deleteEventAction(View view)
    {
        if (eventIndex >= 0)
        {
            EventAlarmScheduler.cancel(this, Event.eventsList.get(eventIndex).getId());
            Event.eventsList.remove(eventIndex);
        }
        EventStorage.save(this);
        finish();
    }
}
