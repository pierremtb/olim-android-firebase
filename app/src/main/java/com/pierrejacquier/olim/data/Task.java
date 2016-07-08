package com.pierrejacquier.olim.data;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Task {
    private String title;
    private String tagUid;
    private long dueDate;
    private boolean done;
    private String tagKey;

    private Tag tag;
    private String key;

    public Task() {
    }

    public Task(Cursor cursor) {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTagUid() {
        return tagUid;
    }

    public void setTagUid(String tagUid) {
        this.tagUid = tagUid;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String dispDueDate() {
        return DateFormat.getDateInstance(DateFormat.SHORT).format(new Date(dueDate));
    }


    public String dispDueTime() {
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(dueDate));
    }

    public Map<String, Object> getObject() {
        Map<String, Object> task = new HashMap<>();
        if (this.title != null) {
            task.put("title", this.title);
        }
        if (this.tagUid != null) {
            task.put("tag", this.tag);
        }
        task.put("dueDate", this.dueDate);
        task.put("done", this.done);
        return task;
    }

    public void postponeToTheNextDay() {
        Calendar dueDate = Calendar.getInstance();
        dueDate.setTimeInMillis(this.dueDate);
        dueDate.add(Calendar.DAY_OF_MONTH, 1);
        this.dueDate = dueDate.getTimeInMillis();
    }

    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public Map<String, Object> getMap() {
        Map<String, Object> task = new HashMap<>();
        if (this.title != null) {
            task.put("title", this.title);
        }
        if (this.tagKey != null) {
            task.put("tagKey", this.tagKey);
        }
        if (dueDate != 0) {
            task.put("dueDate", this.dueDate);
        }
        task.put("done", this.done);
        return task;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        result.append(this.getClass().getName());
        result.append(" Object {");
        result.append(newLine);
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                result.append(field.get(this));
            } catch (IllegalAccessException ex) {
            }
            result.append(newLine);
        }
        result.append("}");
        return result.toString();
    }
}
