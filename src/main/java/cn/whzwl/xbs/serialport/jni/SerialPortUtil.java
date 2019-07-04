package cn.whzwl.xbs.serialport.jni;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.whzwl.xbs.serialport.Manage.Baudrate;
import cn.whzwl.xbs.serialport.utils.LogUtils;
import cn.whzwl.xbs.serialport.utils.ThreadUtil;


/**
 * Created by Administrator on 2018/5/15.
 */

public abstract class SerialPortUtil {
    public static SerialPort serialPort = null;
    public static InputStream inputStream = null;
    public static OutputStream outputStream = null;
    public static Thread receiveThread = null;
    private static ThreadUtil inspectThread = null;
    public static int STATE_VALUE_EXIST = 1;
    public static int STATE_VALUE_WAIT = 2;
    public static int STATE_VALUE_NO = 3;
    public static int STATE_VALUE_ERROR = 4;
    public static boolean flag = false;
    public static String serialData;

    private String pathName = "";
    private int daudrate = 0;
    private int flags = 0;

    public SerialPortUtil(String pathName, int daudrate, int flags) {
        this.pathName = pathName;
        this.daudrate = daudrate;
        this.flags = flags;
    }

    /**
     * default
     * pathName="/dev/ttySAC3";
     * daudrate= Baudrate.BAUDRATE_115200;
     * flags=0;
     */
    public SerialPortUtil() {
        pathName = "/dev/ttySAC3";
        daudrate = Baudrate.BAUDRATE_115200;
        flags = 0;
    }

    /**
     * 打开串口的方法
     */
    public void openSrialPort() {
        try {
            serialPort = new SerialPort(new File(pathName), daudrate, flags);
            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            flag = true;
            Log.i("SerialPortUtil", "打开串口成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("SerialPortUtil", "打开串口失败");
        }
    }

    /**
     * 关闭串口的方法
     * 关闭串口中的输入输出流
     * 然后将flag的值设为flag，终止接收数据线程
     */
    public void closeSerialPort() {


        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            serialPort.close();
            receiveThread.interrupt();
            receiveThread = null;
            inspectThread.interrupt();
            inspectThread = null;
            flag = false;
            Log.i("SerialPortUtil", "关闭串口成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("SerialPortUtil", "关闭异常");
        }

    }

    public interface SendInterface {
        /**
         * @param stateCode 状态码
         */
        void state(int stateCode);
    }

    private static SendInterface sendInterface;

    /**
     * @param sendInterface 状态监听码
     */
    public void sendInterface(SendInterface sendInterface) {
        this.sendInterface = sendInterface;
    }

    /**
     * 发送串口数据的方法
     *
     * @param data 要发送的数据
     */
    public static void sendSerialPort(String data) {

        try {
            byte[] sendData = data.getBytes();
            outputStream.write(sendData);
            outputStream.flush();

            inspectThread.resumeThread();
            if (sendInterface != null) {
                sendInterface.state(STATE_VALUE_EXIST);
            }

            Log.i("SerialPortUtil", "串口数据发送成功：" + data);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("SerialPortUtil", "串口数据发送失败");
        }
    }


    public String rece() {
        byte[] readData = new byte[1024];
        if (inputStream == null) {
            return "";
        }
        try {
            if (flag) {
                int size = inputStream.read(readData);
                if (size > 0) {
                    serialData = new String(readData, 0, size);//直接发送文本
                    Log.i("SerialPortUtil", "接收到串口数据:" + serialData);
                    return serialData;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";

    }


    /**
     * 接收串口数据的方法
     */
    public void receiveSerialPort() {

        if (receiveThread != null)
            return;
        /*定义一个handler对象要来接收子线程中接收到的数据
            并调用Activity中的刷新界面的方法更新UI界面
         */

        /*创建子线程接收串口数据
         */
        receiveThread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    String data = rece();
                    if (!data.equals("")) {
                        inspectSendValue(STATE_VALUE_NO);
                        if (inspectThread != null)
                            inspectThread.pauseThread();
                        onDataReceived(data);
                    }
                }

            }
        };
        //启动接收线程
        receiveThread.start();
    }


    private int countState = 0;

    /**
     * @param seconds 多少秒后没有消息回应，报异常
     *                发送消息后，判断时候是否有回应消息
     */
    public void inspectThread(final int seconds) {
        if (inspectThread != null) {
            return;
        }

        inspectThread = new ThreadUtil() {
            @Override
            public void runMain() throws InterruptedException {
                while (true) {
                    while (pause) {

                        if (countState != seconds) {
                            LogUtils.d("等待开启");
                            inspectSendValue(STATE_VALUE_WAIT);
                        } else {
                            LogUtils.d("无消息返回");
                            inspectSendValue(STATE_VALUE_ERROR);
                        }

                        countState = 0;
                        onThreadPause();
                    }

                    sleep(1000);
                    countState++;
                    if (countState == seconds) {
                        pauseThread();

                    }

                }
            }
        };
        inspectThread.pause = true;
        inspectThread.start();
    }


    public abstract void inspectSendValue(int state);

    public abstract void onDataReceived(String serialData);
}
