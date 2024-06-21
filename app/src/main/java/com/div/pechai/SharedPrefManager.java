package com.div.pechai;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

public class SharedPrefManager {
    private static final String DEFAULT_STRING_VALUE = "";
    private static final Boolean DEFAULT_BOOLEAN_VALUE = false;

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();

    }
    public int getInt(String key) {
        return sharedPreferences.getInt(key, 1);
    }

    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();

    }
    public void saveBool(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }
    public boolean getBool(String key) {

        return sharedPreferences.getBoolean(key, false);
    }
    public String getString(String key) {
        return sharedPreferences.getString(key, DEFAULT_STRING_VALUE);
    }
    public String getStringIgnoreCase(String key) {
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue().toString();
            }
        }

        // Return the default value if the key is not found
        return DEFAULT_STRING_VALUE;
    }
    public boolean getBoolIgnoreCase(String key) {
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                Object value = entry.getValue();
                if (value instanceof Boolean) {
                    return (Boolean) value;
                } else if (value instanceof String) {
                    // Handle the case where the value might be stored as a string representation of boolean
                    String stringValue = (String) value;
                    return Boolean.parseBoolean(stringValue);
                }
            }
        }

        // Return the default value if the key is not found or the value is not a boolean
        return DEFAULT_BOOLEAN_VALUE;
    }

}
