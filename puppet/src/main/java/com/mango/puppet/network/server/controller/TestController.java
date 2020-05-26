package com.mango.puppet.network.server.controller;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.mango.puppet.network.server.model.ReturnData;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;


import com.yanzhenjie.andserver.util.MediaType;




@RestController
@RequestMapping(path = "/api")
class TestController {
    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void login(HttpRequest request, HttpResponse response, @RequestParam(name = "account") String account,
                 @RequestParam(name = "password") String password) {
        try {
            ResponseBody body = new StringBody("GOOD JOB!");
            response.setBody(body);
        } catch (Exception e){
            ReturnData returnData = new ReturnData();
            returnData.setStatus(1);
            returnData.setErrorMsg(e.getMessage());
            ResponseBody body = new StringBody(JSON.toJSONString(returnData));
            response.setBody(body);
        }
    }

    @PostMapping(path = "/reportEvent", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String reportEvent(HttpRequest request, HttpResponse response, @RequestParam(name = "event_json") String event_json) {
        Log.e("testController", event_json);
        return "reportEvent successful.";
    }
}