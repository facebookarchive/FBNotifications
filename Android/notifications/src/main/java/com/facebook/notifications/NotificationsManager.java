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

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.notifications.internal.activity.CardActivity;
import com.facebook.notifications.internal.appevents.AppEventsLogger;
import com.facebook.notifications.internal.asset.Asset;
import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.asset.handlers.BitmapAssetHandler;
import com.facebook.notifications.internal.asset.handlers.ColorAssetHandler;
import com.facebook.notifications.internal.asset.handlers.GifAssetHandler;
import com.facebook.notifications.internal.configuration.CardConfiguration;
import com.facebook.notifications.internal.content.ContentManager;
import com.facebook.notifications.internal.utilities.Version;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Manages incoming remote notifications for push card presentation.
 */
public final class NotificationsManager {
  /**
   * Represents handlers to be invoked when preparation of a card is completed.
   */
  public interface PrepareCallback {
    void onPrepared(@NonNull Intent presentationIntent);

    void onError(@NonNull Exception exception);
  }

  /**
   * Allows for customizing of notifications before they are displayed.
   */
  public interface NotificationExtender {
    Notification.Builder extendNotification(@NonNull Notification.Builder notification);
  }

  /**
   * The request code to use for intents returned by {@link PrepareCallback}
   */
  public static final int REQUEST_CODE = 0xCA4D; // CARD

  /**
   * The intent extra key to be used for pending intents used in notifications created by
   * `presentNotification()`
   */
  public static final String EXTRA_PAYLOAD_INTENT = "notification_push_payload_intent";

  /**
   * The highest supported payload version by this version of the Notifications SDK
   */
  public static final String PAYLOAD_VERSION = "1.0";

  /**
   * The version of the In-App Notifications Library
   */
  public static final String LIBRARY_VERSION = "1.0.1";

  private static final @NonNull Version PAYLOAD_VERSION_OBJECT = new Version(1, 0, 0);

  private static final String LOG_TAG = NotificationsManager.class.getCanonicalName();
  private static final String NOTIFICATION_TAG = "fb_notification_tag";
  private static final String PUSH_PAYLOAD_KEY = "fb_push_payload";
  private static final String CARD_PAYLOAD_KEY = "fb_push_card";

  private static final AssetManager ASSET_MANAGER = new AssetManager();
  private static final ContentManager CONTENT_MANAGER = new ContentManager();

  static {
    ASSET_MANAGER.registerHandler(BitmapAssetHandler.TYPE, new BitmapAssetHandler());
    ASSET_MANAGER.registerHandler(ColorAssetHandler.TYPE, new ColorAssetHandler());
    ASSET_MANAGER.registerHandler(GifAssetHandler.TYPE, new GifAssetHandler());
  }

  private NotificationsManager() {
  }

  @NonNull
  private static JSONObject getPushJSON(@NonNull Bundle bundle) throws JSONException {
    String pushPayload = bundle.getString(PUSH_PAYLOAD_KEY);
    if (pushPayload == null) {
      throw new IllegalArgumentException(PUSH_PAYLOAD_KEY);
    }

    return new JSONObject(pushPayload);
  }

  @NonNull
  private static JSONObject getCardJSON(@NonNull Bundle bundle) throws JSONException {
    String cardPayload = bundle.getString(CARD_PAYLOAD_KEY);
    if (cardPayload == null) {
      throw new IllegalArgumentException(CARD_PAYLOAD_KEY);
    }

    return new JSONObject(cardPayload);
  }

  @Nullable
  private static Intent intentForBundle(
    @NonNull Context context,
    @NonNull JSONObject pushJSON,
    @NonNull JSONObject cardJSON,
    @NonNull AssetManager assetManager,
    @NonNull ContentManager contentManager
  ) throws JSONException {
    String campaignIdentifier = AppEventsLogger.getCampaignIdentifier(pushJSON);
    Version cardVersion = Version.parse(cardJSON.optString("version"));

    if (cardVersion == null || cardVersion.compareTo(PAYLOAD_VERSION_OBJECT) > 0) {
      return null;
    }

    Intent intent = new Intent(context, CardActivity.class);
    intent.putExtra(CardActivity.EXTRA_CAMPAIGN_IDENTIFIER, campaignIdentifier);
    intent.putExtra(CardActivity.EXTRA_ASSET_MANAGER, assetManager);
    intent.putExtra(CardActivity.EXTRA_CONTENT_MANAGER, contentManager);
    intent.putExtra(CardActivity.EXTRA_CARD_PAYLOAD, cardJSON.toString());

    return intent;
  }

  @NonNull
  private static AssetManager getAssetManager(@NonNull Context context) {
    AssetManager manager = new AssetManager(ASSET_MANAGER);
    manager.setContext(context);
    return manager;
  }

  @NonNull
  private static ContentManager getContentManager(@NonNull Context context) {
    ContentManager manager = new ContentManager(CONTENT_MANAGER);
    manager.setContext(context);
    return manager;
  }

