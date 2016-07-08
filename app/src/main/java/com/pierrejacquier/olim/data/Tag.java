package com.pierrejacquier.olim.data;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Tag {
    private String name;
    private String comments;
    private String color;
    private String icon;

    private String key;

    public Tag(Cursor cursor) {
    }

    public Tag() {
    }

    public String getName() {
        return name;
    }

    public String getHashName() {
        return "#" + name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Tag withName(String name) {
        setName(name);
        return this;
    }

    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }

    public Tag withComments(String comments) {
        setComments(comments);
        return this;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Tag withColor(String color) {
        setColor(color);
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public String getIconicsName() {
        return "gmd-" + this.icon.replace("_", "-").replace(" ", "-").toLowerCase();
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Tag withIcon(String icon) {
        setIcon(icon);
        return this;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, Object> getMap() {
        Map<String, Object> task = new HashMap<>();
        if (this.name != null) {
            task.put("name", this.name);
        }
        if (this.comments != null) {
            task.put("comments", this.comments);
        }
        if (icon != null) {
            task.put("icon", this.icon);
        }
        if (color != null) {
            task.put("color", this.color);
        }
        return task;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        result.append( this.getClass().getName() );
        result.append( " Object {" );
        result.append(newLine);
        Field[] fields = this.getClass().getDeclaredFields();
        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                result.append( field.get(this) );
            } catch ( IllegalAccessException ex ) {  }
            result.append(newLine);
        }
        result.append("}");
        return result.toString();
    }
}
