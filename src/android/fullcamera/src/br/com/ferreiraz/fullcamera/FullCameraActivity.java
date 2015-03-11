package br.com.ferreiraz.fullcamera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.*;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.learnncode.mediachooser.activity.HomeFragmentActivity;
import com.learnncode.mediachooser.fragment.ImageFragment;
import com.learnncode.mediachooser.fragment.VideoFragment;

import us.feras.ecogallery.EcoGallery;
import us.feras.ecogallery.EcoGalleryAdapterView;

@SuppressWarnings("deprecation")
public class FullCameraActivity extends HomeFragmentActivity implements EcoGallery.EcoGalleryDelegate {

    public static final String BUTTON_SOURCE_GALLERY = "BUTTON_SOURCE_GALLERY";
    public static final String BUTTON_SOURCE_PHOTO = "BUTTON_SOURCE_PHOTO";
    public static final String BUTTON_SOURCE_VIDEO = "BUTTON_SOURCE_VIDEO";
    private static final String TAG = "FULL_CAM";


    @SuppressWarnings("deprecation")
    private Camera mCamera;
    private CameraPreview mCameraPreviewSurface;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private boolean mPagerSourcesRollingBack = false;
    static private int cameraId = 0;

    private SurfaceHolder               surfaceHolder;

    protected Activity                  self;
    private View.OnClickListener        onClickListener;
    private int                         currentFlashMode = -1;
    private EcoGallery mPagerSources;
    private SourcesAdapter              pagerSourcesAdapter;
    private EcoGallery mCarouselPhotos;
    private PhotosAdapter               pagerPhotosAdapter;

    private ArrayList<ResultClass> mPagerPhotosItems;
    private ArrayList<ResultFile> mVideosItems = new ArrayList<ResultFile>();
    private File                        tempVideoFile;

    private VideoFragmentFC             videoFragment;
    private ImageFragmentFC             imageFragment;

    private File                        galleryVideo;
    private RelativeLayout              mPreviewHolder;

    private ProgressBarAnimation        progressAnimation;
    private ProgressBar                 progressBar;
    private ProgressDialog              progressDialog;

    ImageButton buttonSwitchCamera;
    ImageButton buttonBack;
    ImageButton buttonNext;
    ImageButton buttonCancel;

    ImageButton buttonFlashOn;
    ImageButton buttonFlashAuto;
    ImageButton buttonFlashOff;

    ImageButton buttonSourceGallery;
    ImageButton buttonSourcePhoto;
    ImageButton buttonSourceVideo;

    //Parameters


    private boolean                     allowSourceGalleryPhoto = true;
    private boolean                     allowSourceGalleryVideo = true;
    private boolean                     allowSourceCameraPhoto = true;
    private boolean                     allowSourceCameraVideo = true;
    private boolean                     shouldSaveOnGallery     = true;
    private int                         imageBox                = 720;
    private int                         imageCompression        = 100;

    private static int                  maxVideoDuration        = 30; //in seconds
    private int                         maxPhotoCount           = 5;
    private int                         totalVideoDuration      = 0;

    private String                      stringOk                = "OK";
    private String                      stringCancel            = "Cancel";
    private String                      stringMaxPhotos         = "You are limited to X photos.";
    private String                      stringDeletePhoto       = "Are you sure that you want to delete this photo?";
    private String                      stringDeleteAllPhotos   = "This will cause all photos to be removed. Are you sure?";
    private String                      stringDeleteVideo       = "Are you sure that you want to delete this video?";
    private String                      stringDeleteAllVideos   = "This will cause your video to be removed. Are you sure?";
    private String                      stringProcessingVideos  = "Processing video";
    private String                      stringAppFolder         = "fullcam";

    @Override
    protected void onResume() {
        super.onResume();

        startCamera();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            if(getIntent().getExtras() == null) {
                savedInstanceState = new Bundle();
            } else {
                savedInstanceState = getIntent().getExtras();
            }
        }

        //TODO Fix rotation issues
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        self = this;

        setupOptionsFC(savedInstanceState);

        super.onCreate(savedInstanceState);

        setupReferencesFC();

        initPagerPhotos();

