package br.com.ferreiraz.fullcamera;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class ResultFile implements Parcelable {
    File file;

    public ResultFile(File file) {
        this.file = file;
    }

    public ResultFile(Parcel in) {
        file = new File(in.readString());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(file.getAbsolutePath());
    }
}