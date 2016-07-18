package com.pierrejacquier.olim.utils;

import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskifyBackend {
    public TaskifyBackend() {
    }

    public String getRegex() {
        return "";
    }

    public Task getResult(Matcher m, Task task, List<Tag> tags) {
        return task;
    }

    public Task run(String taskText, Task task, List<Tag> tags) {
        Pattern p = Pattern.compile(getRegex());
        return getResult(p.matcher(taskText), task, tags);
    }
}
