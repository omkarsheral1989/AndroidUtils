package com.sheral.omkar.twguestwifipoc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.content.Context.WIFI_SERVICE;

public class WifiUtils {
  private WifiUtils() {
  }

  public static List<WifiConfiguration> getConfiguredNetworks(Context context) {
    WifiManager wifiManager = Utils.getSystemService(context, WIFI_SERVICE);
    List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
    for (WifiConfiguration configuredNetwork : configuredNetworks) {
    }
    return configuredNetworks;
  }

  @Nullable
  public static WifiConfiguration getConfiguredNetwork(Context context, String networkSSID) {
    networkSSID = "\"" + networkSSID + "\"";
    List<WifiConfiguration> configuredNetworks = getConfiguredNetworks(context);
    for (WifiConfiguration configuredNetwork : configuredNetworks) {
      if (configuredNetwork.SSID.equals(networkSSID)) {
        return configuredNetwork;
      }
    }
    return null;
  }

  public static void configureAndConnectToWifiNetwork(Context context,
                                                      String networkSSID,
                                                      String password) {
    WifiConfiguration configuredWifi = configureWifiNetwork(context, networkSSID, password);
    connectToConfiguredWifiNetwork(context, configuredWifi);
  }

  @RequiresPermission(CHANGE_WIFI_STATE)
  @NonNull
  public static WifiConfiguration configureWifiNetwork(Context context,
                                                       String networkSSID,
                                                       String password) {
    WifiConfiguration conf = new WifiConfiguration();
    conf.SSID = "\"" + networkSSID + "\"";   // note the quotes. String should contain SSID in quotes
    conf.preSharedKey = "\"" + password + "\"";

    WifiManager wifiManager = Utils.getSystemService(context, WIFI_SERVICE);
    wifiManager.addNetwork(conf);

    return conf;
  }

  public static void connectToConfiguredWifiNetwork(Context context,
                                                    WifiConfiguration configuredWifiNetwork) {
    WifiManager wifiManager = Utils.getSystemService(context, WIFI_SERVICE);
    wifiManager.disconnect();
    wifiManager.enableNetwork(configuredWifiNetwork.networkId, true);
    wifiManager.reconnect();
  }

  public interface WifiScanResultListener {
    void onWifiScanResult(List<ScanResult> scanResults);
  }

  @RequiresPermission(allOf = {ACCESS_COARSE_LOCATION, ACCESS_WIFI_STATE})
  public static void getAvailableWifiNetworks(Context context,
                                              final WifiScanResultListener wifiScanResultListener) {
    context = context.getApplicationContext();
    final WifiManager wifiManager = Utils.getSystemService(context, WIFI_SERVICE);

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        context.getApplicationContext().unregisterReceiver(this);
        List<ScanResult> scanResults = wifiManager.getScanResults();
        wifiScanResultListener.onWifiScanResult(scanResults);
      }
    };
    context.registerReceiver(
        wifiScanReceiver,
        new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    boolean scanningStarted = wifiManager.startScan();

    if (!scanningStarted) {
      context.unregisterReceiver(wifiScanReceiver);
      wifiScanResultListener.onWifiScanResult(Collections.<ScanResult>emptyList());
    }
  }
}
