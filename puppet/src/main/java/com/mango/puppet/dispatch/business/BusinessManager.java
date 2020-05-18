package com.mango.puppet.dispatch.business;

import com.mango.puppet.dispatch.business.i.IBusiness;

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
    public void getUploadResourceWay(IUploadResourceWayResult result) {

    }
}
