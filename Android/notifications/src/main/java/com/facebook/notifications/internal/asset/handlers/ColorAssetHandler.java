// Copyright (c) 2016-present, Facebook, Inc. All rights reserved.
//
// You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
// copy, modify, and distribute this software in source code or binary form for use
// in connection with the web services and APIs provided by Facebook.
//
// As with any software that integrates with the Facebook platform, your use of
// this software is subject to the Facebook Developer Principles and Policies
// [http://developers.facebook.com/policy/]. This copyright notice shall be
// included in all copies or substantial portions of the software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.facebook.notifications.internal.asset.handlers;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.notifications.internal.asset.Asset;
import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.utilities.InvalidParcelException;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Set;

/**
 * Handles assets of Color type.
 */
public class ColorAssetHandler implements AssetManager.AssetHandler<ColorAssetHandler.ColorAsset> {
  /**
   * An asset implementation for a single static color
   */
  static class ColorAsset implements Asset {
    public static final Creator<ColorAsset> CREATOR = new Creator<ColorAsset>() {
      @Override
      public ColorAsset createFromParcel(Parcel source) {
        return new ColorAsset(source);
      }

      @Override
      public ColorAsset[] newArray(int size) {
        return new ColorAsset[size];
      }
    };

    private final int color;

    private ColorAsset(int color) {
      this.color = color;
    }

    private ColorAsset(@NonNull Parcel parcel) {
      color = parcel.readInt();
    }

    public int getColor() {
      return color;
    }

    @NonNull
    @Override
    public String getType() {
      return TYPE;
    }

    @Override
    public void validate() throws InvalidParcelException {
      // Nothing to validate
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(color);
    }
  }

  @SuppressWarnings("ViewConstructor")
  private static class ColorView extends FrameLayout {
    public ColorView(Context context, int color) {
      super(context);
      setBackgroundColor(color);
    }
  }

  public static final String TYPE = "Color";
  private static final String LOG_TAG = ColorAssetHandler.class.getCanonicalName();

  /**
   * Parses a string in the format '#RRGGBBAA', and returns it as an android-compatible color
   *
   * @param input The string to parse
   * @return The color, in an android-compatible format
   */
  public static int fromRGBAHex(@Nullable String input) {
    if (input == null || input.equals("")) {
      return 0;
    }
    input = input.substring(1);

    try {
      long value = Long.parseLong(input, 16);

      // Android color has alpha first, not last. Flip the bytes around.
      return (int) ((value >> 8) | ((value & 0xFF) << 24));
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  @Nullable
  @Override
  public Set<URL> getCacheURLs(@NonNull JSONObject payload) {
    return null;
  }

  @Nullable
  @Override
  public ColorAsset createAsset(@NonNull JSONObject payload, @NonNull AssetManager.AssetCache cache) {
    try {
      return new ColorAsset(fromRGBAHex(payload.getString("rgbaHex")));
    } catch (JSONException ex) {
      Log.e(LOG_TAG, "JSON Exception", ex);
      return null;
    }
  }

  @NonNull
  @Override
  public View createView(@NonNull ColorAsset asset, @NonNull Context context) {
    return new ColorView(context, asset.getColor());
  }
}
