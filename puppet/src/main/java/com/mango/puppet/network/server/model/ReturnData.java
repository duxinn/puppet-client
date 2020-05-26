package com.mango.puppet.network.server.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;

public class ReturnData implements Parcelable {

    @JSONField(name = "errorCode")
    private int errorCode;

    @JSONField(name = "errorMsg")
    private String errorMsg;

    @JSONField(name = "data")
    private Object data;

    @JSONField(name = "status")
    private int status;

    public ReturnData() {
    }

    protected ReturnData(Parcel in) {
        errorCode = in.readInt();
        errorMsg = in.readString();
        status = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(errorCode);
        dest.writeString(errorMsg);
        dest.writeInt(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReturnData> CREATOR = new Creator<ReturnData>() {
        @Override
        public ReturnData createFromParcel(Parcel in) {
            return new ReturnData(in);
        }

        @Override
        public ReturnData[] newArray(int size) {
            return new ReturnData[size];
        }
    };

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}