package br.com.ferreiraz.fullcamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.learnncode.mediachooser.MediaChooserConstants;
import com.learnncode.mediachooser.MediaModel;
import com.learnncode.mediachooser.adapter.GridViewAdapter;
import com.learnncode.mediachooser.fragment.VideoFragment;

import java.io.File;

public class VideoFragmentFC extends VideoFragment {
    MediaModel currentMediaModel;

    public void initVideos() {

        try {
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            //Here we set up a string array of the thumbnail ID column we want to get back

            String [] projection = {MediaStore.Video.Media.DATA,MediaStore.Video.Media._ID};
            String    args = "";
            String [] params = { "0" };
            int maxVideoDuration = FullCameraActivity.getMaxVideoDuration() * 1000;
            if(maxVideoDuration > 0) {
                args = MediaStore.Video.Media.DURATION + " >= ?";
                params[0] = String.valueOf(maxVideoDuration);
            }

            mCursor =  getActivity().getContentResolver().query(MEDIA_EXTERNAL_CONTENT_URI, projection, args, params, orderBy + " DESC");
            setAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override

    protected void setupOnItemClickListener() {
        mVideoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // update the mStatus of each category in the adapter
                GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
                MediaModel galleryModel = (MediaModel) adapter.getItem(position);

                mSelectedItems.clear();

                galleryModel.status = !galleryModel.status;
                if(currentMediaModel != null) {
                    if(currentMediaModel.url.equals(galleryModel.url)) {
                        currentMediaModel = null;
                    } else {
                        currentMediaModel.status = false;
                        currentMediaModel = galleryModel;
                    }
                } else {
                    currentMediaModel = galleryModel;
                }
                if(currentMediaModel != null) {
                    mSelectedItems.add(currentMediaModel.url.toString());
                }

                adapter.notifyDataSetChanged();

                if (mCallback != null) {
                    mCallback.onVideoSelected(mSelectedItems.size());
                }
            }
        });
    }

    protected void clearSelection() {
        mSelectedItems.clear();
        if(currentMediaModel != null) {
            currentMediaModel.status = false;
            currentMediaModel = null;
        }
        mVideoAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getGridLayoutResource() {
        return R.layout.video_fragment_grid;
    }
}
