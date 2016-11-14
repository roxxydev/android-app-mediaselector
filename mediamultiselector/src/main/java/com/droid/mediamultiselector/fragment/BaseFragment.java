package com.droid.mediamultiselector.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.MediaItemClickListener;
import com.droid.mediamultiselector.MediaSelectCallback;
import com.droid.mediamultiselector.R;
import com.droid.mediamultiselector.activity.MediaSelectorActivity;
import com.droid.mediamultiselector.adapter.MediaAdapter;
import com.droid.mediamultiselector.model.Media;
import com.droid.mediamultiselector.model.Video;
import com.droid.mediamultiselector.utils.FileUtils;
import com.droid.mediamultiselector.view.VideoPlayerDlgFgmt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.droid.mediamultiselector.activity.MediaSelectorActivity.DEFAULT_SELECTION_LIMIT;
import static com.droid.mediamultiselector.activity.MediaSelectorActivity.EXTRAS_PREVIOUSLY_SELECTED;
import static com.droid.mediamultiselector.activity.MediaSelectorActivity.SELECTION_MODE_SINGLE;

public class BaseFragment extends Fragment implements MediaItemClickListener {

    private static final String TAG = "BaseFragment";

    public static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 110;
    public static final int REQUEST_CAMERA = 100;

    public ArrayList<Media> arrPrevMediaPath = new ArrayList<>();// Previously selected media

    public File mTmpFile;

    public int mode = MediaSelectorActivity.SELECTION_MODE_SINGLE;// Selection mode of media
    public int selectionLimit = MediaSelectorActivity.DEFAULT_SELECTION_LIMIT;
    public boolean isShowCamera = true;

    public boolean isCameraActionPhoto = true;

    public MediaAdapter adapter;

    private MediaSelectCallback mediaSelectCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mediaSelectCallback = (MediaSelectCallback) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = getArguments().getInt(MediaSelectorActivity.EXTRAS_MODE);
        selectionLimit = getArguments().getInt(MediaSelectorActivity.EXTRAS_SELECTION_LIMIT);
        isShowCamera = getArguments().getBoolean(MediaSelectorActivity.EXTRAS_SHOW_CAMERA);

        if (mode == SELECTION_MODE_SINGLE) {
            selectionLimit = DEFAULT_SELECTION_LIMIT;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mode == MediaSelectorActivity.SELECTION_MODE_MULTI) {
            ArrayList<Media> tmp = getArguments().getParcelableArrayList(EXTRAS_PREVIOUSLY_SELECTED);
            if (tmp != null && tmp.size() > 0) {
                arrPrevMediaPath = tmp;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAMERA){
            if(resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    mediaSelectCallback.onCameraTake(mTmpFile);
                }
            }else{
                // delete tmp file
                while (mTmpFile != null && mTmpFile.exists()){
                    boolean success = mTmpFile.delete();
                    if(success){
                        mTmpFile = null;
                    }
                }
            }
        }
    }

    /**
     * Open camera for taking photo or video.
     * @param isTakePhoto true if camera action is taking photo, otherwise false if camera action
     *                    is taking video.
     */
    private void showCameraAction(boolean isTakePhoto) {
        isCameraActionPhoto = isTakePhoto;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    getString(R.string.msg_permission_write_storage),
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (!isCameraActionPhoto) {
                intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            }

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                try {
                    String fileExt = isTakePhoto ? FileUtils.JPEG_FILE_SUFFIX: FileUtils.VIDEO_FILE_SUFFIX;
                    mTmpFile = FileUtils.createTmpFile(fileExt, getActivity());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (mTmpFile != null && mTmpFile.exists()) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else {
                    Toast.makeText(getActivity(), R.string.msg_error_media_not_existing, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), R.string.msg_error_no_camera_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPermission(final String permission, String rationale, final int requestCode){
        if (shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.msg_permission_request)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.label_btn_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.label_btn_cancel, null)
                    .create().show();
        } else {
            requestPermissions(new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_WRITE_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCameraAction(isCameraActionPhoto);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onItemClick(Media media, int position) {
        if (media == null && isShowCamera) {
            showCameraAction(isCameraActionPhoto);
        }
        else {
            if (mode == MediaSelectorActivity.SELECTION_MODE_MULTI) {
                if (arrPrevMediaPath.contains(media)) {
                    arrPrevMediaPath.remove(media);
                    mediaSelectCallback.onMediaItemUnselected(media);
                    adapter.selectData(media, position);
                } else {
                    int countSelectedMedia = ((MediaSelectorActivity) getActivity()).getCountSelectedMedia();
                    if (countSelectedMedia >= selectionLimit) {
                        Toast.makeText(getActivity(), R.string.msg_selection_limit_reached, Toast.LENGTH_SHORT).show();
                    } else {
                        arrPrevMediaPath.add(media);
                        mediaSelectCallback.onMediaItemSelected(media);
                        adapter.selectData(media, position);
                    }
                }
            } else if (mode== MediaSelectorActivity.SELECTION_MODE_SINGLE) {
                mediaSelectCallback.onSingleMediaSelected(media);
            }
        }
    }

    @Override
    public void onVideoItemPlay(Media media) {
        VideoPlayerDlgFgmt dialog = VideoPlayerDlgFgmt.newInstance((Video) media, true, false);
        dialog.show(getChildFragmentManager(), TAG);
    }
}
