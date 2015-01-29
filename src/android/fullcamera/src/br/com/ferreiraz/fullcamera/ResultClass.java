package br.com.ferreiraz.fullcamera;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;

public class ResultClass implements Parcelable {
    Bitmap scaled;
    File file;

    public ResultClass(Bitmap bitmap, File file, Context context) {
        int w, h;

        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = 100 * (metrics.densityDpi / 160);

        if(bitmap.getWidth() > bitmap.getHeight()) {
            w = px;
            h = bitmap.getHeight() * px / bitmap.getWidth();
        } else {
            h = px;
            w = bitmap.getWidth() * px / bitmap.getHeight();
        }

        this.scaled = rotatedBitmap(Bitmap.createScaledBitmap(bitmap, w, h, true), context);
        this.file = file;
    }

    public ResultClass(Parcel in) {
        file = new File(in.readString());
        scaled = Bitmap.CREATOR.createFromParcel(in);
    }

    public static final Parcelable.Creator<ResultClass> CREATOR
            = new Parcelable.Creator<ResultClass>() {
        public ResultClass createFromParcel(Parcel in) {
            return new ResultClass(in);
        }

        public ResultClass[] newArray(int size) {
            return new ResultClass[size];
        }
    };

    public File getFile() {
        return file;
    }

    public Bitmap getScaled() {
        return scaled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(file.getAbsolutePath());
        scaled.writeToParcel(dest, flags);
    }

    public static Bitmap rotatedBitmap(Bitmap bitmap, Context context) {
        Matrix matrix = new Matrix();
        int rotate = 90;
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_90:
                rotate = 0;
                break;
            case Surface.ROTATION_270:
                rotate = 180;
                break;
        }

        matrix.postRotate(rotate);

        return Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}