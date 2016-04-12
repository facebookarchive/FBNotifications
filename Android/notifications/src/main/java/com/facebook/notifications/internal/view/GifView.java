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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.facebook.notifications.internal.utilities.GifDecoder;

@SuppressLint("ViewConstructor")
public class GifView extends View {
  private class DecoderThread extends Thread {
    private final @Nullable GifDecoder decoder;
    private @Nullable Bitmap currentFrame;

    public DecoderThread(@Nullable GifDecoder decoder) {
      this.decoder = decoder;
    }

    @Override
    public void run() {
      if (decoder == null) {
        return;
      }

      while (true) {
        long frameStart = System.nanoTime();

        decoder.advance();

        int frameDelay = decoder.getNextDelay();
        long targetNextFrame = frameStart + (frameDelay * 1000000);

        synchronized (this) {
          currentFrame = decoder.getNextFrame();
          notifyAll();
        }

        // Naive: assume all frames take the same amount of time to decode.
        long currentTime = System.nanoTime();
        long frameDecodeTime = currentTime - frameStart;

        long sleepTimeNs = (targetNextFrame - currentTime) - frameDecodeTime;
        long sleepTimeMs = Math.max(0, sleepTimeNs / 1000000);

        postInvalidate();

        try {
          Thread.sleep(sleepTimeMs);
          if (Thread.interrupted()) {
            break;
          }
        } catch (InterruptedException ex) {
          // Thread was killed by View, RIP.
          break;
        }
      }

      currentFrame = null;
      decoder.recycle();
    }
  }

  private static final String LOG_TAG = GifView.class.getCanonicalName();

  private final @NonNull DecoderThread decoderThread;
  private final @NonNull Paint antiAliasPaint;
  private final @NonNull Rect sourceRect;
  private final @NonNull RectF targetRect;

  public GifView(@NonNull Context context, @Nullable GifDecoder decoder) {
    super(context);
    setDrawingCacheEnabled(false);
    setWillNotCacheDrawing(true);

    decoderThread = new DecoderThread(decoder);
    antiAliasPaint = new Paint();
    sourceRect = new Rect();
    targetRect = new RectF();

    antiAliasPaint.setAntiAlias(true);
    antiAliasPaint.setFilterBitmap(true);
    antiAliasPaint.setDither(true);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    decoderThread.start();
  }

  @Override
  protected void onDetachedFromWindow() {
    decoderThread.interrupt();

    try {
      decoderThread.join();
    } catch (InterruptedException ex) {
      Log.e(LOG_TAG, "Failed to kill decoder thread", ex);
    }

    super.onDetachedFromWindow();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    GifDecoder decoder = decoderThread.decoder;
    if (decoder == null) {
      setMeasuredDimension(0, 0);
      return;
    }

    boolean variableWidth = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY;
    boolean variableHeight = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;

    int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
    int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

    float sourceWidth = decoder.getWidth();
    float sourceHeight = decoder.getHeight();

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

    setMeasuredDimension(measuredWidth, measuredHeight);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Bitmap currentFrame;
    synchronized (decoderThread) {
      currentFrame = decoderThread.currentFrame;
      if (currentFrame == null) {
        try {
          decoderThread.wait(100);
          currentFrame = decoderThread.currentFrame;
        } catch (InterruptedException e) {
          Log.e(LOG_TAG, "Failed to wait", e);
        }
      }
    }

    if (currentFrame == null) {
      return;
    }

    // Scale the frame that we have already in our bitmap and draw it.
    // This is the CENTER_CROP algorithm that we use for displaying images.
    float sourceWidth = currentFrame.getWidth();
    float sourceHeight = currentFrame.getHeight();

    float destWidth = getWidth();
    float destHeight = getHeight();

    float heightRatio = destHeight / sourceHeight;
    float widthRatio = destWidth / sourceWidth;

    float scale = Math.max(heightRatio, widthRatio);

    float targetWidth = sourceWidth * scale;
    float targetHeight = sourceHeight * scale;

    float offsetW = (destWidth - targetWidth) / 2f;
    float offsetH = (destHeight - targetHeight) / 2f;

    sourceRect.set(0, 0, currentFrame.getWidth(), currentFrame.getHeight());
    targetRect.set(offsetW, offsetH, offsetW + targetWidth, offsetH + targetHeight);

    canvas.drawBitmap(currentFrame, sourceRect, targetRect, antiAliasPaint);
  }
}
