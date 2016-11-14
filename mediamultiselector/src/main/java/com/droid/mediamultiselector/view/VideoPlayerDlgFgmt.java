package com.droid.mediamultiselector.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class VideoPlayerDlgFgmt extends DialogFragment {

    /**
     * The local file path of the video. If video is to be streamed, then
     * MPEG-DASH is used for http streaming of the video url. e.g.
     * http://www.bok.net/dash/tears_of_steel/cleartext/stream.mpd
     */
    public static final String EXTRAS_VIDEO_PATH = "video_path";

    public static final String EXTRAS_IS_LOCAL_VIDEO = "is_local_video";
    public static final String EXTRAS_IS_SHOW_VID_CONTROLLER = "is_show_vid_controller";

    private String videoPath;
    private boolean isLocalVideo;
    private boolean isShowController;

    private SimpleExoPlayerView simpleExoPlayerView;

    private SimpleExoPlayer player;
    private Handler mainHandler;

    // Required default constructor for Fragment
    public VideoPlayerDlgFgmt() {

    }

    public static VideoPlayerDlgFgmt newInstance(Video video, boolean isLocalVideo, boolean isShowController) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRAS_VIDEO_PATH, video.path);
        bundle.putBoolean(EXTRAS_IS_LOCAL_VIDEO, isLocalVideo);
        bundle.putBoolean(EXTRAS_IS_SHOW_VID_CONTROLLER, isShowController);

        VideoPlayerDlgFgmt fgmt = new VideoPlayerDlgFgmt();
        fgmt.setArguments(bundle);
        return fgmt;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler();

        if (getArguments() != null) {
            videoPath = getArguments().getString(EXTRAS_VIDEO_PATH);
            isLocalVideo = getArguments().getBoolean(EXTRAS_IS_LOCAL_VIDEO);
            isShowController = getArguments().getBoolean(EXTRAS_IS_SHOW_VID_CONTROLLER);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_video_dlg_fgmt, null);
        builder.setView(view);

        initView(view);

        return builder.create();
    }

    private void initView(View view) {
        simpleExoPlayerView = (SimpleExoPlayerView) view.findViewById(R.id.layout_video_dlg_fgmt_player_view);

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);

        TrackSelector trackSelector =
                new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);

        // Initialize player to use with SimpleExoPlayerView
        player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, new DefaultLoadControl());

        // Set the player to use for SimpleExoPlayerView
        simpleExoPlayerView.setUseController(isShowController);
        simpleExoPlayerView.setPlayer(player);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(),
                Util.getUserAgent(getActivity(), "MediaMultiSelector"), bandwidthMeter);

        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(getActivity(), "MediaMultiSelector"));

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
                    VideoPlayerDlgFgmt.this.dismiss();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        // Release resources taken up by ExoPlayer
        if (player != null)
            player.release();
    }
}
