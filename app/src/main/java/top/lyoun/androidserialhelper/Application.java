package top.lyoun.androidserialhelper;

import java.security.PublicKey;

import top.lyoun.serialport.SerialPort;
import top.lyoun.serialport.SerialPortFinder;

/**
 * Author: LyounJAP
 * Date: 2022/1/15 0015 14:11
 * Desc: Application
 */
public class Application extends android.app.Application {

    public SerialPortFinder mSerialPortFinder =
            new SerialPortFinder();

    private SerialPort mSerial1;

    public SerialPort getSerial1(){
        if(mSerial1 == null){
            mSerial1 = SerialPort.newBuilder(
                    "/dev/ttyS1",
                    9600)
                    .build();
        }

        return mSerial1;
    }

    //关闭串口
    public void closeSerial(){
        closeSerial1();
    }

    public void closeSerial1(){
        if(mSerial1 != null){
            mSerial1.tryClose();
        }
    }
}
