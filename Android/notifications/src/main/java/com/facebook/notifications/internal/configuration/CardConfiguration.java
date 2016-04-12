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
import android.util.Log;

import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.asset.handlers.ColorAssetHandler;
import com.facebook.notifications.internal.content.ContentManager;
import com.facebook.notifications.internal.utilities.EnumCreator;
import com.facebook.notifications.internal.utilities.InvalidParcelException;

import org.json.JSONException;
import org.json.JSONObject;

public class CardConfiguration implements Parcelable {
  public enum CardSize implements Parcelable {
    Invalid,
    Small,
    Medium,
    Large;

    public static final Creator<CardSize> CREATOR = new EnumCreator<>(CardSize.class, values());

    public static CardSize parse(String input) {
      switch (input) {
        case "small":
          return CardSize.Small;
        case "medium":
          return CardSize.Medium;
        case "large":
          return CardSize.Large;
        default:
          return CardSize.Invalid;
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

  private static final String LOG_TAG = CardConfiguration.class.getCanonicalName();
  private final CardSize cardSize;
  private final float cornerRadius;
  private final float contentInset;
  private final int backdropColor;
  private final @Nullable HeroConfiguration heroConfiguration;
  private final @Nullable BodyConfiguration bodyConfiguration;
  private final @Nullable ActionsConfiguration actionsConfiguration;
  public static final Creator<CardConfiguration> CREATOR = new Creator<CardConfiguration>() {
    @Override
    public CardConfiguration createFromParcel(Parcel source) {
      try {
        CardConfiguration configuration = new CardConfiguration(source);
        configuration.validate();

        return configuration;
      } catch (InvalidParcelException ex) {
        Log.w(LOG_TAG, "Failed to decode card configuration", ex);
        return null;
      }
    }

    @Override
    public CardConfiguration[] newArray(int size) {
      return new CardConfiguration[size];
    }
  };

  public CardConfiguration(@NonNull JSONObject jsonObject, @NonNull AssetManager assetManager, @NonNull ContentManager contentManager) throws JSONException {
    cardSize = CardSize.parse(jsonObject.getString("size"));
    cornerRadius = (float) jsonObject.optDouble("cornerRadius", 0.0);
    contentInset = (float) jsonObject.optDouble("contentInset", 10.0);

    backdropColor = ColorAssetHandler.fromRGBAHex(jsonObject.getString("backdropColor"));

    heroConfiguration = HeroConfiguration.fromJSON(jsonObject.optJSONObject("hero"), assetManager, contentManager);
    bodyConfiguration = BodyConfiguration.fromJSON(jsonObject.optJSONObject("body"), assetManager, contentManager);
    actionsConfiguration = ActionsConfiguration.fromJSON(jsonObject.optJSONObject("actions"), assetManager);
  }

  private CardConfiguration(@NonNull Parcel source) {
    ClassLoader loader = getClass().getClassLoader();

    cardSize = source.readParcelable(loader);
    cornerRadius = source.readFloat();
    contentInset = source.readFloat();

    backdropColor = source.readInt();

    heroConfiguration = source.readParcelable(loader);
    bodyConfiguration = source.readParcelable(loader);
    actionsConfiguration = source.readParcelable(loader);
  }

  public void validate() throws InvalidParcelException {
    if (heroConfiguration != null) {
      heroConfiguration.validate();
    }

    if (bodyConfiguration != null) {
      bodyConfiguration.validate();
    }

    if (actionsConfiguration != null) {
      actionsConfiguration.validate();
    }
  }

  public CardSize getCardSize() {
    return cardSize;
  }

  public float getCornerRadius() {
    return cornerRadius;
  }

  public float getContentInset() {
    return contentInset;
  }

  public int getBackdropColor() {
    return backdropColor;
  }

  @Nullable
  public HeroConfiguration getHeroConfiguration() {
    return heroConfiguration;
  }

  @Nullable
  public BodyConfiguration getBodyConfiguration() {
    return bodyConfiguration;
  }

  @Nullable
  public ActionsConfiguration getActionsConfiguration() {
    return actionsConfiguration;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(cardSize, flags);
    dest.writeFloat(cornerRadius);
    dest.writeFloat(contentInset);

    dest.writeInt(backdropColor);

    dest.writeParcelable(heroConfiguration, flags);
    dest.writeParcelable(bodyConfiguration, flags);
    dest.writeParcelable(actionsConfiguration, flags);
  }
}
