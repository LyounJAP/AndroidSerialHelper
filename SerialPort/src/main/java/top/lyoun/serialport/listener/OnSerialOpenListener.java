package top.lyoun.serialport.listener;

import java.io.File;

/**
 * Author: LyounJAP
 * Date: 2022/1/15 0015 12:34
 * Desc: 监控串口打开状态
 */
public interface OnSerialOpenListener {

    void onSuccess(File device);

    void onFail(File device, Status status);
}
