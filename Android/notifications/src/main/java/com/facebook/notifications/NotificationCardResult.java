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

package com.facebook.notifications;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class NotificationCardResult implements Parcelable {
  public static final Creator<NotificationCardResult> CREATOR = new Creator<NotificationCardResult>() {
    @Override
    public NotificationCardResult createFromParcel(Parcel source) {
      return new NotificationCardResult(source);
    }

    @Override
    public NotificationCardResult[] newArray(int size) {
      return new NotificationCardResult[size];
    }
  };

  private final @Nullable Uri actionUri;

  public NotificationCardResult(@Nullable Uri actionUri) {
    this.actionUri = actionUri;
  }

  private NotificationCardResult(Parcel parcel) {
    this.actionUri = parcel.readParcelable(getClass().getClassLoader());
  }

  @Nullable
  public Uri getActionUri() {
    return actionUri;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(actionUri, flags);
  }
}
