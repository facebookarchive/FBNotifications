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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.StateSet;
import android.widget.Button;

import com.facebook.notifications.internal.configuration.ActionConfiguration;
import com.facebook.notifications.internal.content.Content;
import com.facebook.notifications.internal.utilities.TransparentStateListDrawable;

@SuppressLint("ViewConstructor")
public class ActionButton extends Button {
  public enum Type {
    Primary,
    Secondary,
    Dismiss
  }

  private final @NonNull ActionConfiguration configuration;
  private final Type type;

  public ActionButton(@NonNull Context context, @NonNull final ActionConfiguration config, @NonNull Type t, final float cornerRadius) {
    super(context, null, android.R.attr.borderlessButtonStyle);

    configuration = config;
    type = t;

    setTransformationMethod(null);
    setPadding(0, 0, 0, 0);

    final DisplayMetrics metrics = getResources().getDisplayMetrics();
    final int backgroundColor = configuration.getBackgroundColor();
    final int pressedColor;
    final int borderWidth = Math.round(configuration.getBorderWidth() * metrics.density);

    Content content = config.getContent();
    if (content != null) {
      content.applyTo(this);
    }

    float[] hsv = {0, 0, 0};
    Color.colorToHSV(backgroundColor, hsv);
    hsv[2] *= 0.5;

    pressedColor = Color.HSVToColor(backgroundColor >> 24, hsv);

    GradientDrawable backgroundGradient = new GradientDrawable() {{
      setCornerRadius(cornerRadius * metrics.density);
      setShape(GradientDrawable.RECTANGLE);
      setStroke(borderWidth, configuration.getBorderColor());
      setColor(backgroundColor);
    }};

    GradientDrawable pressedGradient = new GradientDrawable() {{
      setCornerRadius(cornerRadius * metrics.density);
      setShape(RECTANGLE);
      setStroke(borderWidth, configuration.getBorderColor());
      setColor(pressedColor);
    }};

    TransparentStateListDrawable stateListDrawable = new TransparentStateListDrawable();
    stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedGradient);
    stateListDrawable.addState(StateSet.WILD_CARD, backgroundGradient);

    setBackgroundDrawable(stateListDrawable);
    setWillNotDraw(false);

    // ClipPath is not hardware-accelerated before 4.3
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
      setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
  }

  @NonNull
  public ActionConfiguration getConfiguration() {
    return configuration;
  }

  public Type getType() {
    return type;
  }
}
