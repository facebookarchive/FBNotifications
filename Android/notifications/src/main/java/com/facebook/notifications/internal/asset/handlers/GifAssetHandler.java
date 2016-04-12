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

import com.facebook.notifications.internal.asset.Asset;
import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.utilities.GifDecoder;
import com.facebook.notifications.internal.utilities.InvalidParcelException;
import com.facebook.notifications.internal.view.GifView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles assets of the GIF type.
 */
public class GifAssetHandler implements AssetManager.AssetHandler<GifAssetHandler.GifAsset> {
  /**
   * A resource implementation for GIFs read from disk
   */
  static class GifAsset implements Asset {
    public static final Creator<GifAsset> CREATOR = new Creator<GifAsset>() {
      @Override
      public GifAsset createFromParcel(Parcel source) {
        return new GifAsset(source);
      }

      @Override
      public GifAsset[] newArray(int size) {
        return new GifAsset[size];
      }
    };
    private final @NonNull File createdFrom;
    private transient @Nullable GifDecoder decoder;

    private GifAsset(@NonNull File createdFrom) {
      this.createdFrom = createdFrom;
    }

    private GifAsset(@NonNull Parcel source) {
      createdFrom = new File(source.readString());
    }

    @Nullable
    private static GifDecoder decodeGif(@NonNull File file) {
      long fileLength = file.length();
      if (fileLength == 0 || fileLength > Integer.MAX_VALUE) {
        return null;
      }

      byte[] bytes = new byte[(int) fileLength];

      try {
        FileInputStream fileInputStream = new FileInputStream(file);
        int fileOffset = 0;

        try {
          while (true) {
            int remaining = (bytes.length - fileOffset);
            if (remaining == 0) {
              break;
            }
            int read = fileInputStream.read(bytes, fileOffset, remaining);
            if (read == -1) {
              throw new IOException("File was shorter than expected!");
            }
            fileOffset += read;
          }
        } finally {
          fileInputStream.close();
        }
      } catch (IOException ex) {
        Log.e(LOG_TAG, "IO Exception while reading GIF data", ex);
        return null;
      }

      GifDecoder decoder = new GifDecoder();
      decoder.read(bytes);

      return decoder;
    }

    @NonNull
    public File getCreatedFrom() {
      return createdFrom;
    }

    @NonNull
    public GifDecoder getDecoder() {
      if (decoder == null) {
        decoder = decodeGif(createdFrom);
        if (decoder == null) {
          throw new RuntimeException("Failed to decode GIF");
        }
      }
      return decoder;
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
            "GIF cache file does not exist: " + createdFrom.getAbsolutePath()
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

  public static final String TYPE = "GIF";
  private static final String LOG_TAG = GifAssetHandler.class.getCanonicalName();

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
  public GifAsset createAsset(@NonNull JSONObject payload, @NonNull AssetManager.AssetCache cache) {
    try {
      URL url = new URL(payload.getString("url"));
      File cacheFile = cache.getCachedFile(url);
      if (cacheFile == null) {
        return null;
      }

      return new GifAsset(cacheFile);
    } catch (MalformedURLException ex) {
      Log.e(LOG_TAG, "JSON key 'url' was not a valid URL.", ex);
      return null;
    } catch (JSONException ex) {
      Log.e(LOG_TAG, "JSON exception", ex);
      return null;
    }
  }

  @NonNull
  @Override
  public View createView(@NonNull GifAsset asset, @NonNull Context context) {
    return new GifView(context, asset.getDecoder());
  }
}
