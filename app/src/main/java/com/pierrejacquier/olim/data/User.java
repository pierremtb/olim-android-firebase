package com.pierrejacquier.olim.data;

import com.pierrejacquier.olim.utils.Tools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class User {
    private String fullName;
    private String email;
    private List<Task> tasks;
    private List<Tag> tags;
    private String coverUrl;
    private String pictureUrl;

    public User() {
        User(null, null, new ArrayList<Task>(), new ArrayList<Tag>());
    }

    public User(String fullName, String email) {
        User(fullName, email, new ArrayList<Task>(), new ArrayList<Tag>());
    }

    public User(String fullName, String email, List<Task> tasks, List<Tag> tags) {
        User(fullName, email, tasks, tags);
    }

    private void User(String fullName, String email, List<Task> tasks, List<Tag> tags) {
        this.fullName = fullName;
        this.email = email;
        this.tasks = tasks;
        this.tags = tags;
        this.coverUrl = null;
        this.pictureUrl = null;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public List<Tag> getTags() {
        if (this.tags == null) {
            return new ArrayList<>();
        }
        return this.tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Task> getTasks() {
        return this.getTasks(null, false);
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getTasks(boolean excludeDone) {
        return this.getTasks(null, excludeDone);
    }

    public List<Task> getTasks(Tag tag) {
        return this.getTasks(tag, false);
    }

    public List<Task> getTasks(Tag tag, boolean excludeDone) {
        if (this.tasks == null) {
            return new ArrayList<>();
        }

        if (excludeDone) {
            List<Task> notDoneTasks = new ArrayList<>();
            for (Task task : this.tasks) {
                if (!task.isDone()) {
                    notDoneTasks.add(task);
                }
            }
            return notDoneTasks;
        }

        return this.tasks;
    }

    public List<Task> getOverdueTasks() {
        return getOverdueTasks(null);
    }

    public List<Task> getTodayTasks() {
        return getTodayTasks(null);
    }

    public List<Task> getTomorrowTasks() {
        return getTomorrowTasks(null);
    }

    public List<Task> getInTheNextSevenDaysTasks() {
        return getInTheNextSevenDaysTasks(null);
    }

    public List<Task> getLaterTasks() {
        return getLaterTasks(null);
    }

    public List<Task> getOverdueTasks(Tag tag) {
        return getOverdueTasks(tag, false);
    }

    public List<Task> getOverdueTasks(Tag tag, boolean excludeDone) {
        List<Task> tasks = this.getTasks(tag, excludeDone);
        List<Task> overdueTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();

        Calendar today = Calendar.getInstance();
        Tools.setStartOfDay(today);

        for (Task task : tasks) {
            dueDate.setTime(new Date(task.getDueDate()));

            if (today.after(dueDate)) {
                overdueTasks.add(task);
            }
        }

        return overdueTasks;
    }

    public List<Task> getTodayTasks(Tag tag) {
        return getTodayTasks(tag, false);
    }

    public List<Task> getTodayTasks(Tag tag, boolean excludeDone) {
        List<Task> tasks = this.getTasks(tag, excludeDone);
        List<Task> todayTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        Tools.setStartOfDay(today);

        for (Task task : tasks) {
            dueDate.setTime(new Date(task.getDueDate()));

            if (today.get(Calendar.DAY_OF_MONTH) == dueDate.get(Calendar.DAY_OF_MONTH)) {
                todayTasks.add(task);
            }
        }

        return todayTasks;
    }

    public List<Task> getTomorrowTasks(Tag tag) {
        return getTomorrowTasks(tag, false);
    }

    public List<Task> getTomorrowTasks(Tag tag, boolean excludeDone) {
        List<Task> tasks = this.getTasks(tag, excludeDone);
        List<Task> tomorrowTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();

        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        for (Task task : tasks) {
            dueDate.setTime(new Date(task.getDueDate()));

            if (tomorrow.get(Calendar.DAY_OF_MONTH) == dueDate.get(Calendar.DAY_OF_MONTH)) {
                tomorrowTasks.add(task);
            }
        }

        return tomorrowTasks;
    }

    public List<Task> getInTheNextSevenDaysTasks(Tag tag) {
        return getInTheNextSevenDaysTasks(tag, false);
    }

    public List<Task> getInTheNextSevenDaysTasks(Tag tag, boolean excludeDone) {
        List<Task> tasks = this.getTasks(tag, excludeDone);
        List<Task> inTheNextSevenDaysTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();
        Calendar inTheNextSevenDaysStart = Calendar.getInstance();
        Calendar inTheNextSevenDaysEnd = Calendar.getInstance();
        inTheNextSevenDaysStart.add(Calendar.DAY_OF_MONTH, 2);
        inTheNextSevenDaysEnd.add(Calendar.DAY_OF_MONTH, 7);
        Tools.setStartOfDay(inTheNextSevenDaysStart);
        Tools.setStartOfDay(inTheNextSevenDaysEnd);

        for (Task task : tasks) {
            dueDate.setTime(new Date(task.getDueDate()));

            if (inTheNextSevenDaysStart.before(dueDate) && inTheNextSevenDaysEnd.after(dueDate)) {
                inTheNextSevenDaysTasks.add(task);
            }
        }

        return inTheNextSevenDaysTasks;
    }

    public List<Task> getLaterTasks(Tag tag) {
        return getLaterTasks(tag, false);
    }

    public List<Task> getLaterTasks(Tag tag, boolean excludeDone) {
        List<Task> tasks = this.getTasks(tag, excludeDone);
        List<Task> laterTasks = new ArrayList<>();
        Calendar dueDate = Calendar.getInstance();
        Calendar inTheNextSevenDaysEnd = Calendar.getInstance();
        inTheNextSevenDaysEnd.add(Calendar.DAY_OF_MONTH, 7);
        Tools.setStartOfDay(inTheNextSevenDaysEnd);

        for (Task task : tasks) {
            dueDate.setTime(new Date(task.getDueDate()));

            if (inTheNextSevenDaysEnd.before(dueDate)) {
                laterTasks.add(task);
            }
        }

        return laterTasks;
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
