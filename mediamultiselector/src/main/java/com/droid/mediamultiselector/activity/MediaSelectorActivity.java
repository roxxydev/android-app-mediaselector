package com.droid.mediamultiselector.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droid.mediamultiselector.MediaSelectCallback;
import com.droid.mediamultiselector.R;
import com.droid.mediamultiselector.adapter.PagerAdapter;
import com.droid.mediamultiselector.fragment.ImageSelectorFragment;
import com.droid.mediamultiselector.fragment.VideoSelectorFragment;
import com.droid.mediamultiselector.model.Media;

import java.io.File;
import java.util.ArrayList;

public class MediaSelectorActivity extends AppCompatActivity implements MediaSelectCallback, View.OnClickListener {

    public static final String RESULTS_SELECTED_MEDIA = "result_media_selected";

    public static final String EXTRAS_PREVIOUSLY_SELECTED = "prev_selected_media";
    public static final String EXTRAS_MODE = "selection_mode";
    public static final String EXTRAS_MEDIA_TYPE = "allowed_media";
    public static final String EXTRAS_SELECTION_LIMIT = "selection_limit";

    public static final String EXTRAS_IS_SHOW_SELECTED_COUNT = "is_show_selected_count";
    public static final String EXTRAS_SHOW_CAMERA = "is_show_camera";

    public static final int MEDIA_TYPE_ALL = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static final int SELECTION_MODE_SINGLE = 11;
    public static final int SELECTION_MODE_MULTI = 12;

    public static final int DEFAULT_SELECTION_LIMIT = 1;

    private static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 201;

    private int selectionMode = SELECTION_MODE_SINGLE;
    private int mediaType = MEDIA_TYPE_ALL;
    private int selectionLimit = DEFAULT_SELECTION_LIMIT;
    private boolean isShowCamera = true;
    private boolean isShowSelectedCount = true;

    private ArrayList<String> arrPrevMediaPath = new ArrayList<>();

    private Toolbar toolbar;
    private TextView tvToolbarTitle;
    private TextView tvMediaCount;
    private ImageView ivClose;
    private ImageView ivAccept;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private RelativeLayout rlFgmtContainer;
    private RelativeLayout rlTabContainer;
    private String strTabPhoto;
    private String strTabVideo;

    private ImageSelectorFragment imageSelectorFragment;
    private VideoSelectorFragment videoSelectorFragment;

    /**
     * Start media selection activity.
     * @param activity Activity instance that will listen to this activity result.
     * @param requestCode The request code to use in activity result event.
     * @param selectionMode Value can either be {@link #SELECTION_MODE_MULTI} or {@link #SELECTION_MODE_SINGLE}
     * @param selectionLimit Length of allowed number of media to pick.
     * @param mediaType Value can either be {@link #MEDIA_TYPE_IMAGE} or {@link #MEDIA_TYPE_VIDEO}
     * @param isShowCamera Flag if to allow user to take photo and video.
     * @param isShowSelectedCount Flag if to show selected count label.
     * @param prevSelectedMediaPaths List of String of media path that were previously selected if any.
     */
    public static void startActivityForResult(Activity activity, int requestCode, int selectionMode,
                                              int selectionLimit, int mediaType,
                                              boolean isShowCamera,
                                              boolean isShowSelectedCount,
                                              ArrayList<String> prevSelectedMediaPaths) {

        Intent intent = new Intent(activity, MediaSelectorActivity.class);
        intent.putExtra(EXTRAS_MODE, selectionMode);
        intent.putExtra(EXTRAS_SELECTION_LIMIT, selectionLimit);
        intent.putExtra(EXTRAS_MEDIA_TYPE, mediaType);
        intent.putExtra(EXTRAS_SHOW_CAMERA, isShowCamera);
        intent.putExtra(EXTRAS_IS_SHOW_SELECTED_COUNT, isShowSelectedCount);
        intent.putStringArrayListExtra(EXTRAS_PREVIOUSLY_SELECTED, prevSelectedMediaPaths);
        activity.startActivityForResult(intent, requestCode);
    }

    /** Get the length of already selected media. */
    public int getCountSelectedMedia() {
        int countSelected = (arrPrevMediaPath != null) ? arrPrevMediaPath.size() : selectionLimit;
        return countSelected;
    }

