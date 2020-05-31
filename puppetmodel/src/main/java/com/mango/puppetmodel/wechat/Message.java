package com.mango.puppetmodel.wechat;


/**
 * Message
 *
 * @author: hehongzhen
 * @date: 2020/05/26
 */
public class Message {

    // 所有消息本地唯一id
    public long field_msgId;

    // 消息在微信服务器上的唯一id
    public long field_msgSvrId;

    // 消息状态 1:发送中 5:发送失败 其他:成功
    public int field_status;

    // 是否是发送方 1:自己发的 0:对方发的
    public int field_isSend;

    // 消息创建时间的毫秒时间戳
    public long field_createTime;

    // 会话对方微信内部ID,若是群聊则为群的微信内部ID
    public String field_talker;

    // 消息具体内容,根据消息的field_type不同而有多种解析方式
    public String field_content;

    // 含资源文件的消息中资源文件的本地路径缩写
    public String field_imgPath;

    // 消息类型
    public int field_type;

    // 文字群聊中包含@人的微信内部id
    public String dqJ;

    // 解析后的参数
    public MessageContent content = new MessageContent();

}
