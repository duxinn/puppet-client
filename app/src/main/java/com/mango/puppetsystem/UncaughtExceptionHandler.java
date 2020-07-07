package com.mango.puppetsystem;


import android.os.Environment;
import android.text.format.Time;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by liberty on 2016/9/20.
 */

public class UncaughtExceptionHandler implements
        Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        String s = getStackTrace(ex);
        writeLog("crash:" + s);
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            t.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }

    public static void recordThrowable(Throwable throwable) {
        String s = getStackTrace(throwable);
        writeLog("crash:" + s);
    }

    static private SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    public static void writeLog(String log) {
//        if (TextUtils.isEmpty(log)) return;
        log = timeString(System.currentTimeMillis() / 1000) + " " + log;
        if (log.length() <= 0) return;
        File file = new File(getLogStorePath());
        boolean flag = true;
        if (!file.exists()) {
            flag = file.mkdirs();
        }
        if (!flag) return;
        String fp = getLogStorePath() + ymd() + ".txt";
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

    public static String getLogStorePath() {
        return getStorePath() + "log" + File.separator;
    }


    public static String getResourceStorePath() {
        return getStorePath() + "resource" + File.separator;
    }


    public static String getStorePath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator + "mango/";
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
