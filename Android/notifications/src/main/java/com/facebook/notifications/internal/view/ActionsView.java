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
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.notifications.internal.asset.AssetManager;
import com.facebook.notifications.internal.configuration.ActionConfiguration;
import com.facebook.notifications.internal.configuration.ActionsConfiguration;
import com.facebook.notifications.internal.configuration.CardConfiguration;
import com.facebook.notifications.internal.utilities.RoundedViewHelper;

@SuppressWarnings("ResourceType")
@SuppressLint("ViewConstructor")
public class ActionsView extends RelativeLayout implements View.OnClickListener {
  public interface Delegate {
    void actionButtonClicked(ActionButton.Type type, @Nullable Uri actionUri);
  }

  private static final int ASSET_VIEW_ID = 0x78d5c27b;
  private static final int BUTTONS_LAYOUT_ID = 0x7cc9a8c8;

  private final @NonNull Delegate delegate;
  private final @Nullable ActionsConfiguration configuration;

  private final @NonNull RoundedViewHelper roundedViewHelper;
  private final @NonNull AssetView assetView;
  private final @NonNull LinearLayout buttonsLayout;
  private final @NonNull ActionButton[] actionButtons;

  public ActionsView(@NonNull Context context, @NonNull AssetManager assetManager, @NonNull Delegate del, @NonNull final CardConfiguration config) {
    super(context);
    delegate = del;
    configuration = config.getActionsConfiguration();

    if (configuration == null) {
      roundedViewHelper = new RoundedViewHelper(context, 0, 0);
      assetView = new AssetView(context, assetManager, null);
      buttonsLayout = new LinearLayout(context);
      actionButtons = new ActionButton[0];
      return;
    }

    roundedViewHelper = new RoundedViewHelper(context, config.getCornerRadius(), getRoundedCorners(config));
    assetView = new AssetView(context, assetManager, configuration.getBackground());

    ActionConfiguration[] actions = configuration.getActions();
    actionButtons = new ActionButton[actions.length];

    final DisplayMetrics metrics = getResources().getDisplayMetrics();

    int margin = Math.round(configuration.getContentInset() * metrics.density);
    int topMargin = Math.round(configuration.getTopInset() * metrics.density);
    int marginLeft = 0, marginTop = 0;
    int buttonWidth = 0, buttonWeight = 0;
    int buttonHeight = Math.round(configuration.getHeight() * metrics.density);

    buttonsLayout = new LinearLayout(context);
    switch (configuration.getLayoutStyle()) {
      case Vertical:
        buttonWidth = LayoutParams.MATCH_PARENT;
        marginTop = margin;
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        break;

      case Horizontal:
        buttonWeight = 1;
        marginLeft = margin;
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        break;

      default:
        throw new RuntimeException("Unknown layout style: " + configuration.getLayoutStyle());
    }

    boolean primary = true;
    for (int actionIndex = 0; actionIndex < actions.length; actionIndex++) {
      ActionConfiguration configuration = actions[actionIndex];
      ActionButton.Type buttonType;
      if (configuration.getActionUri() != null) {
        buttonType = primary ? ActionButton.Type.Primary : ActionButton.Type.Secondary;
        primary = false;
      } else {
        buttonType = ActionButton.Type.Dismiss;
      }

      actionButtons[actionIndex] = new ActionButton(context, actions[actionIndex], buttonType, this.configuration.getCornerRadius());
      actionButtons[actionIndex].setOnClickListener(this);

      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
      layoutParams.weight = buttonWeight;

      if (actionIndex > 0) {
        layoutParams.setMargins(marginLeft, marginTop, 0, 0);
      }
      buttonsLayout.addView(actionButtons[actionIndex], layoutParams);
    }

    assetView.setId(ASSET_VIEW_ID);
    buttonsLayout.setId(BUTTONS_LAYOUT_ID);

    addView(assetView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) {{
      addRule(ALIGN_TOP, BUTTONS_LAYOUT_ID);
      addRule(ALIGN_BOTTOM, BUTTONS_LAYOUT_ID);
    }});

    buttonsLayout.setPadding(margin, topMargin, margin, margin);
    addView(buttonsLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    setWillNotDraw(false);

    // ClipPath is not hardware-accelerated before 4.3
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
      setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
  }

  private static int getRoundedCorners(@NonNull CardConfiguration configuration) {
    if (configuration.getActionsConfiguration() == null) {
      return 0;
    }

    int corners = RoundedViewHelper.BOTTOM_LEFT | RoundedViewHelper.BOTTOM_RIGHT;
    if (configuration.getHeroConfiguration() == null &&
      configuration.getBodyConfiguration() == null) {
      corners |= RoundedViewHelper.TOP_LEFT | RoundedViewHelper.TOP_RIGHT;
    }

    return corners;
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

  @Override
  public void onClick(View v) {
    ActionButton button = (ActionButton) v;
    delegate.actionButtonClicked(button.getType(), button.getConfiguration().getActionUri());
  }
}
