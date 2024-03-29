package cn.whzwl.xbs.serialport.jni;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 项目名称:SerialportTest
 * <p>
 * 版权：智味来 版权所有
 * <p>
 * 作者：ASheng
 * <p>
 * 创建日期：2019/2/27 13:00
 * <p>
 * 描述：
 */
public class SerialPort {
    static {
        System.loadLibrary("serial_port");
    }

    private static final String TAG = "SerialPort";
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

        //检查访问权限，如果没有读写权限，进行文件操作，修改文件访问权限
        if (!device.canRead() || !device.canWrite()) {
            try {
                //通过挂载到linux的方式，修改文件的操作权限
                Process su = Runtime.getRuntime().exec("/system/xbin/su");
                String cmd = "chmod 777 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());

                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = open(device.getAbsolutePath(), baudrate, flags);

        if (mFd == null) {

            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }


    // JNI(调用java本地接口，实现串口的打开和关闭)
/**串口有五个重要的参数：串口设备名，波特率，检验位，数据位，停止位
 其中检验位一般默认位NONE,数据位一般默认为8，停止位默认为1*/
    /**
     * @param path     串口设备的据对路径
     * @param baudrate 波特率
     * @param flags    校验位
     */
    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();
}
