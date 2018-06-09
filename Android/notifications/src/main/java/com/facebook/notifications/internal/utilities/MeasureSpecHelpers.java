package com.facebook.notifications.internal.utilities;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View.MeasureSpec;

/**
 * Simple helpers that are shared for calculating scaling modes.
 */
public final class MeasureSpecHelpers {
  private MeasureSpecHelpers() { }

  public static int[] calculateAspectFillSize(int frameWidth, int frameHeight, int widthMeasureSpec, int heightMeasureSpec) {
    boolean variableWidth = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY;
    boolean variableHeight = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;

    int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
    int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

    float sourceWidth = frameWidth;
    float sourceHeight = frameHeight;

    float destWidth = measuredWidth;
    float destHeight = measuredHeight;

    float heightRatio = destHeight / sourceHeight;
    float widthRatio = destWidth / sourceWidth;

    if (variableWidth && variableHeight) {
      float scale = Math.min(heightRatio, widthRatio);

      measuredWidth = Math.round(measuredWidth * scale);
      measuredHeight = Math.round(measuredHeight * scale);
    } else if (variableWidth) {
      measuredWidth = Math.round(measuredHeight * (sourceWidth / sourceHeight));
    } else if (variableHeight) {
      measuredHeight = Math.round(measuredWidth * (sourceHeight / sourceWidth));
    }

    return new int[] { measuredWidth, measuredHeight };
  }

  public static @NonNull RectF calculateAspectFillRect(int frameWidth, int frameHeight, int targetWidth, int targetHeight, @Nullable RectF toSet) {
    float sourceWidth = frameWidth;
    float sourceHeight = frameHeight;

    float destWidth = targetWidth;
    float destHeight = targetHeight;

    float heightRatio = destHeight / sourceHeight;
    float widthRatio = destWidth / sourceWidth;

    float scale = Math.max(heightRatio, widthRatio);

    float finalWidth = sourceWidth * scale;
    float finalHeight = sourceHeight * scale;

    float offsetW = (destWidth - finalWidth) / 2f;
    float offsetH = (destHeight - finalHeight) / 2f;

    if (toSet == null) {
      toSet = new RectF();
    }

    toSet.set(offsetW, offsetH, offsetW + finalWidth, offsetH + finalHeight);
    return toSet;
  }
}
