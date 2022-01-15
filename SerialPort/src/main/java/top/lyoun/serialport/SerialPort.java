/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.lyoun.serialport;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import top.lyoun.serialport.listener.OnSerialDataListener;
import top.lyoun.serialport.listener.OnSerialOpenListener;
import top.lyoun.serialport.listener.Status;

/**
 * Author: LyounJAP
 * Date: 2022/1/15 0015 12:35
 * Desc: SerialPort 串口读写工具类
 */
public final class SerialPort {

    private static final String TAG = "SerialPort";

    public static final String DEFAULT_SU_PATH = "/system/bin/su";

    private static String sSuPath = DEFAULT_SU_PATH;
    private File device; //串口设备文件
    private int baudrate; //波特率
    private int dataBits; //数据位；默认8,可选值为5~8
    private int parity; //奇偶校验；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
    private int stopBits; //停止位；默认1；1:1位停止位；2:2位停止位
    private int flags; //默认0

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    private OnSerialOpenListener mSerialOpenListener;
    private OnSerialDataListener mSerialDataListener;
    public boolean isOpen = false; //串口是否打开标记

    private HandlerThread mSendingHandlerThread;
    private Handler mSendingHandler;
    private SerialReadThread mSerialReadThread;

    static {
        System.loadLibrary("serial_port");
    }

    // JNI
    private native FileDescriptor open(String absolutePath, int baudrate, int dataBits, int parity,
                                       int stopBits, int flags);

    public native void close();


    /**
     * 串口
     *
     * @param device 串口设备文件
     * @param baudrate 波特率
     * @param dataBits 数据位；默认8,可选值为5~8
     * @param parity 奇偶校验；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
     * @param stopBits 停止位；默认1；1:1位停止位；2:2位停止位
     * @param flags 默认0
     * @throws SecurityException
     * @throws IOException
     */
    public SerialPort(@NonNull File device, int baudrate, int dataBits, int parity, int stopBits,
        int flags) {

        this.device = device;
        this.baudrate = baudrate;
        this.dataBits = dataBits;
        this.parity = parity;
        this.stopBits = stopBits;
        this.flags = flags;
    }

    /**
     * 串口，默认的8n1
     *
     * @param device 串口设备文件
     * @param baudrate 波特率
     * @throws SecurityException
     * @throws IOException
     */
    public SerialPort(@NonNull File device, int baudrate) throws SecurityException, IOException {
        this(device, baudrate, 8, 0, 1, 0);
    }

    /**
     * 串口
     *
     * @param device 串口设备文件
     * @param baudrate 波特率
     * @param dataBits 数据位；默认8,可选值为5~8
     * @param parity 奇偶校验；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
     * @param stopBits 停止位；默认1；1:1位停止位；2:2位停止位
     * @throws SecurityException
     * @throws IOException
     */
    public SerialPort(@NonNull File device, int baudrate, int dataBits, int parity, int stopBits)
        throws SecurityException, IOException {
        this(device, baudrate, dataBits, parity, stopBits, 0);
    }


    /**
     * 设置串口打开监听
     */
    public void setOnSerialOpenListener(OnSerialOpenListener listener){
        if (listener != null) {
            mSerialOpenListener = listener;
        }
    }

    /**
     * 设置串口数据收发监听
     */
    public void setOnSerialDataListener(OnSerialDataListener listener){
        if(listener != null){
            mSerialDataListener = listener;
        }
    }

    /**
     * 打开串口
     *
     * @return 串口打开状态 true:打开 false：打开失败
     */
    public boolean open() {
        isOpen = openSafe(device,baudrate, dataBits, parity, stopBits, flags);
        return isOpen;
    }

    /** 关闭流和串口，已经try-catch */
    public void tryClose() {
        stopSendThread();
        stopReceivedThread();
        closeSafe();
        isOpen = false;

        Log.i(TAG, "tryClose: 串口关闭成功！" + device);
    }

    /**
     * 发送数据
     *
     * @param bytes 发送的字节
     * @return 发送状态 true:发送成功 false：发送失败
     */
    public boolean sendBytes(byte[] bytes) {
        if (null != mSendingHandler) {
            Message message = Message.obtain();
            message.obj = bytes;
            return mSendingHandler.sendMessage(message);
        }
        return false;
    }

    /**
     * 发送Hex
     *
     * @param hex 16进制文本
     */
    public void sendHex(String hex) {
        byte[] hexArray = ByteUtils.hexToBytes(hex);
        sendBytes(hexArray);
    }

    /**
     * 发送文本
     *
     * @param txt 文本
     */
    public void sendTxt(String txt) {
        byte[] txtArray = txt.getBytes();
        sendBytes(txtArray);
    }

