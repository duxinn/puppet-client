package com.mango.puppet.network;

import android.content.Context;

import com.mango.puppet.network.i.INetwork;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.Job;

/**
 * NetworkManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class NetworkManager implements INetwork {
    private static final NetworkManager ourInstance = new NetworkManager();

    public static NetworkManager getInstance() {
        return ourInstance;
    }

    private NetworkManager() {
    }

    /************   INetwork   ************/
    @Override
    public void setupNetwork(Context context, ISetupResult result) {

    }

    @Override
    public void reportJobResult(Job jobResult, IJobRequestResult requestResult) {

    }

    @Override
    public void reportEvent(Event event, IEventRequestResult requestResult) {

    }

    @Override
    public void requestUploadResourceWay(IRequestResult requestResult) {

    }
}
