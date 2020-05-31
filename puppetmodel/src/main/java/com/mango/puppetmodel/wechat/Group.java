package com.mango.puppetmodel.wechat;

import android.database.Cursor;

import java.util.List;

/**
 * Group
 *
 * @author: hehongzhen
 * @date: 2020/05/26
 */
public class Group {

    // 群的微信内部id
    public String field_chatroomname;

    // 群名称 (获取不到)
    public String field_chatroomnick;

    // 群主微信内部ID
    public String field_roomowner;

    // 在群中的名称
    public String field_selfDisplayName;

    // 群公告
    public String field_chatroomnotice;

    // 群公告发布者的微信内部ID
    public String field_chatroomnoticeEditor;

    // 群成员个数
    public int field_memberCount;

    // 群成员简要信息列表
    public List<MemberContent> member;

    // 群头像 将头像图片文件上传到我们自己服务器后的url地址
    public String icon_url;

    // 资源文件在本地的路径
    public String icon_file_url;

    // 群成员微信内部id ;隔开
    public String field_memberlist;


}
