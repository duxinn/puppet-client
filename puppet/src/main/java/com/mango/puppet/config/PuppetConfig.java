package com.mango.puppet.config;

/**
 * PuppetConfig
 *
 * @author: hehongzhen
 * @date: 2020/09/01
 */
public class PuppetConfig {

    // 是否启动本地服务器 true:启动本地服务器  false:启动websocket  默认false
    public static final boolean IS_LOCAL_SERVER = false;

    // 微信插件名称 正式环境为wechat.apk
    public static final String WECHAT_APK_NAME = "wechat.apk";

    // 是否是root手机版本 默认true
    public static final boolean IS_NEED_ROOT = true;

    // 是否是调试模式 调试模式在插件打断点时不会重启目标进程 正式环境为false
    public static final boolean IS_PLUGIN_DEBUG = false;

}
