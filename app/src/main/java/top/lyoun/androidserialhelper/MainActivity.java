package top.lyoun.androidserialhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;

import top.lyoun.androidserialhelper.databinding.ActivityMainBinding;
import top.lyoun.serialport.Hello;
import top.lyoun.serialport.SerialPort;
import top.lyoun.serialport.Welcome;
import top.lyoun.serialport.listener.OnSerialDataListener;
import top.lyoun.serialport.listener.OnSerialOpenListener;
import top.lyoun.serialport.listener.Status;

/**
 * Author: LyounJAP
 * Date: 2022/1/15 0015 12:36
 * Desc: MainActivity
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding mViewBinding;

    Hello mHello;
    Welcome mWelcome;

    Application mApplication;
    SerialPort mSerial1;

    StringBuffer sb = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());
        initView();
        mHello = new Hello();
        mWelcome = new Welcome();

        //获取串口实例
        mApplication = (Application) getApplication();
        mSerial1 = mApplication.getSerial1();

        //读取设备列表
        Log.i(TAG, "allDevices: " + Arrays.toString(mApplication.mSerialPortFinder.getAllDevices()));
        Log.i(TAG, "allDevicesPath: " + Arrays.toString(mApplication.mSerialPortFinder.getAllDevicesPath()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mApplication != null){
            mApplication.closeSerial();
        }
    }

    void initView(){

        mViewBinding.reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewBinding.text1.setText("text1");
                mViewBinding.text2.setText("text2");
            }
        });

        mViewBinding.getvalue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewBinding.text1.setText("" + mHello.stringFromJNI());
                mViewBinding.text2.setText("" + mWelcome.welcome());
            }
        });

        mViewBinding.openserialBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSerial();
            }
        });

        mViewBinding.closeserialBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSerial1();
                mViewBinding.serialstatusTx.setText("串口已关闭！");
            }
        });

        mViewBinding.sendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendText(mViewBinding.msget.getText().toString());
            }
        });

        mViewBinding.clearBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sb = new StringBuffer();
                mViewBinding.content.setText("");
            }
        });
    }

    //打开串口
    void openSerial(){
        if(mSerial1 == null){
            Log.i(TAG, "openSerial: 串口对象不能为空！");
            return;
        }

        if(mSerial1.isOpen){
            Log.i(TAG, "openSerial: 检测到当前串口已开启，是否再重复开启？" );
        }

        //设置串口打开监听
        mSerial1.setOnSerialOpenListener(new OnSerialOpenListener() {
            @Override
            public void onSuccess(File device) {
                Log.i(TAG, "onSuccess: 串口打开成功！" + device);
                mViewBinding.serialstatusTx.post(new Runnable() {
                    @Override
                    public void run() {
                        mViewBinding.serialstatusTx.setText("串口打开成功！" + device);
                    }
                });
            }

            @Override
            public void onFail(File device, Status status) {
                String tips = "";
                if(Status.NO_READ_WRITE_PERMISSION == status){
                    tips = "串口文件没有读写权限！" + device;
                }
                if(Status.OPEN_FAIL == status){
                    tips = "串口打开失败！" + device;
                }
                Log.e(TAG, "onFail: " + tips);
                String finalTips = tips;
                mViewBinding.serialstatusTx.post(new Runnable() {
                    @Override
                    public void run() {
                        mViewBinding.serialstatusTx.setText(finalTips);
                    }
                });
            }
        });

        //设置串口数据读写监听
        mSerial1.setOnSerialDataListener(new OnSerialDataListener() {
            @Override
            public void onDataReceived(byte[] bytes) {
                Log.i(TAG, "mSerial1.onDataReceived: " + Arrays.toString(bytes));
                sb.append("read: ").append(Arrays.toString(bytes)).append("\n");

                mViewBinding.content.post(new Runnable() {
                    @Override
                    public void run() {
                        mViewBinding.content.setText(sb.toString() + "");
                    }
                });
            }

            @Override
            public void onDataSend(byte[] bytes) {
                Log.i(TAG, "mSerial1.onDataSend: " + Arrays.toString(bytes));
                sb.append("send: ").append(Arrays.toString(bytes)).append("\n");

                mViewBinding.content.post(new Runnable() {
                    @Override
                    public void run() {
                        mViewBinding.content.setText(sb.toString() + "");
                    }
                });
            }
        });

        mSerial1.open();

        Log.i(TAG, "openSerial: 串口开启" + ((mSerial1.isOpen) ? "成功！" : "失败！"));
    }

    //关闭串口
    void closeSerial1(){
        if(mApplication != null){
            mApplication.closeSerial1();
        }
    }

    //发送数据
    void sendText(String text){
        if(text == null || "".equals(text.trim())){
            return;
        }
        if(mSerial1 == null || !mSerial1.isOpen){
            Log.i(TAG, "sendText: 串口尚未打开，发送数据失败！" + mSerial1);
            return;
        }

        mSerial1.sendTxt(text);
    }
}