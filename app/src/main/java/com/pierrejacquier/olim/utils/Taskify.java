package com.pierrejacquier.olim.utils;

import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;

public class Taskify extends TaskifyBackend {

    private class TagTaskify extends TaskifyBackend {
        @Override
        public String getRegex() {
            return "(#[a-zA-Z0-9_-]{1,20})";
        }

        @Override
        public Task getResult(Matcher m, Task task, List<Tag> tags) {
            while (m.find()) {
                String tagName = m.group();
                task.setTagKey(null);
                task.setTag(null);
                for (Tag tag : tags) {
                    if (tag.getHashName().toUpperCase().equals(tagName.toUpperCase())) {
                        task.setTagKey(tag.getKey());
                        task.setTag(tag);
                        task.setTitle(task.getTitle().replace(tagName, "").trim());
                    }
                }
            }
            return task;
        }
    }

    private class TodayOrTomorrowOrAfterTomorrowDayTaskify extends TaskifyBackend {
        @Override
        public String getRegex() {
            return "(tomorrow|the day after tomorrow)";
        }

        @Override
        public Task getResult(Matcher m, Task task, List<Tag> tags) {
            if (m.find()) {
                String tagName = m.group();
                Calendar dueDate = Calendar.getInstance();
                switch (tagName) {
                    case "tomorrow":
                        dueDate.add(Calendar.DAY_OF_MONTH, 1);
                        break;
                    case "the day after tomorrow":
                        dueDate.add(Calendar.DAY_OF_MONTH, 2);
                        break;
                }
                task.setDueDate(dueDate.getTimeInMillis());
                String newTitle = task.getTitle();
                newTitle = newTitle.replace("tomorrow", "");
                newTitle = newTitle.replace("the day after tomorrow", "");
                task.setTitle(newTitle);
            } else {
                task.setDueDate(Calendar.getInstance().getTimeInMillis());
            }
            return task;
        }
    }

    @Override
    public Task run(String taskText, Task task, List<Tag> tags) {
        task = new TagTaskify().run(taskText, task, tags);
        task = new TodayOrTomorrowOrAfterTomorrowDayTaskify().run(taskText, task, tags);
        return task;
    }
}