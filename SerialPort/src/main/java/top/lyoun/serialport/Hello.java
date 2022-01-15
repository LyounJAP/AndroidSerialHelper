package top.lyoun.serialport;

/**
 * Author: LyounJAP
 * Date: 2022/1/15 0015 12:35
 * Desc: Hello
 */
public class Hello {

    static {
        System.loadLibrary("hello");
    }

    public native String stringFromJNI();

    public native String otherInfo();

}
