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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.facebook.notifications.internal.asset.cache.ContentCache;
import com.facebook.notifications.internal.utilities.InvalidParcelException;
import com.facebook.notifications.internal.utilities.JSONObjectVisitor;

import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates different Asset types based on the contents of a JSON payload
 */
public class AssetManager implements Parcelable {
  /**
   * Allows for the fetching of previously-fetched content from an on-disk cache.
   */
  public interface AssetCache {
    /**
     * Gets the on-disk file for a previously cached URL.
     *
     * @param url The url to fetch from the cache
     * @return The file which contains the contents of the url, or null if the URL was not
     * previously cached, or if caching failed
     */
    File getCachedFile(URL url);
  }

  /**
   * An interface that allows different types of {@link Asset} to be displayed and used by the push
   * card.
   *
   * This must be either {@link Parcelable} or constructable with a default constructor.
   */
  public interface AssetHandler<AssetType extends Asset> {
    /**
     * Invoked by {@link AssetManager} when an asset is being cached
     *
     * @param payload The payload to cache
     * @return A set of URLs that contains the content to download into the asset cache, or null if
     * no URLs exist to cache.
     */
    @Nullable
    Set<URL> getCacheURLs(@NonNull JSONObject payload);

    /**
     * Invoked by {@link AssetManager} when an asset should be inflated from a JSON payload
     * All of the URLs returned by `getCacheURLs()` are guaranteed to have been fully downloaded
     *
     * @param payload The payload to inflate from
     * @param cache   The cache which contains the URLs that have been requested to be downloaded
     * @return The fully inflated asset, or null if inflation fails
     */
    @Nullable
    AssetType createAsset(@NonNull JSONObject payload, @NonNull AssetCache cache);

    /**
     * Invoked by {@link AssetManager} when an asset from this handler has been requested to be
     * displayed
     *
     * @param asset   The asset to create a view for
     * @param context The context to create a view for
     * @return A new view
     */
    @NonNull
    View createView(@NonNull AssetType asset, @NonNull Context context);
  }

  /**
   * An interface for receiving a callback when the caching of a set of resources has completed.
   */
  public interface CacheCompletionCallback {
    /**
     * Invoked by the {@link AssetManager} whenever caching has finished.
     *
     * @param payload The payload which has been cached.
     */
    void onCacheCompleted(@NonNull JSONObject payload);
  }

  public static final Creator<AssetManager> CREATOR = new Creator<AssetManager>() {
    @Override
    public AssetManager createFromParcel(Parcel source) {
      try {
        AssetManager assetManager = new AssetManager(source);
        assetManager.validate();

        return assetManager;
      } catch (InvalidParcelException ex) {
        Log.w(LOG_TAG, "Failed to decode asset manager", ex);
        return null;
      }
    }

    @Override
    public AssetManager[] newArray(int size) {
      return new AssetManager[size];
    }
  };
  private static final String LOG_TAG = AssetManager.class.getCanonicalName();

  private @Nullable ContentCache contentCache;
  private final @NonNull Map<String, ParcelableAssetHandler> registeredHandlers;

  public AssetManager() {
    registeredHandlers = new ConcurrentHashMap<>();
  }

  /**
   * Creates a new AssetManager from another manager's handlers, but without a context set.
   * @param other The asset manager to clone
   */
  public AssetManager(AssetManager other) {
    registeredHandlers = new ConcurrentHashMap<>(other.registeredHandlers);
  }

  public AssetManager(@NonNull Parcel parcel) {
    registeredHandlers = new ConcurrentHashMap<>();

    Bundle handlersBundle = parcel.readBundle(getClass().getClassLoader());
    for (String type : handlersBundle.keySet()) {
      registeredHandlers.put(type, (ParcelableAssetHandler)handlersBundle.getParcelable(type));
    }
  }

  public void setContext(@NonNull Context context) {
    if (contentCache != null) {
      throw new UnsupportedOperationException("Can only call setContext() once on an AssetManager!");
    }

    contentCache = new ContentCache(context);
  }

  private void validate() throws InvalidParcelException {
    for (ParcelableAssetHandler handler : registeredHandlers.values()) {
      handler.validate();
    }
  }

  public void registerHandler(@NonNull String assetType, @NonNull AssetHandler<? extends Asset> handler) {
    registeredHandlers.put(assetType, new ParcelableAssetHandler(handler));
  }

  @Nullable
  public Asset inflateAsset(@Nullable JSONObject payload) {
    if (contentCache == null) {
      throw new UnsupportedOperationException("Cannot call inflateAsset() before setContext() has been called!");
    }

    if (payload == null) {
      return null;
    }

    String type = payload.optString("_type");
    AssetHandler<? extends Asset> handler = registeredHandlers.get(type);
    if (handler == null) {
      return null;
    }

    return handler.createAsset(payload, contentCache);
  }

  @NonNull
  public <AssetType extends Asset> View inflateView(@NonNull AssetType asset, @NonNull Context context) {
    String type = asset.getType();

    @SuppressWarnings("unchecked")
    AssetHandler<AssetType> handler = (AssetHandler<AssetType>) registeredHandlers.get(type);
    if (handler == null) {
      throw new IllegalArgumentException("Asset type \"" + type + "\" not registered!");
    }

    return handler.createView(asset, context);
  }

  /**
   * Caches a given JSON payload in the background.
   *
   * @param payload  The payload to cache
   * @param callback The callback to be invoked when caching completes.
   */
  public void cachePayload(final @NonNull JSONObject payload, final @NonNull CacheCompletionCallback callback) {
    if (contentCache == null) {
      throw new UnsupportedOperationException("Cannot call cachePayload() before setContext() has been called!");
    }

    contentCache.cache(getCacheURLs(payload), new ContentCache.CompletionCallback() {
      @Override
      public void onCacheCompleted(@NonNull Set<URL> urlsToCache) {
        callback.onCacheCompleted(payload);
      }
    });
  }

  /**
   * Clears the cache for a given JSON payload.
   *
   * @param payload The payload to clear the cache for.
   */
  public void clearCache(@NonNull JSONObject payload) {
    if (contentCache == null) {
      throw new UnsupportedOperationException("Cannot call cachePayload() before setContext() has been called!");
    }

    contentCache.clear(getCacheURLs(payload));
  }

  /**
   * Stops any caching that may be occurring in the background.
   */
  public void stopCaching() {
    if (contentCache == null) {
      throw new UnsupportedOperationException("Cannot call stopCaching() before setContext() has been called!");
    }
    contentCache.stop();
  }

  @NonNull
  private Set<URL> getCacheURLs(@Nullable JSONObject payload) {
    if (contentCache == null) {
      throw new UnsupportedOperationException("Cannot call stopCaching() before setContext() has been called!");
    }

    if (payload == null) {
      return new HashSet<>();
    }

    final Set<URL> cacheURLs = new HashSet<>();
    JSONObjectVisitor.walk(payload, new JSONObjectVisitor() {
      @Override
      protected void visit(JSONObject object) {
        super.visit(object);

        String type = object.optString("_type");
        AssetHandler<? extends Asset> handler = registeredHandlers.get(type);
        if (handler == null) {
          return;
        }

        Set<URL> newURLs = handler.getCacheURLs(object);
        if (newURLs == null) {
          return;
        }

        cacheURLs.addAll(newURLs);
      }
    });

    return cacheURLs;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    Bundle bundle = new Bundle();
    for (Map.Entry<String, ParcelableAssetHandler> entry : registeredHandlers.entrySet()) {
      bundle.putParcelable(entry.getKey(), entry.getValue());
    }

    dest.writeBundle(bundle);
  }
}