  /**
   * Returns whether or not a notification bundle has a valid push payload.
   *
   * @param notificationBundle The bundle to check for a push payload
   */
  public static boolean canPresentCard(@NonNull Bundle notificationBundle) {
    return notificationBundle.containsKey(CARD_PAYLOAD_KEY);
  }

  /**
   * Present an intent from a given activity to show the notification bundle contained in notificationBundle.
   *
   * @param activity           The activity to present from
   * @param notificationBundle The bundle containing the notification payload to present
   * @return whether or not the activity could successfully be presented
   */
  public static boolean presentCard(@NonNull Activity activity, @NonNull Bundle notificationBundle) {
    try {
      if (!notificationBundle.containsKey(CARD_PAYLOAD_KEY) || !notificationBundle.containsKey(PUSH_PAYLOAD_KEY)) {
        return false;
      }

      Intent presentationIntent = intentForBundle(
        activity,
        getPushJSON(notificationBundle), getCardJSON(notificationBundle),
        getAssetManager(activity), getContentManager(activity)
      );
      if (presentationIntent == null) {
        return false;
      }

      activity.startActivityForResult(presentationIntent, REQUEST_CODE);
      return true;
    } catch (JSONException ex) {
      Log.e(LOG_TAG, "Error while parsing JSON", ex);
      return false;
    }
  }

