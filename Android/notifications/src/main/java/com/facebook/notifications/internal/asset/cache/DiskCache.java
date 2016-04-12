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

package com.facebook.notifications.internal.asset.cache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class DiskCache {
  private static final String LOG_TAG = DiskCache.class.getCanonicalName();

  private final @NonNull File cacheDirectory;

  public DiskCache(@NonNull Context context) {
    cacheDirectory = context.getCacheDir();
  }

  public void remove(final @NonNull String key) {
    File diskFile = new File(cacheDirectory, key);
    if (!diskFile.delete()) {
      Log.w(LOG_TAG, "Failed to delete cache file \"" + diskFile.getAbsolutePath() + "\"");
    }
  }

  @NonNull
  public File fetch(@NonNull String key) {
    return new File(cacheDirectory, key);
  }

  @NonNull
  public Set<String> getCacheKeys() {
    String[] files = cacheDirectory.list();
    Set<String> keys = new HashSet<>(files.length);
    Collections.addAll(keys, files);
    return keys;
  }
}
