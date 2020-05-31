package com.mango.puppetmodel.wechat;

import android.database.Cursor;

public class FriendApply {

    public String wxid;

    public FriendApply(Cursor cursor) {
        wxid = cursor.getString(cursor.getColumnIndex("talker"));
    }
}
