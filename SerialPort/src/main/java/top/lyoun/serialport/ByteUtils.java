package top.lyoun.serialport;

/**
 * Author: LyounJAP
 * Date: 2022/1/15 0015 13:41
 * Desc: ByteUtils
 */
public class ByteUtils {

    //判断是否是奇数：true: 奇数 ， false: 偶数
    public final static int isOdd(int num) {
        return num & 0x1;
    }

    /**
     * 十六进制字符串转byte数组,当hex为小于256时，byte数组长度为1，
     * 当hex为大于256，小于65536时，byte数组长度为2，以此类推
     * 功能已验证，success
     * @param hexString
     * @return byte[]
     */
    public final static byte[] hexToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1])) & 0xFF);
        }
        return d;
    }

    /**
     * 字符转byte
     * 功能已验证，success
     * @param c char
     * @return byte
     */
    private final static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    //十六进制转整数
    public final static int HexToInt(String inHex) {
        if (inHex == null || inHex.equals("")) {
            return -1;
        }
        try {
            return Integer.parseInt(inHex, 16);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
