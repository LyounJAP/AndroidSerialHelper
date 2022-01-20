# AndroidSerialHelper
封装的一款简单好用的android串口读写api

参考了很多优秀的串口通信框架，优势组合：
1、git@github.com:cepr/android-serialport-api.git
2、git@github.com:licheedev/Android-SerialPort-API.git
3、git@github.com:kongqw/AndroidSerialPort.git
4、git@github.com:GeekBugs/Android-SerialPort.git



### 注意事项：
1、SELinux是Google从android 5.0开始，强制引入的一套非常严格的权限管理机制，主要用于增强系统的安全性。
在开发中，我们经常会遇到由于SELinux造成的各种权限不足，即使拥有“万能的root权限”，也不能获取全部的权限。
2、SerialPort类中将sSuPath改成：/system/bin/sh ， 而不是/system/bin/su。
3、可通过 adb shell 进入设备后，输入：setenforce 0 （临时禁用掉SELinux），可参考: https://blog.csdn.net/tung214/article/details/72734086/
4、可通过androidstudio自带的模拟器调试串口，可参考：https://www.jianshu.com/p/491481c8a971
5、具体命令如下：
cd D:\androidSdk\emulator
emulator.exe -list-avds
emulator @Pixel_2_API_22 -writable-system  -qemu  -serial  COM5
