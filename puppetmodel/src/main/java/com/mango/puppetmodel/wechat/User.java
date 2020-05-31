package com.mango.puppetmodel.wechat;


/**
 * User
 *
 * @author: hehongzhen
 * @date: 2020/05/26
 */
public class User {

    // 微信内部ID
    public String field_username;

    // 微信号
    public String field_alias;

    // 备注名
    public String field_conRemark;

    // 昵称
    public String field_nickname;

    // 拼音首字母大写
    public String field_pyInitial;

    // 昵称全拼
    public String field_quanPin;

    // 备注名全拼
    public String field_conRemarkPYFull;

    // 备注名首字母大写
    public String field_conRemarkPYShort;

    // 将头像图片文件上传到我们自己服务器后的url地址
    public String icon_url;

    // 资源文件在本地的路径
    public String icon_file_url;

}
