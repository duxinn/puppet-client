package com.mango.wechattool;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.mango.wechattool.taskQueue.ChangeUserTagTask;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ChangeUserLableTaskTest {

    @Test
    public void removeSingleUserLabel() {
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add("wxid_0dlamu6jibno21");
        ArrayList<ArrayList<String>> tagList = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        tagList.add(tags);
        new ChangeUserTagTask(userIds, tagList, null).execute();
    }

    @Test
    public void removeAllUserLabel() {
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add("wxid_0dlamu6jibno21");
        userIds.add("wxid_7wm5lbhp0so311");
        ArrayList<ArrayList<String>> tagList = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        tagList.add(tags);
        ArrayList<String> tags2 = new ArrayList<>();
        tagList.add(tags2);
        new ChangeUserTagTask(userIds, tagList, null).execute();
    }


    @Test
    public void addNewLabelToSinglePerson() {
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add("wxid_0dlamu6jibno21");
        ArrayList<ArrayList<String>> tagList = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        tags.add("bl0001");
        tags.add("bl0002");
        tagList.add(tags);
        new ChangeUserTagTask(userIds, tagList, null).execute();
    }

    @Test
    public void addNewLabelToMultiPerson() {
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add("wxid_0dlamu6jibno21");
        userIds.add("wxid_7wm5lbhp0so311");
        ArrayList<ArrayList<String>> tagList = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        tags.add("bl1111");
        tags.add("bl2222");
        tagList.add(tags);
        ArrayList<String> tags2 = new ArrayList<>();
        tags2.add("bl1111");
        tags2.add("bl2222");
        tagList.add(tags2);
        new ChangeUserTagTask(userIds, tagList, null).execute();

    }

    @Test
    public void addExitLabelToSinglePerson() {
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add("wxid_0dlamu6jibno21");
        ArrayList<ArrayList<String>> tagList = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        tags.add("bl0001");
        tags.add("bl0002");
        new ChangeUserTagTask(userIds, tagList, null).execute();
    }


}
