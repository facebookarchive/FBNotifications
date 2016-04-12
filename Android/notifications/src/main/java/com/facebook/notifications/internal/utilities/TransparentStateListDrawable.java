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

import android.graphics.PixelFormat;
import android.graphics.drawable.StateListDrawable;

/**
 * HACK:
 * <p/>
 * Due to an optimization in how canvases work, drawables which have an 'opaque' opacity
 * setting actually still get immediately blitted to the screen, regardless of what canvas
 * they're drawing in, which means they ignore the clip path of the canvas they're drawing into.
 * <p/>
 * We need a button that can be properly clipped, so we just simply extend the existing, working
 * drawable, and make it transparent.
 */
public class TransparentStateListDrawable extends StateListDrawable {
  @Override
  public int getOpacity() {
    return PixelFormat.TRANSPARENT;
  }
}
