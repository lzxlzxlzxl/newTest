package com.android.myandroid.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.android.myandroid.util.Comment;


public class BTBroadcastReceiver extends BroadcastReceiver {
    private BluetoothDevice device;//蓝牙设备
    private Handler mHandler;
    private Message message;
    public BTBroadcastReceiver(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action){
            case BluetoothAdapter.ACTION_STATE_CHANGED://蓝牙开关
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                message=new Message();
                message.what = Comment.SWITCH;
                message.obj=blueState;
                mHandler.sendMessage(message);
                break;
            case BluetoothDevice.ACTION_FOUND: // 发现设备的广播
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) { // 判断是否配对过
                   // DatasEntity.mBluetoothDevices.add(device);
                    //if(device.getName()==null){
                        //DatasEntity.mBluetoothDevices.remove(device);
                    message=new Message();
                    message.what = Comment.FOUND;
                    message.obj=device;
                    mHandler.sendMessage(message);
                }
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: //发现完成
                mHandler.sendEmptyMessage(Comment.FINISHED);
                break;

            case BluetoothDevice.ACTION_BOND_STATE_CHANGED://配对
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                message=new Message();
                message.what = Comment.BOND;
                message.obj=device.getBondState();
                mHandler.sendMessage(message);
                break;
        }

    }
}
