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

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.facebook.notifications.internal.content.Content;
import com.facebook.notifications.internal.content.TextContent;

import org.json.JSONException;
import org.json.JSONObject;

import static com.facebook.notifications.internal.asset.handlers.ColorAssetHandler.fromRGBAHex;

public class ActionConfiguration implements Parcelable {
  public static final Creator<ActionConfiguration> CREATOR = new Creator<ActionConfiguration>() {
    @Override
    public ActionConfiguration createFromParcel(Parcel source) {
      return new ActionConfiguration(source);
    }

    @Override
    public ActionConfiguration[] newArray(int size) {
      return new ActionConfiguration[size];
    }
  };

  private final int backgroundColor;
  private final int borderColor;
  private final float borderWidth;

  private final @Nullable Content content;
  private final @Nullable Uri actionUri;

  public ActionConfiguration(JSONObject json) throws JSONException {
    backgroundColor = fromRGBAHex(json.optString("backgroundColor"));
    borderColor = fromRGBAHex(json.optString("borderColor"));

    borderWidth = (float) json.optDouble("borderWidth", 0);

    JSONObject contentJSON = json.optJSONObject("content");
    content = contentJSON == null ? null : new TextContent(contentJSON);

    String jsonUri = json.optString("url", null);
    actionUri = jsonUri == null ? null : Uri.parse(jsonUri);
  }

  private ActionConfiguration(Parcel source) {
    ClassLoader classLoader = getClass().getClassLoader();

    backgroundColor = source.readInt();
    borderColor = source.readInt();

    borderWidth = source.readFloat();
    content = source.readParcelable(classLoader);

    actionUri = source.readParcelable(classLoader);
  }

  public int getBackgroundColor() {
    return backgroundColor;
  }

  public int getBorderColor() {
    return borderColor;
  }

  public float getBorderWidth() {
    return borderWidth;
  }

  @Nullable
  public Content getContent() {
    return content;
  }

  @Nullable
  public Uri getActionUri() {
    return actionUri;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(backgroundColor);
    dest.writeInt(borderColor);

    dest.writeFloat(borderWidth);
    dest.writeParcelable(content, flags);

    dest.writeParcelable(actionUri, flags);
  }
}
