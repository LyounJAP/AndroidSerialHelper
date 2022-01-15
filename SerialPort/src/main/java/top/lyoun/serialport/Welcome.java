package top.lyoun.serialport;

/**
 * Author: LyounJAP
 * Date: 2022/1/15 0015 12:35
 * Desc: Welcome
 */
public class Welcome {

    static {
        System.loadLibrary("welcome");
    }

    public native String welcome();
}
