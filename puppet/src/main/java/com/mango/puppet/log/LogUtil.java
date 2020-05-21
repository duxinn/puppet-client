package com.mango.puppet.log;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import com.mango.puppet.MyApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LogUtil {
    static private SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    public static void writeLog(String log, Context context) {
//        if (TextUtils.isEmpty(log)) return;
        if (log.length() <= 0) return;
        File file = new File(getLogStorePath(context));
        boolean flag = true;
        if (!file.exists()) {
            flag = file.mkdirs();
        }
        if (!flag) return;
        String fp = getLogStorePath(context) + ymd() + ".txt";
        file = new File(fp);
        if (!file.exists()) {
            try {
                flag = file.createNewFile();
//                if (flag) {
//                    uploadYesterdayLog();
//                }
            } catch (IOException e) {
                e.printStackTrace();
                flag = false;
            }
        }
        if (!flag) return;

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath(), true)));
//            out.write(timeString(System.currentTimeMillis() / 1000) + " ");
            out.write(log);
            out.write("\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getLogStorePath(Context context) {
        return getStorePath(context) + "log" + File.separator;
    }

    public static String getStorePath(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + "mango/";
    }

    static String ymd() {
        Time t = new Time("GMT+8");
        t.setToNow();
        int year = t.year;
        int month = t.month + 1;
        int day = t.monthDay;
        return "" + year + "-" + month + "-" + day;
    }

    public static String timeString(long value) {
        long stamp = value * 1000;
        if (stamp == 0) return "";
        return dateFormat1.format(stamp);
    }
}
