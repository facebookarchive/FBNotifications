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

package com.facebook.notifications.internal.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;

public class RoundedViewHelper {
  public static final int TOP_LEFT = 1 << 0;
  public static final int TOP_RIGHT = 1 << 1;
  public static final int BOTTOM_LEFT = 1 << 2;
  public static final int BOTTOM_RIGHT = 1 << 3;
  public static final int ALL = TOP_LEFT | TOP_RIGHT | BOTTOM_LEFT | BOTTOM_RIGHT;

  private final float cornerRadius;
  private final int corners;

  private final @NonNull RectF rect;
  private final @NonNull Path clipPath;

  /**
   * Create a new {@link RoundedViewHelper}, with a specified corner radius and corners.
   *
   * @param context         The context to help inside (usually an {@link Activity})
   * @param cornerRadiusDip The corner radius to use.
   * @param cornersMask     The corners to automatically round.
   */
  public RoundedViewHelper(@NonNull Context context, float cornerRadiusDip, int cornersMask) {
    corners = cornersMask;
    cornerRadius = context.getResources().getDisplayMetrics().density * cornerRadiusDip;
    rect = new RectF();
    clipPath = new Path();
  }

  public void onLayout(boolean changed, int l, int t, int r, int b) {
    if (corners == 0 || !changed) {
      return;
    }

    clipPath.reset();
    rect.set(0, 0, (r - l), (b - t));
    float[] radii = {0, 0, 0, 0, 0, 0, 0, 0};

    if ((corners & TOP_LEFT) != 0) {
      radii[0] = cornerRadius;
      radii[1] = cornerRadius;
    }

    if ((corners & TOP_RIGHT) != 0) {
      radii[2] = cornerRadius;
      radii[3] = cornerRadius;
    }

    if ((corners & BOTTOM_LEFT) != 0) {
      radii[4] = cornerRadius;
      radii[5] = cornerRadius;
    }

    if ((corners & BOTTOM_RIGHT) != 0) {
      radii[6] = cornerRadius;
      radii[7] = cornerRadius;
    }

    clipPath.addRoundRect(rect, radii, Path.Direction.CW);
  }

  public void preDraw(Canvas canvas) {
    if (corners == 0) {
      return;
    }

    if (canvas.isOpaque()) {
      canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), 255, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
    }
    canvas.clipPath(clipPath);
  }
}
