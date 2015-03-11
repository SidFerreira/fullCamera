package br.com.ferreiraz.fullcamera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.*;

import java.io.IOException;
import java.util.List;

// http://developer.android.com/guide/topics/media/camera.html#preview-layout
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private static final String TAG = "FULL_CAM_PREVIEW";

    public CameraPreview(Context context, Camera camera, int width, int height) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        if(width > 0 && height > 0) {
//            mHolder.setFixedSize(width, height);
        }

        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void removeCallback() {
        mHolder.removeCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        Log.d(TAG, "surfaceDestroyed");
        try {
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

//        Log.d("TARGET", "" + w + "x" + h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            Log.d(TAG, "Size: " + size.width + "x" + size.height + " Ratio: " + ratio + "/" + targetRatio);
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int pixelFormat, int holderWidth, int holderHeight) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d(TAG, "surfaceChanged " + holderWidth + "x" + holderHeight);
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
            Log.d(TAG, "Error stoping camera preview: " + e.getMessage());
            e.printStackTrace();
        }

        Camera.Parameters cameraParameters = mCamera.getParameters();

        boolean isPortrait = false;
        final int rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_90:
                mCamera.setDisplayOrientation(0);
                cameraParameters.setRotation(0);
                break;
            case Surface.ROTATION_270:
                mCamera.setDisplayOrientation(180);
                cameraParameters.setRotation(180);
                break;
            default:
                isPortrait = true;
                mCamera.setDisplayOrientation(90);
                cameraParameters.setRotation(90);
/*                int t = holderWidth;
                holderWidth = holderHeight;
                holderHeight = t;*/
                break;
        }

        if (cameraParameters.getSupportedPictureSizes() != null) {
            List<Size> sizes = cameraParameters.getSupportedPictureSizes();
            Size smaller = getOptimalPreviewSize(sizes, holderWidth, holderHeight);

/*            float relationHeight = (float) largest.height / holderHeight;
            float relationWidth  = (float) largest.width  / w;
            float relation       = (relationHeight > relationWidth) ? relationHeight : relationWidth;
            int previewHeight = (int) Math.floor(largest.height / relation);
            int previewWidth  = (int) Math.floor(largest.width  / relation);*/
            Log.d(TAG, "Choosen size:" + smaller.width + "x" + smaller.height);
//            if(isPortrait)
//                cameraParameters.setPreviewSize(smaller.height, smaller.width);
//            else
//                cameraParameters.setPreviewSize(smaller.width, smaller.height);
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
//            mCamera.setParameters(cameraParameters);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            e.printStackTrace();
        }
    }
}