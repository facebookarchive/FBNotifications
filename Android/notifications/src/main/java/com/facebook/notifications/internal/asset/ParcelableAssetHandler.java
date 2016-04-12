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

package com.facebook.notifications.internal.asset;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.facebook.notifications.internal.utilities.InvalidParcelException;

import org.json.JSONObject;

import java.net.URL;
import java.util.Set;

/**
 * Represents a parcelable entry in {@link AssetManager}'s handler list.
 */
class ParcelableAssetHandler implements AssetManager.AssetHandler<Asset>, Parcelable {
  public static final Creator<ParcelableAssetHandler> CREATOR = new Creator<ParcelableAssetHandler>() {
    @Override
    public ParcelableAssetHandler createFromParcel(Parcel source) {
      return new ParcelableAssetHandler(source);
    }

    @Override
    public ParcelableAssetHandler[] newArray(int size) {
      return new ParcelableAssetHandler[size];
    }
  };

  private final @Nullable AssetManager.AssetHandler<Asset> handler;
  private final @Nullable InvalidParcelException exception;

  @SuppressWarnings("unchecked")
  public ParcelableAssetHandler(@NonNull AssetManager.AssetHandler<? extends Asset> handler) {
    this.handler = (AssetManager.AssetHandler) handler;
    this.exception = null;
  }

  @SuppressWarnings("unchecked")
  public ParcelableAssetHandler(Parcel source) {
    boolean isParcelable = source.readInt() != 0;

    if (isParcelable) {
      handler = source.readParcelable(getClass().getClassLoader());
      exception = null;
    } else {
      AssetManager.AssetHandler handler = null;
      InvalidParcelException exception = null;

      try {
        Class assetHandlerClass = (Class) Class.forName(source.readString(), true, getClass().getClassLoader());
        handler = (AssetManager.AssetHandler) assetHandlerClass.newInstance();
      } catch (Exception ex) {
        exception = new InvalidParcelException(ex);
      }

      this.handler = handler;
      this.exception = exception;
    }
  }

  public void validate() throws InvalidParcelException {
    if (exception != null) {
      throw exception;
    }

    if (handler == null) {
      throw new IllegalStateException("AssetHandler should not be null when parceling if no exception was thrown!");
    }
  }

  @Nullable
  @Override
  public Set<URL> getCacheURLs(@NonNull JSONObject payload) {
    if (handler == null) {
      throw new IllegalStateException("AssetHandler should not be null, did you forget to call validate()?");
    }
    return handler.getCacheURLs(payload);
  }

  @Nullable
  @Override
  public Asset createAsset(@NonNull JSONObject payload, @NonNull AssetManager.AssetCache cache) {
    if (handler == null) {
      throw new IllegalStateException("AssetHandler should not be null, did you forget to call validate()?");
    }
    return handler.createAsset(payload, cache);
  }

  @NonNull
  @Override
  public View createView(@NonNull Asset asset, @NonNull Context context) {
    if (handler == null) {
      throw new IllegalStateException("AssetHandler should not be null, did you forget to call validate()?");
    }
    return handler.createView(asset, context);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    if (handler == null) {
      throw new IllegalStateException("AssetHandler should not be null, did you forget to call validate()?");
    }

    if (handler instanceof Parcelable) {
      dest.writeInt(1);
      dest.writeParcelable((Parcelable)handler, flags);
    } else {
      dest.writeInt(0);
      dest.writeString(handler.getClass().getName());
    }
  }
}
