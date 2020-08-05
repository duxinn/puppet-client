package com.mango.puppet.tool;

import android.widget.TextView;

import com.mango.puppet.bean.NormalConst;

/**
 * 用于记录最后的文字状态
 */
public class TextTool {

    public static void resetAllLog() {
        PreferenceUtils.getInstance().setString(NormalConst.TYPE_EVENT_WATCHER, "");
        PreferenceUtils.getInstance().setString(NormalConst.TYPE_JOB, "");
        PreferenceUtils.getInstance().setString(NormalConst.TYPE_JOB_ENGINE_STATUS, "");
        PreferenceUtils.getInstance().setString(NormalConst.TYPE_JOB_RESULT, "");
        PreferenceUtils.getInstance().setString(NormalConst.TYPE_LOG, "");
        PreferenceUtils.getInstance().setString(NormalConst.TYPE_NET, "");
        PreferenceUtils.getInstance().setString(NormalConst.TYPE_PLUGIN_RUNNING, "");
    }

    public static void setText(TextView tvLog, TextView tvNet, TextView tvJobEngineStatus,
                               TextView tvEventWatcher, TextView tvLocalStatus,
                               TextView tvJobCount, TextView tvJobResultCount) {
        if (!getText(NormalConst.TYPE_LOG).equals("")){
            tvLog.setText(getText(NormalConst.TYPE_LOG));
            tvNet.setText(getText(NormalConst.TYPE_NET));
            tvJobEngineStatus.setText(getText(NormalConst.TYPE_JOB_ENGINE_STATUS));
            tvEventWatcher.setText(getText(NormalConst.TYPE_EVENT_WATCHER));
            tvLocalStatus.setText(getText(NormalConst.TYPE_PLUGIN_RUNNING));
            tvJobCount.setText(getText(NormalConst.TYPE_JOB));
            tvJobResultCount.setText(getText(NormalConst.TYPE_JOB_RESULT));
        }
    }

    public static void statusChange(String key, String value) {
        PreferenceUtils.getInstance().setString(key, value);
    }

    private static String getText(String key) {
        return PreferenceUtils.getInstance().getString(key);
    }
}
