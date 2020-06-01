package com.mango.puppet.network.server.component;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.mango.puppet.network.server.model.ReturnData;
import com.mango.puppet.network.utils.JsonUtils;
import com.yanzhenjie.andserver.annotation.Resolver;
import com.yanzhenjie.andserver.error.BasicException;
import com.yanzhenjie.andserver.framework.ExceptionResolver;
import com.yanzhenjie.andserver.framework.body.JsonBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.StatusCode;

@Resolver
public class AppExceptionResolver implements ExceptionResolver {

    @Override
    public void onResolve(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull Throwable e) {
        e.printStackTrace();
        if (e instanceof BasicException) {
            BasicException exception = (BasicException) e;
            response.setStatus(exception.getStatusCode());
        } else {
            response.setStatus(StatusCode.SC_INTERNAL_SERVER_ERROR);
        }
        ReturnData returnData = new ReturnData();
        returnData.status = response.getStatus();
        returnData.message = e.getMessage();
        ResponseBody body = new JsonBody(JSON.toJSONString(returnData));
        response.setBody(body);
    }
}