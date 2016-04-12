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
import android.support.annotation.Nullable;

import com.facebook.notifications.internal.asset.Asset;
import com.facebook.notifications.internal.asset.AssetManager;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Manages the caching of an {@link Asset}'s resources
 * from the internet
 */
public class ContentCache implements AssetManager.AssetCache {
  public interface CompletionCallback {
    void onCacheCompleted(@NonNull Set<URL> urlsToCache);
  }

  private static final String LOG_TAG = ContentCache.class.getCanonicalName();

  private final @NonNull Context context;

  private final @NonNull ContentDownloader downloader;
  private final @NonNull Thread downloadThread;

  private final @NonNull DiskCache diskCache;

  private final @NonNull Object synchronizationMutex;
  private final @NonNull Map<String, Set<CacheOperation>> cacheOperations;
  private @Nullable Set<String> cachedKeys;

  public ContentCache(@NonNull Context context) {
    this.context = context;

    downloader = new ContentDownloader();
    diskCache = new DiskCache(context);

    downloadThread = new Thread(downloader);

    synchronizationMutex = new Object();
    cacheOperations = new HashMap<>();

    downloadThread.start();
  }

  @NonNull
  private static String getCacheKey(@NonNull URL url) {
    String urlString = url.toString();

    try {
      MessageDigest MD5 = MessageDigest.getInstance("MD5");
      Charset UTF8 = Charset.forName("UTF-8");

      byte[] toDigest = urlString.getBytes(UTF8);
      byte[] digested = MD5.digest(toDigest);

      return String.format(
        "%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
        digested[0], digested[1], digested[2], digested[3], digested[4], digested[5], digested[6],
        digested[7], digested[8], digested[9], digested[10], digested[11], digested[12],
        digested[13], digested[14], digested[15]);
    } catch (NoSuchAlgorithmException ex) {
      // If for some ungodly reason MD5 doesn't exist in this JVM, use the string's built-in hash
      // code.
      return Integer.toHexString(urlString.hashCode());
    }
  }

  public void stop() {
    downloadThread.interrupt();

    try {
      downloadThread.join();
    } catch (InterruptedException ex) {
      // If we were interrupted while waiting to have our children die, re-interrupt the
      // child thread and hope for the best.
      downloadThread.interrupt();
    }
  }

  @NonNull
  public Context getContext() {
    return context;
  }

  public void cache(@NonNull Set<URL> urlsToCache, @NonNull CompletionCallback completion) {
    CacheOperation operation = new CacheOperation(urlsToCache, completion);

    synchronized (synchronizationMutex) {
      int scheduledCount = 0;
      for (Iterator<URL> urlIterator = operation.getUrlsToCache().iterator(); urlIterator.hasNext(); ) {
        URL url = urlIterator.next();
        final String hashKey = getCacheKey(url);
        if (hasCachedData(hashKey)) {
          urlIterator.remove();
          continue;
        }

        if (cacheOperations.containsKey(hashKey)) {
          Set<CacheOperation> existingOperations = cacheOperations.get(hashKey);
          existingOperations.add(operation);
          continue;
        }

        final Set<CacheOperation> newOperations = new HashSet<>();
        newOperations.add(operation);

        cacheOperations.put(hashKey, newOperations);
        downloader.downloadAsync(url, diskCache.fetch(getCacheKey(url)), new ContentDownloader.DownloadCallback() {
          @Override
          public void onResourceDownloaded(@NonNull URL url, @Nullable File file) {
            Set<CacheOperation> operations;
            synchronized (synchronizationMutex) {
              operations = new HashSet<>(newOperations);
            }

            for (CacheOperation operation : operations) {
              operation.onResourceDownloaded(url, file);
            }
          }
        });

        scheduledCount++;
      }

      if (scheduledCount == 0) {
        completion.onCacheCompleted(urlsToCache);
      }
    }
  }

  public void clear(@NonNull Set<URL> urlsToClear) {
    synchronized (synchronizationMutex) {
      for (URL url : urlsToClear) {
        String cacheKey = getCacheKey(url);
        diskCache.remove(cacheKey);
        if (cachedKeys != null) {
          cachedKeys.remove(cacheKey);
        }
      }
    }
  }

  @Nullable
  @Override
  public File getCachedFile(@NonNull URL contentURL) {
    File file = diskCache.fetch(getCacheKey(contentURL));
    if (!file.exists()) {
      return null;
    }
    return file;
  }

  private boolean hasCachedData(@NonNull String cacheKey) {
    synchronized (synchronizationMutex) {
      if (cachedKeys == null) {
        cachedKeys = diskCache.getCacheKeys();
      }

      return cachedKeys.contains(cacheKey);
    }
  }
}