        initPagerSources();

    }

    //region SETUP and Init
    private void setupReferencesFC() {
        buttonSwitchCamera  = (ImageButton) findViewById(R.id.switchCamera);
        buttonBack          = (ImageButton) findViewById(R.id.back);
        buttonNext          = (ImageButton) findViewById(R.id.next);
        buttonCancel        = (ImageButton) findViewById(R.id.cancel);

        buttonFlashOn       = (ImageButton) findViewById(R.id.flashOn);
        buttonFlashAuto     = (ImageButton) findViewById(R.id.flashAuto);
        buttonFlashOff      = (ImageButton) findViewById(R.id.flashOff);

        mPreviewHolder = (RelativeLayout) findViewById(R.id.cameraPreviewHolder);

        progressBar         = (ProgressBar) findViewById(R.id.progressBar);

        if(buttonSourceGallery == null) {
            buttonSourceGallery = new ImageButton(self);
            buttonSourcePhoto = new ImageButton(self);
            buttonSourceVideo = new ImageButton(self);

            initOnClickListener();
        }
    }

    private void setupOptionsFC(Bundle savedInstanceState) {
        stringOk                = getString(savedInstanceState, "stringOk" , stringOk);
        stringCancel            = getString(savedInstanceState, "stringCancel" , stringCancel);
        stringMaxPhotos         = getString(savedInstanceState, "stringMaxPhotos" , stringMaxPhotos);
        stringDeletePhoto       = getString(savedInstanceState, "stringDeletePhoto" , stringDeletePhoto);
        stringDeleteAllPhotos   = getString(savedInstanceState, "stringDeleteAllPhotos" , stringDeleteAllPhotos);
        stringDeleteVideo       = getString(savedInstanceState, "stringDeleteVideo" , stringDeleteVideo);
        stringDeleteAllVideos   = getString(savedInstanceState, "stringDeleteAllVideos" , stringDeleteAllVideos );
        stringProcessingVideos  = getString(savedInstanceState, "stringProcessingVideos" , stringProcessingVideos);
        stringAppFolder         = getString(savedInstanceState, "stringAppFolder" , stringAppFolder);

        maxVideoDuration        = savedInstanceState.getInt("maxVideoDuration", maxVideoDuration);
        maxPhotoCount           = savedInstanceState.getInt("maxPhotoCount", maxPhotoCount);
        imageBox                = savedInstanceState.getInt("imageBox", 1200);
        imageCompression        = savedInstanceState.getInt("imageCompression", 100);

        allowSourceGalleryPhoto = savedInstanceState.getBoolean("allowSourceGalleryPhoto", true);
        allowSourceGalleryVideo = savedInstanceState.getBoolean("allowSourceGalleryVideo", true);
        getIntent().putExtra("showImage", allowSourceGalleryPhoto);
        getIntent().putExtra("showVideo", allowSourceGalleryVideo);

        allowSourceCameraPhoto = savedInstanceState.getBoolean("allowSourceCameraPhoto", true);
        allowSourceCameraVideo = savedInstanceState.getBoolean("allowSourceCameraVideo", true);

        shouldSaveOnGallery     = savedInstanceState.getBoolean("shouldSaveOnGallery", shouldSaveOnGallery);
    }

    @Override
    protected void setup() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setupContentView();

        if(allowSourceGalleryPhoto || allowSourceGalleryVideo) {
            setupTabHost();
            setupTabHostChangedListener();
        }
    }

    private void initOnClickListener() {
        if(onClickListener == null) {
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickProxy((ImageButton) v);
                }
            };
            buttonSwitchCamera.setOnClickListener(onClickListener);
            buttonBack.setOnClickListener(onClickListener);
            buttonNext.setOnClickListener(onClickListener);
            buttonCancel.setOnClickListener(onClickListener);

            buttonSourceGallery.setOnClickListener(onClickListener);
            buttonSourcePhoto.setOnClickListener(onClickListener);
            buttonSourceVideo.setOnClickListener(onClickListener);

            buttonFlashOn.setOnClickListener(onClickListener);
            buttonFlashAuto.setOnClickListener(onClickListener);
            buttonFlashOff.setOnClickListener(onClickListener);
        }
    }

    private void initPagerPhotos() {
        mCarouselPhotos = (EcoGallery) findViewById(R.id.pagerPhotos);
        if(mPagerPhotosItems == null) {
            mPagerPhotosItems = new ArrayList<ResultClass>();
            pagerPhotosAdapter = new PhotosAdapter(this, mPagerPhotosItems);
            mCarouselPhotos.setAdapter(pagerPhotosAdapter);
            mCarouselPhotos.setSpacing(10);
            mCarouselPhotos.setOnItemClickListener(new EcoGalleryAdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(EcoGalleryAdapterView<?> parent, View view, final int position, long id) {
                    if (mCarouselPhotos.getSelectedItemPosition() == position) {
                        AlertDialog dialog = new AlertDialog.Builder(self).create();
                        dialog.setMessage(stringDeletePhoto);
                        dialog.setCancelable(false);
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, stringOk, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int buttonId) {
                                dialog.dismiss();
                                ResultClass result = mPagerPhotosItems.remove(position);
                                if (!shouldSaveOnGallery) {
                                    if (!result.getFile().delete())
                                        result.getFile().deleteOnExit();
                                }
                                pagerPhotosAdapterChanged();
                            }
                        });

                        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, stringCancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int buttonId) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setIcon(android.R.drawable.ic_dialog_alert);
                        dialog.show();
                    }
                }
            });
        }
    }

    private void initPagerSources() {
        mPagerSources = (EcoGallery) findViewById(R.id.pagerSources);
        if(pagerSourcesAdapter == null) {
            ArrayList<ImageButton> items = new ArrayList<ImageButton>();
            if(allowSourceGalleryPhoto || allowSourceGalleryVideo) {
                setBackgroundOn(buttonSourceGallery, R.drawable.fullcamgallerystyle);
                buttonSourceGallery.setTag(BUTTON_SOURCE_GALLERY);
                items.add(buttonSourceGallery);
            }

            if(allowSourceCameraPhoto) {
                setBackgroundOn(buttonSourcePhoto, R.drawable.fullcamphotostyle);
                buttonSourcePhoto.setTag(BUTTON_SOURCE_PHOTO);
                items.add(buttonSourcePhoto);
            }

            if(allowSourceCameraVideo) {
                setBackgroundOn(buttonSourceVideo, R.drawable.fullcamvideostyle);
                buttonSourceVideo.setTag(BUTTON_SOURCE_VIDEO);
                items.add(buttonSourceVideo);
            }

            pagerSourcesAdapter = new SourcesAdapter(this, items);
            mPagerSources.setSpacing(30);
            mPagerSources.setDelegate((EcoGallery.EcoGalleryDelegate) self);
            mPagerSources.setAdapter(pagerSourcesAdapter);
            mPagerSources.setSelection(0);
            mPagerSources.setCallbackDuringFling(false);
            mPagerSources.setOnItemSelectedListener(new EcoGalleryAdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                    boolean maySwitchButtons = true;
                    int oldPosition = mPagerSources.getOldPosition();
//                    logMessage("onItemSelected" + oldPosition + " -> " + position);;
                    if (!mPagerSourcesRollingBack && oldPosition != EcoGalleryAdapterView.INVALID_POSITION) {
                        String previousViewTag = (String) mPagerSources.getChildAt(oldPosition).getTag();
//                        logMessage("onItemSelected" + oldPosition + " -> " + position + " = " + previousViewTag);;
                        if (previousViewTag.equals(BUTTON_SOURCE_GALLERY)) {
                            if (mTabHost.getCurrentTabTag().equals(TAB_IMAGE)) {
                                if (mPagerPhotosItems.size() > 0) {
                                    dropGalleryPhotosDialog();
                                    maySwitchButtons = false;
                                }
                            } else if (mTabHost.getCurrentTabTag().equals(TAB_VIDEO)) {
                                if (galleryVideo != null) {
                                    dropGalleryVideoDialog(oldPosition);
                                    maySwitchButtons = false;
                                }
                            }
                        } else if (previousViewTag.equals(BUTTON_SOURCE_PHOTO)) {
                            if (mPagerPhotosItems.size() > 0) {
                                dropAllPhotosDialog();
                                maySwitchButtons = false;
                            }
                        } else if (previousViewTag.equals(BUTTON_SOURCE_VIDEO)) {
                            if (mVideosItems.size() > 0) {
                                dropAllVideosDialog();
                                maySwitchButtons = false;
                            }
                        }
                    }

                    if (view == null)
                        return;

                    if (maySwitchButtons) {
                        switchSourceButtons(view.getTag());

                        if (view.getTag() == BUTTON_SOURCE_PHOTO || view.getTag() == BUTTON_SOURCE_VIDEO) {
                            showPreview();
                            hideGallery();
                        } else {
                            hidePreview();
                            showGallery();
                        }
                    }
                    mPagerSourcesRollingBack = false;
                }

                @Override
                public void onNothingSelected(EcoGalleryAdapterView<?> parent) {

                }
            });

            try {
                if(allowSourceCameraPhoto) {
                    mPagerSources.setSelection(items.indexOf(buttonSourcePhoto));
                } else if (allowSourceCameraVideo) {
                    mPagerSources.setSelection(items.indexOf(buttonSourceVideo));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void switchSourceButtons(Object viewTag) {
        if (viewTag == BUTTON_SOURCE_GALLERY) {
            buttonSourceGallery.setClickable(true);
            buttonSourceGallery.setEnabled(true);

            buttonSourcePhoto.setClickable(false);
            buttonSourcePhoto.setEnabled(false);

            buttonSourceVideo.setClickable(false);
            buttonSourceVideo.setEnabled(false);
        } else if (viewTag == BUTTON_SOURCE_PHOTO) {
            buttonSourceGallery.setClickable(false);
            buttonSourceGallery.setEnabled(false);

            buttonSourcePhoto.setClickable(true);
            buttonSourcePhoto.setEnabled(true);

            buttonSourceVideo.setClickable(false);
            buttonSourceVideo.setEnabled(false);
        } else if (viewTag == BUTTON_SOURCE_VIDEO) {
            buttonSourceGallery.setClickable(false);
            buttonSourceGallery.setEnabled(false);

            buttonSourcePhoto.setClickable(false);
            buttonSourcePhoto.setEnabled(false);

            buttonSourceVideo.setClickable(true);
            buttonSourceVideo.setEnabled(true);
        }
    }


    public boolean shouldStartScrolling() {
        if(isRecording) {
            return false;
        }
        return true;
    }


    protected void onClickProxy(ImageButton v) {
        int id = v.getId();
        String tag = (String) v.getTag();

        if(id == R.id.next)
            finishWithItems();
        else if(id == R.id.back)
            finishWithoutItems();
        else if(id == R.id.switchCamera)
            switchCameras();
        else if(id == R.id.flashOn || id == R.id.flashOff || id == R.id.flashAuto)
            flashSwitch();
        else if(tag.equals(BUTTON_SOURCE_PHOTO))
            capturePicture();
        else if(tag.equals(BUTTON_SOURCE_VIDEO))
            captureVideo();
        else if(id == R.id.cancel)
            dropVideosDialog();
    }
    //endregion

    //region Camera Switches

    protected void stopCamera() {
        if(mCameraPreviewSurface != null) {
            mCameraPreviewSurface.removeCallback();
            if(mPreviewHolder != null) {
                mPreviewHolder.removeView(mCameraPreviewSurface);
            }
            surfaceHolder = mCameraPreviewSurface.getHolder();
            if(surfaceHolder != null) {
                surfaceHolder = null;
            }
            mCameraPreviewSurface = null;
        }
        releaseCamera();
    }

    protected void switchCameras() {
        stopCamera();
        cameraId++;
        if(cameraId >= Camera.getNumberOfCameras()) {
            cameraId = 0;
        }
        startCamera();
    }

    protected void flashHide() {
        buttonFlashOn.setVisibility( View.GONE );
        buttonFlashAuto.setVisibility( View.GONE );
        buttonFlashOff.setVisibility(View.GONE);
    }

    private void flashSwitch() {
        currentFlashMode++;
        if(currentFlashMode == 3)
            currentFlashMode = 0;
        flashSet();
    }

    private void flashSet() {
        if(mCamera != null) {
            Camera.Parameters cameraParameters = mCamera.getParameters();
            List<String> flashModes = cameraParameters.getSupportedFlashModes();
            if(flashModes == null) {
                flashHide();
                return;
            }
            if(flashModes.size() == 1 && flashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
                flashHide();
                return;
            }

            try {
                if(currentFlashMode == 0) {
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                } else if(currentFlashMode == 1) {
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                } else if(currentFlashMode == 2) {
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                mCamera.setParameters(cameraParameters);

                buttonFlashOn.setVisibility( (currentFlashMode == 0) ? View.VISIBLE : View.GONE );
                buttonFlashAuto.setVisibility( (currentFlashMode == 1) ? View.VISIBLE : View.GONE );
                buttonFlashOff.setVisibility( (currentFlashMode == 2) ? View.VISIBLE : View.GONE );
            } catch (Exception e) {
                e.printStackTrace();
                flashSwitch();
            }
        }
    }
    //endregion

    //region Helper Methods

    private String getString(Bundle bundle, String key, String defaultValue){
        if(bundle != null)
            if(bundle.containsKey(key))
                return bundle.getString(key);
        return defaultValue;
    }

    protected void finishWithItems() {
        stopCamera();
        ArrayList<String> items = new ArrayList<String>();
        this.setResult(Activity.RESULT_OK, this.getIntent());
        String source_tag = (String) mPagerSources.getSelectedView().getTag();

        if(source_tag.equals(BUTTON_SOURCE_GALLERY)) {
            if(mTabHost.getCurrentTabTag().equals(TAB_IMAGE)) {
                this.getIntent().putExtra("source", "gallery_photo");
                for(ResultClass result : mPagerPhotosItems) {
                    items.add(result.getFile().getAbsolutePath());
                }
            } else if(mTabHost.getCurrentTabTag().equals(TAB_VIDEO)) {
                this.getIntent().putExtra("source", "gallery_video");
                items.add(galleryVideo.getAbsolutePath());
            }
        } else if(source_tag.equals(BUTTON_SOURCE_PHOTO)) {
            this.getIntent().putExtra("source", "camera_photo");
            for(ResultClass result : mPagerPhotosItems) {
                items.add(result.getFile().getAbsolutePath());
            }
        } else if(source_tag.equals(BUTTON_SOURCE_VIDEO)) {
            this.getIntent().putExtra("source", "camera_video");
            if(mVideosItems.size() > 1) {
                processVideo();
                return;
            } else if(mVideosItems.size() == 1) {
                items.add(mVideosItems.get(0).getFile().getAbsolutePath());
            }
        }
        this.getIntent().putStringArrayListExtra("items", items);
        this.finish();
    }


    protected void finishWithoutItems() {
        stopCamera();
        this.setResult(Activity.RESULT_CANCELED, this.getIntent());
        this.finish();
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPagerPhotosItems = savedInstanceState.getParcelableArrayList("mPagerPhotosItems");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("mPagerPhotosItems", mPagerPhotosItems);
    }

    private Bitmap rotateBitmap(Bitmap bitmap) {
        return ResultClass.rotatedBitmap(bitmap, self, Camera.CameraInfo.CAMERA_FACING_FRONT == cameraId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBackgroundOn(View v, int d) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackgroundDrawable( getResources().getDrawable(d) );
        } else {
            v.setBackground( getResources().getDrawable(d));
        }
    }

    void showNext() {
        buttonNext.setVisibility(View.VISIBLE);
    }

    void hideNext() {
        buttonNext.setVisibility(View.GONE);
    }

    void showPagerPhotos() {
        mCarouselPhotos.setVisibility(View.VISIBLE);
    }

    void hidePagerPhotos() {
        mCarouselPhotos.setVisibility(View.GONE);
    }

    void showGallery() {
        if(mTabHost != null)
            mTabHost.setVisibility(View.VISIBLE);
    }

    void hideGallery() {
        if(mTabHost != null)
            mTabHost.setVisibility(View.GONE);
    }

    void showPreview() {
        if(mPreviewHolder.getVisibility() != View.VISIBLE) {
            mPreviewHolder.setVisibility(View.VISIBLE);
            restartPreview();
        }
    }

    void hidePreview() {
        mPreviewHolder.setVisibility(View.GONE);
        stopCamera();
    }

    public static int getMaxVideoDuration() {
        return maxVideoDuration;
    }

    public void dropVideos(boolean shouldDelete) {
        if(videoFragment != null)
            videoFragment.clearSelection();

        if(!shouldSaveOnGallery && shouldDelete) {
            for(ResultFile resultFile : mVideosItems) {
                if(!resultFile.getFile().delete())
                    resultFile.getFile().deleteOnExit();
            }
        }

        galleryVideo = null;
        mVideosItems.clear();
        progressBar.setProgress(0);
        totalVideoDuration = 0;
        buttonCancel.setVisibility(View.GONE);
        hideNext();
    }

    protected void dropPhotos(boolean shouldDelete) {
        if(imageFragment != null)
            imageFragment.clearSelection();

        if(!shouldSaveOnGallery && shouldDelete) {
            for(ResultClass result : mPagerPhotosItems) {
                if(!result.getFile().delete())
                    result.getFile().deleteOnExit();
            }
        }

        mPagerPhotosItems.clear();
        pagerPhotosAdapter.notifyDataSetChanged();
        hidePagerPhotos();
        hideNext();
    }


    protected void processVideo() {
        progressDialog = ProgressDialog.show(this, "", stringProcessingVideos, true);
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                List<Track> videoTracks = new LinkedList<Track>();
                List<Track> audioTracks = new LinkedList<Track>();

                Movie[] inMovies = new Movie[mVideosItems.size()];
                for (int i = 0; i < mVideosItems.size(); i++) {
                    try {
                        inMovies[i] = MovieCreator.build(mVideosItems.get(i).getFile().getAbsolutePath());
                        for (Track t : inMovies[i].getTracks()) {
                            if (t.getHandler().equals("soun")) {
                                audioTracks.add(t);
                            }
                            if (t.getHandler().equals("vide")) {
                                videoTracks.add(t);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Movie result = new Movie();

                    if (audioTracks.size() > 0) {
                        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                    }
                    if (videoTracks.size() > 0) {
                        result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                    }

                    Container out = new DefaultMp4Builder().build(result);

                    File ret = getOutputMediaFile(MEDIA_TYPE_MERGED_VIDEO);

                    FileChannel fc = new RandomAccessFile(ret.getAbsolutePath(), "rw").getChannel();
                    out.writeContainer(fc);
                    fc.close();
                    logMessage("ProcessVideo RET" + ret.getAbsolutePath() + " >>> "  + ret.length());;
                    if(shouldSaveOnGallery) {
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(ret);
                        mediaScanIntent.setData(contentUri);
                        self.sendBroadcast(mediaScanIntent);
                    }
                    mVideosItems.add(new ResultFile(ret));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dropVideos(true);
                        progressDialog.dismiss();
                        finishWithItems();
                    }
                });
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    //endregion

    //region Adapters

    private class PhotosAdapter extends BaseAdapter {
        private Context context;
        private List<ResultClass> items;

        PhotosAdapter(Context context, List<ResultClass> items) {
            this.context = context;
            this.items = items;
        }

        public int getCount() {
            return (items != null) ? items.size() : 0;
        }

        public Object getItem(int position) {
            return items.get(position).scaled;
        }

        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if(convertView == null) {
                imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
                EcoGallery.LayoutParams layoutParams = new EcoGallery.LayoutParams(EcoGallery.LayoutParams.WRAP_CONTENT, EcoGallery.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(layoutParams);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(items.get(position).getScaled());

            return imageView;
        }

    }

    private class SourcesAdapter extends BaseAdapter {
        private Context context;
        private List<ImageButton> items;

        SourcesAdapter(Context context, List<ImageButton> items) {
            this.context = context;
            this.items = items;
            logMessage("Context" + this.context.getPackageName());; //Avoid "unused" message on IDE
        }

        public int getCount() {
            return (items != null) ? items.size() : 0;
        }

        public Object getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public View getView(int position, View convertView, ViewGroup parent) {
            if(items.size() == 0)
                return convertView;
            return items.get(position);
        }
    }

    //endregion

    //region Media Chooser


    @Override
    protected void setupHeaderUI() { }

    @Override
    protected void setHeaderTitle(int id_text, int id_image) { }

    @Override
    protected void setupContentView() {
        if(!allowSourceGalleryPhoto && !allowSourceGalleryVideo)
            setContentView(R.layout.activity_full_cam_no_gallery);
        else
            setContentView(R.layout.activity_full_cam);
    }

    @Override
    protected Class<? extends ImageFragment> getImageFragmentClass() {
        return ImageFragmentFC.class;
    }

    @Override
    protected Class<? extends VideoFragment> getVideoFragmentClass() {
        return VideoFragmentFC.class;
    }

    @Override
    protected void setupTabTitle(int i) {
        TabWidget tabWidget = mTabHost.getTabWidget();
        TextView textView = (TextView) tabWidget.getChildAt(i).findViewById(android.R.id.title);
        if(textView.getLayoutParams() instanceof RelativeLayout.LayoutParams){

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textView.getLayoutParams();
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.width  = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.alignWithParent = true;
            params.setMargins(0,0,0,0);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0,0,0,0);
            textView.setLayoutParams(params);
            textView.setBackgroundColor(Color.BLACK);

        }else if(textView.getLayoutParams() instanceof LinearLayout.LayoutParams){
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
            params.gravity = Gravity.CENTER;
            textView.setLayoutParams(params);
        }
        textView.setTextSize(convertDipToPixels(10));
    }

    @Override
    protected void changeTabTitleUnselected(int tabNumber) {
    }

    @Override
    protected void changeTabTitleSelected(int tabNumber) {
    }


    @Override
    public void onVideoSelected(int count){
        if(videoFragment == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            videoFragment = (VideoFragmentFC) fragmentManager.findFragmentByTag(TAB_VIDEO);
        }
        if(count > 0){
            galleryVideo = new File(videoFragment.currentMediaModel.url);
            showNext();
        }else{
            galleryVideo = null;
            hideNext();
        }
    }

    @Override
    public void onImageSelected(int count){
        if(imageFragment == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            imageFragment = (ImageFragmentFC) fragmentManager.findFragmentByTag(TAB_IMAGE);
        }

        int pagerCount = mPagerPhotosItems.size();
        if(count > 0 && count > pagerCount){
            mPagerPhotosItems.add(new ResultClass(imageFragment.getSelectedImageList().get(count - 1), self));
            pagerPhotosAdapterChanged();
        }else{
            for (Iterator<ResultClass> iterator = mPagerPhotosItems.iterator(); iterator.hasNext();) {
                ResultClass result = iterator.next();
                if (!imageFragment.getSelectedImageList().contains(result.getFile().getAbsolutePath())) {
                    iterator.remove();
                }
            }
            if(count == 0) {
                hideNext();
                mCarouselPhotos.setVisibility(View.GONE);
            }
        }
        pagerPhotosAdapter.notifyDataSetChanged();
    }

    @Override
    protected void setupTabHostChangedListener() {
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                if(mTabHost.getCurrentTabTag().equals(TAB_VIDEO)) {
                    if(mPagerPhotosItems.size() > 0) {
                        dropGalleryPhotosDialog();
                    }
                } else if(mTabHost.getCurrentTabTag().equals(TAB_IMAGE)) {
                    if(galleryVideo != null) {
                        dropGalleryVideoDialog(mPagerSources.getSelectedItemPosition());
                    }
                }
            }
        });
    }

    //endregion

    //region Sources Dialogs

    protected void dropGalleryPhotosDialog() {
        AlertDialog dialog = new AlertDialog.Builder(self).create();
        dialog.setMessage(stringDeleteAllPhotos);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, stringOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                dropPhotos(false);
                switchSourceButtons(mPagerSources.getSelectedView().getTag());
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, stringCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                mPagerSourcesRollingBack = true;
                mTabHost.setCurrentTabByTag(TAB_IMAGE);
                if(mPagerSources.getOldPosition() != EcoGalleryAdapterView.INVALID_POSITION)
                    mPagerSources.setSelection(mPagerSources.getOldPosition(), true);
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }

    protected void dropGalleryVideoDialog(final int oldPosition) {
        AlertDialog dialog = new AlertDialog.Builder(self).create();
        dialog.setMessage(stringDeleteVideo);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, stringOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                dropVideos(false);
//                switchSourceButtons(mPagerSources.getSelectedView().getTag());
                mPagerSources.getOnItemSelectedListener().onItemSelected(mPagerSources, mPagerSources.getSelectedView(), mPagerSources.getSelectedItemPosition(), mPagerSources.getSelectedItemId());
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, stringCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                mPagerSourcesRollingBack = true;
                mTabHost.setCurrentTabByTag(TAB_VIDEO);
                if(oldPosition != EcoGalleryAdapterView.INVALID_POSITION)
                    mPagerSources.setSelection(oldPosition, true);
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }

    protected void dropAllPhotosDialog() {
        AlertDialog dialog = new AlertDialog.Builder(self).create();
        dialog.setMessage(stringDeleteAllPhotos);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, stringOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                dropPhotos(true);
                switchSourceButtons(mPagerSources.getSelectedView().getTag());
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, stringCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                mPagerSourcesRollingBack = true;
                if(mPagerSources.getOldPosition() != EcoGalleryAdapterView.INVALID_POSITION)
                    mPagerSources.setSelection(mPagerSources.getOldPosition(), true);
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }

    protected void dropVideosDialog() {
        AlertDialog dialog = new AlertDialog.Builder(self).create();
        dialog.setMessage(stringDeleteVideo);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, stringOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                dropVideos(true);
                switchSourceButtons(mPagerSources.getSelectedView().getTag());
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, stringCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                mPagerSourcesRollingBack = true;
                if(mPagerSources.getOldPosition() != EcoGalleryAdapterView.INVALID_POSITION)
                    mPagerSources.setSelection(mPagerSources.getOldPosition(), true);
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }


    protected void dropAllVideosDialog() {
        AlertDialog dialog = new AlertDialog.Builder(self).create();
        dialog.setMessage(stringDeleteAllVideos);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, stringOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                dropVideos(true);
                switchSourceButtons(mPagerSources.getSelectedView().getTag());
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, stringCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
                mPagerSourcesRollingBack = true;
                if(mPagerSources.getOldPosition() != EcoGalleryAdapterView.INVALID_POSITION)
                    mPagerSources.setSelection(mPagerSources.getOldPosition(), true);
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }

    //endregion

    //region Camera Implementation based on Google's Code
    //Reference:
    // http://developer.android.com/guide/topics/media/camera.html#preview-layout

    public void restartCamera() {
        stopCamera();
        if(mCamera == null) {
            startCamera();
            logMessage("Start Preview (NEW)");;
        } else {
            logMessage("Start Preview (Restart)");;
            mCamera.startPreview();
        }
    }

    public void restartPreview() {
        try {
            if(mCamera != null) {
                mCamera.startPreview();
            } else {
                startCamera();
            }
        } catch (Exception e) {
            logMessage("Failed to restart preview");
            e.printStackTrace();
            restartCamera();
        }
    }

    public void startCamera() {
        logMessage("startCamera");
        int numberOfCameras = Camera.getNumberOfCameras();
        if(numberOfCameras > 0) {
            if(numberOfCameras > 1) {
                buttonSwitchCamera.setVisibility(View.VISIBLE);
            }
            if(mCamera == null) {
                mCamera = getCameraInstance();
                mCamera.setErrorCallback(new Camera.ErrorCallback() {
                    @Override
                    public void onError(int error, Camera camera) {
                        logMessage("onError");
                    }
                });
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        //logMessage("onPreviewFrame");
                    }
                });
            }
//
            mCameraPreviewSurface = new CameraPreview(this, mCamera, 0, 0); //mPreviewHolder.getHeight(), mPreviewHolder.getHeight()); //
            RelativeLayout.LayoutParams layoutParamsForPreviewSurface = (RelativeLayout.LayoutParams) mCameraPreviewSurface.getLayoutParams();
            if(layoutParamsForPreviewSurface == null)
                layoutParamsForPreviewSurface = new RelativeLayout.LayoutParams(mCamera.getParameters().getPreviewSize().width, mCamera.getParameters().getPreviewSize().height);

            layoutParamsForPreviewSurface.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParamsForPreviewSurface.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParamsForPreviewSurface.setMargins(0, 0, 0, 0);
            mCameraPreviewSurface.setLayoutParams(layoutParamsForPreviewSurface);
            mPreviewHolder.addView(mCameraPreviewSurface, 0);
            currentFlashMode = 1; //AUTO
            flashSet();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        stopCamera();
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_MERGED_VIDEO = 3;

    /** Create a file Uri for saving an image or video *
     private static Uri getOutputMediaFileUri(int type){
     return Uri.fromFile(getOutputMediaFile(type));
     }*/

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), stringAppFolder);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                logMessage("MyCameraApp" + "failed to create directory");;
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else if(type == MEDIA_TYPE_MERGED_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "MERGED_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            logMessage("Camera to open: " + cameraId);;
            c = Camera.open(cameraId); // attempt to get a Camera instance
        }
        catch (Exception e){
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder(){

        if(mCamera == null)
            mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.reset();

        // Step 2: Set sources

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //TODO define quality
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        // Step 4: Set output file
        tempVideoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(tempVideoFile.toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mCameraPreviewSurface.getHolder().getSurface());
        mMediaRecorder.setMaxDuration(maxVideoDuration * 1000);
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    captureVideo();
                }
            }
        });

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            logMessage(TAG + "IllegalStateException preparing MediaRecorder: " + e.getMessage());;
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            logMessage(TAG + "IOException preparing MediaRecorder: " + e.getMessage());;
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {

            final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                logMessage(TAG + "Error creating media file...");;
                return;
            }

            restartPreview();
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap bitmap;
                        Bitmap originalBitmap = rotateBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));

                        mPagerPhotosItems.add(new ResultClass(originalBitmap, pictureFile, self));

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCarouselPhotos.setVisibility(View.VISIBLE);
                                pagerPhotosAdapterChanged();
                            }
                        });

                        if(imageBox > 0) {
                            int dstW = (originalBitmap.getHeight() < originalBitmap.getWidth()) ? imageBox : originalBitmap.getWidth() * imageBox / originalBitmap.getHeight();
                            int dstH = (originalBitmap.getHeight() > originalBitmap.getWidth()) ? imageBox : originalBitmap.getHeight() * imageBox / originalBitmap.getWidth();
                            originalBitmap = Bitmap.createScaledBitmap(originalBitmap, dstW, dstH, true);
                        }

                        //Rotates the bitmap inside the ResultClass and here. The goal is to place the thumb as soon as possible
