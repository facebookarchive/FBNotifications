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

package com.facebook.notifications.internal.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.configuration.CardConfiguration;
import com.facebook.notifications.internal.configuration.HeroConfiguration;
import com.facebook.notifications.internal.content.ContentManager;

/**
 * This class is the start of the Push Card hierarchy.
 * <p/>
 * It contains a {@link HeroView}, a {@link BodyView}, and a {@link ActionsView}.
 */
// As this class will only be created via code, suppress the following warning.
@SuppressLint("ViewConstructor")
public class CardView extends LinearLayout {
  private final @NonNull CardConfiguration configuration;

  private final @NonNull HeroView heroView;
  private final @NonNull BodyView bodyView;
  private final @NonNull ActionsView actionsView;

  /**
   * Create a new card view, from the given configuration.
   *
   * @param context The context of the view (usually an activity).
   * @param config  The configuration of the push card to display.
   */
  public CardView(
    @NonNull Context context,
    @NonNull AssetManager assetManager,
    @NonNull ContentManager contentManager,
    @NonNull ActionsView.Delegate actionsDelegate,
    @NonNull CardConfiguration config
  ) {
    super(context);
    configuration = config;

    heroView = new HeroView(context, assetManager, contentManager, configuration);
    bodyView = new BodyView(context, assetManager, contentManager, configuration);
    actionsView = new ActionsView(context, assetManager, actionsDelegate, configuration);

    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER_VERTICAL);

    addView(heroView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    addView(bodyView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    addView(actionsView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    setBackgroundColor(configuration.getBackdropColor());
  }

  /**
   * Get a card size that fits for the given dimensions
   *
   * @param sizes    An array of two floats (width and height) **measured in DIP**.
   * @param cardSize The target card size to fit
   * @return A new array with the proper card size for the given dimensions.
   */
  private static
  @NonNull
  float[] sizeForCardSize(@NonNull float[] sizes, CardConfiguration.CardSize cardSize) {
    float layoutW = Math.min(400, sizes[0]);
    float layoutH = Math.min(700, sizes[1]);

    switch (cardSize) {
      case Invalid:
        return new float[]{0, 0};
      case Small:
        layoutW *= 0.75;
        layoutH *= 0.7;
        break;
      case Medium:
        layoutW *= 0.83;
        layoutH *= 0.9;
        break;
      case Large:
        break;
    }

    return new float[]{layoutW, layoutH};
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    DisplayMetrics metrics = getResources().getDisplayMetrics();
    int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
    int totalHeight = MeasureSpec.getSize(heightMeasureSpec);

    float[] size = {
      totalWidth / metrics.density,
      totalHeight / metrics.density
    };
    float[] cardSize = sizeForCardSize(size, configuration.getCardSize());
    cardSize[0] *= metrics.density;
    cardSize[1] *= metrics.density;

    int hPadding = Math.round((totalWidth - cardSize[0]) / 2);

    setPadding(hPadding, 0, hPadding, 0);

    int measuredContentHeight = Integer.MAX_VALUE;
    HeroConfiguration heroConfiguration = configuration.getHeroConfiguration();

    float heroHeight = 1;
    if (heroConfiguration == null || heroConfiguration.getHeight() == -1) {
      // Measure the total hero content size
      heroView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);

      measuredContentHeight = heroView.getMeasuredHeight();
    } else {
      heroHeight = heroConfiguration.getHeight();
    }

    // Reset the hero view's height to zero so we can measure everything else
    heroView.getLayoutParams().height = 0;

    // NOTE: It is extremely important that this call is in the middle of the function. It feels
    // strange, but if we do not call it *after* we set our padding, it will calculate the width of
    // our children without taking the padding into account and cause things to flow off of the
    // screen.
    // Also note that android 6.0+ does not require an explicit second call to `onMeasure`, the View
    // subsystem appears to do it automatically whenever the padding is changed.
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int bodyHeight = bodyView.getMeasuredHeight();
    int actionsHeight = actionsView.getMeasuredHeight();

    int availableHeight = Math.round(cardSize[1]);
    int remainingHeight = availableHeight - bodyHeight - actionsHeight;

    int maxHeight = Math.round(remainingHeight * Math.abs(heroHeight));
    heroView.getLayoutParams().height = Math.min(maxHeight, measuredContentHeight);

    // We must explicitly tell the view subsystem to re-calculate with the new hero height,
    // otherwise it will assume we want to keep the height of zero that we last measured with.
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}
