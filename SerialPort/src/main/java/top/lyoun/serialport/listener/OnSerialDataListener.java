package top.lyoun.serialport.listener;

/**
 * Author: LyounJAP
 * Date: 2022/1/15 0015 12:36
 * Desc: 监控串口数据收发
 */
public interface OnSerialDataListener {

    void onDataReceived(byte[] bytes);

    void onDataSend(byte[] bytes);
}
