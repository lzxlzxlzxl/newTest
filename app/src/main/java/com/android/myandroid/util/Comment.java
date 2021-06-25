package com.android.myandroid.util;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.UUID;

public class Comment {

    /**
     * 蓝牙开关
     */
    public static final int SWITCH = 101;

    /**
     * 蓝牙搜索
     */
    public static final int FOUND = 102;

    /**
     * 蓝牙搜索完毕
     */
    public static final int FINISHED = 103;

    /**
     * 蓝牙配对
     */
    public static final int BOND =104;

    /**
     * 修改蓝牙名的请求码
     */
    public static final int NAME_CODE = 1;
}
