package top.lyoun.serialport;

import android.Manifest;
import android.app.Activity;
import android.app.Application;

import androidx.core.app.ActivityCompat;

/**
 * @author LyounJAP
 * @title: RequestStoragePrivileges
 * @projectName AndroidSerialHelper
 * @description: dynamic acquire storage read/write privileges
 * @date 2022-01-17 16:32
 */
public class RequestStoragePrivileges {

    public static void requestStoragePrivileges(Activity activity){
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
    }
}
