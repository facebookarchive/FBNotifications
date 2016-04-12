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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.facebook.notifications.internal.asset.Asset;
import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.utilities.InvalidParcelException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles assets of the bitmap type
 */
public class BitmapAssetHandler implements AssetManager.AssetHandler<BitmapAssetHandler.BitmapAsset> {
  /**
   * A resource implementation for Bitmaps read from disk
   */
  static class BitmapAsset implements Asset {
    public static final Creator<BitmapAsset> CREATOR = new Creator<BitmapAsset>() {
      @Override
      public BitmapAsset createFromParcel(Parcel source) {
        return new BitmapAsset(source);
      }

      @Override
      public BitmapAsset[] newArray(int size) {
        return new BitmapAsset[size];
      }
    };
    private final @NonNull File createdFrom;
    private transient @Nullable Bitmap bitmap;

    private BitmapAsset(@NonNull File createdFrom) {
      this.createdFrom = createdFrom;
    }

    private BitmapAsset(@NonNull Parcel parcel) {
      createdFrom = new File(parcel.readString());
    }

    @Nullable
    private static Bitmap decodeBitmap(@NonNull File file) {
      try {
        // NOTE: We must be careful when decoding images on android. If a malicious push sends down
        // a payload image that is too large for us to reasonably decode, we must ensure that we can
        // safely fall back to a lower resolution if we don't have the memory for it.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        while (true) {
          InputStream cachedData = new FileInputStream(file);

          try {
            return BitmapFactory.decodeStream(cachedData, null, options);
          } catch (OutOfMemoryError ex) {
            // Ignore out of memory, try loading with less requested resolution.
            System.gc();
            options.inSampleSize *= 2;
          } finally {
            cachedData.close();
          }
        }
      } catch (IOException ex) {
        Log.e(LOG_TAG, "IO Exception!", ex);
        return null;
      }
    }

    @NonNull
    public File getCreatedFrom() {
      return createdFrom;
    }

    @NonNull
    public Bitmap getBitmap() {
      if (bitmap == null) {
        bitmap = decodeBitmap(createdFrom);
        if (bitmap == null) {
          throw new RuntimeException("Failed to decode bitmap from file");
        }
      }
      return bitmap;
    }

    @NonNull
    @Override
    public String getType() {
      return TYPE;
    }

    @Override
    public void validate() throws InvalidParcelException {
      if (!createdFrom.exists()) {
        throw new InvalidParcelException(
          new FileNotFoundException(
            "Bitmap cache file does not exist: " + createdFrom.getAbsolutePath()
          )
        );
      }
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(createdFrom.getAbsolutePath());
    }
  }

  public static final String TYPE = "Image";
  private static final String LOG_TAG = BitmapAssetHandler.class.getCanonicalName();

  @Nullable
  @Override
  public Set<URL> getCacheURLs(@NonNull JSONObject payload) {
    try {
      URL url = new URL(payload.getString("url"));
      Set<URL> set = new HashSet<>();
      set.add(url);

      return set;
    } catch (MalformedURLException ex) {
      return null;
    } catch (JSONException ex) {
      return null;
    }
  }

  @Nullable
  @Override
  public BitmapAsset createAsset(@NonNull JSONObject payload, @NonNull AssetManager.AssetCache cache) {
    try {
      URL url = new URL(payload.getString("url"));
      File cacheFile = cache.getCachedFile(url);
      if (cacheFile == null) {
        return null;
      }

      return new BitmapAsset(cacheFile);
    } catch (MalformedURLException ex) {
      Log.e(LOG_TAG, "JSON key 'url' was not a valid URL", ex);
      return null;
    } catch (JSONException ex) {
      Log.e(LOG_TAG, "JSON exception", ex);
      return null;
    }
  }

  @NonNull
  @Override
  public View createView(@NonNull BitmapAsset asset, @NonNull Context context) {
    ImageView imageView = new ImageView(context);
    imageView.setImageBitmap(asset.getBitmap());
    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

    return imageView;
  }
}