  /**
   * Prepare and pre-load a notification bundle into memory.
   *
   * @param context            The current context of your program. Usually an activity, application, or
   *                           service context.
   * @param notificationBundle The bundle containing the notification payload to present
   * @param callback           The callback to invoke once preparation is complete. This is guaranteed to be
   *                           invoked on the same thread as this method is invoked from.
   */
  public static void prepareCard(
    @NonNull final Context context,
    @NonNull final Bundle notificationBundle,
    @NonNull final PrepareCallback callback
  ) {
    final Handler handler = new Handler();
    final AssetManager assetManager = getAssetManager(context);
    final ContentManager contentManager = getContentManager(context);

    // Cache and prepare in background.
    new Thread() {
      @Override
      public void run() {
        try {
          String cardPayload = notificationBundle.getString(CARD_PAYLOAD_KEY);
          JSONObject cardJSON = new JSONObject(cardPayload);
          Version cardVersion = Version.parse(cardJSON.optString("version"));
          if (cardVersion == null || cardVersion.compareTo(PAYLOAD_VERSION_OBJECT) > 0) {
            throw new Exception("Payload version " + cardVersion + " not supported by this version of the notifications SDK.");
          }

          assetManager.cachePayload(cardJSON, new AssetManager.CacheCompletionCallback() {
            @Override
            public void onCacheCompleted(@NonNull JSONObject payload) {
              assetManager.stopCaching();

              try {
                JSONObject cardJSON = getCardJSON(notificationBundle);
                final Intent presentIntent = intentForBundle(context, getPushJSON(notificationBundle), cardJSON, assetManager, contentManager);
                if (presentIntent == null) {
                  throw new NullPointerException("presentIntent was null, this should never happen!");
                }

                CardConfiguration configuration = new CardConfiguration(cardJSON, assetManager, contentManager);
                presentIntent.putExtra(CardActivity.EXTRA_CONFIGURATION, configuration);

                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    callback.onPrepared(presentIntent);
                  }
                });
              } catch (final Exception ex) {
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    callback.onError(ex);
                  }
                });
              }
            }
          });
        } catch (final Exception ex) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              callback.onError(ex);
            }
          });
        }
      }
    }.start();
  }

  /**
   * Handle the result of an activity started using
   * {@code prepare(Context, Bundle, PrepareCallback)} or {@code present(Activity, Bundle)}.
   *
   * @param requestCode The request code used to start the activity
   * @param resultCode  The result code returned by the activity
   * @param data        The data returned by the activity
   * @return The notification card result of the activity if it exists, or null if it does not.
   */
  public static NotificationCardResult handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode != REQUEST_CODE) {
      return null;
    }
    if (resultCode != Activity.RESULT_OK || data == null) {
      return null;
    }

    return data.getParcelableExtra(CardActivity.EXTRA_NOTIFICATION_CARD_RESULT);
  }

  /**
   * Present a {@link Notification} to be presented from a GCM push bundle.
   * <p/>
   * This does not present a notification immediately, instead it caches the assets from the
   * notification bundle, and then presents a notification to the user. This allows for a smoother
   * interaction without loading indicators for the user.
   * <p/>
   * Note that only one notification can be created for a specific push bundle, should you attempt
   * to present a new notification with the same payload bundle as an existing notification, it will
   * replace and update the old notification.
   *
   * @param context            The context to send the notification from
   * @param notificationBundle The content of the push notification
   * @param launcherIntent     The launcher intent that contains your Application's activity.
   *                           This will be modified with the FLAG_ACTIVITY_CLEAR_TOP and
   *                           FLAG_ACTIVITY_SINGLE_TOP flags, in order to properly show the
   *                           notification in an already running application.
   *                           <p/>
   *                           Should you not want this behavior, you may use the notificationExtender
   *                           parameter to customize the contentIntent of the notification before
   *                           presenting it.
   */
  public static void presentNotification(
    @NonNull Context context,
    @NonNull Bundle notificationBundle,
    @NonNull Intent launcherIntent) {
    presentNotification(context, notificationBundle, launcherIntent, null);
  }

  /**
   * Present a {@link Notification} to be presented from a GCM push bundle.
   * <p/>
   * This does not present a notification immediately, instead it caches the assets from the
   * notification bundle, and then presents a notification to the user. This allows for a smoother
   * interaction without loading indicators for the user.
   * <p/>
   * Note that only one notification can be created for a specific push bundle, should you attempt
   * to present a new notification with the same payload bundle as an existing notification, it will
   * replace and update the old notification.
   *
   * @param context              The context to send the notification from
   * @param notificationBundle   The content of the push notification
   * @param launcherIntent       The launcher intent that contains your Application's activity.
   *                             This will be modified with the FLAG_ACTIVITY_CLEAR_TOP and
   *                             FLAG_ACTIVITY_SINGLE_TOP flags, in order to properly show the
   *                             notification in an already running application.
   *                             <p/>
   *                             Should you not want this behavior, you may use the notificationExtender
   *                             parameter to customize the contentIntent of the notification before
   *                             presenting it.
   * @param notificationExtender A nullable argument that allows you to customize the notification
   *                             before displaying it. Use this to configure Icons, text, sounds,
   *                             etc. before we pass the notification off to the OS.
   */
  public static void presentNotification(
    @NonNull final Context context,
    @NonNull final Bundle notificationBundle,
    @NonNull final Intent launcherIntent,
    @Nullable final NotificationExtender notificationExtender) {
    final JSONObject alert;
    final int payloadHash;

    try {
      String payload = notificationBundle.getString(CARD_PAYLOAD_KEY);

      JSONObject payloadObject = new JSONObject(payload);
      alert = payloadObject.optJSONObject("alert") != null
        ? payloadObject.optJSONObject("alert")
        : new JSONObject();

      payloadHash = payload == null
        ? 0
        : payload.hashCode();
    } catch (JSONException ex) {
      Log.e(LOG_TAG, "Error while parsing notification bundle JSON", ex);
      return;
    }

    final Thread backgroundThread = new Thread(new Runnable() {
      @Override
      public void run() {
        Looper.prepare();
        prepareCard(context, notificationBundle, new PrepareCallback() {
          @Override
          public void onPrepared(@NonNull Intent presentationIntent) {
            Intent contentIntent = new Intent(launcherIntent);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            contentIntent.putExtra(EXTRA_PAYLOAD_INTENT, presentationIntent);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(context)
              .setSmallIcon(android.R.drawable.ic_dialog_alert)
              .setContentTitle(alert.optString("title"))
              .setContentText(alert.optString("body"))
              .setAutoCancel(true)
              .setContentIntent(
                PendingIntent.getActivity(
                  context.getApplicationContext(),
                  payloadHash,
                  contentIntent,
                  PendingIntent.FLAG_ONE_SHOT
                )
              );

            if (notificationExtender != null) {
              builder = notificationExtender.extendNotification(builder);
            }

            manager.notify(NOTIFICATION_TAG, payloadHash, builder.getNotification());
            Looper.myLooper().quit();
          }

          @Override
          public void onError(@NonNull Exception exception) {
            Log.e(LOG_TAG, "Error while preparing card", exception);
            Looper.myLooper().quit();
          }
        });

        Looper.loop();
      }
    });

    backgroundThread.start();

    try {
      backgroundThread.join();
    } catch (InterruptedException ex) {
      Log.e(LOG_TAG, "Failed to wait for background thread", ex);
    }
  }

  /**
   * Present a card from the notification this activity
   * was created from, if the notification exists.
   *
   * @param activity The activity to present from.
   * @return Whether or not a card was presented.
   */
  public static boolean presentCardFromNotification(@NonNull Activity activity) {
    return presentCardFromNotification(activity, activity.getIntent());
  }

  /**
   * Present a card from the notification this activity
   * was relaunched from, if the notification exists.
   *
   * @param activity The activity to present from.
   * @param intent   Intent that was used to re-launch the activity.
   * @return Whether or not a card was presented.
   */
  public static boolean presentCardFromNotification(@NonNull Activity activity,
                                                    @NonNull Intent intent) {
    Intent notificationIntent = intent.getParcelableExtra(EXTRA_PAYLOAD_INTENT);
    if (notificationIntent == null) {
      return false;
    }

    activity.startActivityForResult(notificationIntent, REQUEST_CODE);
    return true;
  }

  /**
   * Registers an Asset Handler for use.
   *
   * @param assetType         The type of the asset to register for.
   * @param assetHandler      The asset handler to register.
   */
  private static void registerAssetHandler(
    @NonNull String assetType,
    @NonNull AssetManager.AssetHandler<? extends Asset> assetHandler
  ) {
    ASSET_MANAGER.registerHandler(assetType, assetHandler);
  }
}
