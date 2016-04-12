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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class ContentDownloader implements Runnable {
  public interface DownloadCallback {
    void onResourceDownloaded(@NonNull URL url, @Nullable File file);
  }

  private interface DownloadOperation extends Runnable {
  }

  private static final String LOG_TAG = ContentDownloader.class.getCanonicalName();
  private final @NonNull BlockingQueue<DownloadOperation> operations;

  public ContentDownloader() {
    operations = new LinkedBlockingQueue<>();
  }

  @Override
  public void run() {
    while (true) {
      try {
        DownloadOperation operation = operations.take();
        operation.run();
      } catch (InterruptedException ex) {
        // This means our parent thread requested us killed, RIP.
        return;
      }
    }
  }

  public void downloadAsync(final @NonNull URL url, final @NonNull File targetFile, final @NonNull DownloadCallback callback) {
    operations.offer(new DownloadOperation() {
      private File download(@NonNull URL url) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
          connection = (HttpURLConnection) url.openConnection();
          if (connection.getResponseCode() != 200) {
            return null;
          }

          inputStream = connection.getInputStream();
          outputStream = new FileOutputStream(targetFile);
          byte[] buffer = new byte[4096];

          while (true) {
            int read = inputStream.read(buffer);
            if (read == -1) {
              break;
            }

            outputStream.write(buffer, 0, read);
          }

          return targetFile;
        } catch (Exception ex) {
          Log.e(LOG_TAG, "Failed to download content for url " + url, ex);
          return null;
        } finally {
          try {
            if (inputStream != null) {
              inputStream.close();
            }

            if (outputStream != null) {
              outputStream.close();
            }
          } catch (IOException ex) {
            Log.e(LOG_TAG, "Failed to close streams", ex);
          }

          if (connection != null) {
            connection.disconnect();
          }
        }
      }

      @Override
      public void run() {
        callback.onResourceDownloaded(url, download(url));
      }
    });
  }
}
