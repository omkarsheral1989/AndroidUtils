package com.sheral.omkar.addcontacts;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by omkars on 06/05/17.
 */

public class IoUtil {
  public static BufferedReader getReader(Context context, String assetFileName) throws IOException {
    InputStream inputStream = context.getAssets().open(assetFileName);
    return new BufferedReader(new InputStreamReader(inputStream));
  }

  public static ArrayList<String> getLines(Context context, String assetFileName) throws IOException {
    BufferedReader reader = getReader(context, assetFileName);
    ArrayList<String> lines = new ArrayList<String>();
    while (true) {
      String line = reader.readLine();
      if (null == line) {
        break;
      }
      lines.add(line);
    }
    return lines;
  }
}
