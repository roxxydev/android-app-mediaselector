package com.droid.mediamultiselector.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.droid.mediamultiselector.R;
import com.droid.mediamultiselector.model.Video;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class VideoPlayerActivity extends AppCompatActivity {

    /**
     * The local file path of the video. If video is to be streamed, then
     * MPEG-DASH is used for http streaming of the video url. e.g.
     * http://www.bok.net/dash/tears_of_steel/cleartext/stream.mpd
     */
    public static final String EXTRAS_VIDEO_PATH = "video_path";

    public static final String EXTRAS_IS_LOCAL_VIDEO = "is_local_video";
    public static final String EXTRAS_IS_SHOW_VID_CONTROLLER = "is_show_vid_controller";
    public static final String EXTRAS_BG_COLOR = "background_color";

    private int bgColor;
    private String videoPath;
    private boolean isLocalVideo;
    private boolean isShowController;

    private LinearLayout playerContainer;
    private SimpleExoPlayerView simpleExoPlayerView;

    private SimpleExoPlayer player;
    private Handler mainHandler;

    public static void startActivity(Activity activity, Video video, boolean isLocalVideo,
                                     boolean isShowController, int bgColor) {
        Intent intent = new Intent(activity, VideoPlayerActivity.class);
        intent.putExtra(EXTRAS_VIDEO_PATH, video.path);
        intent.putExtra(EXTRAS_IS_LOCAL_VIDEO, isLocalVideo);
        intent.putExtra(EXTRAS_IS_SHOW_VID_CONTROLLER, isShowController);
        intent.putExtra(EXTRAS_BG_COLOR, bgColor);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorStatusBar));
        }

        setContentView(R.layout.activity_video_player);

        mainHandler = new Handler();

        if (getIntent().getExtras() != null) {
            videoPath = getIntent().getExtras().getString(EXTRAS_VIDEO_PATH);
            isLocalVideo = getIntent().getExtras().getBoolean(EXTRAS_IS_LOCAL_VIDEO);
            isShowController = getIntent().getExtras().getBoolean(EXTRAS_IS_SHOW_VID_CONTROLLER);
            bgColor = getIntent().getExtras().getInt(EXTRAS_BG_COLOR);
        }

        initView();
    }

    private void initView() {
        playerContainer = (LinearLayout) findViewById(R.id.layout_video_player_container);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.layout_video_player_view);

        playerContainer.setBackgroundColor(bgColor);

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);

        TrackSelector trackSelector =
                new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);

        // Initialize player to use with SimpleExoPlayerView
        player = ExoPlayerFactory.newSimpleInstance(VideoPlayerActivity.this, trackSelector, new DefaultLoadControl());

        // Set the player to use for SimpleExoPlayerView
        simpleExoPlayerView.setUseController(isShowController);
        simpleExoPlayerView.setPlayer(player);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(VideoPlayerActivity.this,
                Util.getUserAgent(VideoPlayerActivity.this, "MediaMultiSelector"), bandwidthMeter);

        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(VideoPlayerActivity.this, "MediaMultiSelector"));

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = null;
        if (isLocalVideo) {
            videoSource = new ExtractorMediaSource(Uri.fromFile(new File(videoPath)),
                    dataSourceFactory, extractorsFactory, null, null);
        } else {
            videoSource = new DashMediaSource(Uri.parse(videoPath), dataSourceFactory,
                    new DefaultDashChunkSource.Factory(httpDataSourceFactory), mainHandler, null);
        }

        // Add listener to dismiss dialog when video ended
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    VideoPlayerActivity.this.finish();
                }
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }
        });

        // Prepare the player with the source.
        player.prepare(videoSource);
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy() {
        // Release resources taken up by ExoPlayer
        if (player != null)
            player.release();

        super.onDestroy();
    }
}
