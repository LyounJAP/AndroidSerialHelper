package top.lyoun.serialport;

import java.io.IOException;
import java.io.InputStream;

/**
 * Author: LyounJAP
 * Date: 2022/1/15 0015 13:22
 * Desc: SerialReadThread
 */
public abstract class SerialReadThread extends Thread{

    private static final String TAG = "SerialReadThread";

    private InputStream mInputStream;
    private byte[] mReceivedBuffer;

    public SerialReadThread(InputStream inputStream) {
        mInputStream = inputStream;
        mReceivedBuffer = new byte[1024];
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()) {
            try {
                if (null == mInputStream) {
                    return;
                }
                int size = mInputStream.read(mReceivedBuffer);
                if (0 >= size) {
                    return;
                }
                byte[] receivedBytes = new byte[size];
                System.arraycopy(mReceivedBuffer, 0, receivedBytes, 0, size);
                onDataReceived(receivedBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void onDataReceived(byte[] bytes);

    /**
     * 释放
     */
    public void release() {
        interrupt();

        if (null != mInputStream) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mInputStream = null;
        }
    }
}
