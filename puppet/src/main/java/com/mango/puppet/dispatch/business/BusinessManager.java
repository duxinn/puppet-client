package com.mango.puppet.dispatch.business;

import com.mango.puppet.dispatch.business.i.IBusiness;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppetmodel.UploadResourceModel;
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

    public static BusinessManager getInstance() {
        return ourInstance;
    }

    private BusinessManager() {
    }

    /************   IBusiness   ************/
    @Override
    public void getUploadResourceWay(final IUploadResourceWayResult mResult) {
        NetworkManager.getInstance().requestUploadResourceWay(new INetwork.IRequestResult() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof List){
                    if (((List) result).size()>0&&((List) result).get(0) instanceof UploadResourceModel){
                        mResult.onSuccess((List)result);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                if (mResult != null){
                    mResult.onError();
                    LogManager.getInstance().recordLog("请求资源时路径时，网络连接失败");
                }
            }

            @Override
            public void onNetworkError() {
                LogManager.getInstance().recordLog("请求资源时路径时，网络连接失败");
            }
        });

    }
}
