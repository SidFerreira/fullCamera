package br.com.ferreiraz.fullCameraLauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import br.com.ferreiraz.fullcamera.FullCameraActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class FullCameraLauncher extends CordovaPlugin  {

    public static final String GET_ACTION = "get";
    public static final String GALLERY_PHOTO = "GalleryPhoto";
    public static final String GALLERY_VIDEO = "GalleryVideo";
    public static final String CAMERA_PHOTO  = "CameraPhoto";
    public static final String CAMERA_VIDEO  = "CameraVideo";

    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;

        if (GET_ACTION.equals(action)) {
            Context context = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(context, FullCameraActivity.class);

            intent.putExtra("shouldSaveOnGallery", args.getBoolean(0));
            intent.putExtra("imageBox", args.getInt(1));
            intent.putExtra("imageCompression", args.getInt(2));

            intent.putExtra("maxVideoDuration", args.getInt(3));
            intent.putExtra("maxPhotoCount", args.getInt(4));
            intent.putExtra("minPhotoCount", args.getInt(5));

            intent.putExtra("stringOk", args.getString(6));
            intent.putExtra("stringCancel", args.getString(7));
            intent.putExtra("stringMaxPhotos", args.getString(8));
            intent.putExtra("stringDeletePhoto", args.getString(9));
            intent.putExtra("stringDeleteAllPhotos", args.getString(10));
            intent.putExtra("stringDeleteVideo", args.getString(11));
            intent.putExtra("stringDeleteAllVideos", args.getString(12));
            intent.putExtra("stringProcessingVideos", args.getString(13));
            intent.putExtra("stringAppFolder", args.getString(14));

            String sourcesAvailable = args.getString(15);
            intent.putExtra("allowSourceGalleryPhoto", sourcesAvailable.contains(GALLERY_PHOTO));
            intent.putExtra("allowSourceGalleryVideo", sourcesAvailable.contains(GALLERY_VIDEO));
            intent.putExtra("allowSourceCameraPhoto",  sourcesAvailable.contains(CAMERA_PHOTO));
            intent.putExtra("allowSourceCameraVideo",  sourcesAvailable.contains(CAMERA_VIDEO));

            this.cordova.startActivityForResult((CordovaPlugin) this, intent, 418);

            return true;

        }

        return false;

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 418) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("Activity", "RESULT_OK");
                JSONObject obj = new JSONObject();
                try {
                    JSONArray objItems = new JSONArray();
                    
                    ArrayList<String> items = intent.getStringArrayListExtra("items");
                    
                    for(String item : items) {
                        JSONObject objItem = new JSONObject();
                        objItem.put("path", item);
                        File file = new File(item);
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[(int) file.length()];

                        fis.read(buffer);
                        fis.close();
                        fis = null;

                        objItem.put("data", Base64.encodeToString(buffer, Base64.DEFAULT));
                        objItems.put(objItem);
                        objItems.put("source", "" + intent.getStringExtra("source"));
                    }

                    obj.put("items", objItems);

                } catch (JSONException e) {
                    Log.d("...", "JSONException!");
                    e.printStackTrace();

                } catch (Exception e) {
                    Log.d("...", "Exception!");
                    e.printStackTrace();
                } finally {
                    // System.gc();
                }
                this.callbackContext.success(obj);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.callbackContext.error("CANCELED");
            }
        }
    }
}
