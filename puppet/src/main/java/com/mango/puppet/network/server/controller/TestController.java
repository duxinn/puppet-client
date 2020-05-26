package com.mango.puppet.network.server.controller;
import android.util.Log;

import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.cookie.Cookie;


import com.yanzhenjie.andserver.util.MediaType;




@RestController
@RequestMapping(path = "/api")
class TestController {
    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String login(HttpRequest request, HttpResponse response, @RequestParam(name = "account") String account,
                 @RequestParam(name = "password") String password) {
        Cookie cookie = new Cookie("account", account + "=" + password);
        response.addCookie(cookie);
        return "Login successful.";
    }

    @PostMapping(path = "/reportEvent", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String reportEvent(HttpRequest request, HttpResponse response, @RequestParam(name = "event_json") String event_json) {
        Log.e("testController", event_json);
        return "reportEvent successful.";
    }
}