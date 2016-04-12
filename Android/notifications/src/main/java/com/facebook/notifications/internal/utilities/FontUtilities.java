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

import android.graphics.Typeface;
import android.support.annotation.Nullable;

public class FontUtilities {
  public static @Nullable Typeface parseFont(@Nullable String fontName) {
    if (fontName == null) {
      return Typeface.DEFAULT;
    }
    switch (fontName.toLowerCase()) {
      case "system-regular": return Typeface.DEFAULT;
      case "system-light": return Typeface.create("sans-serif-light", Typeface.NORMAL);
      case "system-bold": return Typeface.DEFAULT_BOLD;
      case "system-italic": return Typeface.defaultFromStyle(Typeface.ITALIC);
      case "system-bolditalic": return Typeface.defaultFromStyle(Typeface.BOLD_ITALIC);
      default: return Typeface.create(fontName, Typeface.NORMAL);
    }
  }
}
