package com.pierrejacquier.olim.utils;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

public final class Graphics {

    public static ShapeDrawable createRoundDrawable(String color) {
        ShapeDrawable colorDrawable = new ShapeDrawable(new OvalShape());
        colorDrawable.setIntrinsicWidth(50);
        colorDrawable.setIntrinsicHeight(50);
        colorDrawable.getPaint().setStyle(Paint.Style.FILL);
        colorDrawable.getPaint().setColor(Color.parseColor(color));
        return colorDrawable;
    }

    public static ShapeDrawable createRoundDrawable(int color) {
        return createRoundDrawable(intColorToHex(color));
    }

    public static String intColorToHex(int color) {
        return "#" + Integer.toHexString(color).toUpperCase();
    }

    public static String intColorToHex6(int color) {
        return "#" + Integer.toHexString(color).toUpperCase().substring(2,7);
    }

    public static int lighten(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = lightenColor(red, fraction);
        green = lightenColor(green, fraction);
        blue = lightenColor(blue, fraction);
        int alpha = Color.alpha(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static int darken(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = darkenColor(red, fraction);
        green = darkenColor(green, fraction);
        blue = darkenColor(blue, fraction);
        int alpha = Color.alpha(color);

        return Color.argb(alpha, red, green, blue);
    }

    private static int darkenColor(int color, double fraction) {
        return (int)Math.max(color - (color * fraction), 0);
    }

    private static int lightenColor(int color, double fraction) {
        return (int) Math.min(color + (color * fraction), 255);
    }
}