package com.pierrejacquier.olim.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public final class Tools {

    public static HashMap<String, Object> getMap(Object doc) {
        HashMap<String, Object> obj = new HashMap<>();
        obj = (HashMap<String, Object>) doc;
        return obj;
    }

    public static String dispDuration(long sec) {
        return String.format("%02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60);
    }

    public static String dispDuration(Object str) {
        int sec = 0;
        try {
            sec = Integer.parseInt(str.toString());
        } catch (Exception e) {
            return "";
        }
        return String.format("%02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60);
    }

    public static String dispDate(Object str) {
        HashMap<String, Long> t = (HashMap<String, Long>) str;
        long timestamp = 0;
        try {
            timestamp = t.get("$date");
        } catch (Exception e) {
        }
        Date date = new Date(timestamp);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public static String dispDate(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public static Date getDate(Object dateObj) {
        try {
            Long t = (Long) dateObj;
            return new Date(t);
        } catch (Exception e) {
        }
        HashMap<String, Long> t = (HashMap<String, Long>) dateObj;
        long timestamp = 0;
        try {
            timestamp = t.get("$date");
        } catch (Exception e) {
        }
        return new Date(timestamp);
    }

    public static String dispType(String t) {
        switch (t) {
            case "wk":
                return "Entrainement";
            case "rc":
                return "Compétition";
            case "nth":
                return "Repos";
            default:
                return "";
        }
    }

    public static String dispSupport(String s) {
        try {
            switch (s) {
                case "mtb":
                    return "VTT";
                case "road":
                    return "Route";
                case "run":
                    return "Course à pied";
                case "ht":
                    return "Home Trainer";
                case "swim":
                    return "Natation";
                case "skix":
                    return "Ski de fond";
                case "endr":
                    return "Enduro";
                case "othr":
                    return "Autre";
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static String getString(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? String.valueOf(obj.get(key)) : null;
    }

    public static int getInt(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? Integer.valueOf(obj.get(key).toString()) : 0;
    }

    public static float getFloat(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? Float.valueOf(obj.get(key).toString()) : 0;
    }

    public static long getLong(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? Long.valueOf(String.valueOf(obj.get(key).toString())) : 0;
    }

    public static Date getDate(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? getDate(obj.get(key)) : new Date();
    }

    public static boolean getBoolean(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) && Boolean.valueOf(obj.get(key).toString());
    }

    public static Object getObject(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? obj.get(key) : null;
    }

    public static HashMap<String, Object> getHashMap(HashMap<String, ?> obj, String key) {
        if (!obj.containsKey(key)) {
            return null;
        }
        try {
            return (HashMap<String, Object>) obj.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public static String[] getStringsArray(HashMap<String, ?> obj, String key) {
        String[] r = {};
        if (obj.containsKey(key)) {
            ArrayList<String> a = (ArrayList<String>) obj.get(key);
            return a.toArray(new String[a.size()]);
        } else {
            return r;
        }
    }

    public static Object getObject(HashMap<String, ?> obj, String key, boolean json) {
        if (obj.containsKey(key)) {
            return obj.get(key);
        } else if (json) {
            return new HashMap<String, Object>();
        } else {
            return new Object();
        }
    }

    public static void setStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static void setEndOfDay(Calendar calendar) {
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static long editDueDate(long dueDate, int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.setTime(new Date(dueDate));
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTimeInMillis();
    }

    public static long editDueDate(long dueDate, int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.setTime(new Date(dueDate));
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        return date.getTimeInMillis();
    }

    public static <T extends Enum<T>> T valueOfIgnoreCase(
            Class<T> enumeration, String name) {

        for (T enumValue : enumeration.getEnumConstants()) {
            if (enumValue.name().equalsIgnoreCase(name)) {
                return enumValue;
            }
        }

        throw new IllegalArgumentException(String.format(
                "There is no value with name '%s' in Enum %s",
                name, enumeration.getName()
        ));
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String toCamelCase(String s) {
        String[] parts = s.split("_");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return Character.toLowerCase(camelCaseString.charAt(0)) + camelCaseString.substring(1);
    }

    public static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }

    public static boolean arePermissionsRequired() {
        return Build.VERSION.SDK_INT >= 23;
    }
}