package com.two.channelmyanmar.updater;

/*
 * Created by Toewaioo on 4/20/25
 * Description: [Add class description here]
 */
// UpdateInfo.java
import android.os.Parcel;
import android.os.Parcelable;

public class UpdateInfo implements Parcelable {
    public final String whatNew;
    public final String version;
    public final String downloadLink;
    public final String apiKey;
    public final String message;
    public final boolean block;

    public UpdateInfo(String whatNew, String version, String downloadLink,
                      String apiKey, String message, boolean block) {
        this.whatNew = whatNew;
        this.version = version;
        this.downloadLink = downloadLink;
        this.apiKey = apiKey;
        this.message = message;
        this.block = block;
    }

    protected UpdateInfo(Parcel in) {
        whatNew = in.readString();
        version = in.readString();
        downloadLink = in.readString();
        apiKey = in.readString();
        message = in.readString();
        block = in.readByte() != 0;
    }

    public static final Creator<UpdateInfo> CREATOR = new Creator<UpdateInfo>() {
        @Override
        public UpdateInfo createFromParcel(Parcel in) {
            return new UpdateInfo(in);
        }

        @Override
        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(whatNew);
        dest.writeString(version);
        dest.writeString(downloadLink);
        dest.writeString(apiKey);
        dest.writeString(message);
        dest.writeByte((byte) (block ? 1 : 0));
    }
}