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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.facebook.notifications.internal.utilities.InvalidParcelException;

/**
 * Manager for content
 */
public class ContentManager implements Parcelable {
  public static final Creator<ContentManager> CREATOR = new Creator<ContentManager>() {
    @Override
    public ContentManager createFromParcel(Parcel source) {
      return new ContentManager(source);
    }

    @Override
    public ContentManager[] newArray(int size) {
      return new ContentManager[size];
    }
  };

  // TODO: Extensible like AssetManager, but for content
  public ContentManager() {
  }

  public ContentManager(ContentManager other) {
  }

  public ContentManager(@NonNull Parcel source) {
  }

  public void setContext(@NonNull Context context) {
  }

  public void validate() throws InvalidParcelException {
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
  }
}
