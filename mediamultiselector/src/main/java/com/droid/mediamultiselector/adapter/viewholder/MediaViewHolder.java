package com.droid.mediamultiselector.adapter.viewholder;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.droid.mediamultiselector.R;
import com.droid.mediamultiselector.activity.MediaSelectorActivity;
import com.droid.mediamultiselector.adapter.MediaAdapter;
import com.droid.mediamultiselector.model.Image;
import com.droid.mediamultiselector.model.Media;
import com.droid.mediamultiselector.model.Video;

import java.io.File;

public class MediaViewHolder extends RecyclerView.ViewHolder {

    private TextView tvCameraLabel;
    private ImageView ivPreview;
    private ImageView ivPlayBtn;
    private View vMaskSelectedBg;
    private ImageView ivCheck;

    private Context mCtx;
    private View itemView;

    public MediaViewHolder(View itemView) {
        super(itemView);

        tvCameraLabel = (TextView) itemView.findViewById(R.id.item_media_tv_take_photo_video);
        ivPreview = (ImageView) itemView.findViewById(R.id.item_media_iv_preview);
        ivPlayBtn = (ImageView) itemView.findViewById(R.id.item_media_vid_playbutton);
        vMaskSelectedBg = itemView.findViewById(R.id.item_media_mask);
        ivCheck = (ImageView) itemView.findViewById(R.id.item_media_iv_check);

        mCtx = itemView.getContext();
        this.itemView = itemView;
    }

    public void bindData(final Activity activity, final MediaAdapter adapter,
                         final int position, final Media media,
                         final int selectionLimit, final boolean isShowCameraButton,
                         boolean isShowSelectIndicator, boolean isSelected) {

        String tag = isSelected ? "selected" : "unselected";
        itemView.setTag(tag);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Media mediaClicked = isShowCameraButton ? null: media;

                if (itemView.getTag().toString() == "selected") {
                    itemView.setTag("unselected");
                    setCellItemViewState(false);

                } else {
                    int selectedCountMedia = ((MediaSelectorActivity) activity).getCountSelectedMedia();
                    boolean hasReachedLimit = (selectedCountMedia + 1) >= selectionLimit;
                    if (!hasReachedLimit) {
                        itemView.setTag("selected");
                        setCellItemViewState(true);
                    }
                }

                adapter.getMediaItemClickListener().onItemClick(mediaClicked, position);
            }
        });

        if (!isShowCameraButton && media != null && media instanceof Video) {
            ivPlayBtn.setVisibility(View.VISIBLE);
            ivPlayBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.getMediaItemClickListener().onVideoItemPlay(media);
                }
            });
        } else {
            ivPlayBtn.setVisibility(View.GONE);
        }

        if (isShowCameraButton) {
            // Display take picture/video button
            tvCameraLabel.setVisibility(View.VISIBLE);
            if (adapter.getMediaType() == MediaSelectorActivity.MEDIA_TYPE_IMAGE) {
                tvCameraLabel.setText(mCtx.getResources().getString(R.string.label_take_photo));
            } else {
                tvCameraLabel.setText(mCtx.getResources().getString(R.string.label_take_video));
            }

            ivPreview.setVisibility(View.GONE);
            ivCheck.setVisibility(View.GONE);
            vMaskSelectedBg.setVisibility(View.GONE);
        }
        else {
            tvCameraLabel.setVisibility(View.GONE);
            ivPreview.setVisibility(View.VISIBLE);
            ivCheck.setVisibility(View.VISIBLE);
            vMaskSelectedBg.setVisibility(View.VISIBLE);

            setCellItemViewState(isSelected);

            // Set check background to previously selected media items
            if (isShowSelectIndicator) {
                ivCheck.setVisibility(View.VISIBLE);
            } else {
                ivCheck.setVisibility(View.GONE);
            }

            // Display preview of image or thumbnail if video
            if (media instanceof Image) {
                Image image = (Image) media;
                Glide.with(mCtx)
                        .load(image.path)
                        .placeholder(R.drawable.ic_preview_error)
                        .error(R.drawable.ic_preview_error)
                        .crossFade()
                        .into(ivPreview);

            } else if (media instanceof Video) {
                Video video = (Video) media;
                Glide.with(mCtx)
                        .load(Uri.fromFile( new File( video.path ) ))
                        .placeholder(R.drawable.ic_preview_error)
                        .error(R.drawable.ic_preview_error)
                        .crossFade()
                        .into(ivPreview);

                ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                ivPreview.setImageBitmap(video.thumbnail);
            }
        }
    }

    private void setCellItemViewState(boolean isSelected) {
        if (isSelected) {
            ivCheck.setImageResource(R.drawable.ic_selected);
            vMaskSelectedBg.setVisibility(View.VISIBLE);
        } else {
            ivCheck.setImageResource(R.drawable.ic_unselected);
            vMaskSelectedBg.setVisibility(View.GONE);
        }
    }
}
