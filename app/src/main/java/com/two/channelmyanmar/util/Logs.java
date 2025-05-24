package com.two.channelmyanmar.util;

/*
 * Created by Toewaioo on 4/1/25
 * Description: [Add class description here]
 */
import android.util.Log;

public class Logs {
    // Toggle this flag to enable or disable logging app-wide
    private static boolean isLoggingEnabled = true;

    // Default tag if none is provided
    private static final String DEFAULT_TAG = "MyCinema";

    // Prevent instantiation
    private Logs() {}

    /**
     * Enable or disable logging globally.
     * @param enabled true to enable, false to disable
     */
    public static void setLoggingEnabled(boolean enabled) {
        isLoggingEnabled = enabled;
    }

    // Debug log with default tag
    public static void d(String msg) {
        d(DEFAULT_TAG, msg);
    }

    // Debug log with custom tag
    public static void d(String tag, String msg) {
        if (isLoggingEnabled) {
            Log.d(tag, msg);
        }
    }

    // Error log with default tag
    public static void e(String msg) {
        e(DEFAULT_TAG, msg);
    }

    // Error log with custom tag
    public static void e(String tag, String msg) {
        if (isLoggingEnabled) {
            Log.e(tag, msg);
        }
    }

    // Info log with default tag
    public static void i(String msg) {
        i(DEFAULT_TAG, msg);
    }

    // Info log with custom tag
    public static void i(String tag, String msg) {
        if (isLoggingEnabled) {
            Log.i(tag, msg);
        }
    }

    // Warning log with default tag
    public static void w(String msg) {
        w(DEFAULT_TAG, msg);
    }

    // Warning log with custom tag
    public static void w(String tag, String msg) {
        if (isLoggingEnabled) {
            Log.w(tag, msg);
        }
    }

    // Verbose log with default tag
    public static void v(String msg) {
        v(DEFAULT_TAG, msg);
    }

    // Verbose log with custom tag
    public static void v(String tag, String msg) {
        if (isLoggingEnabled) {
            Log.v(tag, msg);
        }
    }
}

