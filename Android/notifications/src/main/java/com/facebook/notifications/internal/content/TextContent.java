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

package com.facebook.notifications.internal.content;

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.facebook.notifications.internal.asset.handlers.ColorAssetHandler;
import com.facebook.notifications.internal.utilities.EnumCreator;
import com.facebook.notifications.internal.utilities.FontUtilities;

import org.json.JSONException;
import org.json.JSONObject;

public class TextContent implements Content {
  public enum Alignment implements Parcelable {
    Left,
    Right,
    Center;

    public static final Creator<Alignment> CREATOR = new EnumCreator<>(Alignment.class, values());

    public static Alignment parse(@NonNull String input) {
      switch (input) {
        case "left":
          return Left;
        case "right":
          return Right;
        case "center":
          return Center;
        default:
          return Left;
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

  public static final Creator<TextContent> CREATOR = new Creator<TextContent>() {
    @Override
    public TextContent createFromParcel(Parcel source) {
      return new TextContent(source);
    }

    @Override
    public TextContent[] newArray(int size) {
      return new TextContent[size];
    }
  };

  private final @NonNull String text;
  private final int textColor;

  private final @Nullable String typeface;
  private final float typefaceSize;

  private final Alignment textAlignment;

  public TextContent(@NonNull JSONObject json) throws JSONException {
    text = json.optString("text", "");
    textColor = ColorAssetHandler.fromRGBAHex(json.optString("color"));

    typeface = json.optString("font");
    typefaceSize = (float) json.optDouble("size", 18); // Default to 18sp, or 'medium' size.

    textAlignment = Alignment.parse(json.optString("align", "center"));
  }

  private TextContent(Parcel source) {
    text = source.readString();
    textColor = source.readInt();

    typeface = source.readString();
    typefaceSize = source.readFloat();

    textAlignment = source.readParcelable(getClass().getClassLoader());
  }

  @Override
  public void applyTo(@NonNull View view) {
    if (view instanceof TextView) {
      TextView textView = (TextView)view;

      textView.setText(getText());
      textView.setTextColor(getTextColor());

      Typeface typeface = FontUtilities.parseFont(getTypeface());
      typeface = typeface != null ? typeface : Typeface.DEFAULT;

      textView.setTypeface(typeface);
      textView.setTextSize(getTypefaceSize());

      switch (getTextAlignment()) {
        case Left:
          textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
          break;

        case Center:
          textView.setGravity(Gravity.CENTER);
          break;

        case Right:
          textView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
          break;
      }
    }
  }

  @NonNull
  public String getText() {
    return text;
  }

  public int getTextColor() {
    return textColor;
  }

  @Nullable
  public String getTypeface() {
    return typeface;
  }

  public float getTypefaceSize() {
    return typefaceSize;
  }

  public Alignment getTextAlignment() {
    return textAlignment;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(text);
    dest.writeInt(textColor);

    dest.writeString(typeface);
    dest.writeFloat(typefaceSize);

    dest.writeParcelable(textAlignment, flags);
  }
}
