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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

import com.facebook.notifications.internal.utilities.EnumCreator;

public interface Content extends Parcelable {
  enum VerticalAlignment implements Parcelable {
    Top,
    Center,
    Bottom;

    public static final Creator<VerticalAlignment> CREATOR = new EnumCreator<>(VerticalAlignment.class, values());

    public static VerticalAlignment parse(String input) {
      switch (input) {
        case "top":
          return VerticalAlignment.Top;
        case "center":
          return VerticalAlignment.Center;
        case "bottom":
          return VerticalAlignment.Bottom;
        default:
          return VerticalAlignment.Center;
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

  /**
   * Attempt to apply this content to a given view
   * @param view The view to apply to
   */
  void applyTo(@NonNull View view);
}
