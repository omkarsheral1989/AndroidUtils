package com.sheral.omkar.twguestwifipoc.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtils {
  public static void putString(Context context, String key, String value) {
    getSharedPreferences(context).edit()
        .putString(key, value)
        .apply();
  }

  public static String getString(Context context, String key, String defaulVal) {
    return getSharedPreferences(context)
        .getString(key, defaulVal);
  }

  public static void putInt(Context context, String key, int value) {
    getSharedPreferences(context).edit()
        .putInt(key, value)
        .apply();
  }

  public static int getInt(Context context, String key, int defVal) {
    return getSharedPreferences(context)
        .getInt(key, defVal);
  }

  public static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences("default", Context.MODE_PRIVATE);
  }
}
