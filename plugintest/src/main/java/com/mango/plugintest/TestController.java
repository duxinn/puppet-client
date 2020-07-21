package com.mango.plugintest;

import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.mango.puppet.network.server.model.ReturnData;
import com.mango.puppet.network.utils.JsonUtils;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.JsonBody;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mango.plugintest.RequestHandler.RequestHandlerAction;


@RestController
@RequestMapping(path = "/test")
class TestController {

    /**
     * 接收任务结果
     *
     * @param httpResponse
     * @param jobJson
     */
    @PostMapping(path = "/receive_job_result", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void receiveJobResult(HttpResponse httpResponse,
                       @RequestBody Object o) {

        ReturnData returnData = new ReturnData();
        returnData.status = 0;
        returnData.message = "";
        ResponseBody body = new JsonBody(JSON.toJSONString(returnData));
        httpResponse.setBody(body);

        com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject)o;
        Intent broadcast = new Intent(RequestHandlerAction);
        broadcast.putExtra("result", jsonObject);
        RequestHandler.getInstance().getContext().sendBroadcast(broadcast);
    }
}
