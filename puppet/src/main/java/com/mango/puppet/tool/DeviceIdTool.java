package com.mango.puppet.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * DeviceIdTool
 *
 * @author: hehongzhen
 * @date: 2020/06/25
 */
public class DeviceIdTool {


    public final static String KEY_CLIENT_DEVICE_ID = "KEY_CLIENT_DEVICE_ID";

    private static String DEVICE_ID = "";

    public static String getDeviceId(Context context) {
        if (TextUtils.isEmpty(DEVICE_ID)) {
            String spDeviceId = PreferenceUtils.getInstance().getString(KEY_CLIENT_DEVICE_ID, null);
            String extDeviceId = readDeviceIdFromSdcard(context);
            if (!TextUtils.isEmpty(spDeviceId)) {
                if (!spDeviceId.equals(extDeviceId)) {
                    writeDeviceIdToSdcard(spDeviceId, context);
                }
                DEVICE_ID = spDeviceId;
            } else if (!TextUtils.isEmpty(extDeviceId)) {
                PreferenceUtils.getInstance().setString(KEY_CLIENT_DEVICE_ID, extDeviceId);
                DEVICE_ID = extDeviceId;
            } else {
                DEVICE_ID = generalDeviceId(context);
                writeDeviceIdToSdcard(DEVICE_ID, context);
                PreferenceUtils.getInstance().setString(KEY_CLIENT_DEVICE_ID, DEVICE_ID);
            }
        }
        return DEVICE_ID;
    }

    @SuppressLint("MissingPermission")
    private static String generalDeviceId(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

    private static String readDeviceIdFromSdcard(Context context) {
        return readDeviceIdFromSdcardBeforeAndroidQ(context);
    }

    private static String readDeviceIdFromSdcardBeforeAndroidQ(Context context) {
        File deviceIdFile = getSdcardDeviceIdFileBeforeAndroidQ(context);
        StringBuilder sb = new StringBuilder("");
        if (deviceIdFile.isFile()) {
            try {
                FileInputStream inputStream = new FileInputStream(deviceIdFile);
                byte temp[] = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(temp)) > 0) {
                    sb.append(new String(temp, 0, len));
                }
                inputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }



    private static void writeDeviceIdToSdcard(String deviceId, Context context) {
        writeDeviceIdToSdcardBeforeAndroidQ(deviceId, context);
    }

    private static void writeDeviceIdToSdcardBeforeAndroidQ(String deviceId, Context context) {
        try {
            PrintWriter printWriter = new PrintWriter(getSdcardDeviceIdFileBeforeAndroidQ(context));
            printWriter.write(deviceId);
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static File getSdcardDeviceIdFileBeforeAndroidQ(Context context) {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath(), getDeviceIdFileName(context));
    }

    private static String getDeviceIdFileName(Context context) {
        return ".system_" + getMd5(context.getPackageName());
    }

    public static String getMd5(String s) {
        if (TextUtils.isEmpty(s) == true) {
            return "";
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] array = md.digest(s.getBytes());
        return toHexString(array);
    }

    private final static String HEX = "0123456789ABCDEF";

    public static String toHexString(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            final byte b = buf[i];
            result.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));

        }
        return result.toString();
    }
}
