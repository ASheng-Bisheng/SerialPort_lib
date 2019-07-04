package cn.whzwl.xbs.serialport.utils;

import android.util.Log;

/**
 * Created by Administrator on 2018/4/10.
 */

public class LogUtils {
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;
    public static final int LEVEL = VERBOSE;//打印所有等级的信息
    public static final String tag ="log_serial";
//  public static final int LEVEL = NOTHING;//关闭所有等级的信息

    public static void v(String msg){
        if(LEVEL <= VERBOSE){
            Log.v(tag,msg);
        }
    }
    public static void d(String msg){
        if(LEVEL <= DEBUG){
            Log.d(tag,msg);
        }
    }
    public static void i(String msg){
        if(LEVEL <= INFO){
            Log.i(tag,msg);
        }
    }
    public static void w(String msg){
        if(LEVEL <= WARN){
            Log.w(tag,msg);
        }
    }
    public static void e(String msg){
        if(LEVEL <= ERROR){
            Log.e(tag,msg);
        }
    }


}
