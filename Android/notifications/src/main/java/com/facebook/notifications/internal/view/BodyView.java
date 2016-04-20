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

package com.facebook.notifications.internal.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.configuration.ActionsConfiguration;
import com.facebook.notifications.internal.configuration.BodyConfiguration;
import com.facebook.notifications.internal.configuration.CardConfiguration;
import com.facebook.notifications.internal.content.ContentManager;
import com.facebook.notifications.internal.utilities.RoundedViewHelper;

@SuppressWarnings("ResourceType")
@SuppressLint("ViewConstructor")
public class BodyView extends RelativeLayout {
  private final int RESOURCE_VIEW_ID = 0xa55d68f4;
  private final int CONTENT_VIEW_ID = 0x2facb6c8;

  private final @Nullable BodyConfiguration configuration;

  private final @NonNull RoundedViewHelper roundedViewHelper;
  private final @NonNull AssetView assetView;
  private final @NonNull ContentView contentView;

  public BodyView(@NonNull Context context, @NonNull AssetManager assetManager, @NonNull ContentManager contentManager, @NonNull CardConfiguration config) {
    super(context);
    configuration = config.getBodyConfiguration();

    ActionsConfiguration actionsConfiguration = config.getActionsConfiguration();
    ActionsConfiguration.ActionsStyle actionsStyle = actionsConfiguration != null
      ? actionsConfiguration.getStyle()
      : ActionsConfiguration.ActionsStyle.Detached;

    int corners = actionsStyle == ActionsConfiguration.ActionsStyle.Detached
      ? RoundedViewHelper.BOTTOM_LEFT | RoundedViewHelper.BOTTOM_RIGHT
      : 0;
    if (config.getHeroConfiguration() == null && configuration != null) {
      corners |= RoundedViewHelper.TOP_LEFT | RoundedViewHelper.TOP_RIGHT;
    }

    roundedViewHelper = new RoundedViewHelper(context, config.getCornerRadius(), corners);

    if (configuration == null) {
      assetView = new AssetView(context, assetManager, null);
      contentView = new ContentView(context, contentManager, null);
      return;
    }

    assetView = new AssetView(context, assetManager, configuration.getBackground());
    contentView = new ContentView(context, contentManager, configuration.getContent());

    assetView.setId(RESOURCE_VIEW_ID);
    contentView.setId(CONTENT_VIEW_ID);

    DisplayMetrics metrics = getResources().getDisplayMetrics();
    final int padding = Math.round(config.getContentInset() * metrics.density);

    contentView.setPadding(padding, padding, padding, padding);

    addView(assetView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) {{
      addRule(ALIGN_TOP, CONTENT_VIEW_ID);
      addRule(ALIGN_BOTTOM, CONTENT_VIEW_ID);
    }});
    addView(contentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    setWillNotDraw(false);

    // ClipPath is not hardware-accelerated before 4.3
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
      setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    roundedViewHelper.onLayout(changed, l, t, r, b);
  }

  @Override
  public void draw(Canvas canvas) {
    roundedViewHelper.preDraw(canvas);
    super.draw(canvas);
  }
}