    /**
     * 开启发送消息线程
     */
    private void startSendThread() {
        mSendingHandlerThread = new HandlerThread("mSendingHandlerThread");
        mSendingHandlerThread.start();

        mSendingHandler = new Handler(mSendingHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                byte[] sendBytes = (byte[]) msg.obj;
                if (null != mFileOutputStream && null != sendBytes && sendBytes.length > 0) {
                    try {
                        mFileOutputStream.write(sendBytes);
                        if (null != mSerialDataListener) {
                            mSerialDataListener.onDataSend(sendBytes);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * 停止发送消息线程
     */
    private void stopSendThread() {
        mSendingHandler = null;
        if (null != mSendingHandlerThread) {
            mSendingHandlerThread.interrupt();
            mSendingHandlerThread.quit();
            mSendingHandlerThread = null;
        }
    }

    /**
     * 开启接收消息的线程
     */
    private void startReadThread() {
        mSerialReadThread = new SerialReadThread(mFileInputStream) {
            @Override
            public void onDataReceived(byte[] bytes) {
                if (null != mSerialDataListener) {
                    mSerialDataListener.onDataReceived(bytes);
                }
            }
        };
        mSerialReadThread.start();
    }

    /**
     * 停止接收消息的线程
     */
    private void stopReceivedThread() {
        if (null != mSerialReadThread) {
            mSerialReadThread.release();
        }
    }

    /**
     * 检查文件权限
     *
     * @param device 文件
     * @return 权限修改是否成功
     */
    private boolean chmod777(File device) {
        if (null == device || !device.exists()) {
            return false;
        }
        try {
            Process su = Runtime.getRuntime().exec(sSuPath);
            String cmd = "chmod 777" + device.getAbsolutePath() + "\n" + "exit\n";
            su.getOutputStream().write(cmd.getBytes());
            if (0 == su.waitFor() && device.canRead() && device.canWrite() && device.canExecute()) {
                return true;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

        }
        return false;
    }


    private boolean openSafe(File device, int baudrate, int dataBits, int parity,
                             int stopBits, int flags) {
        if (null == device || !device.exists()) {
            Log.e(TAG, "openSafe: 文件不存在或文件不能为空！");
            return false;
        }
        Log.i(TAG, String.format("SerialPort: %s: %d,%d,%d,%d,%d,%d", device.getPath(), baudrate, dataBits, parity, stopBits, flags));
        if (!device.canRead() || !device.canWrite()) {
            boolean chmod777 = chmod777(device);
            if (!chmod777) {
                Log.e(TAG, device.getPath() + " : 没有读写权限");
                if (null != mSerialOpenListener) {
                    mSerialOpenListener.onFail(device, Status.NO_READ_WRITE_PERMISSION);
                }
                return false;
            }
        }
        try {
            mFd = open(device.getAbsolutePath(), baudrate, dataBits, parity, stopBits, flags);
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
            startSendThread();
            startReadThread();
            if (null != mSerialOpenListener) {
                mSerialOpenListener.onSuccess(device);
            }
            Log.i(TAG, device.getPath() + " : 串口已经打开");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (null != mSerialOpenListener) {
                mSerialOpenListener.onFail(device, Status.OPEN_FAIL);
            }
        }
        return false;
    }

    private void closeSafe() {
        if (null != mFd) {
            close();
            mFd = null;
        }
        if (null != mFileInputStream) {
            try {
                mFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileInputStream = null;
        }

        if (null != mFileOutputStream) {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileOutputStream = null;
        }
    }

    // Getters and setters
    @NonNull
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    @NonNull
    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    /**
     * Set the su binary path, the default su binary path is {@link #DEFAULT_SU_PATH}
     *
     * @param suPath su binary path
     */
    public static void setSuPath(@Nullable String suPath) {
        if (suPath == null) {
            return;
        }
        sSuPath = suPath;
    }

    /**
     * Get the su binary path
     *
     * @return
     */
    @NonNull
    public static String getSuPath() {
        return sSuPath;
    }

    /** 串口设备文件 */
    @NonNull
    public File getDevice() {
        return device;
    }

    /** 波特率 */
    public int getBaudrate() {
        return baudrate;
    }

    /** 数据位；默认8,可选值为5~8 */
    public int getDataBits() {
        return dataBits;
    }

    /** 奇偶校验；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN) */
    public int getParity() {
        return parity;
    }

    /** 停止位；默认1；1:1位停止位；2:2位停止位 */
    public int getStopBits() {
        return stopBits;
    }

    public int getFlags() {
        return flags;
    }

    public static Builder newBuilder(File device, int baudrate) {
        return new Builder(device, baudrate);
    }

    public static Builder newBuilder(String devicePath, int baudrate) {
        return new Builder(devicePath, baudrate);
    }

    public final static class Builder {

        private File device;
        private int baudrate;
        private int dataBits = 8;
        private int parity = 0;
        private int stopBits = 1;
        private int flags = 0;

        private Builder(File device, int baudrate) {
            this.device = device;
            this.baudrate = baudrate;
        }

        private Builder(String devicePath, int baudrate) {
            this(new File(devicePath), baudrate);
        }

        /**
         * 数据位
         *
         * @param dataBits 默认8,可选值为5~8
         * @return
         */
        public Builder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        /**
         * 校验位
         *
         * @param parity 0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
         * @return
         */
        public Builder parity(int parity) {
            this.parity = parity;
            return this;
        }

        /**
         * 停止位
         *
         * @param stopBits 默认1；1:1位停止位；2:2位停止位
         * @return
         */
        public Builder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        /**
         * 标志
         *
         * @param flags 默认0
         * @return
         */
        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        /**
         * 打开并返回串口
         *
         * @return
         * @throws SecurityException
         * @throws IOException
         */
        public SerialPort build() {
            return new SerialPort(device, baudrate, dataBits, parity, stopBits, flags);
        }
    }
}
