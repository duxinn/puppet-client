package com.mango.puppet.plugin;

import android.text.TextUtils;

import com.mango.puppet.dispatch.business.BusinessManager;
import com.mango.puppet.dispatch.business.i.IBusiness;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
import com.qiniu.android.common.AutoZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hehongzhen on 2018/8/1.
 */

public class UploadImageUtils {

    private static QinNiuInfo mQinNiuInfo = null;
    private static Configuration mQiNiuConfig = null;

    public static void uploadImage(final int requestCode,
                                   final String identifier,
                                   final String path,
                                   final UploadImageCallBack callBack) {
        if (mQinNiuInfo != null && !TextUtils.isEmpty(mQinNiuInfo.getToken())) {
            tryUpload(requestCode, identifier, path, callBack, 0);
        } else {
            getQinNiuToken(new GetQinNiuCallBack() {
                @Override
                public void onSuccess(QinNiuInfo token) {
                    tryUpload(requestCode, identifier, path, callBack, 0);
                }

                @Override
                public void onError(int errorCode, String message) {
                    if (null != callBack) {
                        callBack.uploadImageFailed(requestCode, identifier);
                    }
                }
            });
        }
    }

    public static void getQinNiuToken(final GetQinNiuCallBack callBack) {
        ArrayList<String> channels = new ArrayList<>();
        channels.add("qiniu");
        BusinessManager.getInstance().getUploadResourceWay(channels, new IBusiness.IUploadResourceWayResult() {
            @Override
            public void onSuccess(List<UploadChannelInfo> modelList) {
                for (int i = 0; i < modelList.size(); i++) {
                    if ("qiniu".equals(modelList.get(i).getChannel())) {
                        mQinNiuInfo = modelList.get(i).getParameter();
                        callBack.onSuccess(mQinNiuInfo);
                        return;
                    }
                }
                callBack.onError(2, "未获取到上传参数");
            }

            @Override
            public void onError() {
                callBack.onError(1, "网络不给力，请稍后再试");
            }
        });
    }

    private static void tryUpload(final int requestCode,
                                  final String identifier,
                                  final String path,
                                  final UploadImageCallBack callBack,
                                  final int tryCount) {
        if (mQinNiuInfo == null || TextUtils.isEmpty(mQinNiuInfo.getToken())) {
            if (null != callBack) {
                callBack.uploadImageFailed(requestCode, identifier);
            }
            return;
        }
        final int nextTryCount = tryCount + 1;
        UploadManager uploadManager = new UploadManager(getmQiNiuConfig());
        uploadManager.put(path, identifier, mQinNiuInfo.getToken(), new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                if (null != callBack) {
                    if (info.isOK()) {
                        callBack.uploadImageSuccess(requestCode,
                                identifier,
                                appendPathComponent(mQinNiuInfo.getDomain(), identifier));
                    } else {
                        if (nextTryCount < 3) {
                            getQinNiuToken(new GetQinNiuCallBack() {
                                @Override
                                public void onSuccess(QinNiuInfo token) {
                                    tryUpload(requestCode, identifier, path, callBack, nextTryCount);
                                }

                                @Override
                                public void onError(int errorCode, String message) {
                                    if (null != callBack) {
                                        callBack.uploadImageFailed(requestCode, identifier);
                                    }
                                }
                            });
                        } else {
                            callBack.uploadImageFailed(requestCode, identifier);
                        }
                    }
                }
            }
        }, new UploadOptions(null, null, false, new UpProgressHandler() {
            @Override
            public void progress(String key, double percent) {
                percent = percent * 100;
                if (null != callBack) {
                    callBack.uploadImageProgress(requestCode, identifier, percent);
                }
            }
        }, null));
    }

    private static Configuration getmQiNiuConfig() {
        if (null == mQiNiuConfig) {
            mQiNiuConfig = new Configuration.Builder().
                    zone(AutoZone.autoZone).
                    connectTimeout(10).
                    useHttps(true).
                    build();
        }
        return mQiNiuConfig;
    }


    public interface UploadImageCallBack {

        void uploadImageSuccess(int requestCode, String identifier, String urlPath);

        void uploadImageFailed(int requestCode, String identifier);

        void uploadImageProgress(int requestCode, String identifier, double percent);
    }


    interface GetQinNiuCallBack {
        void onSuccess(QinNiuInfo token);

        void onError(int errorCode, String message);
    }

    /**
     * append file path
     *
     * @param foreString foreString
     * @param tailString tailString
     * @return total string
     */
    public static String appendPathComponent(String foreString, String tailString) {
        if (foreString == null) {
            return tailString;
        }
        if (tailString == null) {
            return foreString;
        }
        String fore = foreString;
        if (fore.length() > 0 && fore.lastIndexOf(File.separator) + 1 == fore.length()) {
            fore = fore.substring(0, fore.length() - File.separator.length());
        }
        String tail = tailString;
        if (tail.length() > 0 && tail.startsWith(File.separator)) {
            tail = tail.substring(File.separator.length());
        }
        return fore + File.separator + tail;
    }
}
