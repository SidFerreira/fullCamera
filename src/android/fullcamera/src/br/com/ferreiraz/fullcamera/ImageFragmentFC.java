package br.com.ferreiraz.fullcamera;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.learnncode.mediachooser.MediaChooserConstants;
import com.learnncode.mediachooser.MediaModel;
import com.learnncode.mediachooser.adapter.GridViewAdapter;
import com.learnncode.mediachooser.fragment.ImageFragment;

import java.io.File;

public class ImageFragmentFC extends ImageFragment {

    @Override

    protected void setupOnItemClickListener() {
        mImageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {
/*
                // update the mStatus of each category in the adapter
                GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
                MediaModel galleryModel = (MediaModel) adapter.getItem(position);


                if (!galleryModel.status) {
                    long size = MediaChooserConstants.CheckMediaFileSize(new File(galleryModel.url.toString()), false);
                    if (size != 0) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(com.learnncode.mediachooser.R.string.file_size_exeeded) + "  " + MediaChooserConstants.SELECTED_IMAGE_SIZE_IN_MB + " " + getActivity().getResources().getString(com.learnncode.mediachooser.R.string.mb), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if ((MediaChooserConstants.MAX_MEDIA_LIMIT == MediaChooserConstants.SELECTED_MEDIA_COUNT)) {
                        if (MediaChooserConstants.SELECTED_MEDIA_COUNT < 2) {
                            Toast.makeText(getActivity(), getActivity().getResources().getString(com.learnncode.mediachooser.R.string.max_limit_file) + "  " + MediaChooserConstants.SELECTED_MEDIA_COUNT + " " + getActivity().getResources().getString(com.learnncode.mediachooser.R.string.file), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            Toast.makeText(getActivity(), getActivity().getResources().getString(com.learnncode.mediachooser.R.string.max_limit_file) + "  " + MediaChooserConstants.SELECTED_MEDIA_COUNT + " " + getActivity().getResources().getString(com.learnncode.mediachooser.R.string.files), Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                }

                // inverse the status
                galleryModel.status = !galleryModel.status;

                adapter.notifyDataSetChanged();

                if (galleryModel.status) {
                    mSelectedItems.add(galleryModel.url.toString());
                    MediaChooserConstants.SELECTED_MEDIA_COUNT++;

                } else {
                    mSelectedItems.remove(galleryModel.url.toString().trim());
                    MediaChooserConstants.SELECTED_MEDIA_COUNT--;
                }

                if (mCallback != null) {
                    mCallback.onImageSelected(mSelectedItems.size());
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("list", mSelectedItems);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                }
*/
            }
        });
    }
}