//                        bitmap = originalBitmap);
//                        originalBitmap.recycle();

                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        originalBitmap.compress(Bitmap.CompressFormat.JPEG, imageCompression, fos);
                        fos.flush();
                        fos.close();
                        originalBitmap.recycle();
                        originalBitmap = null;

                        if(shouldSaveOnGallery) {
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(pictureFile);
                            mediaScanIntent.setData(contentUri);
                            self.sendBroadcast(mediaScanIntent);
                        }
                        logMessage(TAG + "Photo Done");;
                    } catch (FileNotFoundException e) {
                        logMessage(TAG + "File not found: " + e.getMessage());;
                    } catch (IOException e) {
                        logMessage(TAG + "Error accessing file: " + e.getMessage());;
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
    };

    protected void pagerPhotosAdapterChanged() {
        pagerPhotosAdapter.notifyDataSetChanged();
        mCarouselPhotos.setSelection(mPagerPhotosItems.size() / 2, true);
        mCarouselPhotos.setVisibility(View.VISIBLE);
        if(mPagerPhotosItems.size() > 0)
            showNext();
        else
            hideNext();
    }


    public void capturePicture() {
        // get an image from the camera
        if(maxPhotoCount > 0 && mPagerPhotosItems.size() == maxPhotoCount) {
            triedToExceedPictureLimit();
        } else {
            if(mCamera != null) {
                mCamera.takePicture(null, null, mPictureCallback);
            } else {
                restartCamera();
            }
        }
    }

    public void triedToExceedPictureLimit() {
        AlertDialog dialog = new AlertDialog.Builder(self).create();
        dialog.setMessage(stringMaxPhotos.replace("X", "" + maxPhotoCount));
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, stringCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }

    public void captureVideo() {
        if (isRecording) {
            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (Exception e) {
                logMessage("CaptureVideo" + e.getMessage());;
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            progressBar.clearAnimation();
            if(shouldSaveOnGallery) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(tempVideoFile);
                mediaScanIntent.setData(contentUri);
                self.sendBroadcast(mediaScanIntent);
            }

            addVideo(tempVideoFile);

            // inform the user that recording has stopped
            isRecording = false;
        } else {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                // inform the user that recording has started
                isRecording = true;
                progressBar.setVisibility(View.VISIBLE);
                int availableDuration = (maxVideoDuration - totalVideoDuration) * 1000;
                if(mVideosItems.size() == 0) {
                    progressBar.setProgress(0);
                    totalVideoDuration = 0;
                }
                progressAnimation = new ProgressBarAnimation(progressBar, progressBar.getProgress(), 100);
                progressAnimation.setDuration(availableDuration);
                progressBar.startAnimation(progressAnimation);
            } else {
                releaseMediaRecorder();
            }
        }
    }

    protected void addVideo(File file) {
        try {
            buttonCancel.setVisibility(View.VISIBLE);
            showNext();

            MediaPlayer mp = new MediaPlayer();
            FileInputStream stream;
            stream = new FileInputStream(file);
            mp.setDataSource(stream.getFD());
            stream.close();
            mp.prepare();
            long duration = mp.getDuration();
            mp.release();
            totalVideoDuration += (duration / 1000);

            mVideosItems.add(new ResultFile(file));
            tempVideoFile = null;
            mCarouselPhotos.setVisibility(View.GONE);
            if(mVideosItems.size() > 0) {
                buttonCancel.setVisibility(View.VISIBLE);
                showNext();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mVideosItems.clear();
        tempVideoFile = null;
        buttonCancel.setVisibility(View.GONE);
        hideNext();
    }

    //endregion

    static void logMessage(String message) {
        Log.d("FZFullCamera", message);
    }

}
