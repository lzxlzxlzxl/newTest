package com.android.myandroid.client;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.android.myandroid.util.Util;

import java.io.Serializable;


/**
 * 客户端，与服务端建立长连接
 */
public class BtClient extends Base implements Serializable {
    BtClient(Listener listener) {
        super(listener);
    }

    /**
     * 与远端设备建立长连接
     *
     * @param dev 远端设备
     */
    public void connect(BluetoothDevice dev) {
        close();
        try {
//             final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
            final BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID); //明文传输(不安全)，无需配对
            // 开启子线程
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    loopRead(socket); //循环读取套接字
                }
            });
        } catch (Throwable e) {
            close();
        }
    }
}