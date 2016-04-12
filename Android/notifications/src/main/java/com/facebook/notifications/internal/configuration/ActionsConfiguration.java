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
import com.facebook.notifications.internal.utilities.EnumCreator;
import com.facebook.notifications.internal.utilities.InvalidParcelException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionsConfiguration implements Parcelable {
  public enum ActionsStyle implements Parcelable {
    Attached,
    Detached;

    public static final Creator<ActionsStyle> CREATOR = new EnumCreator<>(ActionsStyle.class, values());

    public static ActionsStyle parse(String input) {
      switch (input) {
        case "attached":
          return ActionsStyle.Attached;
        case "detached":
          return ActionsStyle.Detached;
        default:
          return ActionsStyle.Attached;
      }
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(ordinal());
    }
  }

  public enum ActionsLayoutStyle implements Parcelable {
    Vertical,
    Horizontal;

    public static final Creator<ActionsLayoutStyle> CREATOR = new EnumCreator<>(ActionsLayoutStyle.class, values());

    public static ActionsLayoutStyle parse(String input) {
      switch (input) {
        case "vertical":
          return ActionsLayoutStyle.Vertical;
        case "horizontal":
          return ActionsLayoutStyle.Horizontal;
        default:
          return ActionsLayoutStyle.Vertical;
      }
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(ordinal());
    }
  }

  public static final Creator<ActionsConfiguration> CREATOR = new Creator<ActionsConfiguration>() {
    @Override
    public ActionsConfiguration createFromParcel(Parcel source) {
      return new ActionsConfiguration(source);
    }

    @Override
    public ActionsConfiguration[] newArray(int size) {
      return new ActionsConfiguration[size];
    }
  };
  private final ActionsStyle style;
  private final ActionsLayoutStyle layoutStyle;

  private final Asset background;

  private final float topInset;
  private final float contentInset;
  private final float cornerRadius;

  private final @NonNull ActionConfiguration[] actions;
  private final float height;

  private ActionsConfiguration(@NonNull JSONObject json, @NonNull AssetManager assetManager) throws JSONException {
    style = ActionsStyle.parse(json.optString("style"));
    layoutStyle = ActionsLayoutStyle.parse(json.optString("layoutStyle"));

    background = assetManager.inflateAsset(json.optJSONObject("background"));

    topInset = (float) json.optDouble("topInset", 0);
    contentInset = (float) json.optDouble("contentInset", 0);
    cornerRadius = (float) json.optDouble("cornerRadius", 0);

    JSONArray rawActions = json.getJSONArray("actions");
    actions = new ActionConfiguration[rawActions.length()];

    for (int jsonIndex = 0; jsonIndex < actions.length; jsonIndex++) {
      actions[jsonIndex] = new ActionConfiguration(rawActions.getJSONObject(jsonIndex));
    }

    height = (float) json.optDouble("height", 44);
  }

  private ActionsConfiguration(Parcel source) {
    ClassLoader loader = getClass().getClassLoader();

    style = source.readParcelable(loader);
    layoutStyle = source.readParcelable(loader);

    background = source.readParcelable(loader);

    topInset = source.readFloat();
    contentInset = source.readFloat();
    cornerRadius = source.readFloat();

    actions = source.createTypedArray(ActionConfiguration.CREATOR);
    height = source.readFloat();
  }

  @Nullable
  public static ActionsConfiguration fromJSON(@Nullable JSONObject json, @NonNull AssetManager assetManager) throws JSONException {
    if (json == null) {
      return null;
    }
    return new ActionsConfiguration(json, assetManager);
  }

  public void validate() throws InvalidParcelException {
    if (background != null) {
      background.validate();
    }
  }

  public ActionsStyle getStyle() {
    return style;
  }

  public ActionsLayoutStyle getLayoutStyle() {
    return layoutStyle;
  }

  public Asset getBackground() {
    return background;
  }

  public float getTopInset() {
    return topInset;
  }

  public float getContentInset() {
    return contentInset;
  }

  public float getCornerRadius() {
    return cornerRadius;
  }

  @NonNull
  public ActionConfiguration[] getActions() {
    return actions;
  }

  public float getHeight() {
    return height;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(style, flags);
    dest.writeParcelable(layoutStyle, flags);

    dest.writeParcelable(background, flags);

    dest.writeFloat(topInset);
    dest.writeFloat(contentInset);
    dest.writeFloat(cornerRadius);

    dest.writeTypedArray(actions, flags);
    dest.writeFloat(height);
  }
}
