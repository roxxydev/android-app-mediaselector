package com.droid.mediamultiselector.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droid.mediamultiselector.R;
import com.droid.mediamultiselector.activity.MediaSelectorActivity;
import com.droid.mediamultiselector.adapter.MediaAdapter;
import com.droid.mediamultiselector.model.Image;
import com.droid.mediamultiselector.model.Media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageSelectorFragment extends BaseFragment {

    private static final String TAG = "ImageSelectorFragment";

    private View rootView;
    private RecyclerView rvImgSelector;

    public static ImageSelectorFragment createIntance(int mode, int selectionLimit, boolean isShowCamera,
                                                      List<String> arrPrevSelectedPath) {
        ImageSelectorFragment fgmt = new ImageSelectorFragment();

        Bundle args = new Bundle();

        ArrayList<Media> arrSelectedMedia = new ArrayList<>();
        for (String path: arrPrevSelectedPath) {
            Media media = new Media(path, "", System.currentTimeMillis());
            arrSelectedMedia.add(media);
        }

        args.putInt(MediaSelectorActivity.EXTRAS_MODE, mode);
        args.putInt(MediaSelectorActivity.EXTRAS_SELECTION_LIMIT, selectionLimit);
        args.putBoolean(MediaSelectorActivity.EXTRAS_SHOW_CAMERA, isShowCamera);
        args.putParcelableArrayList(MediaSelectorActivity.EXTRAS_PREVIOUSLY_SELECTED, arrSelectedMedia);

        fgmt.setArguments(args);

        return fgmt;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fgmt_image_selector, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isCameraActionPhoto = true;

        initView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(0, null, callbackLoaderMngr);
    }

    private void initView() {
        rvImgSelector = (RecyclerView) rootView.findViewById(R.id.fgmt_image_selector_rv);
        rvImgSelector.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        rvImgSelector.setHasFixedSize(true);

        adapter = new MediaAdapter(getActivity(), MediaSelectorActivity.MEDIA_TYPE_IMAGE,
                this, mode, selectionLimit, isShowCamera);

        rvImgSelector.setAdapter(adapter);
    }

    private LoaderManager.LoaderCallbacks<Cursor> callbackLoaderMngr = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cursorLoader = new CursorLoader(
                    getActivity(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    IMAGE_PROJECTION,
                    IMAGE_PROJECTION[4]+">0 AND "+ IMAGE_PROJECTION[3]+"=? OR "+IMAGE_PROJECTION[3]+"=? ",
                    new String[]{"image/jpeg", "image/png"},
                    IMAGE_PROJECTION[2] + " DESC");
            return cursorLoader;
        }

        private boolean fileExist(String path){
            if(!TextUtils.isEmpty(path)){
                return new File(path).exists();
            }
            return false;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (data.getCount() > 0) {
                    ArrayList<Media> images = new ArrayList<>();
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));

                        if(!fileExist(path)) { continue; }

                        Image image = null;
                        if (!TextUtils.isEmpty(name)) {
                            image = new Image(path, name, dateTime);
                            images.add(image);
                        }

                    } while(data.moveToNext());

                    adapter.setData(images);
                    if (arrPrevMediaPath != null && arrPrevMediaPath.size()>0) {
                        adapter.setSelectedData(arrPrevMediaPath);
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };
}
