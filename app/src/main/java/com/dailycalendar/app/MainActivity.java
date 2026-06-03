package com.dailycalendar.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends android.app.Activity {
    private final Calendar visibleMonth = Calendar.getInstance();
    private final Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat titleFormat = new SimpleDateFormat("yyyy年M月", Locale.CHINA);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINA);

    private EventStore eventStore;
    private ArrayList<UserEvent> events;
    private TextView monthTitle;
    private GridLayout calendarGrid;
    private TextView detailTitle;
    private LinearLayout detailList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventStore = new EventStore(this);
        events = eventStore.load();
        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
        buildUi();
        render();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(247, 249, 250));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(14), dp(14), dp(14), dp(8));

        Button previous = smallButton("‹");
        previous.setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, -1);
            render();
        });

        monthTitle = new TextView(this);
        monthTitle.setTextSize(22);
        monthTitle.setTypeface(Typeface.DEFAULT_BOLD);
        monthTitle.setTextColor(Color.rgb(24, 35, 44));
        monthTitle.setGravity(Gravity.CENTER);
        top.addView(previous, new LinearLayout.LayoutParams(dp(46), dp(42)));
        top.addView(monthTitle, new LinearLayout.LayoutParams(0, dp(42), 1));

        Button next = smallButton("›");
        next.setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, 1);
            render();
        });
        top.addView(next, new LinearLayout.LayoutParams(dp(46), dp(42)));
        root.addView(top);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setPadding(dp(14), 0, dp(14), dp(10));
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        Button today = actionButton("今天");
        today.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            selectedDate.setTime(now.getTime());
            visibleMonth.setTime(now.getTime());
            visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
            render();
        });
        Button add = actionButton("+ 新增提醒");
        add.setOnClickListener(v -> showAddDialog());
        toolbar.addView(today, new LinearLayout.LayoutParams(0, dp(42), 1));
        toolbar.addView(add, new LinearLayout.LayoutParams(0, dp(42), 1));
        root.addView(toolbar);

        LinearLayout week = new LinearLayout(this);
        week.setPadding(dp(10), 0, dp(10), 0);
        String[] names = {"日", "一", "二", "三", "四", "五", "六"};
        for (String name : names) {
            TextView label = new TextView(this);
            label.setText(name);
            label.setGravity(Gravity.CENTER);
            label.setTextSize(13);
            label.setTextColor(Color.rgb(88, 101, 112));
            week.addView(label, new LinearLayout.LayoutParams(0, dp(28), 1));
        }
        root.addView(week);

        calendarGrid = new GridLayout(this);
        calendarGrid.setColumnCount(7);
        calendarGrid.setPadding(dp(8), 0, dp(8), dp(6));
        root.addView(calendarGrid);

        ScrollView detailScroll = new ScrollView(this);
        LinearLayout detail = new LinearLayout(this);
        detail.setOrientation(LinearLayout.VERTICAL);
        detail.setPadding(dp(16), dp(12), dp(16), dp(20));
        detailTitle = new TextView(this);
        detailTitle.setTextSize(18);
        detailTitle.setTypeface(Typeface.DEFAULT_BOLD);
        detailTitle.setTextColor(Color.rgb(24, 35, 44));
        detail.addView(detailTitle);
        detailList = new LinearLayout(this);
        detailList.setOrientation(LinearLayout.VERTICAL);
        detail.addView(detailList);
        detailScroll.addView(detail);
        root.addView(detailScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
    }

    private void render() {
        monthTitle.setText(titleFormat.format(visibleMonth.getTime()));
        calendarGrid.removeAllViews();
        Calendar first = (Calendar) visibleMonth.clone();
        int offset = first.get(Calendar.DAY_OF_WEEK) - 1;
        Calendar cellDate = (Calendar) first.clone();
        cellDate.add(Calendar.DAY_OF_MONTH, -offset);

        int screenWidth = getResources().getDisplayMetrics().widthPixels - dp(16);
        int cellWidth = screenWidth / 7;
        for (int i = 0; i < 42; i++) {
            calendarGrid.addView(dayCell((Calendar) cellDate.clone()), new ViewGroupParams(cellWidth, dp(82)));
            cellDate.add(Calendar.DAY_OF_MONTH, 1);
        }
        renderDetails();
    }

    private View dayCell(Calendar date) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(5), dp(4), dp(5), dp(3));
        box.setBackgroundColor(cellBackground(date));
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setOnClickListener(v -> {
            selectedDate.setTime(date.getTime());
            if (date.get(Calendar.MONTH) != visibleMonth.get(Calendar.MONTH)) {
                visibleMonth.setTime(date.getTime());
                visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
            }
            render();
        });

        TextView day = new TextView(this);
        day.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        day.setTextSize(17);
        day.setTypeface(Typeface.DEFAULT_BOLD);
        day.setGravity(Gravity.CENTER);
        day.setTextColor(date.get(Calendar.MONTH) == visibleMonth.get(Calendar.MONTH) ? Color.rgb(25, 34, 42) : Color.rgb(156, 164, 172));
        box.addView(day, new LinearLayout.LayoutParams(-1, dp(22)));

        TextView lunar = new TextView(this);
        lunar.setText(CalendarData.toLunar(date).text());
        lunar.setTextSize(10);
        lunar.setGravity(Gravity.CENTER);
        lunar.setTextColor(Color.rgb(100, 112, 122));
        box.addView(lunar, new LinearLayout.LayoutParams(-1, dp(18)));

        List<DayNote> notes = CalendarData.notesFor(date, events);
        int shown = 0;
        for (DayNote note : notes) {
            if (shown >= 2) break;
            if (CalendarData.TYPE_PRODUCE.equals(note.type)) continue;
            TextView chip = new TextView(this);
            chip.setText(note.text.replace("休假", "").replace("调休上班", "班"));
            chip.setTextSize(9);
            chip.setSingleLine(true);
            chip.setGravity(Gravity.CENTER);
            chip.setTextColor(Color.WHITE);
            chip.setBackgroundColor(colorFor(note.type));
            box.addView(chip, new LinearLayout.LayoutParams(-1, dp(15)));
            shown++;
        }
        return box;
    }

    private void renderDetails() {
        LunarDate lunar = CalendarData.toLunar(selectedDate);
        detailTitle.setText(dateFormat.format(selectedDate.getTime()) + "  ·  " + lunar.fullText());
        detailList.removeAllViews();
        List<DayNote> notes = CalendarData.notesFor(selectedDate, events);
        if (notes.isEmpty()) {
            detailList.addView(detailLine("今日暂无特别事项", Color.rgb(100, 112, 122)));
            return;
        }
        for (DayNote note : notes) {
            detailList.addView(detailLine(note.type + "： " + note.text, colorFor(note.type)));
        }
    }

    private TextView detailLine(String text, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(15);
        view.setTextColor(Color.rgb(32, 42, 50));
        view.setPadding(dp(12), dp(10), dp(12), dp(10));
        view.setBackgroundColor(tint(color));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(8), 0, 0);
        view.setLayoutParams(params);
        return view;
    }

    private void showAddDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), dp(8), dp(18), 0);

        EditText name = new EditText(this);
        name.setHint("提醒名称，例如：妈妈生日");
        form.addView(name);

        RadioGroup typeGroup = new RadioGroup(this);
        typeGroup.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton lunar = new RadioButton(this);
        lunar.setText("农历提醒");
        lunar.setId(1);
        RadioButton solar = new RadioButton(this);
        solar.setText("公历提醒");
        solar.setId(2);
        typeGroup.addView(lunar);
        typeGroup.addView(solar);
        typeGroup.check(1);
        form.addView(typeGroup);

        final int[] picked = {6, 12};
        TextView date = new TextView(this);
        date.setText("日期：6月12日");
        date.setTextSize(16);
        date.setPadding(0, dp(12), 0, dp(12));
        date.setOnClickListener(v -> new DatePickerDialog(this, (picker, year, month, dayOfMonth) -> {
            picked[0] = month + 1;
            picked[1] = dayOfMonth;
            date.setText("日期：" + picked[0] + "月" + picked[1] + "日");
        }, 2026, 5, 12).show());
        form.addView(date);

        new AlertDialog.Builder(this)
                .setTitle("新增生日/纪念日")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String text = name.getText().toString().trim();
                    if (text.isEmpty()) text = "生日提醒";
                    String type = typeGroup.getCheckedRadioButtonId() == 1 ? UserEvent.LUNAR : UserEvent.SOLAR;
                    events.add(new UserEvent(text, type, picked[0], picked[1]));
                    eventStore.save(events);
                    Toast.makeText(this, "已保存提醒", Toast.LENGTH_SHORT).show();
                    render();
                }).show();
    }

    private int cellBackground(Calendar date) {
        boolean selected = sameDay(date, selectedDate);
        boolean today = sameDay(date, Calendar.getInstance());
        if (selected) return Color.rgb(219, 235, 255);
        if (today) return Color.rgb(255, 244, 219);
        return Color.WHITE;
    }

    private boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private int colorFor(String type) {
        if (CalendarData.TYPE_EVENT.equals(type)) return Color.rgb(228, 87, 46);
        if (CalendarData.TYPE_PRODUCE.equals(type)) return Color.rgb(47, 133, 90);
        if (CalendarData.TYPE_SOLAR_TERM.equals(type)) return Color.rgb(35, 100, 170);
        if (CalendarData.TYPE_WORKDAY.equals(type)) return Color.rgb(105, 82, 157);
        return Color.rgb(191, 67, 91);
    }

    private int tint(int color) {
        return Color.rgb(
                235 + Color.red(color) / 12,
                235 + Color.green(color) / 12,
                235 + Color.blue(color) / 12
        );
    }

    private Button smallButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(22);
        button.setTextColor(Color.rgb(35, 100, 170));
        return button;
    }

    private Button actionButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setTextColor(Color.rgb(35, 100, 170));
        return button;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class ViewGroupParams extends GridLayout.LayoutParams {
        ViewGroupParams(int width, int height) {
            super();
            this.width = width;
            this.height = height;
            setMargins(1, 1, 1, 1);
        }
    }
}
