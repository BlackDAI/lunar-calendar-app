package com.dailycalendar.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends android.app.Activity {
    private static final int REQ_EXPORT_BACKUP = 1001;
    private static final int REQ_IMPORT_BACKUP = 1002;
    private static final String FRUIT_MARK = "\uD83C\uDF51";

    private final Calendar visibleMonth = Calendar.getInstance();
    private final Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat titleFormat = new SimpleDateFormat("yyyy年M月", Locale.CHINA);
    private final SimpleDateFormat solarDateFormat = new SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINA);

    private EventStore eventStore;
    private FruitStore fruitStore;
    private ArrayList<UserEvent> events;
    private ArrayList<FruitItem> fruits;
    private TextView monthTitle;
    private GridLayout calendarGrid;
    private LinearLayout detailList;
    private float downX;
    private float downY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventStore = new EventStore(this);
        fruitStore = new FruitStore(this);
        events = eventStore.load();
        fruits = fruitStore.load();
        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
        buildUi();
        render();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(246, 247, 249));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(14), dp(14), dp(14), dp(8));

        Button previous = smallButton("<");
        previous.setOnClickListener(v -> moveMonth(-1));

        monthTitle = new TextView(this);
        monthTitle.setTextSize(22);
        monthTitle.setTypeface(Typeface.DEFAULT_BOLD);
        monthTitle.setTextColor(Color.rgb(24, 35, 44));
        monthTitle.setGravity(Gravity.CENTER);
        top.addView(previous, new LinearLayout.LayoutParams(dp(46), dp(42)));
        top.addView(monthTitle, new LinearLayout.LayoutParams(0, dp(42), 1));

        Button next = smallButton(">");
        next.setOnClickListener(v -> moveMonth(1));
        top.addView(next, new LinearLayout.LayoutParams(dp(46), dp(42)));
        root.addView(top);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setPadding(dp(10), 0, dp(10), dp(8));
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        Button today = actionButton("今天");
        today.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            selectedDate.setTime(now.getTime());
            visibleMonth.setTime(now.getTime());
            visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
            render();
        });
        Button addEvent = actionButton("+日程");
        addEvent.setOnClickListener(v -> showAddEventDialog());
        Button addFruit = actionButton("水果");
        addFruit.setOnClickListener(v -> showFruitManagerDialog());
        Button backup = actionButton("备份");
        backup.setOnClickListener(v -> exportBackup());
        Button restore = actionButton("导入");
        restore.setOnClickListener(v -> importBackup());
        toolbar.addView(today, new LinearLayout.LayoutParams(0, dp(42), 1));
        toolbar.addView(addEvent, new LinearLayout.LayoutParams(0, dp(42), 1));
        toolbar.addView(addFruit, new LinearLayout.LayoutParams(0, dp(42), 1));
        toolbar.addView(backup, new LinearLayout.LayoutParams(0, dp(42), 1));
        toolbar.addView(restore, new LinearLayout.LayoutParams(0, dp(42), 1));
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
        calendarGrid.setOnTouchListener(this::handleSwipe);
        root.addView(calendarGrid);

        ScrollView detailScroll = new ScrollView(this);
        detailList = new LinearLayout(this);
        detailList.setOrientation(LinearLayout.VERTICAL);
        detailList.setPadding(dp(14), dp(10), dp(14), dp(20));
        detailScroll.addView(detailList);
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
            calendarGrid.addView(dayCell((Calendar) cellDate.clone()), new ViewGroupParams(cellWidth, dp(84)));
            cellDate.add(Calendar.DAY_OF_MONTH, 1);
        }
        renderDetails();
    }

    private View dayCell(Calendar date) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(5), dp(3), dp(5), dp(3));
        box.setBackgroundColor(cellBackground(date));
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setOnTouchListener(this::handleSwipe);
        box.setOnClickListener(v -> {
            selectedDate.setTime(date.getTime());
            if (date.get(Calendar.MONTH) != visibleMonth.get(Calendar.MONTH)) {
                visibleMonth.setTime(date.getTime());
                visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
            }
            render();
        });

        LinearLayout topRow = new LinearLayout(this);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView day = new TextView(this);
        day.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        day.setTextSize(17);
        day.setTypeface(Typeface.DEFAULT_BOLD);
        day.setGravity(Gravity.CENTER);
        day.setTextColor(date.get(Calendar.MONTH) == visibleMonth.get(Calendar.MONTH) ? Color.rgb(25, 34, 42) : Color.rgb(156, 164, 172));
        TextView fruitMark = new TextView(this);
        fruitMark.setText(hasFruitInSeason(date) ? FRUIT_MARK : "");
        fruitMark.setTextSize(12);
        fruitMark.setGravity(Gravity.RIGHT);
        topRow.addView(day, new LinearLayout.LayoutParams(0, dp(22), 1));
        topRow.addView(fruitMark, new LinearLayout.LayoutParams(dp(18), dp(22)));
        box.addView(topRow, new LinearLayout.LayoutParams(-1, dp(22)));

        TextView lunar = new TextView(this);
        lunar.setText(CalendarData.toLunar(date).text());
        lunar.setTextSize(10);
        lunar.setGravity(Gravity.CENTER);
        lunar.setTextColor(Color.rgb(100, 112, 122));
        box.addView(lunar, new LinearLayout.LayoutParams(-1, dp(18)));

        int shown = 0;
        for (DayNote note : CalendarData.notesFor(date, events, fruits)) {
            if (shown >= 2) break;
            if (CalendarData.TYPE_PRODUCE.equals(note.type) || CalendarData.TYPE_FRUIT.equals(note.type)) continue;
            TextView chip = new TextView(this);
            chip.setText(shortChip(note));
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
        detailList.removeAllViews();
        LunarDate lunar = CalendarData.toLunar(selectedDate);

        LinearLayout hero = card();
        TextView dayNumber = new TextView(this);
        dayNumber.setText(String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH)));
        dayNumber.setTextSize(44);
        dayNumber.setTypeface(Typeface.DEFAULT_BOLD);
        dayNumber.setTextColor(Color.rgb(24, 35, 44));
        hero.addView(dayNumber);
        hero.addView(line("公历：" + solarDateFormat.format(selectedDate.getTime()), Color.rgb(24, 35, 44), 16, true));
        hero.addView(line("农历：" + lunar.fullText(), Color.rgb(88, 101, 112), 15, false));
        detailList.addView(hero);

        addSection("当日历程", CalendarData.TYPE_EVENT, filterNotes(CalendarData.TYPE_EVENT), "今日没有已添加的生日或纪念日");
        addInfoSection("日期信息", new String[]{
                "公历日期：" + solarDateFormat.format(selectedDate.getTime()),
                "农历日期：" + lunar.fullText(),
                "农历月份：" + LunarDate.monthName(lunar.month),
                "农历日：" + LunarDate.dayName(lunar.day)
        });
        addSection("节气 / 节假日 / 调休", CalendarData.TYPE_SOLAR_TERM, calendarNotes(), "今日无节气、节假日或调休标记");
        addSection("应季果蔬", CalendarData.TYPE_PRODUCE, filterNotes(CalendarData.TYPE_PRODUCE), "今日暂无应季果蔬数据");
        addSection("关注水果上市情况", CalendarData.TYPE_FRUIT, filterNotes(CalendarData.TYPE_FRUIT), "今日没有关注水果上市");
    }

    private void addSection(String title, String colorType, List<String> rows, String emptyText) {
        LinearLayout section = card();
        section.addView(sectionTitle(title, colorFor(colorType)));
        if (rows.isEmpty()) {
            section.addView(line(emptyText, Color.rgb(118, 128, 138), 14, false));
        } else {
            for (String row : rows) section.addView(line(row, Color.rgb(34, 44, 52), 15, false));
        }
        detailList.addView(section);
    }

    private void addInfoSection(String title, String[] rows) {
        LinearLayout section = card();
        section.addView(sectionTitle(title, Color.rgb(35, 100, 170)));
        for (String row : rows) section.addView(line(row, Color.rgb(34, 44, 52), 15, false));
        detailList.addView(section);
    }

    private List<String> filterNotes(String type) {
        ArrayList<String> rows = new ArrayList<>();
        for (DayNote note : CalendarData.notesFor(selectedDate, events, fruits)) {
            if (type.equals(note.type)) rows.add(note.text);
        }
        return rows;
    }

    private List<String> calendarNotes() {
        ArrayList<String> rows = new ArrayList<>();
        for (DayNote note : CalendarData.notesFor(selectedDate, events, fruits)) {
            if (CalendarData.TYPE_SOLAR_TERM.equals(note.type)
                    || CalendarData.TYPE_HOLIDAY.equals(note.type)
                    || CalendarData.TYPE_WORKDAY.equals(note.type)) {
                rows.add(note.type + "：" + note.text);
            }
        }
        return rows;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);
        return card;
    }

    private TextView sectionTitle(String text, int color) {
        TextView view = line(text, color, 16, true);
        view.setPadding(0, 0, 0, dp(6));
        return view;
    }

    private TextView line(String text, int color, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, dp(3), 0, dp(3));
        if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    private void showAddEventDialog() {
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

        final int[] picked = {selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH)};
        TextView date = pickerLine("日期：" + picked[0] + "月" + picked[1] + "日");
        date.setOnClickListener(v -> showMonthDayPicker(date, picked, "日期："));
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
                    Toast.makeText(this, "已保存日程", Toast.LENGTH_SHORT).show();
                    render();
                }).show();
    }

    private void showAddFruitDialog() {
        showFruitEditorDialog(-1);
    }

    private void showFruitManagerDialog() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(10), dp(4), dp(10), 0);

        if (fruits.isEmpty()) {
            list.addView(line("还没有关注水果", Color.rgb(118, 128, 138), 15, false));
        } else {
            for (int i = 0; i < fruits.size(); i++) {
                FruitItem fruit = fruits.get(i);
                final int index = i;
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, dp(6), 0, dp(6));

                TextView info = new TextView(this);
                info.setText(fruit.name + "\n" + fruit.seasonText());
                info.setTextSize(15);
                info.setTextColor(Color.rgb(34, 44, 52));
                row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

                Button edit = actionButton("改");
                edit.setOnClickListener(v -> showFruitEditorDialog(index));
                Button delete = actionButton("删");
                delete.setTextColor(Color.rgb(191, 67, 91));
                delete.setOnClickListener(v -> confirmDeleteFruit(index));
                row.addView(edit, new LinearLayout.LayoutParams(dp(48), dp(38)));
                row.addView(delete, new LinearLayout.LayoutParams(dp(48), dp(38)));
                list.addView(row);
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("关注水果")
                .setView(list)
                .setNegativeButton("关闭", null)
                .setPositiveButton("新增", (dialog, which) -> showAddFruitDialog())
                .show();
    }

    private void confirmDeleteFruit(int index) {
        if (index < 0 || index >= fruits.size()) return;
        FruitItem fruit = fruits.get(index);
        new AlertDialog.Builder(this)
                .setTitle("删除关注水果")
                .setMessage("确定删除“" + fruit.name + "”吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    fruits.remove(index);
                    fruitStore.save(fruits);
                    Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                    render();
                    showFruitManagerDialog();
                })
                .show();
    }

    private void showFruitEditorDialog(int editIndex) {
        FruitItem editing = editIndex >= 0 && editIndex < fruits.size() ? fruits.get(editIndex) : null;
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), dp(8), dp(18), 0);

        EditText fruitName = new EditText(this);
        fruitName.setHint("水果或品种名称，例如：阳山水蜜桃");
        if (editing != null) fruitName.setText(editing.name);
        form.addView(fruitName);

        final int[] start = {
                editing == null ? 6 : editing.startMonth,
                editing == null ? 1 : editing.startDay
        };
        final int[] end = {
                editing == null ? 8 : editing.endMonth,
                editing == null ? 31 : editing.endDay
        };
        TextView startView = pickerLine("上市开始：" + start[0] + "月" + start[1] + "日");
        TextView endView = pickerLine("上市结束：" + end[0] + "月" + end[1] + "日");
        startView.setOnClickListener(v -> showMonthDayPicker(startView, start, "上市开始："));
        endView.setOnClickListener(v -> showMonthDayPicker(endView, end, "上市结束："));
        form.addView(startView);
        form.addView(endView);

        new AlertDialog.Builder(this)
                .setTitle(editing == null ? "新增关注水果" : "修改关注水果")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    String fruit = fruitName.getText().toString().trim();
                    if (fruit.isEmpty()) fruit = "水果";
                    FruitItem item = new FruitItem(fruit, start[0], start[1], end[0], end[1], true);
                    if (editIndex >= 0 && editIndex < fruits.size()) {
                        fruits.set(editIndex, item);
                    } else {
                        fruits.add(item);
                    }
                    fruitStore.save(fruits);
                    Toast.makeText(this, "已保存水果", Toast.LENGTH_SHORT).show();
                    render();
                }).show();
    }

    private TextView pickerLine(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(16);
        view.setPadding(0, dp(12), 0, dp(6));
        view.setTextColor(Color.rgb(35, 100, 170));
        return view;
    }

    private void showMonthDayPicker(TextView target, int[] picked, String prefix) {
        new DatePickerDialog(this, (picker, year, month, dayOfMonth) -> {
            picked[0] = month + 1;
            picked[1] = dayOfMonth;
            target.setText(prefix + picked[0] + "月" + picked[1] + "日");
        }, 2026, picked[0] - 1, picked[1]).show();
    }

    private void exportBackup() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "daily-calendar-backup.json");
        startActivityForResult(intent, REQ_EXPORT_BACKUP);
    }

    private void importBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQ_IMPORT_BACKUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            if (requestCode == REQ_EXPORT_BACKUP) {
                writeBackup(uri);
                Toast.makeText(this, "备份已导出", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQ_IMPORT_BACKUP) {
                readBackup(uri);
                Toast.makeText(this, "备份已导入", Toast.LENGTH_SHORT).show();
                render();
            }
        } catch (Exception exception) {
            Toast.makeText(this, "操作失败：" + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void writeBackup(Uri uri) throws Exception {
        OutputStream output = getContentResolver().openOutputStream(uri);
        if (output == null) throw new Exception("无法写入文件");
        output.write(BackupData.exportJson(events, fruits).getBytes("UTF-8"));
        output.close();
    }

    private void readBackup(Uri uri) throws Exception {
        InputStream input = getContentResolver().openInputStream(uri);
        if (input == null) throw new Exception("无法读取文件");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096];
        int read;
        while ((read = input.read(bytes)) != -1) buffer.write(bytes, 0, read);
        input.close();
        BackupData.Result result = BackupData.importJson(new String(buffer.toByteArray(), "UTF-8"));
        events = result.events;
        fruits = result.fruits;
        eventStore.save(events);
        fruitStore.save(fruits);
    }

    private boolean handleSwipe(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float deltaX = event.getX() - downX;
            float deltaY = event.getY() - downY;
            if (Math.abs(deltaX) > dp(70) && Math.abs(deltaX) > Math.abs(deltaY) * 1.4f) {
                moveMonth(deltaX < 0 ? 1 : -1);
                return true;
            }
        }
        return false;
    }

    private void moveMonth(int delta) {
        visibleMonth.add(Calendar.MONTH, delta);
        selectedDate.setTime(visibleMonth.getTime());
        selectedDate.set(Calendar.DAY_OF_MONTH, Math.min(selectedDate.get(Calendar.DAY_OF_MONTH), visibleMonth.getActualMaximum(Calendar.DAY_OF_MONTH)));
        render();
    }

    private boolean hasFruitInSeason(Calendar date) {
        for (FruitItem fruit : fruits) {
            if (fruit.favorite && fruit.isInSeason(date)) return true;
        }
        return false;
    }

    private String shortChip(DayNote note) {
        return note.text.replace("休假", "").replace("调休上班", "班");
    }

    private int cellBackground(Calendar date) {
        if (sameDay(date, selectedDate)) return Color.rgb(219, 235, 255);
        if (sameDay(date, Calendar.getInstance())) return Color.rgb(255, 244, 219);
        return Color.WHITE;
    }

    private boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private int colorFor(String type) {
        if (CalendarData.TYPE_EVENT.equals(type)) return Color.rgb(228, 87, 46);
        if (CalendarData.TYPE_PRODUCE.equals(type)) return Color.rgb(47, 133, 90);
        if (CalendarData.TYPE_FRUIT.equals(type)) return Color.rgb(36, 143, 93);
        if (CalendarData.TYPE_SOLAR_TERM.equals(type)) return Color.rgb(35, 100, 170);
        if (CalendarData.TYPE_WORKDAY.equals(type)) return Color.rgb(105, 82, 157);
        return Color.rgb(191, 67, 91);
    }

    private Button smallButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);
        button.setTextColor(Color.rgb(35, 100, 170));
        return button;
    }

    private Button actionButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(13);
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
