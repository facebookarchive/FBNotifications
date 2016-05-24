package com.facebook.notifications.internal.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.facebook.notifications.internal.asset.handlers.VideoAssetHandler;

@SuppressLint("ViewConstructor")
public class VideoAssetView extends FrameLayout implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener, SurfaceHolder.Callback2 {
  private static final String LOG_TAG = VideoAssetView.class.getCanonicalName();

  private final @NonNull VideoAssetHandler.VideoAsset asset;
  private final @NonNull VideoView videoView;
  private final @NonNull MediaController mediaController;
  private final @NonNull RelativeLayout loadingFrame;
  private final @NonNull ProgressBar loadingView;

  private @Nullable MediaPlayer mediaPlayer;

  public VideoAssetView(@NonNull Context context, @NonNull VideoAssetHandler.VideoAsset asset) {
    super(context);

    this.asset = asset;

    videoView = new VideoView(context);

    loadingFrame = new RelativeLayout(context);
    loadingFrame.setBackgroundColor(Color.BLACK);

    loadingView = new ProgressBar(context);
    loadingView.setIndeterminate(true);

    loadingFrame.addView(loadingView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) {{
      addRule(RelativeLayout.CENTER_IN_PARENT);
    }});

    addView(videoView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT) {{
      gravity = Gravity.CENTER;
    }});
    addView(loadingFrame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    mediaController = new MediaController(context);
    // videoView.setMediaController(mediaController);

    videoView.setOnPreparedListener(this);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      videoView.setOnInfoListener(this);
    }

    videoView.getHolder().addCallback(this);
    videoView.setVideoPath(asset.getCreatedFrom().getAbsolutePath());
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    Log.d(LOG_TAG, "Media player prepared");

    mediaPlayer = mp;

    mp.setLooping(true);
    mp.start();
    mp.setOnCompletionListener(this);
    mp.setOnErrorListener(this);

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
    return false;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    Log.d(LOG_TAG, "Video completed");

    videoView.start();
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    Log.d(LOG_TAG, "Media Player error: (what: " + what + ", extra: " + extra + ")");
    return false;
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    Log.d(LOG_TAG, "Surface Created");
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Log.d(LOG_TAG, "Surface Changed");
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    Log.d(LOG_TAG, "Surface Destroyed");
  }

  @Override
  public void surfaceRedrawNeeded(SurfaceHolder holder) {
    Log.d(LOG_TAG, "Surface Redraw required.");
  }
}
