package com.mango.puppet.dispatch.business;

import android.content.Context;

import com.mango.puppet.dispatch.business.i.IBusiness;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.plugin.UploadChannelInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * BusinessManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class BusinessManager implements IBusiness {
    private static final BusinessManager ourInstance = new BusinessManager();
    private Context mContext;

    public static BusinessManager getInstance() {
        return ourInstance;
    }

    private BusinessManager() {
    }

    public void init(Context context){
        mContext = context;
    }
    /************   IBusiness   ************/
    @Override
    public void getUploadResourceWay(ArrayList<String> supportChannels, final IUploadResourceWayResult mResult) {
        LogManager.getInstance().recordDebugLog("获取资源文件上传方式");
        NetworkManager.getInstance().requestUploadResourceWay(supportChannels, new INetwork.IRequestResult() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof List) {
                    if (((List) result).size() > 0 && ((List) result).get(0) instanceof UploadChannelInfo) {
                        mResult.onSuccess((List) result);
                    }
                } else {
                    mResult.onError();
                    LogManager.getInstance().recordLog("请求资源时路径时，返回格式错误");
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                if (mResult != null) {
                    mResult.onError();
                    LogManager.getInstance().recordLog("请求资源时路径时，返回资源错误");
                }
            }

            @Override
            public void onNetworkError() {
                LogManager.getInstance().recordLog("请求资源时路径时，网络连接失败");
            }
        });
    }
}
