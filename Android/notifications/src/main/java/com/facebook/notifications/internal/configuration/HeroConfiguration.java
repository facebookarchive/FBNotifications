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

public class HeroConfiguration implements Parcelable {
  public static final Creator<HeroConfiguration> CREATOR = new Creator<HeroConfiguration>() {
    @Override
    public HeroConfiguration createFromParcel(Parcel source) {
      return new HeroConfiguration(source);
    }

    @Override
    public HeroConfiguration[] newArray(int size) {
      return new HeroConfiguration[size];
    }
  };

  private final float height;
  private final @Nullable Asset background;

  private final @Nullable Content content;
  private final Content.VerticalAlignment contentVerticalAlignment;

  private HeroConfiguration(
    @NonNull JSONObject json,
    @NonNull AssetManager assetManager,
    @NonNull ContentManager contentManager
  ) throws JSONException {
    height = (float) json.optDouble("height", -1);
    background = assetManager.inflateAsset(json.getJSONObject("background"));

    // TODO: Go through content manager
    JSONObject contentJSON = json.optJSONObject("content");
    content = contentJSON != null
      ? new TextContent(contentJSON)
      : null;

    contentVerticalAlignment = Content.VerticalAlignment.parse(json.optString("contentAlign"));
  }

  private HeroConfiguration(Parcel parcel) {
    ClassLoader loader = getClass().getClassLoader();

    height = parcel.readFloat();
    background = parcel.readParcelable(loader);

    content = parcel.readParcelable(loader);
    contentVerticalAlignment = parcel.readParcelable(loader);
  }

  /**
   * Create a hero configuration from a possibly `null` JSON payload
   *
   * @param json           JSON Payload. Can be null
   * @param assetManager   Asset manager to use for any assets in the hero
   * @param contentManager Content manager to use for any content in the hero
   * @return A hero configuration if the JSON payload exists, or `null` if it does not
   * @throws JSONException if the JSON is in an invalid format
   */
  @Nullable
  public static HeroConfiguration fromJSON(
    @Nullable JSONObject json,
    @NonNull AssetManager assetManager,
    @NonNull ContentManager contentManager
  ) throws JSONException {
    if (json == null) {
      return null;
    }
    return new HeroConfiguration(json, assetManager, contentManager);
  }

  public void validate() throws InvalidParcelException {
    if (background != null) {
      background.validate();
    }
  }

  public float getHeight() {
    return height;
  }

  @Nullable
  public Asset getBackground() {
    return background;
  }

  @Nullable
  public Content getContent() {
    return content;
  }

  public Content.VerticalAlignment getContentVerticalAlignment() {
    return contentVerticalAlignment;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeFloat(height);
    dest.writeParcelable(background, flags);

    dest.writeParcelable(content, flags);
    dest.writeParcelable(contentVerticalAlignment, flags);
  }
}
