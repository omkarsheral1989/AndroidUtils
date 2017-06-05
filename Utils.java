package com.sheral.omkar.twguestwifipoc;

import android.content.Context;
import android.support.annotation.NonNull;

public class Utils {
  public static <T> T getSystemService(@NonNull Context context, @NonNull String serviceName) {
    //noinspection unchecked
    return (T) context.getSystemService(serviceName);
  }
}
