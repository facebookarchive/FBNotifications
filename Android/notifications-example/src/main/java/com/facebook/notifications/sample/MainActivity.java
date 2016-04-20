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

package com.facebook.notifications.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.notifications.NotificationCardResult;
import com.facebook.notifications.NotificationsManager;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
  private static final String LOG_TAG = MainActivity.class.getCanonicalName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    FacebookSdk.sdkInitialize(getApplicationContext());
    NotificationsManager.presentCardFromNotification(this);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // register for GCM
    Intent intent = new Intent(this, RegistrationIntentService.class);
    startService(intent);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    NotificationsManager.presentCardFromNotification(this, intent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    NotificationCardResult result = NotificationsManager.handleActivityResult(requestCode, resultCode, data);

    if (result != null) {
      Toast.makeText(this, "Result: " + result.getActionUri(), Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Mock an example push notification bundle from one of our local example JSON files.
   * @param exampleId The example id of the asset to load
   * @return a bundle with the contents of the specified example id
   */
  @NonNull
  private Bundle getBundle(int exampleId) {
    try {
      InputStream inputStream = getAssets().open("example" + exampleId + ".json");

      StringWriter output = new StringWriter();
      IOUtils.copy(inputStream, output, Charset.forName("UTF-8"));

      JSONObject json = new JSONObject(output.toString());

      Bundle bundle = new Bundle();
      bundle.putString("fb_push_payload", json.getJSONObject("fb_push_payload").toString());
      bundle.putString("fb_push_card", json.getJSONObject("fb_push_card").toString());

      output.close();
      inputStream.close();

      return bundle;
    } catch (Exception ex) {
      Log.e(LOG_TAG, "Error while getting bundle", ex);
      return new Bundle();
    }
  }

  public void showExample(View view) {
    Bundle exampleBundle = getBundle(Integer.parseInt(view.getTag().toString()));

    NotificationsManager.presentCard(this, exampleBundle);
  }

  public void showNotification(View view) {
    Bundle exampleBundle = getBundle(Integer.parseInt(view.getTag().toString()));

    NotificationsManager.presentNotification(this, exampleBundle, getIntent());
  }
}
