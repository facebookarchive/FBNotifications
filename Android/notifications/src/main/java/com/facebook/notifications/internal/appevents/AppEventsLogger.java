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

package com.facebook.notifications.internal.appevents;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.notifications.internal.view.ActionButton;

import org.json.JSONObject;

import java.lang.reflect.Method;

/**
 * Simple class to call into core Facebook SDK and create/invoke the logger from there to report
 * App Events that have been triggered by a push card.
 */
public class AppEventsLogger {
  private static final String LOG_TAG = AppEventsLogger.class.getCanonicalName();

  private static final String PUSH_OPEN_EVENT = "fb_mobile_push_opened";
  private static final String PUSH_CAMPAIGN_KEY = "fb_mobile_push_opened";

  private @Nullable Object fbSDKLogger;
  private @Nullable Method fbSDKLogMethod;

  public AppEventsLogger(@NonNull Context context) {
    try {
      Class<?> loggerClass = Class.forName("com.facebook.appevents.AppEventsLogger");
      Method instantiateMethod = loggerClass.getMethod("newLogger", Context.class);
      fbSDKLogMethod = loggerClass.getMethod("logEvent", String.class, Bundle.class);
      fbSDKLogger = instantiateMethod.invoke(null, context);
    } catch (Exception ex) {
      Log.w(
        LOG_TAG,
        "Failed to initialize AppEventsLogger. " +
          "Did you forget to include the Facebook SDK in your application?",
        ex
      );
    }
  }

  @Nullable
  public static String getCampaignIdentifier(@NonNull JSONObject payload) {
    return payload.optString("campaign", null);
  }

  @NonNull
  private static String getAppEventName(ActionButton.Type action) {
    switch (action) {
      case Primary:
        return "fb_mobile_push_card_action_primary";
      case Secondary:
        return "fb_mobile_push_card_action_secondary";
      case Dismiss:
        return "fb_mobile_push_card_action_dismiss";
      default:
        throw new RuntimeException("Unknown action type: " + action);
    }
  }

  public void logPushOpen(@Nullable String campaignIdentifier) {
    logEvent(PUSH_OPEN_EVENT, campaignIdentifier);
  }

  public void logButtonAction(ActionButton.Type action, @Nullable String campaignIdentifier) {
    logEvent(getAppEventName(action), campaignIdentifier);
  }

  private void logEvent(@NonNull String eventName, @Nullable String campaignIdentifier) {
    if (campaignIdentifier == null || campaignIdentifier.equals("") ||
      fbSDKLogger == null || fbSDKLogMethod == null) {
      return;
    }

    Bundle parameters = new Bundle();
    parameters.putString(PUSH_CAMPAIGN_KEY, campaignIdentifier);

    try {
      fbSDKLogMethod.invoke(fbSDKLogger, eventName, parameters);
    } catch (Exception ex) {
      Log.w(
        LOG_TAG,
        "Failed to invoke AppEventsLogger." +
          "Did you forget to include the Facebook SDK in your application?",
        ex
      );
    }
  }
}
