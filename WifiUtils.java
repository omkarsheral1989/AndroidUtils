package com.sheral.omkar.twguestwifipoc.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.RequiresPermission;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.content.Context.WIFI_SERVICE;

public class WifiUtils {
  private WifiUtils() {
  }

  /**
   * Make sure wifi is on
   */
  public static List<WifiConfiguration> getConfiguredNetworks(Context context) {
    WifiManager wifiManager = getWifiManager(context);
    List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
    if (configuredNetworks == null) {
      configuredNetworks = Collections.emptyList();
    }
    return configuredNetworks;
  }

  /**
   * Make sure wifi is on
   */
  public static Optional<WifiConfiguration> getConfiguredNetwork(Context context, String networkSSID) {
    networkSSID = "\"" + networkSSID + "\"";
    List<WifiConfiguration> configuredNetworks = getConfiguredNetworks(context);
    for (WifiConfiguration configuredNetwork : configuredNetworks) {
      if (configuredNetwork.SSID.equals(networkSSID)) {
        return Optional.of(configuredNetwork);
      }
    }
    return Optional.absent();
  }

  /**
   * Make sure wifi is on
   *
   * @return true on success or wifi wasn't configured
   */
  public static boolean forgetConfiguredNetwork(Context context, String networkSSID) {
    if (!isWifiEnabled(context)) {
      return false;
    }
    Optional<WifiConfiguration> configuredNetworkOptional = getConfiguredNetwork(context, networkSSID);
    if (configuredNetworkOptional.isPresent()) {
      WifiConfiguration wifiConfiguration = configuredNetworkOptional.get();
      WifiManager wifiManager = getWifiManager(context);
      return wifiManager.removeNetwork(wifiConfiguration.networkId);
    }
    return true;
  }

  /**
   * Make sure wifi is on. otherwise will fail
   */
  public static boolean configureAndConnectToWifiNetwork(Context context,
                                                         String networkSSID,
                                                         String password) {
    Optional<WifiConfiguration> configuredWifiOpt = configureWifiNetwork(context, networkSSID, password);
    if (!configuredWifiOpt.isPresent()) {
      return false;
    }
    return connectToConfiguredWifiNetwork(context, configuredWifiOpt.get());
  }

  /**
   * Make sure wifi is on.
   *
   * @return absent on failure
   */
  @RequiresPermission(CHANGE_WIFI_STATE)
  public static Optional<WifiConfiguration> configureWifiNetwork(Context context,
                                                                 String networkSSID,
                                                                 String password) {
    WifiConfiguration conf = new WifiConfiguration();
    conf.SSID = "\"" + networkSSID + "\"";   // note the quotes. String should contain SSID in quotes
    conf.preSharedKey = "\"" + password + "\"";

    WifiManager wifiManager = getWifiManager(context);
    int res = wifiManager.addNetwork(conf);

    final int FAILURE = -1;
    if (res == FAILURE) {
      return Optional.absent();
    }

    conf.networkId = res;

    return Optional.of(conf);
  }

  /**
   * Make sure wifi is on.
   */
  public static boolean connectToConfiguredWifiNetwork(Context context,
                                                       WifiConfiguration configuredWifiNetwork) {
    WifiManager wifiManager = getWifiManager(context);
    boolean res = wifiManager.disconnect();
    if (!res) {
      return false;
    }
    res = wifiManager.enableNetwork(configuredWifiNetwork.networkId, true);
    if (!res) {
      return false;
    }
    res = wifiManager.reconnect();
    return res;
  }

  public static boolean isWifiEnabled(Context context) {
    WifiManager wifiManager = getWifiManager(context);
    return wifiManager.isWifiEnabled();
  }

  public static void setWifiState(Context context, boolean enabled) {
    WifiManager wifiManager = getWifiManager(context);
    wifiManager.setWifiEnabled(enabled);
  }

  public interface WifiScanResultListener {
    void onWifiScanResult(List<ScanResult> scanResults);
  }

  @RequiresPermission(allOf = {ACCESS_COARSE_LOCATION, ACCESS_WIFI_STATE})
  public static void getAvailableWifiNetworks(Context context,
                                              final WifiScanResultListener wifiScanResultListener) {
    context = context.getApplicationContext();
    final WifiManager wifiManager = getWifiManager(context);

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

  public static WifiManager getWifiManager(Context context) {
    return Utils.getSystemService(context, WIFI_SERVICE);
  }
}
