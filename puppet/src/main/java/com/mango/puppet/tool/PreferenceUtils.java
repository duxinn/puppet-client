package com.mango.puppet.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtils {
    public static PreferenceUtils sInstance = new PreferenceUtils();
    protected SharedPreferences mPref;
    protected SharedPreferences.Editor mEditor;
    public Context mContext;

    public void init(Context context) {
        mContext = context;
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPref.edit();
    }

    public SharedPreferences pref() {
        return mPref;
    }

    public SharedPreferences.Editor editor() {
        return mEditor;
    }


    private PreferenceUtils() {
    }

    public static PreferenceUtils getInstance() {
        return sInstance;
    }

    private void checkPref(Context context) {
        if (null == mPref) {
            mPref = PreferenceManager.getDefaultSharedPreferences(context);
            mEditor = mPref.edit();
        }
    }

    // ===== boolean
    public boolean getBoolean(Context context, String keyName) {
        checkPref(context);
        return getBoolean(keyName);
    }

    public boolean getBoolean(String keyName) {
        return mPref.getBoolean(keyName, false);
    }

    public boolean getBoolean(String keyName, boolean defaultValue) {
        return mPref.getBoolean(keyName, defaultValue);
    }


    public void setBoolean(Context context, String keyName, boolean value) {
        checkPref(context);
        setBoolean(keyName, value);
    }

    public void setBoolean(String keyName, boolean value) {
        mEditor.putBoolean(keyName, value).commit();
    }

    // ===== int
    public int getInt(Context context, String keyName) {
        checkPref(context);
        return getInt(keyName);
    }

    public int getInt(String keyName) {
        return mPref.getInt(keyName, 0);
    }

    public int getInt(String keyName, int defaultValue) {
        return mPref.getInt(keyName, defaultValue);
    }

    public void setInt(Context context, String keyName, int value) {
        checkPref(context);
        setInt(keyName, value);
    }

    public void setInt(String keyName, int value) {
        mEditor.putInt(keyName, value).commit();
    }


    public long getLong(String keyName) {
        return mPref.getLong(keyName, 0);
    }

    public long getLong(String keyName, long defaultValue) {
        return mPref.getLong(keyName, defaultValue);
    }

    public void setLong(Context context, String keyName, long value) {
        checkPref(context);
        setLong(keyName, value);
    }

    public void setLong(String keyName, long value) {
        mEditor.putLong(keyName, value).commit();
    }

    // ===== float
    public float getFloat(Context context, String keyName) {
        checkPref(context);
        return getFloat(keyName);
    }

    public float getFloat(String keyName) {
        return mPref.getInt(keyName, 0);
    }

    public float getFloat(String keyName, float defaultValue) {
        return mPref.getFloat(keyName, defaultValue);
    }

    public void setFloat(Context context, String keyName, float value) {
        checkPref(context);
        setFloat(keyName, value);
    }

    public void setFloat(String keyName, float value) {
        mEditor.putFloat(keyName, value).commit();
    }

    // ===== string
    public String getString(Context context, String keyName) {
        checkPref(context);
        return getString(keyName);
    }

    public String getString(String keyName) {
        return mPref.getString(keyName, "");
    }

    public String getString(String keyName, String defaultValue) {
        return mPref.getString(keyName, defaultValue);
    }

    public void setString(String keyName, String value) {
        mEditor.putString(keyName, value).commit();
    }

    public void removeProperty(String keyName) {
        mEditor.remove(keyName).commit();
    }


    /**
     * Comment notification
     **/
    public boolean isSubscribedCommentNotification() {
        return getBoolean("push_live_msg", true);
    }

    public void setSubscribeCommentNotification(boolean subscribed) {
        setBoolean("push_live_msg", subscribed);
    }

    /**
     * Content notification
     **/
    public boolean isSubscribedContentNotification() {
        return getBoolean("push_content_msg", true);
    }

    public void setSubscribedContentNotification(boolean subscribed) {
        setBoolean("push_content_msg", subscribed);
    }

    /**
     * 计算是否是第N次打开APP
     */
    public static boolean isStartAPPCount(Context context, String flagName, int count) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int isFirst = sharedPreferences.getInt(flagName, 0);
        sharedPreferences.edit().putInt(flagName, isFirst + 1).commit();
        if (isFirst >= count) {
            return true;
        } else {
            return false;
        }

    }
}
