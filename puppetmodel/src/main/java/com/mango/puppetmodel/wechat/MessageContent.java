package com.mango.puppetmodel.wechat;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageContent
 *
 * @author: hehongzhen
 * @date: 2020/05/26
 */
public class MessageContent {

    public int type;

    public String text;

    public String resource_url;

    public int image_width;

    public int image_height;

    public int image_origin;

    public String voice_data;

    public String voice_duration;

    public int is_group;

    public String group_sender;

    public String at_string;

    public int transfer_send;

    public String transfer_id;

    public String get_money_string;

    public String money_desc;

    public String money_amount;

    public int money_status;

    public String alias;

    public String bigheadimgurl;

    public String nickname;

    public String wxid;

    public int is_withdraw;

    public String invite_ticket;

    public String invite_reason;

    public String inviter_nickname;

    public String inviter_wxid;

    public List<InviteMember> members = new ArrayList<>();


}
