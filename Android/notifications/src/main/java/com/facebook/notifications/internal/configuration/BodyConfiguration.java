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

package com.facebook.notifications.internal.configuration;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.notifications.internal.asset.Asset;
import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.content.Content;
import com.facebook.notifications.internal.content.ContentManager;
import com.facebook.notifications.internal.content.TextContent;
import com.facebook.notifications.internal.utilities.InvalidParcelException;

import org.json.JSONException;
import org.json.JSONObject;

public class BodyConfiguration implements Parcelable {
  public static final Creator<BodyConfiguration> CREATOR = new Creator<BodyConfiguration>() {
    @Override
    public BodyConfiguration createFromParcel(Parcel source) {
      return new BodyConfiguration(source);
    }

    @Override
    public BodyConfiguration[] newArray(int size) {
      return new BodyConfiguration[size];
    }
  };

  private final @Nullable Asset background;
  private final @Nullable Content content;

  private BodyConfiguration(
    @NonNull JSONObject json,
    @NonNull AssetManager assetManager,
    @NonNull ContentManager contentManager
  ) throws JSONException {
    background = assetManager.inflateAsset(json.getJSONObject("background"));

    // TODO: Go through content manager
    JSONObject contentJSON = json.optJSONObject("content");
    content = contentJSON != null
      ? new TextContent(contentJSON)
      : null;
  }

  private BodyConfiguration(Parcel source) {
    ClassLoader loader = getClass().getClassLoader();

    background = source.readParcelable(loader);
    content = source.readParcelable(loader);
  }

  @Nullable
  public static BodyConfiguration fromJSON(@Nullable JSONObject json, @NonNull AssetManager assetManager, @NonNull ContentManager contentManager) throws JSONException {
    if (json == null) {
      return null;
    }
    return new BodyConfiguration(json, assetManager, contentManager);
  }

  public void validate() throws InvalidParcelException {
    if (background != null) {
      background.validate();
    }
  }

  @Nullable
  public Asset getBackground() {
    return background;
  }

  @Nullable
  public Content getContent() {
    return content;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(background, flags);
    dest.writeParcelable(content, flags);
  }
}
