package com.sheral.omkar.twguestwifipoc.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;

public class Utils {
  private Utils() {
  }

  public static <T> T getSystemService(@NonNull Context context, @NonNull String serviceName) {
    //noinspection unchecked
    return (T) context.getSystemService(serviceName);
  }

  public static boolean hasPermission(Context context, String permission) {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
  }

  @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
  public static boolean isConnectedToInternet(Context context) {
    ConnectivityManager connectivityManager = getSystemService(context, Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    return activeNetwork != null && activeNetwork.isConnected();
  }
}
