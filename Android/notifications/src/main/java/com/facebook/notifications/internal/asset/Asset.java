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

package com.facebook.notifications.internal.asset;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.facebook.notifications.internal.utilities.InvalidParcelException;

/**
 * A 'marker' interface used for identifying an object as a resource. On its own, doesn't do much.
 */
public interface Asset extends Parcelable {

  /**
   * Get the type of this Asset. Used by {@link AssetManager} to find the proper
   * {@link AssetManager.AssetHandler} for this asset.
   *
   * @return The asset type of the asset.
   */
  @NonNull
  String getType();

  /**
   * Validate that all of the content for this asset has been properly cached and prepared.
   *
   * @throws InvalidParcelException
   */
  void validate() throws InvalidParcelException;
}
