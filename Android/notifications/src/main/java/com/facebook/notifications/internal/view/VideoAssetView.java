package com.facebook.notifications.internal.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.facebook.notifications.internal.asset.handlers.VideoAssetHandler;
import com.facebook.notifications.internal.utilities.MeasureSpecHelpers;

import java.io.IOException;

@SuppressLint("ViewConstructor")
public class VideoAssetView extends FrameLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener, SurfaceHolder.Callback2 {
  private static final String LOG_TAG = VideoAssetView.class.getCanonicalName();

  private final @NonNull VideoAssetHandler.VideoAsset asset;
  private final @NonNull SurfaceView surfaceView;
  private final @NonNull MediaPlayer mediaPlayer;
  private final @NonNull RelativeLayout loadingFrame;
  private final @NonNull ProgressBar loadingView;

  public VideoAssetView(@NonNull Context context, @NonNull VideoAssetHandler.VideoAsset asset) {
    super(context);

    this.asset = asset;

    surfaceView = new SurfaceView(context);
    loadingFrame = new RelativeLayout(context);
    loadingFrame.setBackgroundColor(Color.BLACK);

    loadingView = new ProgressBar(context);
    loadingView.setIndeterminate(true);

    mediaPlayer = new MediaPlayer();
    mediaPlayer.setOnErrorListener(this);

    SurfaceHolder holder = surfaceView.getHolder();
    holder.addCallback(this);
    holder.setFixedSize(asset.getFrameWidth(), asset.getFrameHeight());

    mediaPlayer.setOnPreparedListener(this);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      mediaPlayer.setOnInfoListener(this);
    }

    try {
      mediaPlayer.setDataSource(asset.getCreatedFrom().getAbsolutePath());
    } catch (IOException ex) {
      Log.e(LOG_TAG, "Error while decoding media", ex);
    }

    mediaPlayer.prepareAsync();

    loadingFrame.addView(loadingView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
      addRule(RelativeLayout.CENTER_IN_PARENT);
    }});

    addView(surfaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT) {{
      gravity = Gravity.CENTER;
    }});
    addView(loadingFrame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
  }


  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int[] size = MeasureSpecHelpers.calculateAspectFillSize(
      asset.getFrameWidth(),
      asset.getFrameHeight(),
      widthMeasureSpec,
      heightMeasureSpec
    );

    RectF surfaceRect = MeasureSpecHelpers.calculateAspectFillRect(asset.getFrameWidth(), asset.getFrameHeight(), size[0], size[1], null);

    LayoutParams params = (LayoutParams) surfaceView.getLayoutParams();
    if (params == null) {
      params = new LayoutParams(0, 0);
    }

    params.width = Math.round(surfaceRect.width());
    params.height = Math.round(surfaceRect.height());
    params.leftMargin = -Math.round((surfaceRect.width() - size[0]) / 2f);
    params.topMargin = -Math.round((surfaceRect.height() - size[1]) / 2f);

    surfaceView.setLayoutParams(params);

    super.onMeasure(
      MeasureSpec.makeMeasureSpec(size[0], MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(size[1], MeasureSpec.EXACTLY)
    );
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    Log.d(LOG_TAG, "Media player prepared");

    mediaPlayer.start();
    mediaPlayer.setOnCompletionListener(this);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
      // On API 17, we can't know when the video rendering actually starts, so just remove it when
      // we call play(). This may result in strange artifacts, but there's really nothing we can do
      // about it.
     removeView(loadingFrame);
    }
  }

  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {
    Log.d(LOG_TAG, "Media Player Info: (what: " + what + " extra: " + extra + ")");
    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
      // Rendering started, hide the loading indicator.
      removeView(loadingFrame);
    }
    return true;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    Log.d(LOG_TAG, "Media completed");
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    Log.d(LOG_TAG, "Media Player error: (what: " + what + ", extra: " + extra + ")");
    return false;
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    Log.d(LOG_TAG, "Surface Created");

    mediaPlayer.setDisplay(holder);
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Log.d(LOG_TAG, "Surface Changed");
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    Log.d(LOG_TAG, "Surface Destroyed");

    mediaPlayer.stop();
    mediaPlayer.release();
  }

  @Override
  public void surfaceRedrawNeeded(SurfaceHolder holder) {
    Log.d(LOG_TAG, "Surface Redraw required.");
  }
}
