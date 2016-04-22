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

package com.facebook.notifications.internal.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;

import com.facebook.notifications.NotificationCardResult;
import com.facebook.notifications.internal.appevents.AppEventsLogger;
import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.asset.handlers.ColorAssetHandler;
import com.facebook.notifications.internal.configuration.CardConfiguration;
import com.facebook.notifications.internal.content.ContentManager;
import com.facebook.notifications.internal.view.ActionButton;
import com.facebook.notifications.internal.view.ActionsView;
import com.facebook.notifications.internal.view.CardView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An activity which displays a push card.
 */
public class CardActivity extends Activity implements ActionsView.Delegate {
  /**
   * The intent extra key to be set with the JSON payload of your push card payload.
   */
  public static final String EXTRA_CARD_PAYLOAD = "fb_push_card_payload";
  /**
   * If the configuration has already been cached and parsed, you may pass simply the entire
   * configuration object to the activity. This is significantly faster than re-parsing the entire
   * JSON object.
   */
  public static final String EXTRA_CONFIGURATION = "fb_push_card_configuration";
  /**
   * The intent extra key to be set with the push campaign identifier.
   */
  public static final String EXTRA_CAMPAIGN_IDENTIFIER = "fb_push_campaign";
  /**
   * The intent extra key to be set with an instance of {@link AssetManager}
   */
  public static final String EXTRA_ASSET_MANAGER = "fb_push_card_asset_manager";
  /**
   * The intent extra key to be set with an instance of {@link ContentManager}
   */
  public static final String EXTRA_CONTENT_MANAGER = "fb_push_card_content_manager";
  /**
   * The intent extra key to be set with the card result.
   */
  public static final String EXTRA_NOTIFICATION_CARD_RESULT = "fb_notification_card_result";
  private static final String LOG_TAG = CardActivity.class.getCanonicalName();

  private @Nullable String campaignIdentifier;
  private @Nullable JSONObject configurationPayload;

  private @NonNull AssetManager assetManager;
  private @NonNull ContentManager contentManager;
  private @NonNull AppEventsLogger appEventsLogger;
  private @Nullable CardView cardView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    appEventsLogger = new AppEventsLogger(this);

    Intent intent = getIntent();
    campaignIdentifier = intent.getStringExtra(EXTRA_CAMPAIGN_IDENTIFIER);

    String payloadString = intent.getStringExtra(EXTRA_CARD_PAYLOAD);
    CardConfiguration configuration = intent.getParcelableExtra(EXTRA_CONFIGURATION);

    AssetManager assetManager = intent.getParcelableExtra(EXTRA_ASSET_MANAGER);
    ContentManager contentManager = intent.getParcelableExtra(EXTRA_CONTENT_MANAGER);

    assetManager = assetManager != null ? assetManager : new AssetManager();
    contentManager = contentManager != null ? contentManager : new ContentManager();

    assetManager.setContext(this);
    contentManager.setContext(this);

    this.assetManager = assetManager;
    this.contentManager = contentManager;

    try {
      configurationPayload = new JSONObject(payloadString);
    } catch (JSONException ex) {
      Log.e(LOG_TAG, "Error parsing JSON payload", ex);
    }

    if (configuration == null) {
      beginLoadingContent();
    } else {
      displayConfiguration(configuration);
    }

    appEventsLogger.logPushOpen(campaignIdentifier);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (assetManager == null) {
      return;
    }
    assetManager.stopCaching();
    if (configurationPayload == null) {
      return;
    }
    assetManager.clearCache(configurationPayload);
  }

  private void beginLoadingContent() {
    if (assetManager == null || contentManager == null) {
      Log.e(LOG_TAG, "Asset & content manager should be available!");
      return;
    }

    if (configurationPayload == null) {
      Log.e(LOG_TAG, "No card payload is available!");
      return;
    }

    ProgressBar loadingView = new ProgressBar(this);
    loadingView.setIndeterminate(true);

    int backgroundColor = ColorAssetHandler.fromRGBAHex(configurationPayload.optString("backdropColor"));

    FrameLayout loadingViewFrame = new FrameLayout(this);
    loadingViewFrame.setBackgroundColor(backgroundColor);

    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    layoutParams.gravity = Gravity.CENTER;
    loadingViewFrame.addView(loadingView, layoutParams);

    setContentView(loadingViewFrame);

    final Handler handler = new Handler();
    if (configurationPayload == null) {
      return;
    }

    assetManager.cachePayload(configurationPayload, new AssetManager.CacheCompletionCallback() {
      @Override
      public void onCacheCompleted(@NonNull JSONObject payload) {
        final CardConfiguration configuration;
        try {
          configuration = new CardConfiguration(configurationPayload, assetManager, contentManager);
        } catch (JSONException ex) {
          Log.e(LOG_TAG, "Error while parsing JSON", ex);
          return;
        }

        handler.post(new Runnable() {
          @Override
          public void run() {
            displayConfiguration(configuration);
          }
        });
      }
    });
  }

  private void displayConfiguration(CardConfiguration configuration) {
    if (assetManager == null || contentManager == null) {
      Log.e(LOG_TAG, "Asset or content manager has unloaded since beginLoadingContent()!");
      return;
    }

    cardView = new CardView(this, assetManager, contentManager, this, configuration);
    setContentView(cardView);
  }

  @Override
  public void onBackPressed() {
    appEventsLogger.logButtonAction(ActionButton.Type.Dismiss, campaignIdentifier);

    Intent resultIntent = new Intent();
    resultIntent.putExtra(EXTRA_NOTIFICATION_CARD_RESULT, new NotificationCardResult(null));
    setResult(RESULT_OK, resultIntent);
    finish();
  }

  @Override
  public void actionButtonClicked(ActionButton.Type type, @Nullable Uri actionUri) {
    appEventsLogger.logButtonAction(type, campaignIdentifier);

    Intent resultIntent = new Intent();
    resultIntent.putExtra(EXTRA_NOTIFICATION_CARD_RESULT, new NotificationCardResult(actionUri));
    setResult(RESULT_OK, resultIntent);
    finish();

    if (actionUri == null) {
      return;
    }
    Intent actionIntent = new Intent(Intent.ACTION_VIEW, actionUri);
    startActivity(actionIntent);
  }
}