    private void requestPermission(final String permission, String rationale, final int requestCode){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.msg_permission_request)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.label_btn_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MediaSelectorActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.label_btn_cancel, null)
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initView();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        tvToolbarTitle = (TextView) findViewById(R.id.toolbar_tv_title);
        tvMediaCount = (TextView) findViewById(R.id.toolbar_tv_count);
        ivClose = (ImageView) findViewById(R.id.toolbar_iv_close);
        ivAccept = (ImageView) findViewById(R.id.toolbar_iv_accept);
        tabLayout = (TabLayout) findViewById(R.id.activity_main_toolbar_tablayout);
        viewPager = (ViewPager) findViewById(R.id.activity_main_viewpager);
        rlFgmtContainer = (RelativeLayout) findViewById(R.id.activity_main_fgmt_container);
        rlTabContainer = (RelativeLayout) findViewById(R.id.activity_main_tab_container);

        strTabPhoto = getResources().getString(R.string.label_tab_photo);
        strTabVideo = getResources().getString(R.string.label_tab_video);

        handleIntent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.msg_permission_read_storage),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);

        } else {
            initView();
        }
    }

    private void handleIntent() {
        selectionMode = getIntent().getExtras().getInt(EXTRAS_MODE, SELECTION_MODE_SINGLE);
        selectionLimit = getIntent().getExtras().getInt(EXTRAS_SELECTION_LIMIT, DEFAULT_SELECTION_LIMIT);

        ArrayList<String> tmp = getIntent().getStringArrayListExtra(EXTRAS_PREVIOUSLY_SELECTED);
        if (tmp != null && tmp.size() > 0) {
            arrPrevMediaPath = tmp;
        }

        mediaType = getIntent().getExtras().getInt(EXTRAS_MEDIA_TYPE, MEDIA_TYPE_ALL);
        isShowCamera = getIntent().getExtras().getBoolean(EXTRAS_SHOW_CAMERA, true);
        isShowSelectedCount = getIntent().getExtras().getBoolean(EXTRAS_IS_SHOW_SELECTED_COUNT, true);
    }

    private void initView() {
        setSupportActionBar(toolbar);

        if (selectionMode == SELECTION_MODE_SINGLE) {
            ivAccept.setVisibility(View.GONE);
        }

        ivAccept.setOnClickListener(this);
        ivClose.setOnClickListener(this);

        if (mediaType == MEDIA_TYPE_IMAGE) {
            tvToolbarTitle.setText(getResources().getString(R.string.label_toolbar_title_image));
        } else if (mediaType == MEDIA_TYPE_VIDEO) {
            tvToolbarTitle.setText(getResources().getString(R.string.label_toolbar_title_video));
        }

        if (selectionMode == SELECTION_MODE_MULTI
                && isShowSelectedCount == true) {
            tvMediaCount.setVisibility(View.VISIBLE);
            updateTextMediaCount();
        }

        imageSelectorFragment = ImageSelectorFragment.createIntance(
                selectionMode, selectionLimit, isShowCamera, arrPrevMediaPath);

        videoSelectorFragment = VideoSelectorFragment.createIntance(
                selectionMode, selectionLimit, isShowCamera, arrPrevMediaPath);

        if (mediaType == MEDIA_TYPE_ALL) {
            rlTabContainer.setVisibility(View.VISIBLE);
            rlFgmtContainer.setVisibility(View.GONE);

            tabLayout.addTab(tabLayout.newTab().setText(strTabPhoto));
            tabLayout.addTab(tabLayout.newTab().setText(strTabVideo));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), mediaType);
            adapter.setVideoFragment(videoSelectorFragment);
            adapter.setImageFragment(imageSelectorFragment);

            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

        } else {
            rlTabContainer.setVisibility(View.GONE);
            rlFgmtContainer.setVisibility(View.VISIBLE);

            if (mediaType == MEDIA_TYPE_IMAGE) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_main_fgmt_container, imageSelectorFragment)
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_main_fgmt_container, videoSelectorFragment)
                        .commit();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.toolbar_iv_close) {
            setResult(RESULT_CANCELED);
        } else if (view.getId() == R.id.toolbar_iv_accept) {
            Intent data = new Intent();
            data.putStringArrayListExtra(RESULTS_SELECTED_MEDIA, arrPrevMediaPath);
            setResult(RESULT_OK, data);
        }
        finish();
    }

    @Override
    public void onSingleMediaSelected(Media media) {
        Intent data = new Intent();
        arrPrevMediaPath.add(media.path);
        data.putStringArrayListExtra(RESULTS_SELECTED_MEDIA, arrPrevMediaPath);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onMediaItemSelected(Media mediaSelected) {
        if(!arrPrevMediaPath.contains(mediaSelected.path)) {
            arrPrevMediaPath.add(mediaSelected.path);
            updateTextMediaCount();
        }
    }

    @Override
    public void onMediaItemUnselected(Media mediaUnselected) {
        if(arrPrevMediaPath.contains(mediaUnselected.path)){
            arrPrevMediaPath.remove(mediaUnselected.path);
            updateTextMediaCount();
        }
    }

    @Override
    public void onCameraTake(File mediaFile) {
        if(mediaFile != null) {
            // notify system the media has change
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mediaFile)));

            if (selectionMode == SELECTION_MODE_SINGLE) {
                arrPrevMediaPath.add(mediaFile.getAbsolutePath());
                Intent data = new Intent();
                data.putStringArrayListExtra(RESULTS_SELECTED_MEDIA, arrPrevMediaPath);
                setResult(RESULT_OK, data);
                finish();
            } else if (arrPrevMediaPath.size() < selectionLimit) {
                arrPrevMediaPath.add(mediaFile.getAbsolutePath());
            }
        }
    }

    // Update media count in toolbar
    private void updateTextMediaCount() {
        if (arrPrevMediaPath != null && selectionMode == SELECTION_MODE_MULTI) {
            tvMediaCount.setText(getCountSelectedMedia() + " / " + selectionLimit);
        }
    }
}
